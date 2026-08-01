# Common Rule — Session Continuity

- **rule_id**: COMMON-SESSION-CONTINUITY
- **load**: always (v2 §5)

## Resume, never restart
1. On session start, read `sdlc-docs/state.md` to find the run id, phase, last approved stage, and node statuses.
2. If a run is in progress, **resume** from the ready set — do not re-run passed nodes.
3. Reconstruct cross-node context from the latest **handoff** artifacts (§2), not from memory.

## Durable context sources (in priority order)
1. `sdlc-docs/state.md` — authoritative node status + `last_approved_stage`.
2. `sdlc-docs/handoffs/<run-id>/<node-id>.yaml` — inter-node context and `context_summary`.
3. `sdlc-docs/approvals/<run-id>/...` — durable high-impact decisions (§8).
4. `sdlc-docs/audit-log.md` — append-only lineage of every decision.

## Rules
- Never treat an in-memory summary as the source of truth; re-read the durable artifacts.
- A missing or malformed upstream handoff blocks the downstream node until repaired.
- Record extension-rule enablement decisions in state and audit (§5).
