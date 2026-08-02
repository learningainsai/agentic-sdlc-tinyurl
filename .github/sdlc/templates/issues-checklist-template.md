<!-- template_id: issues-checklist-template.md -->
<!-- v2 §4 mandatory template. Produced by the `plan` inception node via the breakdown-plan skill.
     Stored at sdlc-docs/inception/plan/issues-checklist.md. -->

# Issue Creation Checklist — <feature/change name>

- **template_id**: issues-checklist-template.md
- **run_id**: <run-id>
- **node_id**: plan
- **status**: draft | approved

## Pre-creation preparation (required)
- [ ] Inception artifacts complete (requirements, user stories, architecture, unit registry)
- [ ] Epic defined with labels and milestone
- [ ] Project board configured (columns, fields, automation)

## Epic-level (required)
- [ ] Epic issue with description + acceptance criteria
- [ ] Labels applied; added to board

## Feature-level (required)
- [ ] Feature issue linked to epic
- [ ] Dependencies identified; estimate assigned; acceptance criteria defined

## Story / Enabler / Test level (required)
- [ ] Stories follow INVEST; enablers identified; tests defined
- [ ] Story points assigned; dependencies mapped; acceptance criteria detailed
- [ ] Each item traces to REQ-/US-/ADR-/UNIT-

## Traceability (required)
- Mapping table: issue ↔ REQ-/US-/ADR-/UNIT-.
