# Common Rule — New-Work Intake Gate

- **rule_id**: COMMON-NEW-WORK-INTAKE-GATE
- **load**: always (workflow start, v2 §5)
- **enforced_by**: `.github/hooks/scripts/intake-gate.sh` (preToolUse), this rule, and the orchestrator core loop (step 0).

## Purpose
Close the gap where a new idea can skip inception and jump straight to construction. **Every** new
idea, feature, change request, or enhancement is *new work* and MUST enter the SDLC at the
`requirements` node and traverse the full inception phase before any product code is written.

## Intake trigger (what counts as "new work")
Treat a user message as new work whenever it asks to add, change, remove, extend, or fix product
behavior — e.g. "I want to…", "add…", "support…", "delete…", "also handle…", a new acceptance
criterion, or a new API/UI capability. Bug reports and enhancements are new work too.

Not new work: questions, explanations, status checks, and edits confined to governance artifacts
under `sdlc-docs/`, `.github/`, or `docs/`.

## Mandatory routing (no skipping)
On detecting new work, the orchestrator MUST, **before any construction tool use**:
1. **STOP** — do not edit, create, or generate any product source (backend/frontend/code) yet.
2. **Record an intake item** at `sdlc-docs/intake/<INTAKE-ID>.yaml` with `construction_unblocked: false`.
3. **Route to inception** starting at `requirements`, which MUST run the `idea-refiner` skill and
   produce `idea-refinement.md` **before** drafting `requirements.md` (discuss with the human until
   requirements are agreed — do not proceed on assumptions). `idea-refiner` is **auto-invoked on every
   inception trigger**, not discretionary: `inception-skill-gate.sh` blocks drafting of
   `requirements.md`/`user-stories.md` until the report exists, and `gate-check.sh` blocks the
   requirements exit gate without it.
4. Traverse `requirements → architecture-design → unit-decomposition → plan`, honoring every entry/exit
   gate and human approval.
5. Only after the `plan` node passes its exit gate may the orchestrator set the intake record's
   `construction_unblocked: true` and enter construction for that idea.

## Enforcement (hard stop)
- The `intake-gate` preToolUse hook **blocks** any mutating tool call targeting product source paths
  while an intake item is still in inception (`construction_unblocked: false`) or while no
  inception-authorized intake exists. Blocked outcome is recorded in the audit log.
- Re-planning an existing run because of new work is a **human decision** (S5) — never re-plan or
  fold new scope into an in-flight run autonomously; open a new run or branch only with confirmation.

## Intake record schema (`sdlc-docs/intake/<INTAKE-ID>.yaml`)
```yaml
intake_id: INTAKE-<UTC-timestamp>-<slug>
raw_request: "<verbatim user request>"
classification: feature | change | enhancement | bugfix
high_impact: true | false
run_id: <run-id this intake belongs to>
entry_node: requirements
current_phase: inception | construction
construction_unblocked: false        # flips true ONLY after the plan node passes for this run
created: <UTC timestamp>
notes: "<optional>"
```
