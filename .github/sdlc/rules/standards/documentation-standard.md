# Standard — Documentation

- **standard_id**: documentation-standard
- **enforcement**: always (v2 §6)
- **required coverage**: user-facing docs, developer docs, generated artifact freshness, traceability

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| DOC-USER-01 | User-facing behaviour changes are documented. | DOC- artifact for user audience exists. |
| DOC-DEV-01 | Developer docs cover interfaces, setup, and design decisions. | DOC- artifact for developer audience exists. |
| DOC-FRESH-01 | Generated artifacts reflect current implementation. | Regeneration steps recorded; no stale references. |
| DOC-TRACE-01 | Documentation traces to requirements and units. | Each DOC- traces to REQ-/UNIT-. |

## Non-compliance handling
- Stale documentation referencing outdated traceability IDs blocks the compliance gate (§9).
- Missing required audience documentation is a medium finding that blocks the documentation exit gate.
