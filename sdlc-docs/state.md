# SDLC State

Single source of truth for orchestration progress. The orchestrator reads this to **resume** a run.
Edit-in-place is allowed here; the audit log (`audit-log.md`) is append-only.

## Run
- **Run ID**: run-20260802T170000Z (Phase 3 — bulk creation DB-query optimization)
- **Phase**: inception (in progress)
- **Last approved stage**: release-readiness (GO — run releasable)
- **Active nodes**: none — run run-20260802T170000Z COMPLETE
- **Prior runs**: run-20260802T150051Z (Phase 2) completed at release-readiness; run-20260801T232309Z (Phase 1 MVP) completed.

## New-work intake (v2 §5 — front door)
Registry dir: `sdlc-docs/intake/` — enforced by `intake-gate.sh` (preToolUse) + `common/new-work-intake.md`.

| Intake | Classification | Phase | Construction unblocked | Notes |
|--------|----------------|-------|------------------------|-------|
| INTAKE-20260801T232309Z-original-scope | feature | construction | yes | Backfill for completed run |
| INTAKE-20260802T000000Z-url-expiry-and-deactivate | feature | inception | no | **Withdrawn** by human; discarded before requirements agreed |
| INTAKE-20260802T150051Z-expiry-and-bulk-creation | feature | completed | **yes** | **Completed** — release-readiness passed (human 'Go'); no automated deploy |
| INTAKE-20260802T170000Z-performance-improvement | enhancement | inception (requirements) | **no** | **Active intake** — "improve performance"; idea-refiner run, awaiting human answers to scope/target before requirements.md |

> A pending (non-withdrawn) intake (`construction_unblocked: false`) hard-blocks all product-source
> edits until it clears the `plan` gate. Withdrawn/superseded intakes are ignored by the gates.
> New scope does not enter the existing run without a human re-plan decision (S5).
> Run `run-20260802T150051Z` is complete: construction, testing, documentation, and release-readiness all passed.

## Loaded rules & standards (v2 §5)
- **Common rules loaded**: process-overview, session-continuity, content-validation, question-format-guide
- **Standards enforced**: coding-standard, security-standard, testing-standard, documentation-standard, code-review-standard, release-readiness-standard
- **Extensions enabled**: none

## Node status (run-20260802T170000Z — Phase 3)
| Node | Phase | Status | Approved | Handoff | Policy evidence |
|------|-------|--------|----------|---------|-----------------|
| requirements | inception | passed | yes | handoffs/run-20260802T170000Z/requirements.yaml | policy-evidence/run-20260802T170000Z/requirements/ |
| architecture-design | inception | passed | yes (durable) | handoffs/run-20260802T170000Z/architecture-design.yaml | policy-evidence/run-20260802T170000Z/architecture-design/ |
| unit-decomposition | inception | passed | yes | handoffs/run-20260802T170000Z/unit-decomposition.yaml | policy-evidence/run-20260802T170000Z/unit-decomposition/ |
| plan | inception | passed | yes | handoffs/run-20260802T170000Z/plan.yaml | policy-evidence/run-20260802T170000Z/plan/ |
| implementation | construction | passed | yes (durable) | handoffs/run-20260802T170000Z/implementation.yaml | policy-evidence/run-20260802T170000Z/implementation/ |
| testing | construction | passed | no | handoffs/run-20260802T170000Z/testing.yaml | policy-evidence/run-20260802T170000Z/testing/ |
| code-review | construction | passed | no | handoffs/run-20260802T170000Z/code-review.yaml | policy-evidence/run-20260802T170000Z/code-review/ |
| documentation | construction | passed | no | handoffs/run-20260802T170000Z/documentation.yaml | policy-evidence/run-20260802T170000Z/documentation/ |
| release-readiness | construction | passed | yes (human GO) | handoffs/run-20260802T170000Z/release-readiness.yaml | policy-evidence/run-20260802T170000Z/release-readiness/ |
| plan | inception | pending | — | — | — |
| implementation | construction | pending | — | — | — |
| testing | construction | pending | — | — | — |
| documentation | construction | pending | — | — | — |
| release-readiness | construction | pending | — | — | — |

## Node status (run-20260802T150051Z — Phase 2)
| Node | Phase | Status | Approved | Handoff | Policy evidence |
|------|-------|--------|----------|---------|-----------------|
| requirements | inception | passed | yes | handoffs/run-20260802T150051Z/requirements.yaml | policy-evidence/run-20260802T150051Z/requirements/ |
| architecture-design | inception | passed | yes (durable) | handoffs/run-20260802T150051Z/architecture-design.yaml | policy-evidence/run-20260802T150051Z/architecture-design/ |
| unit-decomposition | inception | passed | yes | handoffs/run-20260802T150051Z/unit-decomposition.yaml | policy-evidence/run-20260802T150051Z/unit-decomposition/ |
| plan | inception | passed | yes | handoffs/run-20260802T150051Z/plan.yaml | policy-evidence/run-20260802T150051Z/plan/ |
| implementation | construction | passed | yes (durable) | handoffs/run-20260802T150051Z/implementation.yaml | policy-evidence/run-20260802T150051Z/implementation/ |
| testing | construction | passed | — | handoffs/run-20260802T150051Z/testing.yaml | policy-evidence/run-20260802T150051Z/testing/ |
| documentation | construction | passed | — | handoffs/run-20260802T150051Z/documentation.yaml | policy-evidence/run-20260802T150051Z/documentation/ |
| release-readiness | construction | passed | yes (`Go`) | handoffs/run-20260802T150051Z/release-readiness.yaml | policy-evidence/run-20260802T150051Z/release-readiness/ |

### Prior run (run-20260801T232309Z — Phase 1 MVP) — for reference
| Node | Status | Handoff |
|------|--------|---------|
| requirements → plan | passed | handoffs/run-20260801T232309Z/*.yaml |
| implementation, testing, documentation | passed | handoffs/run-20260801T232309Z/*.yaml |
| release-readiness | in-progress (entry approved) | — |

Status values: `pending | in-progress | passed | failed | safe-stopped`.

## Unit status (v2 §3)
Registry: `sdlc-docs/construction/units/unit-registry.yaml`

| Unit | Depends on | Status | High-impact | Approved |
|------|-----------|--------|-------------|----------|
| UNIT-001 Backend API (Phase 2) | — | passed | yes | yes (durable, 'confirm') |
| UNIT-002 Frontend UI (Phase 2) | — | passed | no | yes (under impl node) |

## Metrics (v2 §12)
- handoff_count: 12
- blocked_gate_count: 0
- policy_error_count: 0
- retry_count: 0
- unresolved_risk_count: 0
- review_finding_count: 0
- traceability_coverage: n/a
