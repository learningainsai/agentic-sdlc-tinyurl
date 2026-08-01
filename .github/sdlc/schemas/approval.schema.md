# Schema — High-Impact Approval Record (v2 §8)

- **applies to**: `sdlc-docs/approvals/<run-id>/<node-id>/<action-id>.yaml`
- **validator**: `.github/hooks/scripts/approval-check.sh`

Durable, file-backed record of a high-impact human decision. An environment variable alone is
NOT sufficient — the record must exist on disk with `decision: approved` before the action runs.

## Required keys
```yaml
run_id: ""
node_id: ""
action_id: ""
classification: core_feature | critical_design
agent_rationale: ""
approved_by: user
decision: approved | rejected | changes_requested
timestamp: ""                # ISO-8601 UTC
raw_user_response: ""        # verbatim user text (never summarized)
```

## Rules
- A high-impact action cannot execute unless a record exists with `decision: approved`.
- The audit log entry for the action must reference the approval record path.
- `rejected` or `changes_requested` blocks the node until a revised `approved` record is written.
