<!-- template_id: test-strategy-template.md -->

# Test Strategy — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: test-strategy-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: plan
- **status**: draft
- **skill**: breakdown-test
- **sources**: requirements.md (REQ-001..010), user-stories.md (US-001..010),
  architecture-design.md (ADR-001..009, RISK-002..005), project-plan.md, unit-registry.yaml (UNIT-001/002)

## 1. Test strategy overview (required)

- **Testing scope**: Backend link-creation + redirect API (UNIT-001) and Angular submission UI
  (UNIT-002). In scope: URL validation, alias handling, Base62 code generation & uniqueness,
  302 redirect, 404 for unknown, per-IP rate limiting, self-host rejection, UI submission/copy/errors.
  Out of scope (Phase 1): auth, analytics, link expiry, multi-instance rate limiting.
- **Quality objectives / success criteria**:
  - 100% of REQ-001..REQ-010 acceptance criteria validated by automated tests.
  - Backend line coverage ≥ 80%, branch coverage ≥ 90% on generation/validation/redirect paths.
  - Redirect p95 ≤ 100 ms; creation p95 ≤ 300 ms (NFR).
  - Zero critical/high open defects at the release-readiness gate.
- **Risk assessment** (from architecture):
  - RISK-002 (medium) redirect abuse → covered by security tests for scheme allowlist, max length,
    self-host rejection (US-010), rate limiting (US-009).
  - RISK-003 code collision → concurrency/uniqueness test (US-008, TEST-005).
  - RISK-004 (low) per-instance rate limiter → single-instance test; documented limitation.
  - RISK-005 (low) FE/BE integration → E2E happy-path test in the testing node.
- **Test approach**: Risk-based, ISTQB-aligned. Black-box for API contract & UI behaviour, white-box
  for generator/validator branches, experience-based (error guessing) for malformed input.

## 2. ISTQB framework implementation (required)

### Test design techniques selected
- **Equivalence Partitioning**: URL classes (valid http/https, invalid scheme, malformed, > 2048),
  alias classes (valid, too short/long, illegal chars, reserved word, duplicate).
- **Boundary Value Analysis**: alias length {2,3,30,31}; URL length {2048, 2049}; Base62 code length = 7.
- **Decision Table Testing**: create-link outcomes over {alias present?, alias valid?, alias taken?,
  URL valid?, self-host?} → 201 / 400 / 409.
- **State Transition Testing**: unknown code → create → known code → redirect; rate-limit window
  open → threshold reached (429) → window reset.
- **Experience-Based Testing**: error guessing on encoded payloads, whitespace, unicode, null alias.

### Test types coverage matrix
| Test type | Coverage | Techniques |
|-----------|----------|------------|
| Functional | US-001..010 create/redirect/validation | EP, BVA, decision table, state transition |
| Non-functional | performance (redirect/create p95), security (SSRF/self-host, rate limit), usability/a11y (UI) | experience-based, BVA |
| Structural | generator/validator/redirect branch coverage ≥90% | white-box |
| Change-related | regression suite re-run on each change | confirmation + regression |

## 3. ISO 25010 quality characteristics assessment (required)

| Characteristic | Priority | Validation approach |
|----------------|----------|---------------------|
| Functional suitability | **Critical** | REQ-001..010 acceptance tests (TEST-001..009) |
| Security | **High** | scheme allowlist, self-host rejection (US-010), rate limit (US-009), input validation, no server-side fetch |
| Performance efficiency | **High** | redirect p95 ≤100 ms, create p95 ≤300 ms; concurrency uniqueness (US-008) |
| Reliability | **Medium** | collision retry, error handling (ErrorResponse advice), 404/409/429 paths |
| Usability | **Medium** | UI submit/copy/inline errors (US-007), keyboard + label accessibility (WCAG AA basics) |
| Compatibility | **Medium** | modern-browser check for Angular UI; JSON API contract stability |
| Maintainability | **Medium** | layered structure, unit-testable services, ≥80% coverage |
| Portability | **Low** | H2 (dev) / PostgreSQL (prod) parity via JPA; env-config only |

## 4. Test environment & data strategy (required)

- **Environment**: JUnit 5 + Spring Boot Test with H2 in-memory for backend; MockMvc for controller
  slice; Jasmine/Karma for Angular unit tests; one E2E happy-path (UI → API) in the testing node.
- **Test data**: generated fixtures — valid/invalid URL sets, alias edge cases, pre-seeded codes for
  redirect/collision; no PII (anonymous MVP).
- **Tooling**: Maven Surefire, MockMvc, Mockito, Angular TestBed/HttpTestingController. No new
  third-party libraries introduced (flag before adding, e.g. Playwright/JaCoCo thresholds).
- **CI/CD integration**: `./mvnw test` + `npm test` run on every change; coverage report published;
  gate blocks on failing tests or unmet coverage thresholds.

## 5. Traceability (required)

| Test item | REQ | US | ADR | UNIT | TEST |
|-----------|-----|----|-----|------|------|
| Create without alias | 001 | 001 | 004 | UNIT-001 | TEST-001 |
| Create with alias | 002 | 002 | 007 | UNIT-001 | TEST-002 |
| Alias/URL error mapping (400/409) | 003,004 | 003,004 | 006,009 | UNIT-001 | TEST-003 |
| URL validation (scheme/length) | 004 | 004 | 006 | UNIT-001 | TEST-004 |
| Redirect 302 | 005 | 005 | 005 | UNIT-001 | TEST-006 |
| Unknown code 404 | 006 | 006 | 009 | UNIT-001 | TEST-007 |
| Code uniqueness under load | 008 | 008 | 004 | UNIT-001 | TEST-005 |
| Per-IP rate limit 429 | 009 | 009 | 008 | UNIT-001 | TEST-003 |
| Self-host rejection 400 | 010 | 010 | 007 | UNIT-001 | TEST-004 |
| UI submit + copy | 007 | 007 | 002 | UNIT-002 | TEST-008 |
| UI inline error display | 007 | 007 | 002 | UNIT-002 | TEST-009 |

## 6. Open questions (required)

N/A — derived from approved inception artifacts; no open questions.
