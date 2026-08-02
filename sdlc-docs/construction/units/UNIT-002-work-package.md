<!-- template_id: work-package-template.md -->

# Work Package — UNIT-002

- **template_id**: work-package-template.md
- **unit_id**: UNIT-002
- **run_id**: run-20260801T232309Z
- **owner_agent**: construction
- **status**: pending

## 1. Objective (required)

Deliver the Angular submission UI: a form to enter a URL and optional alias, calling the backend via
a `LinkService`, displaying the resulting short URL with copy-to-clipboard and inline error handling.

## 2. Inputs (required)

- Requirements: REQ-007
- Design refs: ADR-002
- Depends on units: — (none; builds against the fixed API contract)

## 3. Target files (required)

- `tiny-url-creator/frontend/**` (Angular project: `package.json`, `src/app/...`)

## 4. Execution loop checklist (required)

- [ ] functional design (if required)
- [ ] NFR requirements (N/A — UI covered by architecture NFRs)
- [ ] NFR design (N/A)
- [ ] infrastructure design (N/A)
- [ ] code generation plan (PLAN-002)
- [ ] implementation
- [ ] UI review with `angular-design-reviewer` skill (visual + Angular conventions + a11y)
- [ ] unit-level tests (TEST-008..)
- [ ] unit documentation (DOC-002)
- [ ] unit code review (CR-002)
- [ ] unit handoff

> **Recommended skill**: run `angular-design-reviewer` after implementation and before code review
> to audit the running UI (layout/a11y) and Angular conventions (standalone, signals, typed forms,
> `catchError`, subscription cleanup). Findings feed CR-002.

## 5. Expected tests (required)

- TEST-008 form submits URL and renders returned short link
- TEST-009 shows inline validation/collision error on API error

## 6. Traceability (required)

- UNIT-002 ↔ REQ-007 ↔ ADR-002 ↔ PLAN-002 ↔ TEST-008/009 ↔ DOC-002 ↔ CR-002.
