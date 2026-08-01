<!-- template_id: security-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by security-standard (§6); part of release readiness. -->

# Security Review — <scope: run or UNIT-nnn>

- **template_id**: security-review-template.md
- **run_id**: <run-id>
- **status**: draft | approved

## 1. Findings (required)
Each finding carries a `SEC-` id.

| SEC id | Area | Severity | Finding | Status | Evidence |
|--------|------|----------|---------|--------|----------|
| SEC-001 | input-validation / secrets / authn-authz / dependency-risk / auditability | low/med/high/critical | | open / resolved / accepted | policy-evidence path |

## 2. Coverage checklist (required)
- [ ] Input validation
- [ ] Secret handling
- [ ] Authn/Authz assumptions
- [ ] Dependency risk
- [ ] Auditability

## 3. Traceability (required)
- SEC-... trace to REQ-.../ADR-.../UNIT-...

## 4. Unresolved risks (required)
<Non-empty high/critical unresolved findings block the gate (§7). Mark N/A with rationale if none.>
