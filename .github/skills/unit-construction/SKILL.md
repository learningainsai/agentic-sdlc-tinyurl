# Unit Construction

Drive decomposed unit-of-work construction (v2 §3). Use during the construction phase to schedule
and execute units from the registry with safe parallelism.

## Inputs
- `sdlc-docs/construction/units/unit-registry.yaml` (schema: `.github/sdlc/schemas/unit-registry.schema.md`).
- The graph's `construction:` block (unit_loop, conflict_rule).

## Scheduling
1. A unit is **ready** when its record exists and all `depends_on_units` are `passed`.
2. Independent units run **concurrently** only when their `target_files` are disjoint.
3. If two ready units share a `target_file` → do not auto-serialize silently; apply the conflict rule
   (`block` → human decision).

## Per-unit ordered loop (execute in order; skip steps marked "when required")
1. functional design (when required)
2. NFR requirements (when required)
3. NFR design (when NFR requirements present)
4. infrastructure design (when infrastructure changes present)
5. code-generation plan (`PLAN-`)
6. implementation
7. unit-level tests (`TEST-`)
8. unit documentation (`DOC-`)
9. unit code review (`CR-`)
10. unit handoff (via `subagent-handoff`)

## High-impact units
If `high_impact: true`, obtain a durable approval record (`approved`) before step 6 (§8).

## Join safety
Block the join if: unit outputs conflict, required tests are missing, or documentation/review
artifacts reference stale traceability IDs (§3). Surface a human decision.
