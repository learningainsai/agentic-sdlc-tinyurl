# Schema — Unit Registry (v2 §3)

- **applies to**: `sdlc-docs/construction/units/unit-registry.yaml`
- **produced by**: `unit-decomposition` node
- **consumed by**: `implementation` node (parallel unit scheduling)

## Top-level
```yaml
run_id: ""
generated_by: unit-decomposition
units: []      # list of unit records
```

## Unit record — required keys
| Key | Type | Constraint |
|-----|------|-----------|
| unit_id | string | `UNIT-` prefix, unique |
| name | string | non-empty |
| owner_agent | string | `construction` |
| depends_on_units | list | UNIT- ids; must be acyclic |
| requirements | list | REQ- ids |
| design_refs | list | ADR- ids |
| target_files | list | conflict basis — no two concurrent units may overlap |
| expected_tests | list | TEST- ids |
| documentation_refs | list | DOC- ids |
| risk_level | string | `low`\|`medium`\|`high` |
| high_impact | bool | `true`\|`false` (durable approval required if true, §8) |
| status | string | `pending`\|`in_progress`\|`passed`\|`failed`\|`blocked` |

## Scheduling rules
- A unit starts only when its record exists and all `depends_on_units` are `passed`.
- Concurrent units must have disjoint `target_files` (else block → human decision).
- A join blocks if unit outputs conflict, required tests are missing, or documentation/review
  artifacts reference stale traceability IDs.
