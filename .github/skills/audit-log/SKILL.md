# Audit Log

Persist state and decision lineage to **append-only** markdown. Use for every gate result, approval,
state transition, failure, and re-plan decision.

## Files
- `sdlc-docs/audit-log.md` — append-only audit trail (never overwrite).
- `sdlc-docs/state.md` — current node states + `last_approved_stage` (edit in place is allowed here).

## Append-only rule (spec S8, S11)
- **Only append** to `sdlc-docs/audit-log.md`. Never rewrite or truncate it.
- Prefer the helper: `.github/hooks/scripts/audit-append.sh "<stage>" "<event>" "<detail>"`.
  It appends one timestamped entry atomically, which also serializes concurrent multi-agent writes.
- If editing directly, only add new lines at the end.

## Entry format
```markdown
## <Stage / Interaction>
- **Timestamp**: <ISO-8601 UTC>
- **Event**: <gate-result | approval | state-change | failure | replan | confirmation>
- **Actor**: <agent id | user>
- **Detail**: <complete raw context — never summarized for user input>
- **Result**: <pass | fail | block | approved | rejected | recovered>
---
```

## What to always log
- Every user input verbatim (never paraphrase user decisions).
- Every gate evaluation and its result (`pass | fail | block | error`, v2 §7).
- Every high-impact classification + the user's confirmation/rejection, **and the durable approval
  record path** (`sdlc-docs/approvals/<run>/<node>/<action>.yaml`, v2 §8).
- Every handoff produced/consumed and its validation result (v2 §2).
- Every failure, retry attempt, and recovery choice.
- Every re-plan pause and the human decision.
- Extension-rule enablement decisions (v2 §5).
- Metric markers — v1 (success/retry/rollback/latency) and v2 (handoff count/latency, blocked gate
  count, policy error count, retry count, unresolved risk count, review finding count by severity,
  traceability coverage) — for later aggregation (v2 §12).
