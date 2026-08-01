# Decision Log — Pre-implementation Grilling

Lineage of open questions resolved before implementation. Each row is a spec-shaping decision.

| # | Question | Decision |
|---|----------|----------|
| 1 | Non-linear behaviors in scope | Conditional branch only |
| 2 | Gate pass/fail contract | Human approval + policy check |
| 3 | "High-impact" definition | Builds a core feature or a critical design decision |
| 4 | Retry / fallback / rollback / safe-stop | Retry limit 3; fallback/rollback/safe-stop target = last approved stage |
| 5 | Non-reversible side-effects | Compensating actions |
| 6 | Re-plan convergence | Human decision |
| 7 | Parallel conflict / partial failure | Wait for human decision |
| 8 | Release readiness (Operations placeholder) | Human decision |
| 9 | Multi-agent vs single-threaded | Single-threaded human-gated in Inception; multi-agent concurrent autonomy in Construction |
| 10 | Persistence layer | Markdown files |
| 11 | Audit-grade meaning | Append-only |
| 12 | Custom constructs | GitHub Copilot custom agents, hooks, skills; no aidlc extensions (Kiro-only) |
| 13 | Metric definitions | Deferred to later product decision (non-blocking v1) |

## Residual questions (resolved)

| # | Question | Decision |
|---|----------|----------|
| RQ-1 | Who classifies core-feature/critical-design | Agent classifies, confirms with user |
| RQ-2 | Retry limit + rollback target | Limit 3; target = last approved stage |
| RQ-3 | "Wait" terminal condition | Wait for human decision |
| RQ-4 | Concurrent markdown write safety | Append-only log (single-writer append) |
| RQ-5 | Metrics deferral non-blocking for v1 | Yes |

**Verdict:** Ready to build.

## v2 addendum decisions

| # | Question | Decision |
|---|----------|----------|
| V2-1 | Should the next spec replace v1 or extend it | Additive v2 addendum; keep locked v1 intact |
| V2-2 | Construction parallelism model | Decomposed unit-of-work execution |
| V2-3 | Durable handoff required for every graph node | Yes |
| V2-4 | Templates as mandatory gate inputs | Yes |
| V2-5 | Local rule-detail location | `.github/sdlc/rules` |
| V2-6 | Required standards | All six: coding, security, testing, documentation, code review, release readiness |
| V2-7 | Policy gate model | Hybrid: automated evidence plus agent/human review |
| V2-8 | Durable high-impact approval state | Yes; store approval state in addition to audit log |

**v2 Verdict:** Draft spec created; implementation should begin after graph, templates, rules, and gate checks are updated to match the addendum.
