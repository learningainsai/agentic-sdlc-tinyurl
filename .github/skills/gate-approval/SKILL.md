# Gate Approval

Evaluate a node's entry or exit gate. A gate = **human approval** and/or **automated policy check**.
Use whenever a node is about to start (entry) or finish (exit), or before any high-impact action.

> v2: gates are **hybrid** (§7). See the `policy-gate` skill for evidence-gathering and the
> `pass | fail | block | error` outcome model, and `template-compliance` for durable high-impact
> approval records (§8). This skill remains the entry point; it now delegates the policy step to the
> hybrid model and requires a durable approval record for high-impact actions.

## Gate contract
A gate resolves to one of: `pass | fail | block | error` (v2 §7).
- `pass` → progression allowed.
- `fail` → a policy check or mandatory standard verification failed; halt and report which.
- `block` → human approval / durable approval record / evidence / risk resolution missing; halt and request it.
- `error` → the checker malfunctioned; treat as `block` unless a human authorizes retry/fallback/safe-stop.

## Steps
1. Read the node's `entry_gate` / `exit_gate` from `.github/sdlc/workflow-graph.yaml`.
2. **Policy checks**: run each listed check (`security`, `compliance`, `change-control`) via
   `.github/hooks/scripts/gate-check.sh <node-id> <entry|exit>`. Any failure → gate `fail`.
3. **Human approval**: if `approval: required`, present a concise summary and request an explicit
   decision. No recorded decision → gate `block`.
4. **High-impact rule (S3 + v2 §8)**: if the node is `high_impact: true` OR the action builds a core
   feature or makes a critical design decision, first **state your classification**, get **explicit
   user confirmation**, and write a **durable approval record** at
   `sdlc-docs/approvals/<run-id>/<node-id>/<action-id>.yaml` with `decision: approved`. Verify via
   `.github/hooks/scripts/approval-check.sh` — an env flag alone is insufficient.
5. **Standards (v2 §6)**: verify no unresolved non-compliance finding for an applicable standard.
   Gate evidence must reference **rule IDs**, not only free text.
6. Append the gate result (pass/fail/block/error + reasons + any human decision) to
   `sdlc-docs/audit-log.md`, and store automated evidence under
   `sdlc-docs/policy-evidence/<run-id>/<node-id>/`.

## Rules
- Never treat silence as approval.
- Never bypass a policy check to "keep moving".
- A single failing applicable policy is a blocking finding.
