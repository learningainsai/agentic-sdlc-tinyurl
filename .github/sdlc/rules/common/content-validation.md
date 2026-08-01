# Common Rule — Content Validation

- **rule_id**: COMMON-CONTENT-VALIDATION
- **load**: always (v2 §5)

Validation gates the transition of any artifact from draft to accepted.

## Template compliance (§4)
- Every artifact must declare its `template_id`.
- Every **required** section must be present.
- An empty required section fails compliance **unless** explicitly marked `N/A` with a rationale.

## Handoff validity (§2)
- Handoff YAML must parse and match the schema in `.github/sdlc/schemas/handoff.schema.md`.
- `status: passed` requires non-empty `output_artifacts`, `policy_evidence`, and `context_summary`.
- Non-empty `open_questions` or unresolved high-severity `risks` block unless a human decision is recorded.

## Traceability validity (§9)
- IDs use the correct prefix (`REQ-`, `ADR-`, `UNIT-`, `PLAN-`, `TEST-`, `DOC-`, `SEC-`, `CR-`, `REL-`).
- References must resolve to existing IDs; stale/missing references block the compliance gate.

## Standards compliance (§6)
- Applicable mandatory standards must have no unresolved non-compliance findings.
- Gate evidence must reference **rule IDs**, not only free-text summaries.
