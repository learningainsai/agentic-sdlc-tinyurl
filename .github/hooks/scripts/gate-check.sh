#!/usr/bin/env bash
# gate-check.sh — Hybrid policy gate + high-impact durable-approval enforcement.
# Realizes spec S2 (gates), S3 (high-impact trigger) and v2 §7 (hybrid policy gates) + §8 (durable approval).
#
# Hybrid model (v2 §7): automated checks gather objective EVIDENCE; agent/human review resolves
# judgment findings. Policy outcomes: pass | fail | block | error.
#   pass  -> all automated checks passed, no applicable review findings remain
#   fail  -> an automated check or mandatory standard verification failed
#   block -> human approval / missing evidence / unresolved risk prevents progression
#   error -> the checker itself failed/timed out/malformed; treated as BLOCK unless human authorizes
#
# Exit codes:
#   0  pass
#   2  block   (human confirmation/approval required, or `error` awaiting authorization)
#   3  fail    (a policy/standard check failed)
#   4  error   (checker malfunction; caller treats as block per failure_policy.policy_error)
#
# Usage: gate-check.sh [<node-id>] [<entry|exit>]
# Automated evidence is written under sdlc-docs/policy-evidence/<run-id>/<node-id>/.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
GRAPH="${REPO_ROOT}/.github/sdlc/workflow-graph.yaml"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"
APPROVAL_CHECK="${REPO_ROOT}/.github/hooks/scripts/approval-check.sh"

NODE_ID="${1:-${COPILOT_SDLC_NODE:-unknown}}"
GATE="${2:-entry}"
RUN_ID="${COPILOT_SDLC_RUN:-unknown-run}"

EVID_DIR="${REPO_ROOT}/sdlc-docs/policy-evidence/${RUN_ID}/${NODE_ID}"
mkdir -p "$EVID_DIR"
EVID="${EVID_DIR}/${GATE}-evidence.md"

log() { "$AUDIT" "gate:${NODE_ID}" "gate-${GATE}" "$1" "${2:-n/a}" "gate-check" >/dev/null 2>&1 || true; }
evid() { printf -- '- %s\n' "$1" >> "$EVID"; }

{
  printf '# Policy Evidence — %s / %s gate\n' "$NODE_ID" "$GATE"
  printf -- '- run_id: %s\n' "$RUN_ID"
  printf -- '- timestamp: %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  printf -- '- checks:\n'
} > "$EVID"

# --- Automated policy checks (v2 §7 objective evidence). Placeholders return 0; wire scanners here. ---
check_security()       { evid "security: passed (placeholder scanner)"; return 0; }
check_compliance()     { evid "compliance: passed (placeholder scanner)"; return 0; }
check_change_control() { evid "change-control: passed (placeholder scanner)"; return 0; }

FAILED=""
for policy in security compliance change-control; do
  case "$policy" in
    security)       check_security       || FAILED="$FAILED security" ;;
    compliance)     check_compliance     || FAILED="$FAILED compliance" ;;
    change-control) check_change_control || FAILED="$FAILED change-control" ;;
  esac
done

if [ -n "$FAILED" ]; then
  evid "outcome: fail (${FAILED})"
  log "policy check failed:${FAILED}; evidence:${EVID}" "fail"
  echo "GATE FAIL (${NODE_ID}/${GATE}): failed policy checks:${FAILED} (evidence: ${EVID})" >&2
  exit 3
fi

# --- Auto-invoked required skills (v2 §5): idea-refiner is mandatory on every inception trigger. ---
# The requirements node cannot pass its EXIT gate unless a FRESH idea-refiner report exists that
# references the active (pending) intake id — a stale report from a prior run does not satisfy this.
if [ "$NODE_ID" = "requirements" ] && [ "$GATE" = "exit" ]; then
  IDEA_REFINEMENT="${REPO_ROOT}/sdlc-docs/inception/requirements/idea-refinement.md"
  INTAKE_DIR="${REPO_ROOT}/sdlc-docs/intake"
  PENDING_FILE=""
  for f in "$INTAKE_DIR"/*.yaml; do
    [ -e "$f" ] || continue
    grep -qE 'construction_unblocked:[[:space:]]*false' "$f" || continue
    grep -qiE 'status:[[:space:]]*(withdrawn|superseded)' "$f" && continue
    PENDING_FILE="$f"; break
  done
  INTAKE_ID="$(awk -F': *' '/^intake_id:/{print $2; exit}' "$PENDING_FILE" 2>/dev/null | tr -d '\r')"
  if [ -n "$PENDING_FILE" ] && { [ ! -s "$IDEA_REFINEMENT" ] || [ -z "$INTAKE_ID" ] || ! grep -qF "$INTAKE_ID" "$IDEA_REFINEMENT" 2>/dev/null; }; then
    evid "required-skill idea-refiner: fresh report for intake '${INTAKE_ID}' missing => block"
    log "requirements exit blocked: idea-refiner report missing/stale for ${INTAKE_ID}" "block"
    echo "GATE BLOCK (requirements/exit): idea-refiner is mandatory; ${IDEA_REFINEMENT} must reference active intake '${INTAKE_ID}' before this gate can pass." >&2
    exit 2
  fi
  evid "required-skill idea-refiner: report present for intake '${INTAKE_ID:-n/a}'"
fi

# --- High-impact durable approval (S3 + v2 §8): agent classifies, human confirms, record persists. ---
if grep -Eq "id:[[:space:]]*${NODE_ID}\b" "$GRAPH" 2>/dev/null; then
  if awk -v n="$NODE_ID" '
      $0 ~ "^  - id: "n"$" {f=1}
      f && /high_impact:[[:space:]]*true/ {print "hi"; exit}
      f && /^  - id:/ && $0 !~ ("id: "n"$") {exit}
    ' "$GRAPH" | grep -q hi; then
    ACTION_ID="${COPILOT_SDLC_ACTION:-${NODE_ID}-${GATE}}"
    # Durable record takes precedence over the env flag (§8).
    if [ -x "$APPROVAL_CHECK" ] && "$APPROVAL_CHECK" "$RUN_ID" "$NODE_ID" "$ACTION_ID" >/dev/null 2>&1; then
      evid "high-impact: durable approval record present (${ACTION_ID})"
    elif [ "${COPILOT_SDLC_HIGH_IMPACT_CONFIRMED:-}" = "true" ]; then
      # Env flag set but no durable record yet => block until the record is written.
      evid "high-impact: env-confirmed but no durable approval record => block"
      log "high-impact requires durable approval record (${ACTION_ID})" "block"
      echo "GATE BLOCK (${NODE_ID}/${GATE}): high-impact needs a durable approval record at sdlc-docs/approvals/${RUN_ID}/${NODE_ID}/${ACTION_ID}.yaml (decision: approved)." >&2
      exit 2
    else
      evid "high-impact: no approval => block"
      log "high-impact action requires user confirmation + durable record" "block"
      echo "GATE BLOCK (${NODE_ID}/${GATE}): high-impact action needs explicit user confirmation and a durable approval record." >&2
      exit 2
    fi
  fi
fi

# --- Human approval requirement for this gate (approval: required in the graph). ---
GATE_KEY="${GATE}_gate"
if awk -v n="$NODE_ID" -v g="$GATE_KEY" '
    $0 ~ "^  - id: "n"$" {f=1}
    f && $0 ~ ("^    "g":") && /approval:[[:space:]]*required/ {print "req"; exit}
    f && /^  - id:/ && $0 !~ ("id: "n"$") {exit}
  ' "$GRAPH" | grep -q req; then
  if [ "${COPILOT_SDLC_APPROVAL_RECORDED:-}" != "true" ]; then
    evid "human approval required but not recorded => block"
    log "human approval required; not recorded" "block"
    echo "GATE BLOCK (${NODE_ID}/${GATE}): human approval required and not yet recorded." >&2
    exit 2
  fi
  evid "human approval: recorded"
fi

evid "outcome: pass"
log "gate passed; evidence:${EVID}" "pass"
echo "GATE PASS (${NODE_ID}/${GATE}); evidence: ${EVID}"
exit 0
