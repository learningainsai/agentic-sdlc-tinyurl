<!-- template_id: architecture-design-template.md -->
<!-- v2 §4 mandatory template. High-impact node: durable approval required (§8). -->

# Architecture & Design — <feature/change name>

- **template_id**: architecture-design-template.md
- **run_id**: <run-id>
- **node_id**: architecture-design
- **status**: draft | approved

## 1. Overview (required)
<System context and how this change fits.>

## 2. Architecture decisions (required)
Each decision carries an `ADR-` id (v2 §9).

| ID | Decision | Rationale | Alternatives considered | Traces to |
|----|----------|-----------|-------------------------|-----------|
| ADR-001 | | | | REQ-... |

## 3. Components & interfaces (required)
<Modules, responsibilities, contracts, data flow. Diagram optional.>

## 4. Data model (required)
<Entities, storage, migration considerations. Mark N/A with rationale if none.>

## 5. Security & privacy considerations (required)
<Trust boundaries, authn/authz assumptions, sensitive data. Cross-reference security-standard.>

## 6. High-impact classification (required)
- **Classification**: core_feature | critical_design | none
- **Rationale**:
- **Approval record**: sdlc-docs/approvals/<run-id>/architecture-design/<action-id>.yaml

## 7. Traceability (required)
- Decisions ADR-... trace to requirements REQ-...

## 8. Open questions & risks (required)
<Non-empty unresolved high-severity risks block the handoff (§2).>
