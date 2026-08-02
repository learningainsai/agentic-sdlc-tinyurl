<!-- template_id: release-readiness-template.md -->
<!-- v2 §4 mandatory template. Final lifecycle check; defers deploy to human decision (S7). -->

# Release Readiness — TinyURL Phase 1 MVP

- **template_id**: release-readiness-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: release-readiness
- **status**: draft (awaiting human go/no-go)

## 1. Readiness items (required)

| REL id | Item | Status | Evidence |
|--------|------|--------|----------|
| REL-001 | Backend build & tests green | ready | `mvn verify` — 18 unit + 1 IT, BUILD SUCCESS (H2 test profile) |
| REL-002 | Frontend build & tests green | ready | `ng test` — 6 specs (TEST-008/009) pass; `ng build` succeeds |
| REL-003 | End-to-end validated (UI → API → redirect) | ready | Live smoke: POST via proxy → 201; GET /{code} → 302 Location |
| REL-004 | Persistence validated (PostgreSQL dev) | ready | Row present in `short_link` via `psql`; survives restart |
| REL-005 | Security review, no unresolved high/critical | ready | code-review/security-review.md (SEC-001..008) |
| REL-006 | Code review approved | ready | code-review/code-review.md (verdict: approved) |
| REL-007 | Documentation published (DOC-001/002) | ready | backend/frontend README.md; documentation-plan.md |
| REL-008 | Config profiles: dev/prod PostgreSQL, tests on H2 | ready | application-{dev,prod,test}.yml; docker-compose.yml |
| REL-009 | Deployment/operations | deferred | Operations is a human decision (S7); no automated deploy configured |

## 2. Traceability coverage (required — §9)

| REQ | ADR | UNIT | PLAN | TEST | DOC | CR | Covered? |
|-----|-----|------|------|------|-----|----|----------|
| REQ-001 | ADR-001/003 | UNIT-001 | PLAN-001 | TEST-001, LinkFlowIT | DOC-001 | CR-002 | yes |
| REQ-002 | ADR-004 | UNIT-001 | PLAN-001 | TEST-001/006 | DOC-001 | CR-002 | yes |
| REQ-003 | ADR-003 | UNIT-001 | PLAN-001 | TEST-002 | DOC-001 | CR-002 | yes |
| REQ-004 | ADR-003 | UNIT-001 | PLAN-001 | TEST-003/004 | DOC-001 | CR-002 | yes |
| REQ-005 | ADR-007 | UNIT-001 | PLAN-001 | TEST-005 | DOC-001 | CR-002/SEC-001 | yes |
| REQ-006 | ADR-003 | UNIT-001 | PLAN-001 | TEST-007, LinkFlowIT | DOC-001 | CR-002/SEC-005 | yes |
| REQ-007 | ADR-002 | UNIT-002 | PLAN-002 | TEST-008/009 | DOC-002 | CR-004/005 | yes |
| REQ-008 | ADR-003 | UNIT-001 | PLAN-001 | LinkFlowIT | DOC-001 | CR-002 | yes |
| REQ-009 | ADR-008 | UNIT-001 | PLAN-001 | (manual/limiter) | DOC-001 | SEC-004 | yes (RISK-004 accepted) |
| REQ-010 | ADR-007 | UNIT-001 | PLAN-001 | TEST-005 | DOC-001 | SEC-001 | yes |

All approved requirements have linked design, implementation, test, documentation, and review evidence.

## 3. Risk & rollback (required)

- **Unresolved risks**: none high/critical. RISK-004 (in-memory per-instance rate limiter) accepted for
  single-instance MVP; SEC-007 (dev-only toolchain advisories) accepted.
- **Rollback / compensating actions**: backend is stateless apart from PostgreSQL; roll back by
  redeploying the previous artifact. Schema managed by `ddl-auto: update` in dev and `validate` in prod
  (prod expects migrations). No destructive data operation in this release.
- **Operational notes**: prod requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_BASE_URL` and
  `SPRING_PROFILES_ACTIVE=prod`. Frontend served as static bundle; API base via reverse proxy.

## 4. Evidence summary (required)

- **Test evidence**: `sdlc-docs/construction/testing/test-plan.md`; Surefire/Failsafe reports;
  frontend Karma run (6 passing).
- **Review evidence**: `sdlc-docs/construction/code-review/code-review.md` (approved).
- **Security evidence (SEC-...)**: `sdlc-docs/construction/code-review/security-review.md`
  (SEC-001..008; no unresolved high/critical).

## 5. Metrics summary (required — §12)

- handoff_count: 7 · handoff_latency: n/a (single session) · blocked_gate_count: 0
- policy_error_count: 0 · retry_count: 0 · unresolved_risk_count: 0
- review_finding_count: high 0, medium 0, low 2 (CR-001 cosmetic; SEC-004 accepted)
- traceability_coverage: 100% (10/10 approved requirements fully linked)

## 6. Verdict (required)

- **Recommendation**: ready
- **Human decision** (Operations placeholder — no automated deploy): pending
