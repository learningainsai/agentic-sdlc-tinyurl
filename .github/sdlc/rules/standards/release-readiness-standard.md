# Standard — Release Readiness

- **standard_id**: release-readiness-standard
- **enforcement**: always (v2 §6)
- **required coverage**: unresolved risks, rollback/compensation, test evidence, review evidence, operational notes

Release readiness is the final lifecycle check before completion. Items are `REL-` ids.
Operations remains a placeholder — deployment defers to a human decision (spec S7).

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| REL-RISK-01 | No unresolved high/critical risks remain, or each is human-accepted. | Open risks reviewed; acceptances recorded in audit. |
| REL-ROLL-01 | Rollback and compensating actions exist for non-reversible effects. | Compensating actions registered (spec S4). |
| REL-TEST-01 | Test evidence is complete and passing. | Coverage + results referenced from testing node. |
| REL-REVIEW-01 | Code and security reviews are complete with no blocking findings. | CR-/SEC- findings resolved or accepted. |
| REL-TRACE-01 | Every approved requirement has linked design, implementation, test, doc, and review evidence. | Traceability matrix complete (§9); else block. |
| REL-OPS-01 | Operational notes and a human release decision are recorded. | Human decision captured in audit + approval record. |
| REL-METRICS-01 | Run metrics summary is included. | §12 metrics present in the release-readiness artifact. |

## Non-compliance handling
- Any incomplete REL-TRACE-01 row forces a gate `block`.
- No automated deploy is assumed; final decision is a recorded human approval.
