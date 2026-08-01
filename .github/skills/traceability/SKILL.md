# Traceability

Maintain end-to-end traceability IDs across the lifecycle (v2 §9). Use whenever authoring or
reviewing any lifecycle artifact.

## ID prefixes
| Prefix | Applies to |
|--------|-----------|
| `REQ-` | requirements (incl. NFRs as `REQ-N...`) |
| `ADR-` | architecture decisions |
| `UNIT-` | units of work |
| `PLAN-` | implementation plans |
| `TEST-` | tests |
| `DOC-` | documentation artifacts |
| `SEC-` | security findings/evidence |
| `CR-` | code review findings |
| `REL-` | release readiness items |

## Rules
- Every requirement, decision, unit, plan, test, doc, review finding, and release item carries an ID.
- References must resolve to an existing ID; stale/missing references **block** the compliance gate.
- The traceability chain must connect: `REQ- → ADR- → UNIT- → PLAN- → TEST- / DOC- / CR-` and
  security findings `SEC-` where applicable.

## Release-readiness check
Release readiness cannot pass if any approved `REQ-` lacks linked design, implementation, test,
documentation, and review evidence. Build the matrix in `release-readiness-template.md`.

## Metric
Emit `traceability_coverage` (percentage of approved requirements fully linked) as an audit marker (§12).
