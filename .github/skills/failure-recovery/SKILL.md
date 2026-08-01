# Failure Recovery

Handle stage failures with bounded, governed recovery. Use whenever a node errors, a gate fails, or
a parallel branch conflicts.

## Bounds (from spec S4)
- **Retry**: max **3** attempts for a transient/idempotent failure.
- **Fallback / Rollback / Safe-stop target**: **last approved stage** (from `sdlc-docs/state.md`).

## Procedure
1. Classify the failure: transient (retryable) vs. terminal.
2. **Retry** transient failures up to 3 times with the same inputs. Log each attempt.
3. On the 3rd failed retry (or a terminal failure), choose one:
   - **Fallback**: switch to an alternate path/artifact, resuming from the last approved stage.
   - **Rollback**: restore workspace/state to the last approved stage.
   - **Safe-stop**: halt at the last approved stage and surface a human decision point.
4. **Compensating actions**: for any non-reversible side-effect already performed (pushed release,
   sent notification, provisioned resource), execute its registered compensating action — do not
   attempt a naive rollback.
5. **Parallel conflict / partial failure**: do not auto-resolve. **Wait for a human decision** (S6).
6. Append the full failure event, attempts, and recovery choice to `sdlc-docs/audit-log.md`.

## Idempotency
Before retrying a node with side-effects, confirm an idempotency key or a compensating action
exists. If neither exists, do not retry — escalate to safe-stop.

## Metrics hooks (S10)
Emit `retry_frequency`, `rollback_frequency`, and `mttr` markers to the audit log for later
aggregation. Exact definitions are deferred (non-blocking).
