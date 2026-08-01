# Construction Rule — Documentation

- **rule_id**: CON-DOCUMENTATION
- **loop step**: 8 (unit documentation) + node: documentation
- **template**: documentation-plan-template.md
- **standard**: documentation-standard

## Objective
Produce user-facing and developer documentation and keep generated artifacts fresh.

## Procedure
1. Author a documentation plan; assign `DOC-` ids and trace each to REQ-/UNIT-.
2. Cover user-facing docs, developer docs, and generated-artifact freshness.
3. Store artifacts under `sdlc-docs/construction/documentation/`.
4. Stale references to outdated traceability IDs block the compliance gate (§9).

## Exit gate
- Documentation node has no mandatory policy check but must satisfy documentation-standard.
- Handoff references DOC- ids and artifact locations.
