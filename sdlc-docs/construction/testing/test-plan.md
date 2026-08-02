<!-- template_id: test-plan-template.md -->
<!-- v2 §4 mandatory template. Enforced by testing-standard (§6). -->

# Test Plan — run-20260801T232309Z (TinyURL Phase 1 MVP)

- **template_id**: test-plan-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: testing
- **status**: approved

## 1. Test scope (required)

**Covered**

- Backend (UNIT-001): URL/alias validation, Base62 code generation, alias collision & reserved-word
  handling, create endpoint, redirect endpoint, error mapping (400/404/409), rate limiting behaviour,
  and a full create→redirect integration flow.
- Frontend (UNIT-002): `LinkService` HTTP contract, submission form success rendering, inline API-error
  display, and client-side invalid-form guarding.

**Excluded (Phase 1)**

- Load/performance benchmarking (p95 targets tracked in the QA plan, not asserted here).
- Distributed/multi-instance rate limiting (RISK-004, accepted).
- Analytics, authentication, link expiry (out of MVP scope).

## 2. Test cases (required)

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

- **Unit**: backend service + code generator + controller slices; frontend service + component.
- **Integration**: `LinkFlowIT` (`@SpringBootTest`, random port) covers the create→redirect path.
- **Edge / failure paths**: invalid URL, reserved/duplicate alias, unknown code, invalid client form.
- **Execution evidence**:
  - Backend: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw -B verify` → `Tests run: 18` (unit) +
    `Tests run: 1` (Failsafe IT), `Failures: 0, Errors: 0`, **BUILD SUCCESS**.
  - Frontend: `ng test --watch=false --browsers=ChromeHeadlessCI` → **6 SUCCESS** (incl. TEST-008/009).
  - Coverage evidence location: `sdlc-docs/construction/testing/` and
    `tiny-url-creator/backend/target/{surefire-reports,failsafe-reports}`.

## 4. Traceability (required)

- TEST-001..007 + LinkFlowIT ↔ UNIT-001 ↔ REQ-001..006.
- TEST-008, TEST-009 ↔ UNIT-002 ↔ REQ-007.

## 5. Open gaps (required)

- Performance NFR assertions (redirect p95 ≤ 100 ms, create p95 ≤ 300 ms) are validated at
  release-readiness, not in this functional suite — tracked in the QA plan.
