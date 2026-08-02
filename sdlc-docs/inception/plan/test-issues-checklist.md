<!-- template_id: test-issues-checklist-template.md -->

# Test Issues Checklist — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: test-issues-checklist-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: plan
- **status**: draft

## Test level issues (required)
- [ ] Test strategy issue (references test-strategy.md) — `test-strategy`, est 2
- [ ] Unit test issues — backend services/generator/validator (UNIT-001), UI component (UNIT-002)
- [ ] Integration test issues — MockMvc API contract (create + redirect), repository layer
- [ ] End-to-end test issue — UI submit → short URL → redirect happy path (testing node)
- [ ] Performance test issue — redirect p95 ≤100 ms, create p95 ≤300 ms
- [ ] Security test issue — self-host rejection, scheme allowlist, per-IP rate limit
- [ ] Accessibility test issue — WCAG AA basics for submission form (labels, keyboard, focus)
- [ ] Regression test issue — full suite re-run on each change

## Coverage targets & metrics (required)
- [ ] Code coverage: >80% line, >90% branch on generation/validation/redirect paths
- [ ] Functional coverage: 100% of REQ-001..010 acceptance criteria
- [ ] Risk coverage: 100% of RISK-002/003/004/005 scenarios
- [ ] ISO 25010 coverage: functional suitability, security, performance validated

## Task breakdown & estimation (required)
| Test task | Type | Est | Depends on |
|-----------|------|-----|-----------|
| Backend unit tests (generator, validator, service) | unit | 2 | EN-2, EN-4 |
| API integration tests (create 201/400/409, redirect 302/404) | integration | 2 | EN-1 |
| Concurrency/uniqueness test | integration | 1 | EN-2 |
| Rate-limit + self-host security tests | security | 2 | EN-4 |
| Performance smoke (redirect/create p95) | performance | 3 | EN-1 |
| UI unit tests (submit, copy, errors) | unit | 2 | EN-3 |
| E2E happy path | e2e | 2 | UNIT-001 + UNIT-002 |
- [ ] Critical path: EN-1 → API integration tests → E2E

## Traceability (required)
| Test issue | REQ | US | UNIT | TEST |
|------------|-----|----|------|------|
| Backend unit/integration | 001–006,008,009,010 | 001–006,008,009,010 | UNIT-001 | TEST-001..007 |
| Security tests | 009,010 | 009,010 | UNIT-001 | TEST-003,004 |
| UI tests | 007 | 007 | UNIT-002 | TEST-008,009 |
| E2E | 001,005,007 | 001,005,007 | UNIT-001/002 | TEST-006,008 |

---

## Phase 2 test issues — Expiry + Bulk creation (run-20260802T150051Z)

### Test level issues
- [ ] Expiry unit/integration — create with expiresAt, past/now→400, expired→404, expired alias→409
      (use a fixed/injected clock for deterministic boundaries)
- [ ] Bulk integration — happy path (ordered results), best-effort partial success, intra-batch alias
      collision, 0/>100→400, N-token budget < N → 429 (nothing created)
- [ ] Security test — bulk cannot bypass per-IP rate limiting; per-item validation parity with single create
- [ ] UI unit tests — expiry picker sends `expiresAt`; blank omits it; created expiry displayed
- [ ] Regression — Phase 1 suite (TEST-001..009) stays green after schema + limiter refactor

### Coverage targets & metrics (Phase 2)
- [ ] 100% of REQ-011..020 acceptance criteria automated
- [ ] ≥80% line / ≥90% branch on new expiry + bulk paths
- [ ] 100% of RISK-011/012/013 scenarios covered
- [ ] Redirect hot path performance unchanged (added expiry comparison only)

### Task breakdown & estimation (Phase 2)
| Test task | Type | Est | Depends on |
|-----------|------|-----|-----------|
| Expiry validation + lazy-expiry redirect tests | unit/integration | 3 | EN-5 |
| Expired-alias reservation test | integration | 1 | EN-5 |
| Bulk happy + partial-success + bounds tests | integration | 3 | EN-6 |
| Bulk N-token rate-limit test | security | 2 | EN-7 |
| UI expiry picker tests | unit | 2 | EN-8 |
| Phase 1 regression re-run | regression | 1 | EN-5, EN-7 |
- [ ] Critical path: EN-5 → expiry tests; EN-6 → bulk tests → EN-7 → rate-limit test

### Phase 2 traceability
| Test issue | REQ | US | UNIT | TEST |
|------------|-----|----|------|------|
| Expiry tests | 011,012,013,014 | 011–014 | UNIT-001 | TEST-010,011,012 |
| Bulk tests | 015,016,017,018 | 015–017 | UNIT-001 | TEST-013,014,015 |
| Bulk rate-limit | 019 | 018 | UNIT-001 | TEST-016 |
| UI expiry | 020 | 019 | UNIT-002 | TEST-017,018 |
