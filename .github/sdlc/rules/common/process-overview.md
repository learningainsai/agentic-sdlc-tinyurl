# Common Rule — Process Overview

- **rule_id**: COMMON-PROCESS-OVERVIEW
- **load**: always (workflow start, v2 §5)

The orchestrator drives an explicit DAG (`.github/sdlc/workflow-graph.yaml`) across two phases:

- **Inception** — single-threaded, human-gated: `requirements` → `architecture-design` → `unit-decomposition` → `plan`. The `plan` node produces the MANDATORY breakdown deliverables (`breakdown-plan` skill) under `sdlc-docs/inception/plan/`.
- **Construction** — multi-agent, concurrent autonomy by independent unit: `implementation` → (`testing` ∥ `documentation`) → `release-readiness`.

## Invariants
0. **New-work intake (front door)**: any new idea/feature/change enters at `requirements` and traverses the full inception phase before any construction edit. Enforced by `intake-gate.sh` + `common/new-work-intake.md`; product-source edits are blocked while an intake is `construction_unblocked: false`.
1. No node runs until every `depends_on` node reached a `passed` exit gate.
2. Every node produces a durable **handoff** artifact before downstream consumption (§2).
3. Every artifact declares the **template** it used (§4) and the **standards** enforced (§6).
4. High-impact actions require a **durable approval record** (§8) before executing.
5. Policy gates are **hybrid**: automated evidence + human judgment; outcomes are `pass|fail|block|error` (§7).
6. All requirements/design/units/tests/docs/reviews carry **traceability IDs** (§9).
7. Audit is **append-only**; state is resumable (spec S8).

## Loaded artifacts each session
- Graph, `sdlc-docs/state.md`, `sdlc-docs/audit-log.md`, always-load common rules, all standards.
- The orchestrator records which rule files and standards were loaded for the run (§5).
