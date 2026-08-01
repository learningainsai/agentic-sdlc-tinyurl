# Policy Gate (Hybrid)

Evaluate a node's hybrid policy gate (v2 §7). Automated scripts gather objective evidence; the
agent/human resolves judgment findings. Use at every entry/exit gate with a `policy` list.

## Outcomes
`pass | fail | block | error`
- **pass** — all automated checks passed; no applicable review findings remain.
- **fail** — an automated check or a mandatory standard verification failed.
- **block** — human approval, missing evidence, or unresolved risk prevents progression.
- **error** — the checker failed/timed out/produced malformed output. Treated as **block** unless a
  human explicitly authorizes retry, fallback, or safe-stop (failure_policy.policy_error).

## Steps
1. Run `.github/hooks/scripts/gate-check.sh <node-id> <entry|exit>`.
   - Evaluate only the policies declared for the current node and gate.
   - Evidence is written to `sdlc-docs/policy-evidence/<run-id>/<node-id>/`.
2. Map the exit code: 0→pass, 3→fail, 2→block, 4→error (treat as block).
3. Resolve judgment findings against the applicable standards; reference **rule IDs** in the evidence,
   not only free text (§6).
4. Record human review decisions in **both** the handoff artifact and the append-only audit log (§7).

## Rules
- Never treat silence as approval; never bypass a check to keep moving.
- A single failing applicable policy or standard verification is blocking.
- Emit `blocked_gate_count` and `policy_error_count` audit markers (§12).
