<!-- template_id: test-plan-template.md -->
<!-- v2 §4 mandatory template. Enforced by testing-standard (§6). -->

# Test Plan — <scope: run or UNIT-nnn>

- **template_id**: test-plan-template.md
- **run_id**: <run-id>
- **node_id**: testing
- **status**: draft | approved

## 1. Test scope (required)
<What is covered; what is deliberately excluded.>

## 2. Test cases (required)
Each test carries a `TEST-` id.

| TEST id | Type | Scenario | Traces to | Expected result |
|---------|------|----------|-----------|-----------------|
| TEST-001 | unit / integration / regression / edge / failure-path | | REQ-.../UNIT-... | |

## 3. Coverage (required)
- Unit / integration / regression / edge cases / failure paths addressed.
- Coverage evidence location: sdlc-docs/construction/testing/

## 4. Traceability (required)
- Each TEST traces to REQ-... and/or UNIT-...

## 5. Open gaps (required)
<Mark N/A with rationale if none.>
