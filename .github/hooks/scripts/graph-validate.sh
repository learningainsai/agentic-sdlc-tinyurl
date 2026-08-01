#!/usr/bin/env bash
# graph-validate.sh — Validate v2-required workflow-graph fields and referenced files (v2 §10).
#
# Fails if:
#   - a node is missing a v2-required field;
#   - a declared template or standard file does not exist;
#   - a construction node declares parallelism without a conflict rule for unit target files.
#
# Exit codes:
#   0  graph valid
#   3  graph invalid
#
# Usage: graph-validate.sh
# Note: intentionally dependency-free (awk/grep). Not a full YAML parser; checks presence + refs.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
GRAPH="${REPO_ROOT}/.github/sdlc/workflow-graph.yaml"
TEMPLATES_DIR="${REPO_ROOT}/.github/sdlc/templates"
STANDARDS_DIR="${REPO_ROOT}/.github/sdlc/rules/standards"
AUDIT="${REPO_ROOT}/.github/hooks/scripts/audit-append.sh"

log() { "$AUDIT" "graph-validate" "graph-check" "$1" "${2:-n/a}" "graph-validate" >/dev/null 2>&1 || true; }
FAIL=""

[ -f "$GRAPH" ] || { echo "GRAPH INVALID: $GRAPH not found" >&2; exit 3; }

# 1) Per-node required v2 fields.
NODE_IDS="$(awk '/^  - id:/{print $3}' "$GRAPH")"
REQUIRED_NODE_FIELDS="agent templates standards input_artifacts output_artifacts handoff policy_evidence_path entry_gate exit_gate"

for node in $NODE_IDS; do
  block="$(awk -v n="$node" '
    $0 ~ "^  - id: "n"$" {f=1; print; next}
    f && /^  - id:/ {exit}
    f {print}
  ' "$GRAPH")"
  for field in $REQUIRED_NODE_FIELDS; do
    printf '%s\n' "$block" | grep -Eq "^    ${field}:" || FAIL="${FAIL}\n  node '${node}' missing field '${field}'"
  done
  # Construction unit node must declare unit fields + conflict rule availability.
  if printf '%s\n' "$block" | grep -Eq "^    phase: construction" \
     && printf '%s\n' "$block" | grep -Eq "^    unit_scope:"; then
    printf '%s\n' "$block" | grep -Eq "^    unit_registry:" || FAIL="${FAIL}\n  node '${node}' unit_scope without unit_registry"
    printf '%s\n' "$block" | grep -Eq "^    parallelism:" || FAIL="${FAIL}\n  node '${node}' unit_scope without parallelism"
    grep -Eq "^  conflict_rule:" "$GRAPH" \
      || FAIL="${FAIL}\n  construction node '${node}' uses parallelism but no top-level conflict_rule"
  fi
done

# 2) Declared templates exist.
for tmpl in $(awk '/^    templates:/{gsub(/[][,]/," "); for(i=2;i<=NF;i++) print $i}' "$GRAPH" | sort -u); do
  [ -f "${TEMPLATES_DIR}/${tmpl}" ] || FAIL="${FAIL}\n  template not found: ${tmpl}"
done

# 3) Declared standards exist (graph uses bare ids; files are <id>.md).
for std in $(awk '/^    standards:/{gsub(/[][,]/," "); for(i=2;i<=NF;i++) print $i}' "$GRAPH" | sort -u); do
  [ -f "${STANDARDS_DIR}/${std}.md" ] || FAIL="${FAIL}\n  standard not found: ${std}.md"
done

if [ -n "$FAIL" ]; then
  printf 'GRAPH INVALID:%b\n' "$FAIL" >&2
  log "graph invalid" "invalid"
  exit 3
fi

echo "GRAPH VALID: v2 fields present; templates and standards resolved."
log "graph valid" "valid"
exit 0
