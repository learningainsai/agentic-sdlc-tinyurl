# Standard — Code Review

- **standard_id**: code-review-standard
- **enforcement**: always (v2 §6)
- **required coverage**: correctness, security, maintainability, test quality, business logic risk

Code review is an explicit lifecycle check before final completion. Findings are `CR-` items.

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| CR-CORRECT-01 | Implementation meets its functional design and requirements. | Review maps behaviour to REQ-/functional design. |
| CR-SEC-01 | No security regressions introduced. | Cross-check against security-standard findings (SEC-). |
| CR-MAINT-01 | Change is maintainable and consistent with coding-standard. | Cross-check against coding-standard (COD-). |
| CR-TESTQ-01 | Tests are meaningful and adequate, not superficial. | Review of TEST- coverage and assertions. |
| CR-BIZ-01 | Business-logic risks and edge cases are considered. | Review notes residual risks and mitigations. |

## Verdict & non-compliance
- Outcome is one of `approved | changes_requested | rejected`.
- Unresolved high-severity CR- findings block release readiness.
- Every CR- traces to a UNIT-/PLAN-/REQ- id.
