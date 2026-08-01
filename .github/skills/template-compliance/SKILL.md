# Template Compliance

Enforce mandatory templates and durable high-impact approvals (v2 §4, §8). Use before a node passes
its exit gate and before any high-impact action executes.

## Template compliance (§4)
1. Every lifecycle artifact must be produced from a template in `.github/sdlc/templates/` and must
   declare its `template_id`.
2. Every **required** section must be present. An empty required section fails compliance unless it is
   explicitly marked `N/A` with a rationale.
3. Template validation is part of the `compliance` policy gate — a node cannot pass its exit gate
   unless every required artifact declares the template it used.

## Required templates
requirements, user-story, architecture-design, unit-decomposition, work-package, functional-design,
nfr-requirements, nfr-design, implementation-plan, test-plan, documentation-plan, security-review,
code-review, release-readiness, and `handoff-template.yaml`.

## Durable high-impact approval (§8)
1. Classify the action: `core_feature` or `critical_design`; state the rationale to the user.
2. On explicit user approval, write a durable record to
   `sdlc-docs/approvals/<run-id>/<node-id>/<action-id>.yaml` (schema:
   `.github/sdlc/schemas/approval.schema.md`) with `decision: approved` and the verbatim
   `raw_user_response`.
3. Verify with `.github/hooks/scripts/approval-check.sh <run-id> <node-id> <action-id>` before executing.
4. `rejected` / `changes_requested` blocks the node until a revised `approved` record exists.
5. The audit entry for the action references the approval record path.
