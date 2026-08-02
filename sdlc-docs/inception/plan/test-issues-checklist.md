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
