# Construction Rule — Implementation

- **rule_id**: CON-IMPLEMENTATION
- **loop steps**: 5–6 (code-generation plan, implementation)
- **templates**: implementation-plan-template.md
- **standards**: coding-standard, security-standard

## Objective
Produce the code-generation plan (`PLAN-`) and implement the unit.

## Procedure
1. Confirm the unit's `depends_on_units` are all `passed` and `target_files` do not conflict with
   any concurrently running unit (§3 conflict rule).
2. **High-impact (§8)**: if building a core feature / critical design, obtain a durable approval
   record with `decision: approved` before writing code.
3. Author the implementation plan; keep `target_files` consistent with the unit registry.
4. Implement, applying coding-standard and security-standard. Application code lives OUTSIDE
   `sdlc-docs/` (§11); only lifecycle artifacts go under `sdlc-docs/`.
5. Register compensating actions for any non-reversible side-effect (spec S4).

## Concurrency
- Independent units (satisfied deps, no `target_files` overlap) may run concurrently.
- A `target_files` overlap forces serialization or a human decision (conflict rule).

## Handoff
- Emit the unit handoff (loop step 10) with output artifacts, PLAN-/UNIT- traceability,
  security policy evidence, and context summary.
