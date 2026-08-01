# Construction Rule — Functional Design

- **rule_id**: CON-FUNCTIONAL-DESIGN
- **loop step**: 1 (per-unit, when required)
- **template**: functional-design-template.md

## Objective
Define the functional behaviour, interfaces, and edge cases for a unit before implementation.

## Procedure
1. Read the unit record and its `REQ-`/`ADR-` references.
2. Specify behaviour, interface contracts, error contracts, edge cases, and failure paths.
3. Record traceability to REQ-/ADR-.

## When required
- Required whenever the unit introduces new behaviour or non-trivial interfaces.
- Mark `N/A` with rationale for trivial units; the decision is auditable.
