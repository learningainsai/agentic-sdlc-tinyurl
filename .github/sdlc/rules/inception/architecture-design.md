# Inception Rule — Architecture & Design

- **rule_id**: INC-ARCHITECTURE
- **node**: architecture-design (high_impact)
- **templates**: architecture-design-template.md, nfr-requirements-template.md, nfr-design-template.md
- **standards**: security-standard, coding-standard

## Objective
Produce the architecture/design artifact at
`sdlc-docs/inception/architecture-design/architecture-design.md`.

## Procedure
1. Derive architecture decisions (`ADR-`) from approved requirements (`REQ-`).
2. Define components, interfaces, data model, and trust boundaries.
3. Capture NFR requirements and NFR design when applicable.
4. Apply security-standard and coding-standard checks; record findings with rule IDs.
5. **High-impact (§8)**: classify as core_feature/critical_design, state rationale, and obtain a
   **durable approval record** at `sdlc-docs/approvals/<run>/architecture-design/<action-id>.yaml`
   with `decision: approved` before proceeding.

## Exit gate
- Human approval + security + compliance policy checks.
- Emit handoff with ADR ids, standards applied, approval record path, and context summary.
