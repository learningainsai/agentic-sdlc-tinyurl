<!-- template_id: release-readiness-template.md -->
<!-- v2 §4 mandatory template. Final lifecycle check; defers deploy to human decision (S7). -->

# Release Readiness — TinyURL Phase 2 (expiry + bulk creation)

- **template_id**: release-readiness-template.md
- **run_id**: run-20260802T150051Z
- **node_id**: release-readiness
- **status**: passed
- **supersedes**: run-20260801T232309Z (Phase 1 MVP) release readiness

## 1. Readiness items (required)

| REL id | Item | Status | Evidence |
|--------|------|--------|----------|
| REL-010 | Backend build & tests green (Phase 2) | ready | `./mvnw -o verify` — 31 unit/slice + 3 IT (`LinkFlowIT`), BUILD SUCCESS (H2 test profile) |
| REL-011 | Frontend build & tests green (Phase 2) | ready | `npm run build` bundle complete; `npm run test:ci` — 9 specs (incl. TEST-017/018) pass (ChromeHeadlessCI) |
| REL-012 | Phase 1 regression green | ready | Phase 1 suites re-run within the same builds; no regressions |
| REL-013 | Expiry behaviour validated (create 400 / resolve 404 / echo) | ready | TEST-010..013 + `createWithFutureExpiryEchoesExpiresAt` IT |
| REL-014 | Bulk create validated (partial success / bounds / 429) | ready | TEST-014..016b + controller 400/429 + `bulkCreateReturnsPerItemResults` IT |
| REL-015 | Security review, no unresolved high/critical | ready | code-review/security-review.md (SEC-010..014 resolved) |
| REL-016 | Code review passed | ready | code-review/code-review.md (verdict: passed) |
| REL-017 | Documentation published (DOC-003/004) | ready | backend/frontend README.md; documentation-plan.md |
| REL-018 | Config profiles unchanged (dev/prod PostgreSQL, tests on H2) | ready | No `application.yml`/dependency version changes this phase |
| REL-019 | Deployment/operations | deferred | Operations is a human decision (S7); no automated deploy configured |

## 2. Traceability coverage (required — §9)

| REQ | ADR | UNIT | PLAN | TEST | DOC | CR/SEC | Covered? |
|-----|-----|------|------|------|-----|--------|----------|
| REQ-011 | ADR-010 | UNIT-001 | PLAN-001 | createWithFutureExpiryEchoesExpiresAt | DOC-003 | CR-010 | yes |
| REQ-012 | ADR-012 | UNIT-001 | PLAN-001 | TEST-010/011/016b | DOC-003 | CR-010/SEC-011 | yes |
| REQ-013 | ADR-011 | UNIT-001 | PLAN-001 | TEST-012/013 | DOC-003 | CR-010/SEC-012 | yes |
| REQ-014 | ADR-013 | UNIT-001 | PLAN-001 | TEST-014, TEST-C1, bulk IT | DOC-003 | CR-011/SEC-010 | yes |
| REQ-015 | ADR-013 | UNIT-001 | PLAN-001 | TEST-C2/C3 | DOC-003 | CR-011/SEC-010 | yes |
| REQ-016 | ADR-013 | UNIT-001 | PLAN-001 | TEST-015/016/016b, bulk IT | DOC-003 | CR-011/SEC-013 | yes |
| REQ-017 | ADR-014 | UNIT-001 | PLAN-001 | TEST-C4 | DOC-003 | CR-012/SEC-010 | yes |
| REQ-018 | ADR-010 | UNIT-001 | PLAN-001 | TEST-013 | DOC-003 | CR-010 | yes |
| REQ-019 | ADR-010 | UNIT-001 | PLAN-001 | createWithFutureExpiryEchoesExpiresAt | DOC-003 | CR-010 | yes |
| REQ-020 | ADR-016 | UNIT-002 | PLAN-002 | TEST-017/018 | DOC-004 | CR-014/SEC-014 | yes |

All Phase 2 approved requirements have linked design, implementation, test, documentation, and review
evidence. Phase 1 requirements (REQ-001..010) remain covered by the baseline readiness record.

## 3. Risk & rollback (required)

- **Unresolved risks**: none high/critical. RISK-011 (bulk DoS amplification) **mitigated** by the
  100-item cap + N-token rate limiting (SEC-010). RISK-012 (no expired-row reaper) **accepted**;
  expired aliases stay reserved (ADR-015). RISK-004 (in-memory per-instance limiter) and SEC-007
  (dev-only toolchain advisories) remain accepted baseline items.
- **Schema change**: a new nullable `expires_at` column is added to `short_link`. It is additive and
  backward-compatible (NULL = never expires); existing rows are unaffected. Dev uses `ddl-auto: update`;
  prod expects a forward-only migration adding the nullable column.
- **Rollback / compensating actions**: the app remains stateless apart from PostgreSQL; roll back by
  redeploying the previous artifact. Because the column is nullable and additive, the prior build
  continues to function against the new schema (it simply ignores the column). No destructive data
  operation in this release.
- **Operational notes**: no new config keys or dependencies introduced. Same prod env
  (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_BASE_URL`, `SPRING_PROFILES_ACTIVE=prod`).

## 4. Evidence summary (required)

- **Test evidence**: `sdlc-docs/construction/testing/test-plan.md`; Surefire/Failsafe reports;
  frontend Karma run (9 passing).
- **Review evidence**: `sdlc-docs/construction/code-review/code-review.md` (verdict: passed).
- **Security evidence (SEC-...)**: `sdlc-docs/construction/code-review/security-review.md`
  (SEC-010..014 resolved; no unresolved high/critical).

## 5. Metrics summary (required — §12)

- handoff_count: 11 · handoff_latency: n/a (single session) · blocked_gate_count: 0
- policy_error_count: 0 · retry_count: 0 · unresolved_risk_count: 0
- review_finding_count: high 0, medium 0, low 1 (CR-001 cosmetic, carried from Phase 1)
- traceability_coverage: 100% (10/10 Phase 2 approved requirements fully linked)

## 6. Verdict (required)

- **Recommendation**: ready
- **Human decision** (Operations placeholder — no automated deploy): approved (`Go`)
