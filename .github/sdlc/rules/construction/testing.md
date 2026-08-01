# Construction Rule — Testing

- **rule_id**: CON-TESTING
- **loop step**: 7 (unit tests) + node: testing
- **template**: test-plan-template.md
- **standard**: testing-standard

## Objective
Produce and execute tests proving the unit and integrated system meet requirements.

## Procedure
1. Author a test plan; assign `TEST-` ids and trace each to REQ-/UNIT-.
2. Cover unit, integration, regression, edge cases, and failure paths (testing-standard).
3. Store coverage evidence under `sdlc-docs/construction/testing/`.
4. Missing required tests for a unit block the construction join (§3 conflict rule).

## Exit gate
- Compliance policy check on the testing node.
- Handoff references TEST- ids and coverage evidence.
