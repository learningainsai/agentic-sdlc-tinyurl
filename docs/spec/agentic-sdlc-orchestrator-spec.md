# Agentic SDLC Orchestrator — Specification (v1, locked)

> Status: **Ready to build** — all requirements resolved into objective, testable criteria.
> Reference model (not a dependency): [awslabs/aidlc-workflows `core-workflow.md`](https://github.com/awslabs/aidlc-workflows/blob/main/aidlc-rules/aws-aidlc-rules/core-workflow.md).
> This spec was produced via a pre-implementation grilling pass; see `docs/spec/decision-log.md` for the Q&A lineage.

## 1. Purpose

An agentic orchestration layer that coordinates the full SDLC lifecycle (requirements,
architecture/design, implementation, testing, documentation, release readiness) with
non-linear, stateful, governed execution — not simple linear task chaining.

## 2. Execution model (S1)

- Execution is driven by an **explicit dependency graph (DAG)** — see `.github/sdlc/workflow-graph.yaml`.
- Non-linear behavior is limited to **conditional branching** (branch selection from a stage output/predicate).
  Cyclic re-entry and dynamic node insertion are **out of scope** for v1.
- **Inception phase**: single-threaded, **human-gated** — one stage at a time, human approval required to advance.
- **Construction/Build phase**: **multi-agent, concurrent autonomy** — independent nodes may run in
  parallel without per-stage approval, **except** where a gate rule (S3) applies.

**Acceptance**
- The graph is declaratively defined and validated before execution.
- No inception stage starts before the prior stage's human approval is recorded in the audit log.
- Construction nodes with no unmet gate dependency may execute concurrently.

## 3. Gates (S2)

- Every node has an **entry gate** and an **exit gate**.
- A gate is composed of **(a) human approval** and/or **(b) automated policy check**
  (security, compliance, change control).
- A gate resolves to `pass | fail | block`; anything other than `pass` halts progression.

**Acceptance**
- A node whose policy check fails cannot exit.
- A node requiring approval cannot exit without a recorded human decision.

## 4. Human-approval trigger — high-impact rule (S3)

- Human approval is **required** when an action **builds a core feature** or makes a
  **critical design decision**.
- Classification is **agent-proposed, human-confirmed**: the agent classifies the action and
  **explicitly confirms with the user** before proceeding.
- All other construction-phase actions may proceed autonomously.

**Acceptance**
- Given an action the agent classifies as `core-feature` or `critical-design`, the orchestrator
  blocks and requests user confirmation even during autonomous construction.

## 5. Failure semantics (S4)

| Mechanism  | Contract |
|------------|----------|
| Retry      | Bounded: **max 3 attempts**. |
| Fallback   | Restores to **last approved stage**. |
| Rollback   | Restores to **last approved stage**. |
| Safe-stop  | Halts at **last approved stage**. |

- Non-reversible side-effects (pushed release, sent notification, provisioned resource) are
  handled with **compensating actions** registered per action.

**Acceptance**
- Retry stops after 3 attempts, then falls back / rolls back / safe-stops to the last approved stage.
- Every non-idempotent action registers a compensating action before execution.

## 6. Re-planning (S5)

- When an upstream output changes, the orchestrator **pauses and requests a human decision**
  before re-planning. No autonomous re-plan (prevents oscillation and orphaned downstream artifacts).

**Acceptance**
- An upstream change never triggers automatic downstream re-execution without a recorded human decision.

## 7. Parallel synchronization (S6)

- On parallel-branch **conflict** or **partial failure** at a join, the orchestrator **waits for a
  human decision** (no auto-merge, no auto-cancel of siblings).

**Acceptance**
- A join with conflicting/failed branches blocks and surfaces a human decision point.

## 8. Release readiness / Operations (S7)

- The reference Operations phase is a placeholder, so release-readiness coordination **defers to a
  human decision** rather than an inherited automation.

**Acceptance**
- Reaching release-readiness raises a human decision point; no automated deploy is assumed.

## 9. State, lineage, and audit (S8)

- Persistence layer = **markdown files**.
- State: `sdlc-docs/state.md`. Audit: `sdlc-docs/audit-log.md`.
- The audit is an **append-only log** — single-writer append semantics; entries are never
  overwritten. This also serializes concurrent multi-agent writes during construction.
- Decision lineage must be reconstructable from the markdown state + audit log.

**Acceptance**
- All audit writes are append-only; no tool overwrites the audit log.
- Cross-stage context and decision lineage are reconstructable from the persisted markdown.

## 10. Platform / constructs (S9)

- Implemented with **GitHub Copilot custom agents, hooks, and skills**.
- **No aidlc extensions** (those target Kiro IDE). The aidlc `core-workflow.md` is used as a
  **reference model only**, not an installed dependency.

## 11. Reliability metrics (S10)

- Track: success rate, retry/rollback frequency, MTTR, end-to-end latency.
- v1 ships **metric hooks**; exact definitions and measurement windows are **deferred to a later
  product decision** (non-blocking for v1).

## 12. Out of scope (v1)

- Cyclic re-entry and dynamic graph node insertion.
- Automated deployment/Operations execution.
- Finalized metric definitions and dashboards.
- Tamper-evident/cryptographic audit guarantees (append-only markdown only).
