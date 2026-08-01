# Agentic SDLC Orchestrator — Specification v2 Addendum

> Status: **Draft for review**.
> Relationship to v1: **Additive**. This document extends `docs/spec/agentic-sdlc-orchestrator-spec.md`; it does not replace the locked v1 orchestration scaffold.
> Goal: close the v1 gaps around subagent handoff, decomposed unit-of-work execution, standardized templates, local rules/standards, hybrid policy gates, and durable approval state.

## 1. Purpose

v2 upgrades the orchestrator from a gated phase DAG into a Copilot-native SDLC methodology layer. It must preserve v1 governance while adding:

- explicit subagent handoff contracts;
- construction execution by decomposed unit of work;
- mandatory artifact templates;
- local rule detail files under `.github/sdlc/rules`;
- coding, security, testing, documentation, code review, and release readiness standards;
- hybrid human-plus-automated policy gates;
- durable approval state for high-impact actions.

**Acceptance**
- v1 graph, gate, audit, and failure semantics remain valid unless explicitly tightened by this addendum.
- A v2 run cannot advance a node unless the node's mandatory handoff, template, standards, and policy evidence requirements are satisfied.

## 2. Subagent Handoff Protocol

Every workflow node must produce a durable handoff artifact before another agent or node may consume its output.

Handoff artifacts are stored at:

```text
sdlc-docs/handoffs/<run-id>/<node-id>.yaml
```

Each handoff must include:

```yaml
run_id: ""
node_id: ""
phase: ""
from_agent: ""
to_agents: []
status: pending | in_progress | passed | failed | blocked | safe_stopped
input_artifacts: []
output_artifacts: []
template_ids: []
standards_applied: []
traceability_ids: []
decisions_made: []
approvals: []
policy_evidence: []
validation_evidence: []
open_questions: []
risks: []
compensating_actions: []
next_ready_nodes: []
context_summary: ""
```

**Acceptance**
- The orchestrator blocks downstream execution if the upstream node's handoff artifact is missing or malformed.
- `status: passed` requires non-empty `output_artifacts`, `policy_evidence`, and `context_summary`.
- Any handoff with non-empty `open_questions` or unresolved high-severity `risks` blocks unless a human decision is recorded.

## 3. Decomposed Unit-of-Work Construction

Construction execution is organized around approved units of work, not only broad phase nodes.

The `unit-decomposition` node must produce a unit registry at:

```text
sdlc-docs/construction/units/unit-registry.yaml
```

Each unit must include:

```yaml
unit_id: ""
name: ""
owner_agent: construction
depends_on_units: []
requirements: []
design_refs: []
target_files: []
expected_tests: []
documentation_refs: []
risk_level: low | medium | high
high_impact: true | false
status: pending | in_progress | passed | failed | blocked
```

For each unit, construction must execute this ordered loop:

1. functional design, when required;
2. NFR requirements, when required;
3. NFR design, when NFR requirements are present;
4. infrastructure design, when infrastructure changes are present;
5. code generation plan;
6. implementation;
7. unit-level tests;
8. unit documentation;
9. unit code review;
10. unit handoff.

**Acceptance**
- No unit implementation starts until its unit record exists and its dependencies are passed.
- Independent units may run concurrently only when their `depends_on_units` are satisfied and their `target_files` do not conflict.
- The orchestrator blocks a join if unit outputs conflict, required tests are missing, or documentation/review artifacts reference stale traceability IDs.

## 4. Mandatory Templates

All lifecycle artifacts must be produced from templates stored under:

```text
.github/sdlc/templates/
```

v2 requires these templates:

- `requirements-template.md`
- `user-story-template.md`
- `architecture-design-template.md`
- `unit-decomposition-template.md`
- `work-package-template.md`
- `functional-design-template.md`
- `nfr-requirements-template.md`
- `nfr-design-template.md`
- `implementation-plan-template.md`
- `test-plan-template.md`
- `documentation-plan-template.md`
- `security-review-template.md`
- `code-review-template.md`
- `release-readiness-template.md`
- `handoff-template.yaml`

**Acceptance**
- A node cannot pass its exit gate unless every required artifact declares the template it used.
- Required template sections must be present; empty required sections fail compliance unless explicitly marked `N/A` with rationale.
- Template validation is part of the `compliance` policy gate.

## 5. Local Rules And Standards

v2 uses Copilot-native rule details under:

```text
.github/sdlc/rules/
```

Required structure:

```text
.github/sdlc/rules/
  common/
    process-overview.md
    session-continuity.md
    content-validation.md
    question-format-guide.md
  standards/
    coding-standard.md
    security-standard.md
    testing-standard.md
    documentation-standard.md
    code-review-standard.md
    release-readiness-standard.md
  inception/
    requirements-analysis.md
    architecture-design.md
    unit-decomposition.md
  construction/
    functional-design.md
    nfr-requirements.md
    nfr-design.md
    implementation.md
    testing.md
    documentation.md
    code-review.md
  extensions/
```

Rules under `common` are always loaded at workflow start. Standards under `standards` are always enforced. Extension rules may be enabled by opt-in files using the naming convention `<rule-name>.opt-in.md` next to `<rule-name>.md`.

**Acceptance**
- The orchestrator records which rule files and standards were loaded for each run.
- A node cannot pass if an applicable mandatory standard has an unresolved non-compliance finding.
- Extension enablement decisions are recorded in state and audit logs.

## 6. Required Standards

v2 requires all six standards.

| Standard | Minimum Required Coverage |
|----------|---------------------------|
| Coding | style, maintainability, dependency use, error handling, testability |
| Security | input validation, secret handling, authn/authz assumptions, dependency risk, auditability |
| Testing | unit, integration, regression, edge cases, failure paths, coverage evidence |
| Documentation | user-facing docs, developer docs, generated artifact freshness, traceability |
| Code review | correctness, security, maintainability, test quality, business logic risk |
| Release readiness | unresolved risks, rollback/compensation, test evidence, review evidence, operational notes |

**Acceptance**
- Each standard defines rule IDs and verification checks.
- Gate evidence must reference rule IDs, not only free-text summaries.
- Code review and release readiness are explicit lifecycle checks before final completion.

## 7. Hybrid Policy Gates

Policy gates use a hybrid model: automated scripts gather objective evidence, and the agent/human review resolves judgment-based findings.

Policy outcomes are:

```text
pass | fail | block | error
```

- `pass`: all automated checks passed and no applicable review findings remain.
- `fail`: an automated check or mandatory standard verification failed.
- `block`: human approval, missing evidence, or unresolved risk prevents progression.
- `error`: the policy checker itself failed, timed out, or produced malformed output.

`error` is treated as `block` unless a human explicitly authorizes retry, fallback, or safe-stop.

**Acceptance**
- Security, compliance, and change-control gates must evaluate only the policies declared for the current node and gate.
- Automated evidence is stored under `sdlc-docs/policy-evidence/<run-id>/<node-id>/`.
- Human review decisions are stored in both the handoff artifact and the append-only audit log.

## 8. Durable High-Impact Approval State

High-impact approval must be durable, not only an environment variable.

State must record high-impact decisions at:

```text
sdlc-docs/approvals/<run-id>/<node-id>/<action-id>.yaml
```

Each approval record must include:

```yaml
run_id: ""
node_id: ""
action_id: ""
classification: core_feature | critical_design
agent_rationale: ""
approved_by: user
decision: approved | rejected | changes_requested
timestamp: ""
raw_user_response: ""
```

**Acceptance**
- A high-impact action cannot execute unless its approval record exists with `decision: approved`.
- The audit log must reference the approval record path.
- Rejected or changes-requested decisions block the node until a revised approval is recorded.

## 9. Traceability

Every requirement, design decision, unit, implementation plan, test, documentation artifact, review finding, and release-readiness item must carry traceability IDs.

Required ID prefixes:

- `REQ-` for requirements;
- `ADR-` for architecture decisions;
- `UNIT-` for units of work;
- `PLAN-` for implementation plans;
- `TEST-` for tests;
- `DOC-` for documentation artifacts;
- `SEC-` for security findings/evidence;
- `CR-` for code review findings;
- `REL-` for release readiness items.

**Acceptance**
- Release readiness cannot pass if any approved requirement lacks linked design, implementation, test, documentation, and review evidence.
- Stale or missing traceability references block the compliance gate.

## 10. Workflow Graph Extensions

Each graph node must declare these additional v2 fields:

```yaml
agent: ""
templates: []
standards: []
input_artifacts: []
output_artifacts: []
handoff:
  required: true
  path: "sdlc-docs/handoffs/{run_id}/{node_id}.yaml"
policy_evidence_path: "sdlc-docs/policy-evidence/{run_id}/{node_id}/"
```

Construction unit nodes must also declare:

```yaml
unit_scope: decomposed
unit_registry: sdlc-docs/construction/units/unit-registry.yaml
parallelism: by_independent_unit
```

**Acceptance**
- Graph validation fails if any v2-required field is missing.
- Graph validation fails if a declared template or standard file does not exist.
- Graph validation fails if a construction node allows parallelism without a conflict rule for unit target files.

## 11. Generated Documentation Layout

v2 generated artifacts must use this layout:

```text
sdlc-docs/
  state.md
  audit-log.md
  approvals/
  handoffs/
  policy-evidence/
  inception/
    requirements/
    architecture-design/
    unit-decomposition/
  construction/
    plans/
    units/
    testing/
    documentation/
    code-review/
  release-readiness/
```

Application code must remain outside `sdlc-docs/`. `sdlc-docs/` is for lifecycle artifacts, evidence, state, approvals, and audit history.

**Acceptance**
- The orchestrator rejects lifecycle artifacts written outside the expected `sdlc-docs/` location unless explicitly classified as application code.
- The release-readiness node reports all generated artifact paths and their traceability coverage.

## 12. Minimal Metrics

v2 defines the minimum metrics needed to evaluate the added methodology layer.

Track:

- handoff count;
- handoff latency per node;
- blocked gate count;
- policy error count;
- retry count;
- unresolved risk count;
- review finding count by severity;
- traceability coverage percentage.

**Acceptance**
- Metrics are emitted as audit markers and can be reconstructed from `sdlc-docs/audit-log.md` plus generated evidence files.
- Release readiness includes a metrics summary for the run.

## 13. Out Of Scope For v2

- Importing AIDLC rule files as a dependency.
- Automated production deployment.
- Cryptographic audit guarantees.
- Dynamic graph node insertion at runtime.
- A full external dashboard for metrics.

## 14. Readiness Verdict

v2 is ready for implementation when these are complete:

1. v2 workflow graph fields are added and validated.
2. Required templates exist.
3. Required rules and standards exist under `.github/sdlc/rules`.
4. Handoff schema validation is implemented.
5. Durable high-impact approval records are implemented.
6. Hybrid policy gate evidence paths are implemented.
7. Construction uses the unit registry for parallel unit execution.
8. Release readiness verifies traceability across requirements, design, implementation, tests, documentation, and reviews.
