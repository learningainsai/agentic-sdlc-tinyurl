<!-- template_id: test-plan-template.md -->
<!-- v2 §4 mandatory template. Enforced by testing-standard (§6). -->

# Test Plan — run-20260802T150051Z (TinyURL Phase 2 — expiry + bulk creation)

- **template_id**: test-plan-template.md
- **run_id**: run-20260802T150051Z
- **node_id**: testing
- **status**: passed
- **supersedes**: run-20260801T232309Z (Phase 1 MVP) test plan, retained below as the regression baseline

## 1. Test scope (required)

**Covered (Phase 2 — this run)**

- Backend (UNIT-001): optional `expires_at` column, create-time expiry validation (past/present
  rejected → 400), lazy expiry on resolve (expired alias → 404), bulk create endpoint
  `POST /api/links/bulk` (1–100 items, best-effort per-item results in order, per-item error codes),
  and shared N-token rate limiting (bulk over budget rejected wholesale → 429, nothing created).
- Frontend (UNIT-002): optional expiry picker converting `datetime-local` to a UTC ISO instant
  (ADR-016), `LinkService` passthrough of `expiresAt`, and rendering the created link's expiry.

**Regression (must stay green)**

- All Phase 1 backend + frontend suites (TEST-001..009, LinkFlowIT) re-run unchanged.

**Excluded (Phase 2)**

- Bulk creation UI (ADR-016 — API only for this phase).
- Expired-row reaper / background cleanup (RISK-012, accepted; alias stays reserved per ADR-015).
- Distributed/multi-instance rate limiting (carried over from Phase 1, accepted).

## 1a. Phase 2 test cases (required)

| TEST id | Type | Scenario | Traces to | Expected result |
|---------|------|----------|-----------|-----------------|
| TEST-010 | unit | Create with past expiry rejected | REQ-012 · ADR-012 · UNIT-001 | `InvalidExpiryException` → 400 |
| TEST-011 | unit | Create with present (== now) expiry rejected | REQ-012 · ADR-012 · UNIT-001 | `InvalidExpiryException` → 400 |
| TEST-012 | unit | Resolve an expired link (lazy) | REQ-013 · ADR-011 · UNIT-001 | `CodeNotFoundException` → 404 |
| TEST-013 | unit | Resolve a not-yet-expired link | REQ-013 · ADR-011 · UNIT-001 | Original URL returned |
| TEST-014 | unit | Bulk creates all valid items | REQ-014/016 · ADR-013 · UNIT-001 | Ordered per-item created results |
| TEST-015 | unit | Bulk reports per-item error, keeps valid ones | REQ-016 · ADR-013 · UNIT-001 | Best-effort partial success |
| TEST-016 | unit | Bulk duplicate alias within batch reported per-item | REQ-016 · ADR-013 · UNIT-001 | `ALIAS_TAKEN` on 2nd item only |
| TEST-016b | unit | Bulk per-item invalid expiry reported | REQ-012/016 · ADR-013 · UNIT-001 | `INVALID_EXPIRY` on offending item |
| TEST-C1 | slice | `POST /bulk` returns 200 with results | REQ-014 · UNIT-001 | 200 + `BulkCreateResponse` |
| TEST-C2 | slice | `POST /bulk` empty items rejected | REQ-015 · UNIT-001 | 400 (`@Size` min) |
| TEST-C3 | slice | `POST /bulk` >100 items rejected | REQ-015 · ADR-013 · UNIT-001 | 400 (`@Size` max) |
| TEST-C4 | slice | `POST /bulk` over rate budget | REQ-017 · ADR-014 · UNIT-001 | 429, nothing created |
| TEST-017 | unit (component) | datetime-local converted to UTC instant before POST | REQ-020 · ADR-016 · UNIT-002 | Body carries UTC ISO `expiresAt` |
| TEST-018 | unit (component) | Created link's expiry rendered | REQ-020 · UNIT-002 | `.result-expiry` shows expiry |
| createWithFutureExpiryEchoesExpiresAt | integration | Create with future expiry | REQ-011/019 · UNIT-001 | 201 echoes `expiresAt` |
| bulkCreateReturnsPerItemResults | integration | Bulk end-to-end | REQ-014/016 · UNIT-001 | 200 with ordered per-item results |

## 1b. Phase 1 test cases (regression baseline)

**Covered**

- Backend (UNIT-001): URL/alias validation, Base62 code generation, alias collision & reserved-word
  handling, create endpoint, redirect endpoint, error mapping (400/404/409), rate limiting behaviour,
  and a full create→redirect integration flow.
- Frontend (UNIT-002): `LinkService` HTTP contract, submission form success rendering, inline API-error
  display, and client-side invalid-form guarding.

**Excluded (Phase 1)**

- Load/performance benchmarking (p95 targets tracked in the QA plan, not asserted here).
- Distributed/multi-instance rate limiting (RISK-004, accepted).
- Analytics, authentication (out of MVP scope).

## 2. Test cases (Phase 1 baseline detail)

| TEST id | Type | Scenario | Traces to | Expected result |
|---------|------|----------|-----------|-----------------|
| TEST-001 | unit | Generated short code returned for a valid URL | REQ-001/002 · UNIT-001 | 7-char Base62 code, persisted |
| TEST-002 | unit | Custom alias accepted when available | REQ-003 · UNIT-001 | Alias used as code |
| TEST-003 | unit | Duplicate custom alias rejected | REQ-004 · UNIT-001 | `AliasAlreadyExistsException` → 409 |
| TEST-004 | unit | Reserved alias rejected | REQ-004 · UNIT-001 | `InvalidAliasException` → 400 |
| TEST-005 | unit | Invalid / non-http(s) / self-host URL rejected | REQ-005 · UNIT-001 | `InvalidUrlException` → 400 |
| TEST-006 | unit | Generated-code collision retried then persisted | REQ-002 · UNIT-001 | Retry on `DataIntegrityViolationException` |
| TEST-007 | unit | Resolve unknown code | REQ-006 · UNIT-001 | `CodeNotFoundException` → 404 |
| TEST-008 | unit (component) | Form submit renders returned short link | REQ-007 · UNIT-002 | `.short-url` shows returned `shortUrl` |
| TEST-009 | unit (component) | API 409 shows inline error | REQ-007 · UNIT-002 | `.api-error` shows backend message |
| LinkFlowIT | integration | Create then redirect end-to-end | REQ-001/006 · UNIT-001 | 201 then 302 with `Location` |

## 3. Coverage (required)

- **Unit**: backend service (expiry validation + lazy expiry + bulk best-effort) + code generator +
  controller slices (bulk 200/400/429) + shared `RateLimiter`; frontend service passthrough +
  component (datetime-local→UTC conversion, expiry rendering).
- **Integration**: `LinkFlowIT` (`@SpringBootTest`, random port) — create→redirect, future-expiry echo,
  and bulk per-item results end-to-end.
- **Edge / failure paths**: past/present expiry, expired-on-resolve, empty/oversized bulk, per-item
  duplicate alias and invalid expiry, rate-budget exhaustion.
- **Execution evidence (Phase 2 — this run)**:
  - Backend: `cd tiny-url-creator/backend && ./mvnw -o verify` → `Tests run: 31` (unit/slice) +
    `Tests run: 3` (Failsafe `LinkFlowIT`), `Failures: 0, Errors: 0`, **BUILD SUCCESS** (rc=0).
  - Frontend: `cd tiny-url-creator/frontend && npm run build` → bundle complete (rc=0);
    `npm run test:ci` (`ChromeHeadlessCI`) → **TOTAL: 9 SUCCESS** (rc=0) incl. TEST-017/018.
  - Coverage evidence location: `sdlc-docs/construction/testing/` and
    `tiny-url-creator/backend/target/{surefire-reports,failsafe-reports}`.

## 4. Traceability (required)

- **Phase 2**: TEST-010..016b + TEST-C1..C4 + IT (future-expiry, bulk) ↔ UNIT-001 ↔ REQ-011..019 ↔
  ADR-010..015. TEST-017, TEST-018 ↔ UNIT-002 ↔ REQ-020 ↔ ADR-016.
- **Phase 1 baseline**: TEST-001..007 + LinkFlowIT ↔ UNIT-001 ↔ REQ-001..006; TEST-008, TEST-009 ↔
  UNIT-002 ↔ REQ-007.

## 5. Open gaps (required)

- Bulk creation has no UI (API-only this phase, ADR-016) — deferred by design.
- Expired rows are not reaped (RISK-012, accepted); aliases stay permanently reserved (ADR-015).
- Performance NFR assertions (redirect p95 ≤ 100 ms, create p95 ≤ 300 ms) are validated at
  release-readiness, not in this functional suite — tracked in the QA plan.
