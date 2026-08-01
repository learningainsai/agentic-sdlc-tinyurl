# Schema — Handoff Artifact (v2 §2)

- **applies to**: `sdlc-docs/handoffs/<run-id>/<node-id>.yaml`
- **template**: `.github/sdlc/templates/handoff-template.yaml`
- **validator**: `.github/hooks/scripts/handoff-validate.sh`

## Required keys
| Key | Type | Constraint |
|-----|------|-----------|
| run_id | string | non-empty |
| node_id | string | non-empty, matches a graph node |
| phase | string | `inception` \| `construction` |
| from_agent | string | non-empty |
| to_agents | list | may be empty |
| status | string | `pending`\|`in_progress`\|`passed`\|`failed`\|`blocked`\|`safe_stopped` |
| input_artifacts | list | |
| output_artifacts | list | non-empty when `status: passed` |
| template_ids | list | |
| standards_applied | list | |
| traceability_ids | list | valid prefixes (§9) |
| decisions_made | list | |
| approvals | list | approval record paths (§8) |
| policy_evidence | list | non-empty when `status: passed` |
| validation_evidence | list | |
| open_questions | list | non-empty => block unless human decision recorded |
| risks | list | entries `{id, severity, description, status}` |
| compensating_actions | list | |
| next_ready_nodes | list | |
| context_summary | string | non-empty when `status: passed` |

## Blocking conditions
- Missing or malformed file => downstream blocked.
- `status: passed` without `output_artifacts` + `policy_evidence` + `context_summary` => invalid.
- Non-empty `open_questions` or unresolved high-severity `risks` => block unless a human decision is
  recorded in the audit log.
