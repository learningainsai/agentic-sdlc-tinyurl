<!-- template_id: code-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by code-review-standard (§6). -->

# Code Review — run-20260802T170000Z (UNIT-001 backend — Phase 3 bulk DB-query optimization)

- **template_id**: code-review-template.md
- **run_id**: run-20260802T170000Z
- **reviewer**: construction
- **status**: passed
- **supersedes**: run-20260802T150051Z (Phase 2) code review

## 1. Findings (required)

### Phase 3 findings (this run)

| CR id | Category | Severity | Finding | Resolution |
|-------|----------|----------|---------|------------|
| CR-016 | performance | — | UNIT-001: `createBulk` re-implemented as a two-pass batched algorithm — Pass 1 validates URL/expiry and assigns codes in memory (custom aliases + generated codes deduped within the batch via a `HashSet`); Pass 2 resolves all persisted collisions with a **single** `findExistingCodes` `SELECT ... WHERE code IN (:codes)` (ADR-019) and inserts survivors with **one** `saveAll` batched insert. A 50-item batch drops from ~2·N (~100) to ≤10 DB statements (TEST-019). | resolved |
| CR-017 | correctness | — | UNIT-001: best-effort partial success preserved — in-memory validation failures map to ordered per-item error codes before persistence; on `DataIntegrityViolationException` from the batched `saveAll`, each survivor is retried via the per-item `createOne`/`createOneCatching` path so a single race cannot poison siblings (ADR-021). Response contract is byte-for-byte unchanged (TEST-020). | resolved |
| CR-018 | correctness / schema | — | UNIT-001: `ShortLink` id switched `IDENTITY → SEQUENCE` (pooled, `allocationSize=50` matching `jdbc.batch_size=50`) because IDENTITY disables Hibernate JDBC insert batching (ADR-018). One sequence fetch per batch. `application.yml` adds `jdbc.batch_size: 50` + `order_inserts`/`order_updates`. | resolved |
| CR-019 | maintainability | — | UNIT-001: single-create path (`createOne`, `saveWithAlias`, `saveWithGeneratedCode`) left intact and reused as the batch fallback — no duplication of the collision/retry logic. `createBulk` intentionally not `@Transactional` (relies on `saveAll`'s own transaction, consistent with ADR-013/021). | resolved |
| CR-020 | test-quality | — | Existing bulk unit tests TEST-013..016 re-pointed at the batched interaction (`findExistingCodes` + `saveAll`) with **unchanged response assertions** (proves contract preservation); no failing test was disabled or commented out. New integration tests TEST-019 (statement count) and TEST-020 (contract/partial-success) added. | resolved |
| CR-021 | operability | low | RISK-023: `prod` runs `ddl-auto: validate` with no in-repo migration tooling; the `short_link_seq` sequence must pre-exist in the prod schema (dev `update` / test `create-drop` create it automatically). Non-blocking for this run — raised to release-readiness. | open (flagged) |

### Phase 2 findings (baseline)

| CR id | Category | Severity | Finding | Resolution |
|-------|----------|----------|---------|------------|
| CR-010 | correctness | — | UNIT-001: nullable `expires_at` column added with backward-compatible constructors (NULL = never expires, ADR-010). Expiry validated at create (`validateExpiry` rejects `<= now` → `InvalidExpiryException` → 400, ADR-012) and evaluated lazily on resolve (`isExpired` → `CodeNotFoundException` → 404, ADR-011). `Clock` injected for deterministic boundaries. | resolved |
| CR-011 | correctness | — | UNIT-001: `createBulk` is deliberately NOT `@Transactional` so a per-item `DataIntegrityViolationException` cannot poison sibling items (best-effort partial success, ADR-013). Each item is persisted via the repository proxy's own transaction; failures are caught and mapped to ordered per-item error codes. | resolved |
| CR-012 | security / availability | — | UNIT-001: bulk bounded to 1–100 items (`@Valid @Size`); shared `RateLimiter.tryAcquire(ip, N)` reserves one token per item and rejects the whole batch (429) without creating anything when the budget is insufficient (ADR-014). Mitigates bulk DoS amplification (RISK-011). | resolved |
| CR-013 | maintainability | — | UNIT-001: `Clock` and `RateLimiter` beans moved into `WebConfig` (@Configuration) so `@WebMvcTest` slices resolve without `AppProperties` leaking into the slice; `TinyUrlApplication` kept minimal. | resolved |
| CR-014 | correctness | — | UNIT-002: `datetime-local` (local wall-clock) converted to an absolute UTC ISO instant via `toUtcInstant` before POST (ADR-016); empty/invalid values omitted. `LinkService` passes `expiresAt` through unchanged; created expiry rendered in `.result-expiry`. | resolved |
| CR-015 | test-quality | — | Orphaned Phase-1 withdrawn-scope `deactivate` test was **removed** (not commented out); inconsistent orphaned frontend spec expectations were corrected to the ADR-016 UTC-conversion contract. All suites green. | resolved |

### Phase 1 findings (baseline)

| CR id | Category | Severity | Finding | Resolution |
|-------|----------|----------|---------|------------|
| CR-001 | maintainability | low | UNIT-001: `LinkControllerTest` retains an unused `@Autowired ObjectMapper` field. Harmless; left as-is (test builds JSON via string). | open (cosmetic, non-blocking) |
| CR-002 | correctness | — | UNIT-001: layered controller→service→repository respected; controllers contain no business logic; DTOs used (no entity exposure); constructor injection throughout; `@Valid` on request bodies. Alias collision handled by DB unique constraint + `DataIntegrityViolationException` catch + bounded retry for generated codes. | resolved |
| CR-003 | test-quality | — | UNIT-001: unit tests (Mockito) + `@WebMvcTest` slices + `LinkFlowIT` integration; edge/failure paths (invalid URL, reserved/duplicate alias, unknown code) covered; all green on H2 test profile. | resolved |
| CR-004 | correctness | — | UNIT-002: `LinkService` owns all HTTP (no `HttpClient` in components); `catchError` maps backend messages; standalone signals component; subscription cleanup via `takeUntilDestroyed(destroyRef)`. | resolved |
| CR-005 | business-logic-risk | — | UNIT-002: client validators mirror the backend contract (URL scheme, alias pattern); server remains source of truth (409 surfaced inline). No client-only trust. | resolved |
| CR-006 | maintainability | — | Config: clean dev/prod/test profile split; secrets env-injected in prod; docker-compose pinned with healthcheck. | resolved |

## 2. Review checklist (required)

- [x] Correctness — layering, injection, error mapping, collision handling verified.
- [x] Security — see security-review.md (SEC-001..008); no unresolved high/critical.
- [x] Maintainability — conventions per AGENTS.md; one cosmetic CR-001 noted.
- [x] Test quality — unit + slice + integration + frontend specs, all passing.
- [x] Business logic risk — server-authoritative validation; reserved words & rate limiting enforced.

## 3. Traceability (required)

- **Phase 2**: CR-010..013 ↔ UNIT-001 ↔ PLAN-001 ↔ REQ-011..019 ↔ ADR-010..015; CR-014..015 ↔ UNIT-002 ↔ PLAN-002 ↔ REQ-020 ↔ ADR-016.
- **Phase 1 baseline**: CR-001..003, CR-006 ↔ UNIT-001 ↔ REQ-001..006/008..010; CR-004..005 ↔ UNIT-002 ↔ REQ-007.

## 4. Verdict (required)

- **Outcome**: passed
- No unresolved high-severity findings. All Phase 2 findings resolved; CR-001 (Phase 1) remains cosmetic and non-blocking.
