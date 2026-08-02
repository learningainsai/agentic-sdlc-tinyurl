#!/usr/bin/env bash
# inception-skill-gate.sh — Auto-invoke enforcement for inception skills (preToolUse).
# Realizes COMMON-NEW-WORK-INTAKE-GATE + workflow-graph new_work_policy.auto_invoke_on_inception_entry.
#
# Makes idea-refiner *implicitly mandatory* on every inception trigger: you cannot draft
# requirements.md or user-stories.md until the idea-refiner report exists and is non-empty.
# This removes reliance on the agent remembering to run the skill.
#
# Exit codes:
#   0  allow (not a guarded drafting call, or the idea-refiner report already exists)
#   2  block (attempt to draft requirements before the idea-refiner report exists)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"
IDEA_REFINEMENT="${REPO_ROOT}/sdlc-docs/inception/requirements/idea-refinement.md"

log() { "$AUDIT" "inception-skill-gate" "pre-tool-use" "$1" "${2:-n/a}" "inception-skill-gate" >/dev/null 2>&1 || true; }

TOOL="${COPILOT_TOOL_NAME:-unknown}"
case "$TOOL" in
  *edit*|*create*|*write*|*apply*|*insert*|*replace*|*patch*) ;;
  *) exit 0 ;;
esac

CANDIDATES="${COPILOT_TOOL_FILEPATH:-} ${COPILOT_TOOL_FILE:-} ${COPILOT_FILE_PATH:-} ${COPILOT_TOOL_ARG_FILEPATH:-} ${COPILOT_TOOL_INPUT:-} ${COPILOT_TOOL_ARGS:-}"

# Guard only the requirements deliverables; the idea-refinement report itself is never guarded.
targets_requirements() {
  case "$1" in
    *inception/requirements/requirements.md|*inception/requirements/user-stories.md) return 0 ;;
  esac
  return 1
}

HIT=""
for tok in $CANDIDATES; do
  if targets_requirements "$tok"; then HIT="$tok"; break; fi
done
[ -n "$HIT" ] || exit 0

# Only a NEW idea in flight requires a fresh report. Find the pending intake (in inception),
# ignoring any marked status: withdrawn|superseded.
INTAKE_DIR="${REPO_ROOT}/sdlc-docs/intake"
PENDING_FILE=""
for f in "$INTAKE_DIR"/*.yaml; do
  [ -e "$f" ] || continue
  grep -qE 'construction_unblocked:[[:space:]]*false' "$f" || continue
  grep -qiE 'status:[[:space:]]*(withdrawn|superseded)' "$f" && continue
  PENDING_FILE="$f"; break
done
[ -n "$PENDING_FILE" ] || exit 0   # no new idea pending => nothing to enforce here
INTAKE_ID="$(awk -F': *' '/^intake_id:/{print $2; exit}' "$PENDING_FILE" | tr -d '\r')"

# The idea-refiner report must exist AND reference THIS intake id (proves it was run for this idea,
# not carried over from a previous run's report).
if [ -s "$IDEA_REFINEMENT" ] && [ -n "$INTAKE_ID" ] && grep -qF "$INTAKE_ID" "$IDEA_REFINEMENT" 2>/dev/null; then
  exit 0
fi

log "blocked '${HIT}' — idea-refiner report missing or not for intake ${INTAKE_ID}" "block"
cat >&2 <<EOF
INCEPTION SKILL GATE BLOCK: cannot draft '${HIT}' yet.
idea-refiner is mandatory on every inception trigger. Produce a FRESH
sdlc-docs/inception/requirements/idea-refinement.md that references the active intake
'${INTAKE_ID}' (run the idea-refiner skill and capture the agreed answers) BEFORE drafting
requirements/user stories, then retry.
EOF
exit 2
