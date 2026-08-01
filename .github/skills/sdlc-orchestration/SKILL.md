# SDLC Orchestration

Resolve and drive the SDLC dependency graph. Use when coordinating stages across the lifecycle,
deciding what runs next, branching, or synchronizing parallel work.

## Inputs
- `.github/sdlc/workflow-graph.yaml` — the DAG, gates, branches, failure policy.
- `sdlc-docs/state.md` — persisted node states + `last_approved_stage`.

## Graph resolution
1. Parse nodes; build the dependency map from `depends_on`.
2. A node is **ready** when every `depends_on` node has exit gate `passed`.
3. **Inception** nodes execute one at a time (single-threaded, human-gated).
4. **Construction** nodes with `parallel: true` and a satisfied dependency set run concurrently.

## Conditional branching
- After a node's exit gate passes, evaluate its `branch` entries in order.
- Take the first `when` predicate that holds and continue at its `goto` node.
- If no `branch` is defined, follow the natural `depends_on` edges.

## Synchronization (joins)
- A join node waits for **all** upstream branches to reach `passed`.
- If branches **conflict** or a branch **partially fails**, do NOT auto-merge or auto-cancel.
  **Wait and request a human decision** (see failure-recovery + spec S6).

## Re-planning
- If an upstream output changes after downstream work started, **pause** and request a human
  decision. Never re-plan autonomously (spec S5).

## State updates
- On every transition, update `sdlc-docs/state.md` (node status + `last_approved_stage`) and append
  to `sdlc-docs/audit-log.md` via the `audit-log` skill.

## Checks before advancing
1. Entry gate passed for the target node (hybrid policy, v2 §7).
2. All `depends_on` nodes are `passed`.
3. Upstream **handoff** artifacts are valid (v2 §2; `subagent-handoff` skill).
4. High-impact **durable approval record** exists with `decision: approved` if `high_impact: true` (v2 §8).
5. Required **templates** declared and required sections present (v2 §4; `template-compliance` skill).
6. Applicable **standards** have no unresolved non-compliance findings (v2 §6).
7. Audit entry written.

## v2 additions
- Load always-load **common rules** and all **standards** at workflow start; record which loaded (§5).
- Construction runs by **decomposed unit** from the unit registry; schedule with `unit-construction` (§3).
- Maintain **traceability IDs** across artifacts; block on stale/missing references (§9; `traceability` skill).
- Emit v2 **metrics** as audit markers (handoff count/latency, blocked gates, policy errors,
  retry count, unresolved risks, review findings by severity, traceability coverage) (§12).
