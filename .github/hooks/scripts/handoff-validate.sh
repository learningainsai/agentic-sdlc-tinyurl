#!/usr/bin/env bash
# handoff-validate.sh — Validate a v2 handoff artifact against the schema (v2 §2).
# Schema: .github/sdlc/schemas/handoff.schema.md
#
# Exit codes:
#   0  valid           (downstream may proceed)
#   2  block            (open questions / unresolved high-severity risks, or passed-status gaps)
#   3  invalid          (missing file, malformed YAML, or missing required keys)
#
# Usage: handoff-validate.sh <path-to-handoff.yaml>
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"

FILE="${1:-}"
log() { "$AUDIT" "handoff-validate" "handoff-check" "$1" "${2:-n/a}" "handoff-validate" >/dev/null 2>&1 || true; }

if [ -z "$FILE" ] || [ ! -f "$FILE" ]; then
  log "missing handoff file: ${FILE:-<none>}" "invalid"
  echo "HANDOFF INVALID: file not found: ${FILE:-<none>}" >&2
  exit 3
fi

REQUIRED_KEYS="run_id node_id phase from_agent status output_artifacts template_ids standards_applied traceability_ids policy_evidence open_questions risks context_summary"
MISSING=""
for key in $REQUIRED_KEYS; do
  grep -Eq "^${key}:" "$FILE" || MISSING="$MISSING $key"
done
if [ -n "$MISSING" ]; then
  log "malformed handoff; missing keys:${MISSING}" "invalid"
  echo "HANDOFF INVALID (${FILE}): missing required keys:${MISSING}" >&2
  exit 3
fi

# Extract scalar status (first match).
STATUS="$(awk -F': *' '/^status:/{print $2; exit}' "$FILE" | tr -d '"' | tr -d "'" | xargs 2>/dev/null || true)"

# passed-status completeness: output_artifacts, policy_evidence, context_summary must be non-empty.
non_empty_list() { # key -> 0 if the list/scalar has content
  awk -v k="$1" '
    $0 ~ "^"k":" {
      # inline non-empty? e.g. context_summary: "text"  or  key: [a]
      inline=$0; sub("^"k": *","",inline); gsub(/[][ \t"'"'"']/,"",inline)
      if (inline != "" && inline != "[]") {print "y"; exit}
      f=1; next
    }
    f && /^[[:alnum:]_]+:/ {exit}          # next top-level key
    f && /^[[:space:]]*-[[:space:]]*/ {print "y"; exit}  # a list item
  ' "$2"
}

if [ "$STATUS" = "passed" ]; then
  GAP=""
  [ "$(non_empty_list output_artifacts "$FILE")" = "y" ] || GAP="$GAP output_artifacts"
  [ "$(non_empty_list policy_evidence  "$FILE")" = "y" ] || GAP="$GAP policy_evidence"
  [ "$(non_empty_list context_summary  "$FILE")" = "y" ] || GAP="$GAP context_summary"
  if [ -n "$GAP" ]; then
    log "passed handoff missing:${GAP}" "block"
    echo "HANDOFF BLOCK (${FILE}): status=passed requires non-empty:${GAP}" >&2
    exit 2
  fi
fi

# open_questions non-empty => block unless a human decision is recorded elsewhere (agent enforces).
if [ "$(non_empty_list open_questions "$FILE")" = "y" ]; then
  log "handoff has open_questions; needs human decision" "block"
  echo "HANDOFF BLOCK (${FILE}): open_questions present — record a human decision to proceed." >&2
  exit 2
fi

# unresolved high-severity risks => block.
if awk '/^risks:/{f=1;next} f && /^[[:alnum:]_]+:/{f=0} f' "$FILE" \
     | grep -Eiq 'severity:[[:space:]]*(high|critical)'; then
  if awk '/^risks:/{f=1;next} f && /^[[:alnum:]_]+:/{f=0} f' "$FILE" \
       | grep -Eiq 'status:[[:space:]]*(open|unresolved)'; then
    log "handoff has unresolved high-severity risk" "block"
    echo "HANDOFF BLOCK (${FILE}): unresolved high-severity risk — record a human decision." >&2
    exit 2
  fi
fi

log "handoff valid" "valid"
echo "HANDOFF VALID: ${FILE}"
exit 0
