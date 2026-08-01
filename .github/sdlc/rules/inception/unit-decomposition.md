# Inception Rule — Unit Decomposition

- **rule_id**: INC-UNIT-DECOMPOSITION
- **node**: unit-decomposition
- **templates**: unit-decomposition-template.md, work-package-template.md
- **standards**: (none mandatory; compliance gate applies)

## Objective
Decompose the approved design into independently buildable units and produce the registry at
`sdlc-docs/construction/units/unit-registry.yaml` (§3).

## Procedure
1. Split the design into units; assign each a `UNIT-` id.
2. For each unit record: name, `depends_on_units`, requirements, design_refs, `target_files`,
   `expected_tests`, documentation_refs, `risk_level`, `high_impact`, `status`.
3. Ensure the unit dependency graph is acyclic.
4. Plan parallelization: units sharing `target_files` must NOT run concurrently (conflict rule).
5. Author a work package per unit from `work-package-template.md`.

## Exit gate
- Human approval + compliance policy check.
- The registry is the durable output; downstream construction reads it to schedule units.
- No unit implementation may start until its unit record exists and dependencies are `passed`.
