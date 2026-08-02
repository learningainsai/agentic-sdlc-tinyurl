<!-- template_id: work-package-template.md -->

# Work Package — UNIT-001

- **template_id**: work-package-template.md
- **unit_id**: UNIT-001
- **run_id**: run-20260801T232309Z
- **owner_agent**: construction
- **status**: pending

## 1. Objective (required)

Deliver the Spring Boot backend for the URL shortener: create links (with/without custom alias),
redirect via short code, input validation, per-IP rate limiting, and a uniform error contract.

## 2. Inputs (required)

- Requirements: REQ-001, REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-008, REQ-009, REQ-010
- Design refs: ADR-001, ADR-003, ADR-004, ADR-005, ADR-006, ADR-007, ADR-008, ADR-009
- Depends on units: — (none)

## 3. Target files (required)

- `tiny-url-creator/backend/**` (Maven project: `pom.xml`, `src/main/java/...`,
  `src/main/resources/application.yml`, `src/test/java/...`)

## 4. Execution loop checklist (required)

- [ ] functional design (if required)
- [ ] NFR requirements (covered by architecture NFR section)
- [ ] NFR design (covered by architecture NFR section)
- [ ] infrastructure design (N/A — local/dev H2, no infra change)
- [ ] code generation plan (PLAN-001)
- [ ] implementation
- [ ] unit-level tests (TEST-001..)
- [ ] unit documentation (DOC-001)
- [ ] unit code review (CR-001)
- [ ] unit handoff

> **Recommended skill**: use `maven-design` when scaffolding/tuning the Spring Boot Maven build
> (lifecycle, Surefire vs Failsafe test separation, JDK 17 compiler config, pinned plugin versions).
> Do not change dependency versions or add libraries without flagging first (AGENTS.md).

## 5. Expected tests (required)

- TEST-001 create short link (no alias) → 201 + code
- TEST-002 create with valid custom alias → 201
- TEST-003 duplicate/invalid alias → 409 / 400
- TEST-004 invalid URL (scheme/length/empty/self-host) → 400
- TEST-005 redirect known code → 302 Location
- TEST-006 unknown code → 404
- TEST-007 rate limit exceeded → 429

## 6. Traceability (required)

- UNIT-001 ↔ REQ-001/002/003/004/005/006/008/009/010 ↔ ADR-001/003/004/005/006/007/008/009 ↔
  PLAN-001 ↔ TEST-001..007 ↔ DOC-001 ↔ CR-001.
