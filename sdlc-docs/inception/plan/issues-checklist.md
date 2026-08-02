<!-- template_id: issues-checklist-template.md -->

# Issue Creation Checklist — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: issues-checklist-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: plan
- **status**: draft

## Pre-creation preparation (required)
- [x] Inception artifacts complete (requirements.md, user-stories.md, architecture-design.md, unit-registry.yaml)
- [ ] Epic EPIC-1 created with labels + milestone
- [ ] Project board configured (columns, fields, automation)

## Epic-level (required)
- [ ] EPIC-1 issue with description + acceptance criteria (REQ-001..010)
- [ ] Labels: `epic`, `priority-critical`, `value-high`; added to board

## Feature-level (required)
- [ ] FEAT-1 Link Creation → EPIC-1 (est S; blocked by EN-1)
- [ ] FEAT-2 Redirection → EPIC-1 (est XS; blocked by EN-1)
- [ ] FEAT-3 Submission UI → EPIC-1 (est S)
- [ ] FEAT-4 Safety & Abuse Controls → EPIC-1 (est S)

## Story / Enabler / Test level (required)
- [ ] US-001..US-010 created (INVEST), points assigned, linked to features
- [ ] EN-1..EN-4 enablers created and prioritized (EN-1 is P0 critical path)
- [ ] TEST-001..009 created and linked to their stories
- [ ] Dependencies mapped: EN-1 → EN-2 → US-001/002; EN-4 → US-003/009; EN-3 → US-007
- [ ] Every item traces to REQ-/US-/ADR-/UNIT- (see project-plan.md §8)

## Traceability (required)
| Issue | REQ | US | ADR | UNIT |
|-------|-----|----|-----|------|
| EPIC-1 | 001–010 | 001–010 | 001–009 | UNIT-001/002 |
| FEAT-1..4 | see project-plan §8 | — | — | — |
| US-001..010 | 001–010 | 001–010 | per story | UNIT-001/002 |
| EN-1..4 | per enabler | — | 002/003/004/007/008/009 | UNIT-001/002 |
| TEST-001..009 | per test | — | — | UNIT-001/002 |
