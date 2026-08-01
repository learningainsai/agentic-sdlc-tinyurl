# Common Rule — Question Format Guide

- **rule_id**: COMMON-QUESTION-FORMAT
- **load**: always (v2 §5)

How the agent asks the user for approvals, confirmations, and decisions.

## When to ask
- Inception exit gates (human approval required).
- High-impact classification confirmation (§8) — core_feature / critical_design.
- Parallel-branch conflict or partial failure at a join (spec S6).
- Re-planning after an upstream change (spec S5).
- Release readiness / Operations decision (spec S7).
- Policy `block` or `error` outcomes requiring authorization (§7).

## Format
State each of the following, concisely:
1. **Context** — node id, phase, what was produced.
2. **Classification** (if high-impact) — core_feature | critical_design + rationale.
3. **Decision requested** — approve | reject | changes_requested (or the specific choice).
4. **Impact of each option**.
5. **Evidence links** — handoff, policy evidence, traceability.

## Rules
- Ask one decision at a time; never bundle unrelated approvals.
- Never treat silence as approval.
- Record the user's **verbatim** response in the audit log and the relevant approval/handoff record.
