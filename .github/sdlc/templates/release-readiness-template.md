<!-- template_id: release-readiness-template.md -->
<!-- v2 §4 mandatory template. Final lifecycle check; defers deploy to human decision (S7). -->

# Release Readiness — <feature/change name>

- **template_id**: release-readiness-template.md
- **run_id**: <run-id>
- **node_id**: release-readiness
- **status**: draft | approved

## 1. Readiness items (required)
Each item carries a `REL-` id.

| REL id | Item | Status | Evidence |
|--------|------|--------|----------|
| REL-001 | | ready / blocked | path |

## 2. Traceability coverage (required — §9)
Release readiness cannot pass if any approved requirement lacks linked design, implementation,
test, documentation, and review evidence.

| REQ | ADR | UNIT | PLAN | TEST | DOC | CR | Covered? |
|-----|-----|------|------|------|-----|----|----------|
| REQ-001 | ADR-... | UNIT-... | PLAN-... | TEST-... | DOC-... | CR-... | yes/no |

## 3. Risk & rollback (required)
- Unresolved risks:
- Rollback / compensating actions:
- Operational notes:

## 4. Evidence summary (required)
- Test evidence:
- Review evidence:
- Security evidence (SEC-...):

## 5. Metrics summary (required — §12)
- handoff_count, handoff_latency, blocked_gate_count, policy_error_count, retry_count,
  unresolved_risk_count, review_finding_count (by severity), traceability_coverage %.

## 6. Verdict (required)
- **Recommendation**: ready | not-ready
- **Human decision** (Operations placeholder — no automated deploy): pending | approved | rejected
