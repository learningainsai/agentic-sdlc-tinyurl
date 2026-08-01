<!-- template_id: work-package-template.md -->
<!-- v2 §4 mandatory template. One work package per unit of work. -->

# Work Package — UNIT-<nnn>

- **template_id**: work-package-template.md
- **unit_id**: UNIT-<nnn>
- **run_id**: <run-id>
- **owner_agent**: construction
- **status**: pending | in_progress | passed | failed | blocked

## 1. Objective (required)
<What this unit delivers.>

## 2. Inputs (required)
- Requirements: REQ-...
- Design refs: ADR-...
- Depends on units: UNIT-...

## 3. Target files (required)
<Files this unit will create/modify. Basis for the conflict rule (§3).>

## 4. Execution loop checklist (required)
Ordered per-unit loop (§3):

- [ ] functional design (if required)
- [ ] NFR requirements (if required)
- [ ] NFR design (if NFR requirements present)
- [ ] infrastructure design (if infra changes present)
- [ ] code generation plan (PLAN-...)
- [ ] implementation
- [ ] unit-level tests (TEST-...)
- [ ] unit documentation (DOC-...)
- [ ] unit code review (CR-...)
- [ ] unit handoff

## 5. Expected tests (required)
- TEST-...

## 6. Traceability (required)
- UNIT-<nnn> ↔ REQ-... ↔ ADR-... ↔ PLAN-... ↔ TEST-... ↔ DOC-... ↔ CR-...
