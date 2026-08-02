<!-- template_id: qa-plan-template.md -->

# Quality Assurance Plan — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: qa-plan-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: plan
- **status**: draft

## 1. Quality validation scope (required)

Validate that the MVP satisfies REQ-001..REQ-010 with acceptable security, performance, and usability
before release. Covers UNIT-001 (backend API) and UNIT-002 (frontend UI), and their integration.

## 2. ISO 25010 quality assessment (required)

| Characteristic | Validation approach |
|----------------|---------------------|
| Functional suitability | Acceptance tests TEST-001..009 map 1:1 to REQ-001..010 |
| Security | Scheme allowlist, self-host rejection, per-IP rate limit, input validation, no server-side fetch (SSRF-safe); OWASP checks — no medium+ unresolved SEC findings |
| Performance efficiency | Redirect p95 ≤100 ms, create p95 ≤300 ms; concurrency uniqueness |
| Reliability | Collision retry; consistent ErrorResponse for 400/404/409/429 |
| Usability | Submission form: submit, copy, inline errors; WCAG AA basics |
| Maintainability | Layered design, ≥80% line coverage, constructor injection, DTOs not entities |
| Compatibility | JSON API contract stable; modern-browser UI |
| Portability | H2 (dev) / PostgreSQL (prod) via JPA; env-config only |

## 3. Quality gates (required)

- **Entry criteria (testing phase)**: implementation node complete for the unit; unit tests present;
  code review approved; build green.
- **Exit criteria**: all test types pass ≥95%; 100% acceptance criteria validated; no critical/high
  defects; coverage thresholds met; performance thresholds met; zero critical security vulnerabilities.
- **Quality metrics**: line ≥80% / branch ≥90% (critical paths); defect density < 1/KLOC; redirect
  p95 ≤100 ms; create p95 ≤300 ms; WCAG AA (form basics); zero critical vulnerabilities.
- **Escalation**: any unmet exit criterion blocks the testing/release-readiness gate and returns the
  unit to construction with a logged defect; orchestrator raises a human decision point.

## 4. Labels & prioritization standards (required)

- Test type: `unit-test`, `integration-test`, `e2e-test`, `performance-test`, `security-test`, `accessibility-test`
- Quality: `quality-gate`, `iso25010`, `istqb-technique`, `risk-based`
- Priority: `test-critical` (redirect, create, security), `test-high`, `test-medium`, `test-low`
- Component: `backend-test`, `frontend-test`, `api-test`, `database-test`

## 5. Traceability (required)

| QA item | REQ | US | UNIT | TEST |
|---------|-----|----|------|------|
| Functional acceptance | 001–010 | 001–010 | UNIT-001/002 | TEST-001..009 |
| Security validation | 004,009,010 | 004,009,010 | UNIT-001 | TEST-003,004 |
| Performance validation | 005,008 | 005,008 | UNIT-001 | TEST-005,006 |
| Usability/accessibility | 007 | 007 | UNIT-002 | TEST-008,009 |

---

## 6. Phase 2 QA — Expiry + Bulk creation (run-20260802T150051Z)

### 6.1 Scope
Validate REQ-011..REQ-020 across UNIT-001 (backend expiry + bulk) and UNIT-002 (frontend expiry
picker) before release: expiry storage/validation, lazy expiry (expired→404), expired-alias
reservation, bulk endpoint (partial success, bounds, N-token rate limiting), and the UI picker.

### 6.2 ISO 25010 (Phase 2)
| Characteristic | Validation approach |
|----------------|---------------------|
| Functional suitability | TEST-010..018 map to REQ-011..020 |
| Security | bulk cap + N-token limit (no rate-limit bypass); per-item validation; expiry→404 no existence leak; no medium+ unresolved SEC findings |
| Performance efficiency | redirect p95 ≤ 100 ms unchanged (single in-memory expiry compare); bulk bounded ≤100 items |
| Reliability | best-effort partial success; consistent ErrorResponse 400/404/409/429 |
| Maintainability | shared RateLimiter refactor keeps paths DRY; ≥80% coverage on new code |

### 6.3 Quality gates (Phase 2)
- **Entry (testing)**: implementation complete for the unit; Phase 2 unit tests present; code review
  approved; build green; Phase 1 regression suite green.
- **Exit**: 100% of REQ-011..020 acceptance criteria pass; no critical/high defects; coverage
  thresholds met; redirect performance unchanged; zero critical security vulnerabilities; bulk cannot
  bypass rate limiting.
- **Escalation**: any unmet criterion blocks the testing/release-readiness gate and returns the unit
  to construction with a logged defect; orchestrator raises a human decision point.

### 6.4 Traceability (Phase 2)
| QA item | REQ | US | UNIT | TEST |
|---------|-----|----|------|------|
| Functional acceptance | 011–020 | 011–019 | UNIT-001/002 | TEST-010..018 |
| Security validation | 017,019 | 017,018 | UNIT-001 | TEST-015,016 |
| Performance validation | 013 | 013 | UNIT-001 | TEST-011 |
| Reliability (partial success) | 016 | 016 | UNIT-001 | TEST-014 |
| Usability (expiry picker) | 020 | 019 | UNIT-002 | TEST-017,018 |

## 7. Phase 3 QA — Bulk-creation DB-query optimization (run-20260802T170000Z)

### 7.1 Scope
Validate REQ-021..REQ-025 across UNIT-001 (backend) before release: that the bulk endpoint issues a
**bounded** number of DB round trips for an N-item batch, and that its **behaviour and response
contract are unchanged** (partial success preserved). This is a performance/characterization change —
no new API surface, no frontend work.

### 7.2 ISO 25010 (Phase 3)
| Characteristic | Validation approach |
|----------------|---------------------|
| Performance efficiency | TEST-019 asserts bounded statement count via Hibernate `Statistics`; batching engages (SEQUENCE id + `batch_size`/`order_inserts`) |
| Functional suitability | TEST-020 asserts bulk response byte-for-byte identical to baseline across input classes (REQ-022, REQ-023) |
| Reliability | per-item transactional fallback preserves best-effort partial success under batched-insert failure (ADR-021) |
| Compatibility | id IDENTITY→SEQUENCE change alters no existing behaviour — full existing suite stays green (RISK-023) |
| Security | no new attack surface; existing bulk cap + N-token limit unaffected; no medium+ unresolved SEC findings |
| Maintainability | two-pass structure keeps validation/persistence separable; ≥80% coverage on changed code |

### 7.3 Quality gates (Phase 3)
- **Entry (testing)**: implementation complete for UNIT-001; Phase 3 tests present (statement-count +
  contract preservation); durable high-impact approval for the id-generation + `application.yml`
  change recorded; code review approved; build green; full existing suite green.
- **Exit**: 100% of REQ-021..025 acceptance criteria pass; measured bulk round-trip count bounded and
  sub-linear vs baseline (REQ-024); bulk response identical to baseline for all input classes
  (REQ-022, REQ-023); no critical/high defects; coverage thresholds met; zero critical security
  findings; full existing suite green.
- **Escalation**: any unmet criterion (including a behavioural diff in the bulk response, or a
  regression from the id-generation change) blocks the gate and returns UNIT-001 to construction with a
  logged defect; orchestrator raises a human decision point.

### 7.4 Traceability (Phase 3)
| QA item | REQ | US | UNIT | TEST |
|---------|-----|----|------|------|
| Performance (bounded round trips) | 021,024 | 020 | UNIT-001 | TEST-019 |
| Functional acceptance (contract unchanged) | 022 | 021 | UNIT-001 | TEST-020 |
| Reliability (partial success preserved) | 023 | 021 | UNIT-001 | TEST-020 |
| Change-control (config + id-generation approved) | 025 | 020 | UNIT-001 | — |
