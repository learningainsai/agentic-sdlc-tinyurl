<!-- template_id: unit-decomposition-template.md -->
<!-- v2 §3/§4 mandatory template. Produces the unit registry. -->

# Unit Decomposition — <feature/change name>

- **template_id**: unit-decomposition-template.md
- **run_id**: <run-id>
- **node_id**: unit-decomposition
- **registry_output**: sdlc-docs/construction/units/unit-registry.yaml
- **status**: draft | approved

## 1. Decomposition rationale (required)
<How the design was split into independently buildable units.>

## 2. Units (required)
Each unit carries a `UNIT-` id and maps to the registry schema (§3).

| UNIT id | Name | Depends on | Target files | Requirements | Risk | High-impact |
|---------|------|-----------|--------------|--------------|------|-------------|
| UNIT-001 | | | | REQ-... | low/med/high | true/false |

## 3. Parallelization plan (required)
<Which units may run concurrently. Units sharing target_files must NOT run in parallel (§3 conflict_rule).>

## 4. Dependency graph (required)
<Unit dependency edges; must be acyclic.>

## 5. Traceability (required)
- Each UNIT traces to REQ-... and ADR-...

## 6. Open questions (required)
<Mark N/A with rationale if none.>
