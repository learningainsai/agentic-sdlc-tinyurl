<!-- template_id: test-strategy-template.md -->
<!-- Required sections must be present for a plan-node artifact to pass compliance. -->

# Test Strategy — {feature/epic name}

- **template_id**: test-strategy-template.md
- **run_id**: {run_id}
- **node_id**: plan
- **status**: draft
- **skill**: breakdown-test
- **sources**: requirements.md, user-stories.md, architecture-design.md, project-plan.md, unit-registry.yaml

## 1. Test strategy overview (required)
- Testing scope
- Quality objectives / success criteria
- Risk assessment (link RISK-ids)
- Test approach

## 2. ISTQB framework implementation (required)
- Test design techniques selected (equivalence partitioning, boundary value, decision table,
  state transition, experience-based) with rationale
- Test types coverage matrix (functional, non-functional, structural, change-related)

## 3. ISO 25010 quality characteristics assessment (required)
Priority (Critical/High/Medium/Low) + validation approach for each: functional suitability,
performance efficiency, compatibility, usability, reliability, security, maintainability, portability.

## 4. Test environment & data strategy (required)
- Environment requirements
- Test data management
- Tool selection
- CI/CD integration

## 5. Traceability (required)
| Test item | REQ | US | ADR | UNIT | TEST |
|-----------|-----|----|-----|------|------|

## 6. Open questions (required)
