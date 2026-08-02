#!/usr/bin/env bash
# intake-gate.sh — New-work intake enforcement (preToolUse).
# Realizes COMMON-NEW-WORK-INTAKE: a new idea must traverse inception before any construction edit.
#
# Blocks mutating tool calls that target PRODUCT SOURCE paths while either:
#   - an intake item is still in inception (construction_unblocked: false), or
#   - no inception-authorized intake exists at all.
# Governance/spec paths (sdlc-docs/, .github/, docs/) are never guarded, so the process can always
# be run and amended.
#
# Exit codes:
#   0  allow (not a guarded/mutating call, or an active intake authorizes construction)
#   2  block (guarded product edit with a pending or missing inception-authorized intake)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
INTAKE_DIR="${REPO_ROOT}/sdlc-docs/intake"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"

log() { "$AUDIT" "intake-gate" "pre-tool-use" "$1" "${2:-n/a}" "intake-gate" >/dev/null 2>&1 || true; }

# pending_intake(): prints the first intake still in inception (construction_unblocked: false),
# ignoring any marked status: withdrawn|superseded. Returns 1 when none.
pending_intake() {
  local f
  for f in "$INTAKE_DIR"/*.yaml; do
    [ -e "$f" ] || continue
    grep -qE 'construction_unblocked:[[:space:]]*false' "$f" || continue
    grep -qiE 'status:[[:space:]]*(withdrawn|superseded)' "$f" && continue
    printf '%s\n' "$f"; return 0
  done
  return 1
}

TOOL="${COPILOT_TOOL_NAME:-unknown}"

# Only guard mutating file tools; read/search/terminal-read tools are always allowed.
case "$TOOL" in
  *edit*|*create*|*write*|*apply*|*insert*|*replace*|*patch*|*notebook*) ;;
  *) exit 0 ;;
esac

# Collect candidate target paths from whatever the runtime exposes for this tool call.
CANDIDATES="${COPILOT_TOOL_FILEPATH:-} ${COPILOT_TOOL_FILE:-} ${COPILOT_FILE_PATH:-} ${COPILOT_TOOL_ARG_FILEPATH:-} ${COPILOT_TOOL_INPUT:-} ${COPILOT_TOOL_ARGS:-}"

# guarded(): true (0) when the path is product source requiring inception authorization.
guarded() {
  local p="$1"
  case "$p" in
    *sdlc-docs/*|*.github/*|*docs/*) return 1 ;;   # governance/spec — never guarded
  esac
  case "$p" in
    *tiny-url-creator/*|*/backend/*|*/frontend/*|*.java|*.ts|*.tsx|*.html|*.css|*pom.xml|*package.json)
      return 0 ;;
  esac
  return 1
}

HIT=""
for tok in $CANDIDATES; do
  if guarded "$tok"; then HIT="$tok"; break; fi
done
[ -n "$HIT" ] || exit 0   # nothing guarded in this call

# A pending intake still in inception blocks ALL product edits until it clears the plan gate.
if [ -d "$INTAKE_DIR" ] && pending_intake >/dev/null 2>&1; then
  log "blocked '${HIT}' — an intake is still in inception (construction_unblocked: false)" "block"
  cat >&2 <<EOF
INTAKE GATE BLOCK: cannot modify product source ('${HIT}').
An intake item is still in the inception phase (construction_unblocked: false).
A new idea/feature/change MUST complete inception (idea-refiner -> requirements ->
architecture-design -> unit-decomposition -> plan) before any construction edit.
Finish inception and set construction_unblocked: true on the intake record, then retry.
EOF
  exit 2
fi

# Require at least one inception-authorized intake before any product edit is allowed.
if [ ! -d "$INTAKE_DIR" ] || ! grep -rEl 'construction_unblocked:[[:space:]]*true' "$INTAKE_DIR" >/dev/null 2>&1; then
  log "blocked '${HIT}' — no inception-authorized intake exists" "block"
  cat >&2 <<EOF
INTAKE GATE BLOCK: cannot modify product source ('${HIT}').
No inception-authorized intake exists under sdlc-docs/intake/.
Route the work through inception first, then create an intake record with
construction_unblocked: true once the plan node passes.
EOF
  exit 2
fi

exit 0
