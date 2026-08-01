# Standard — Coding

- **standard_id**: coding-standard
- **enforcement**: always (v2 §6)
- **required coverage**: style, maintainability, dependency use, error handling, testability

A node cannot pass if an applicable rule below has an unresolved non-compliance finding.
Gate evidence must reference these rule IDs, not only free text.

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| COD-STYLE-01 | Code follows the project/language style conventions and formatting. | Linter/formatter run clean or findings resolved. |
| COD-MAINT-01 | Functions/modules are cohesive, single-purpose, and named clearly. | Review confirms no god-objects / dead code; complexity within limits. |
| COD-MAINT-02 | No unnecessary abstractions or duplicated logic. | Review confirms one-time operations are not over-engineered. |
| COD-DEP-01 | Dependencies are justified, pinned/managed, and license-compatible. | Dependency manifest reviewed; no unused/unvetted deps. |
| COD-ERR-01 | Errors handled at boundaries; no swallowed exceptions; no invalid states. | Review of error paths; failure-path tests exist. |
| COD-TEST-01 | Code is structured for testability (DI, pure functions where practical). | Unit tests can target logic without heavy scaffolding. |

## Non-compliance handling
- Any open COD-* finding of medium+ severity blocks the node's exit gate.
- Findings link to `CR-` code-review IDs and the affected `UNIT-`/`PLAN-` IDs.
