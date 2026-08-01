# SDLC State

Single source of truth for orchestration progress. The orchestrator reads this to **resume** a run.
Edit-in-place is allowed here; the audit log (`audit-log.md`) is append-only.

## Run
- **Run ID**: _not started_
- **Phase**: _n/a_
- **Last approved stage**: _none_
- **Active nodes**: _none_

## Loaded rules & standards (v2 §5)
- **Common rules loaded**: _none_
- **Standards enforced**: _none_
- **Extensions enabled**: _none_

## Node status
| Node | Phase | Status | Approved | Handoff | Policy evidence |
|------|-------|--------|----------|---------|-----------------|
| requirements | inception | pending | no | — | — |
| architecture-design | inception | pending | no | — | — |
| unit-decomposition | inception | pending | no | — | — |
| implementation | construction | pending | no | — | — |
| testing | construction | pending | no | — | — |
| documentation | construction | pending | no | — | — |
| release-readiness | construction | pending | no | — | — |

Status values: `pending | in-progress | passed | failed | safe-stopped`.

## Unit status (v2 §3)
Registry: `sdlc-docs/construction/units/unit-registry.yaml`

| Unit | Depends on | Status | High-impact | Approved |
|------|-----------|--------|-------------|----------|
| _none yet_ | | | | |

## Metrics (v2 §12)
- handoff_count: 0
- blocked_gate_count: 0
- policy_error_count: 0
- retry_count: 0
- unresolved_risk_count: 0
- review_finding_count: 0
- traceability_coverage: n/a
