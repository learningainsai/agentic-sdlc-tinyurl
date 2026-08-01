# Construction Rule — NFR Requirements

- **rule_id**: CON-NFR-REQUIREMENTS
- **loop step**: 2 (per-unit, when required)
- **template**: nfr-requirements-template.md

## Objective
Capture unit-scoped non-functional requirements (performance, availability, security,
scalability, observability).

## Procedure
1. Identify NFRs relevant to the unit; assign `REQ-N` ids.
2. Define measurable targets and verification methods.
3. Trace to parent REQ-/ADR-.

## When required
- Required when the unit affects an NFR-sensitive path.
- If NFR requirements are present, an NFR design (step 3) must follow.
