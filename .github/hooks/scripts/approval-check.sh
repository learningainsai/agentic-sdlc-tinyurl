#!/usr/bin/env bash
# approval-check.sh — Verify a durable high-impact approval record (v2 §8).
# Schema: .github/sdlc/schemas/approval.schema.md
#
# A high-impact action may execute ONLY if an approval record exists with `decision: approved`.
#
# Exit codes:
#   0  approved   (action may proceed)
#   2  block      (record missing, or decision rejected/changes_requested/absent)
#
# Usage: approval-check.sh <run-id> <node-id> <action-id>
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"

RUN_ID="${1:-${COPILOT_SDLC_RUN:-}}"
NODE_ID="${2:-${COPILOT_SDLC_NODE:-}}"
ACTION_ID="${3:-${COPILOT_SDLC_ACTION:-}}"

log() { "$AUDIT" "approval:${NODE_ID}" "high-impact-approval" "$1" "${2:-n/a}" "approval-check" >/dev/null 2>&1 || true; }

if [ -z "$RUN_ID" ] || [ -z "$NODE_ID" ] || [ -z "$ACTION_ID" ]; then
  log "missing run/node/action id for approval lookup" "block"
  echo "APPROVAL BLOCK: run-id, node-id, and action-id are required." >&2
  exit 2
fi

REC="${REPO_ROOT}/sdlc-docs/approvals/${RUN_ID}/${NODE_ID}/${ACTION_ID}.yaml"
if [ ! -f "$REC" ]; then
  log "approval record missing: ${REC}" "block"
  echo "APPROVAL BLOCK: no durable approval record at ${REC}" >&2
  exit 2
fi

DECISION="$(awk -F': *' '/^decision:/{print $2; exit}' "$REC" | tr -d '"' | tr -d "'" | xargs 2>/dev/null || true)"
if [ "$DECISION" != "approved" ]; then
  log "approval decision=${DECISION:-none} at ${REC}" "block"
  echo "APPROVAL BLOCK: decision='${DECISION:-none}' at ${REC} (need 'approved')." >&2
  exit 2
fi

log "approved via ${REC}" "approved"
echo "APPROVAL OK: ${REC}"
exit 0
