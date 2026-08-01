# Construction Rule — Code Review

- **rule_id**: CON-CODE-REVIEW
- **loop step**: 9 (unit code review)
- **template**: code-review-template.md
- **standard**: code-review-standard

## Objective
Review each unit for correctness, security, maintainability, test quality, and business-logic risk.

## Procedure
1. Review against functional design, coding-standard, security-standard, and tests.
2. Record findings as `CR-` ids with severity and resolution; trace to UNIT-/PLAN-/REQ-.
3. Issue a verdict: `approved | changes_requested | rejected`.
4. Unresolved high-severity CR- findings block release readiness.

## Note
- Code review and release readiness are explicit lifecycle checks before final completion (§6).
- Review artifacts live under `sdlc-docs/construction/code-review/`.
