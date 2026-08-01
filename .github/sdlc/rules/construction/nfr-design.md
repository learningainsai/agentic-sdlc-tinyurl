# Construction Rule — NFR Design

- **rule_id**: CON-NFR-DESIGN
- **loop step**: 3 (per-unit, when NFR requirements present)
- **template**: nfr-design-template.md

## Objective
Design the unit's response to its NFR requirements, including infrastructure implications.

## Procedure
1. For each `REQ-N`, describe the design approach, trade-offs, and verification method.
2. Note infrastructure/config changes; these trigger step 4 (infrastructure design).
3. Trace each design response to its NFR requirement and ADR.

## When required
- Required whenever NFR requirements were produced for the unit (§3).
