# SDLC Orchestrator Hooks

Lifecycle hooks that enforce the spec at runtime. Wired via [`hooks.json`](./hooks.json).

| Event | Hook | Purpose |
|-------|------|---------|
| `sessionStart` | `audit-append.sh` | Log session start (append-only). |
| `sessionStart` | `graph-validate.sh` | Validate v2 graph fields + referenced templates/standards (§10). |
| `preToolUse` | `gate-check.sh` | Block gated/high-impact actions until hybrid policy passes + durable approval exists. |
| `postToolUse` | `audit-append.sh` | Append every tool action to the audit log. |
| `stop` | `audit-append.sh` | Log session end. |

## Scripts
- `scripts/audit-append.sh` — atomic, lock-serialized **append-only** writer for `sdlc-docs/audit-log.md`.
- `scripts/gate-check.sh` — **hybrid** policy gate + high-impact durable-approval enforcement (v2 §7/§8).
  Exit codes: `0` pass, `2` block, `3` fail, `4` error (caller treats as block).
- `scripts/graph-validate.sh` — validate v2 graph fields + template/standard references (v2 §10).
  Exit codes: `0` valid, `3` invalid.
- `scripts/handoff-validate.sh` — validate a handoff artifact against the schema (v2 §2).
  Exit codes: `0` valid, `2` block, `3` invalid.
- `scripts/approval-check.sh` — verify a durable high-impact approval record (v2 §8).
  Exit codes: `0` approved, `2` block.

## Environment contract
- `COPILOT_SDLC_RUN` — current run id (used for evidence/approval/handoff paths).
- `COPILOT_SDLC_NODE` — current graph node id (set by the orchestrator).
- `COPILOT_SDLC_ACTION` — current high-impact action id (for the approval record path).
- `COPILOT_SDLC_HIGH_IMPACT_CONFIRMED=true` — set only after explicit user confirmation; a durable
  approval record is still required (v2 §8).
- `COPILOT_SDLC_APPROVAL_RECORDED=true` — set only after a required human gate approval is recorded.
- `COPILOT_TOOL_NAME` — populated by the hook runtime for `postToolUse`.

## Notes
- Policy checks in `gate-check.sh` are v1 placeholders (always pass). Wire real security/compliance/
  change-control scanners into `check_security`, `check_compliance`, `check_change_control`.
- Make scripts executable: `chmod +x .github/hooks/scripts/*.sh`.
