# Subagent Handoff

Produce and consume durable handoff artifacts between nodes/agents (v2 §2). Use at the end of every
node and before consuming any upstream output.

## Artifact
- Path: `sdlc-docs/handoffs/<run-id>/<node-id>.yaml`
- Template: `.github/sdlc/templates/handoff-template.yaml`
- Schema: `.github/sdlc/schemas/handoff.schema.md`
- Validator: `.github/hooks/scripts/handoff-validate.sh <path>`

## Producing a handoff (node exit)
1. Copy the template to the handoff path and fill every required key.
2. Set `status`. For `passed`, ensure non-empty `output_artifacts`, `policy_evidence`, `context_summary`.
3. List `template_ids`, `standards_applied`, `traceability_ids`, `approvals`, `next_ready_nodes`.
4. Record any `open_questions` and `risks` ({id, severity, description, status}).
5. Run the validator. On block/invalid, fix before releasing control.

## Consuming a handoff (downstream entry)
1. Run the validator on each upstream handoff.
2. If missing/malformed → **block** downstream execution (§2).
3. If `open_questions` non-empty or an unresolved high-severity risk exists → **block** until a human
   decision is recorded in the audit log.
4. Load `context_summary` + `input_artifacts`; do not rely on in-memory context (session-continuity).

## Metrics
Emit `handoff_count` and `handoff_latency` audit markers per node (§12).
