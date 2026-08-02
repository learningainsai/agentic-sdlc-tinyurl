<!-- template_id: code-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by code-review-standard (§6). -->

# Code Review — run-20260802T150051Z (UNIT-001 backend + UNIT-002 frontend — Phase 2)

- **template_id**: code-review-template.md
- **run_id**: run-20260802T150051Z
- **reviewer**: construction
- **status**: passed
- **supersedes**: run-20260801T232309Z (Phase 1 MVP) code review

## 1. Findings (required)

### Phase 2 findings (this run)

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
