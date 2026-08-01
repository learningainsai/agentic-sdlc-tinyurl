#!/usr/bin/env bash
# audit-append.sh — Append a single timestamped entry to the append-only audit log.
# Realizes spec S8/S11: append-only, single-writer serialization of concurrent agent writes.
#
# Usage: audit-append.sh "<stage>" "<event>" "<detail>" ["<result>"] ["<actor>"]
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
AUDIT_LOG="${REPO_ROOT}/sdlc-docs/audit-log.md"
LOCK="${AUDIT_LOG}.lock"

STAGE="${1:-unknown}"
EVENT="${2:-event}"
DETAIL="${3:-}"
RESULT="${4:-n/a}"
ACTOR="${5:-orchestrator}"
TS="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

mkdir -p "$(dirname "$AUDIT_LOG")"
[ -f "$AUDIT_LOG" ] || printf '# SDLC Audit Log (append-only)\n\n' > "$AUDIT_LOG"

# Serialize concurrent writers with a mkdir-based lock (portable, atomic).
for _ in $(seq 1 50); do
  if mkdir "$LOCK" 2>/dev/null; then
    trap 'rmdir "$LOCK" 2>/dev/null || true' EXIT
    break
  fi
  sleep 0.1
done

{
  printf '## %s\n' "$STAGE"
  printf -- '- **Timestamp**: %s\n' "$TS"
  printf -- '- **Event**: %s\n' "$EVENT"
  printf -- '- **Actor**: %s\n' "$ACTOR"
  printf -- '- **Detail**: %s\n' "$DETAIL"
  printf -- '- **Result**: %s\n' "$RESULT"
  printf -- '---\n\n'
} >> "$AUDIT_LOG"
