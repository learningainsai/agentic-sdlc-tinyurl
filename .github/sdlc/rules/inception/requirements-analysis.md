# Inception Rule — Requirements Analysis

- **rule_id**: INC-REQUIREMENTS
- **node**: requirements
- **templates**: requirements-template.md, user-story-template.md
- **standards**: documentation-standard

## Objective
Produce a complete, testable requirements artifact at
`sdlc-docs/inception/requirements/requirements.md`.

## Procedure
1. Load always-load common rules and the documentation-standard.
2. Capture context, scope, functional + non-functional requirements, assumptions, constraints.
3. Assign a `REQ-` id to every requirement; write acceptance criteria that are objective/testable.
4. Author user stories from `user-story-template.md` for user-facing behaviour.
5. Record open questions; a non-empty list blocks the handoff unless a human decision is recorded (§2).

## Exit gate
- Human approval required; compliance policy check must pass.
- Emit handoff `sdlc-docs/handoffs/<run>/requirements.yaml` with output artifacts, template ids,
  standards applied, traceability ids, and a context summary.
