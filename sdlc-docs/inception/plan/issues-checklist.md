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

---

## Phase 2 issues — Expiry + Bulk creation (run-20260802T150051Z)

### Epic / Feature level
- [ ] EPIC-2 Link lifecycle & bulk creation (labels `epic`,`priority-high`,`value-high`; AC REQ-011..020)
- [ ] FEAT-5 Link expiry → EPIC-2 (UNIT-001; est S; blocked by EN-5)
- [ ] FEAT-6 Bulk creation → EPIC-2 (UNIT-001; est M; blocked by EN-6, then EN-7)
- [ ] FEAT-7 Expiry in UI → EPIC-2 (UNIT-002; est S; blocked by EN-8)

### Story / Enabler / Test level
- [ ] US-011..US-019 created (INVEST), points assigned, linked to FEAT-5/6/7
- [ ] EN-5 (schema + lazy expiry), EN-6 (bulk DTOs+endpoint), EN-7 (shared RateLimiter+N-token),
      EN-8 (expiry picker) created and prioritized
- [ ] TEST-010..018 created and linked to their stories
- [ ] Dependencies mapped: EN-5 → US-011/012/013/014; EN-6 → US-015/016/017; EN-6 → EN-7 → US-018;
      EN-8 → US-019
- [ ] Every Phase 2 item traces to REQ-/US-/ADR-/UNIT- (see project-plan.md §10.6)

### Phase 2 traceability
| Issue | REQ | US | ADR | UNIT |
|-------|-----|----|-----|------|
| EPIC-2 | 011–020 | 011–019 | 010–016 | UNIT-001/002 |
| FEAT-5 | 011–014 | 011–014 | 010,011,012,015 | UNIT-001 |
| FEAT-6 | 015–019 | 015–018 | 013,014 | UNIT-001 |
| FEAT-7 | 020 | 019 | 016 | UNIT-002 |
| EN-5..8 | per enabler | — | 010–016 | UNIT-001/002 |
| TEST-010..018 | 011–020 | 011–019 | 010–016 | UNIT-001/002 |
