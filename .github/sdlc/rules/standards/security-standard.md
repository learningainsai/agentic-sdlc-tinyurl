# Standard — Security

- **standard_id**: security-standard
- **enforcement**: always (v2 §6)
- **required coverage**: input validation, secret handling, authn/authz assumptions, dependency risk, auditability

Aligns with OWASP Top 10 concerns. A node cannot pass with an unresolved medium+ finding.
Findings are recorded as `SEC-` items with evidence under `sdlc-docs/policy-evidence/<run>/<node>/`.

| Rule ID | Requirement | Verification check |
|---------|-------------|--------------------|
| SEC-INPUT-01 | All external input is validated/sanitized (injection, path traversal, deserialization). | Review + tests for boundary inputs; SEC- finding if gaps. |
| SEC-SECRET-01 | No secrets in code, logs, or artifacts; secrets sourced from a manager. | Secret scan clean; review confirms no hardcoded credentials. |
| SEC-AUTHZ-01 | Authn/authz assumptions are explicit and enforced at trust boundaries. | Design states trust boundaries; checks present on protected paths. |
| SEC-DEP-01 | Dependencies screened for known vulnerabilities and excessive privilege. | Dependency/vuln scan reviewed; risky deps justified or replaced. |
| SEC-AUDIT-01 | Security-relevant actions are auditable. | Audit markers present for sensitive operations. |

## Non-compliance handling
- Open high/critical SEC- findings force a gate `block` (§7).
- Security review is an explicit lifecycle check before final completion (release readiness).
