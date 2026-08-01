# Standard — Testing

- **standard_id**: testing-standard
- **enforcement**: always (v2 §6)
- **required coverage**: unit, integration, regression, edge cases, failure paths, coverage evidence

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| TEST-UNIT-01 | Each unit of work has unit tests for its core logic. | TEST- ids exist and trace to UNIT-/REQ-. |
| TEST-INT-01 | Integration points between units are tested. | Integration TEST- ids present for cross-unit contracts. |
| TEST-REG-01 | Regression coverage protects previously passed behaviour. | Regression suite runs; no unexplained regressions. |
| TEST-EDGE-01 | Edge cases and boundary conditions are covered. | Edge-case TEST- ids present. |
| TEST-FAIL-01 | Failure paths and error handling are exercised. | Failure-path TEST- ids present. |
| TEST-COV-01 | Coverage evidence is captured. | Coverage report stored under sdlc-docs/construction/testing/. |

## Non-compliance handling
- Missing required tests for a unit block the construction join (§3 conflict rule).
- Every TEST- must trace to a REQ- or UNIT- id; untraced tests are a compliance finding.
