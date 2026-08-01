<!-- template_id: code-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by code-review-standard (§6). -->

# Code Review — <scope: run or UNIT-nnn>

- **template_id**: code-review-template.md
- **run_id**: <run-id>
- **reviewer**: construction | user
- **status**: draft | approved

## 1. Findings (required)
Each finding carries a `CR-` id.

| CR id | Category | Severity | Finding | Resolution |
|-------|----------|----------|---------|------------|
| CR-001 | correctness / security / maintainability / test-quality / business-logic-risk | low/med/high | | open / resolved |

## 2. Review checklist (required)
- [ ] Correctness
- [ ] Security
- [ ] Maintainability
- [ ] Test quality
- [ ] Business logic risk

## 3. Traceability (required)
- CR-... trace to UNIT-.../PLAN-.../REQ-...

## 4. Verdict (required)
- **Outcome**: approved | changes_requested | rejected
- Unresolved high-severity findings block progression.
