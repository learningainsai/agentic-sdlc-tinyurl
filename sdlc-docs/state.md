# SDLC State

Single source of truth for orchestration progress. The orchestrator reads this to **resume** a run.
Edit-in-place is allowed here; the audit log (`audit-log.md`) is append-only.

## Run
- **Run ID**: run-20260801T232309Z
- **Phase**: construction
- **Last approved stage**: documentation
- **Active nodes**: release-readiness (entry approved; awaiting human go/no-go at exit gate)

## Loaded rules & standards (v2 §5)
- **Common rules loaded**: process-overview, session-continuity, content-validation, question-format-guide
- **Standards enforced**: coding-standard, security-standard, testing-standard, documentation-standard, code-review-standard, release-readiness-standard
- **Extensions enabled**: none

## Node status
| Node | Phase | Status | Approved | Handoff | Policy evidence |
|------|-------|--------|----------|---------|-----------------|
| requirements | inception | passed | yes | handoffs/run-20260801T232309Z/requirements.yaml | policy-evidence/run-20260801T232309Z/requirements/ |
| architecture-design | inception | passed | yes | handoffs/run-20260801T232309Z/architecture-design.yaml | policy-evidence/run-20260801T232309Z/architecture-design/ |
| unit-decomposition | inception | passed | yes | handoffs/run-20260801T232309Z/unit-decomposition.yaml | policy-evidence/run-20260801T232309Z/unit-decomposition/ |
| plan | inception | passed | yes | handoffs/run-20260801T232309Z/plan.yaml | policy-evidence/run-20260801T232309Z/plan/ |
| implementation | construction | passed | yes | handoffs/run-20260801T232309Z/implementation.yaml | policy-evidence/run-20260801T232309Z/implementation/ |
| testing | construction | passed | yes | handoffs/run-20260801T232309Z/testing.yaml | policy-evidence/run-20260801T232309Z/testing/ |
| documentation | construction | passed | yes | handoffs/run-20260801T232309Z/documentation.yaml | policy-evidence/run-20260801T232309Z/documentation/ |
| release-readiness | construction | in-progress | entry | — | policy-evidence/run-20260801T232309Z/release-readiness/ |

Status values: `pending | in-progress | passed | failed | safe-stopped`.

## Unit status (v2 §3)
Registry: `sdlc-docs/construction/units/unit-registry.yaml`

| Unit | Depends on | Status | High-impact | Approved |
|------|-----------|--------|-------------|----------|
| UNIT-001 Backend API | — | passed | no | via implementation |
| UNIT-002 Frontend UI | — | passed | no | via implementation |

## Metrics (v2 §12)
- handoff_count: 7
- blocked_gate_count: 0
- policy_error_count: 0
- retry_count: 0
- unresolved_risk_count: 0
- review_finding_count: 0
- traceability_coverage: n/a
