# Extension Rules (opt-in)

- **dir**: .github/sdlc/rules/extensions
- **spec**: v2 §5

Extension rules are **disabled by default**. Enable one by adding an opt-in file next to the rule:

```
<rule-name>.md          # the extension rule detail
<rule-name>.opt-in.md   # presence enables the rule for the run
```

## Rules
- Extension enablement decisions must be recorded in `sdlc-docs/state.md` and the audit log (§5).
- The orchestrator records which extension rules were loaded for each run.
- No extension rule may weaken an always-enforced standard (§6) or the append-only audit guarantee.

No extension rules are enabled in this repository by default.
