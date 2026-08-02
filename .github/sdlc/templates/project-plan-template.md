<!-- template_id: project-plan-template.md -->
<!-- v2 §4 mandatory template. Produced by the `plan` inception node via the breakdown-plan skill.
     Stored at sdlc-docs/inception/plan/project-plan.md. Required sections must be present. -->

# Project Plan — <feature/change name>

- **template_id**: project-plan-template.md
- **run_id**: <run-id>
- **node_id**: plan
- **authored_by**: inception
- **status**: draft | approved

## 1. Project overview (required)
- **Summary & business value**:
- **Success criteria / KPIs**:
- **Key milestones** (no timelines):
- **Risk assessment**:

## 2. Work item hierarchy (required)
Epic > Feature > Story/Enabler > Test/Task. Include a Mermaid `graph TD` diagram.

## 3. Issues breakdown (required)
Epic, Feature, Story, Enabler, and Test issues with acceptance criteria, dependencies, Definition of
Done, labels, and estimates. Each item cites its traceability ids.

## 4. Priority & value matrix (required)
P0–P3 vs value tier, with labels.

## 5. Estimation (required)
Story points (Fibonacci) and/or t-shirt sizes per item.

## 6. Dependency management & critical path (required)
Blocks / Blocked by / Related / Prerequisite / Parallel; identify the critical path. Mermaid optional.

## 7. Sprint / board configuration (required)
Kanban columns, custom fields, and (optional) automation.

## 8. Traceability (required)
Every Epic/Feature/Story/Enabler/Test traces to REQ-/US-/ADR-/UNIT- ids.

## 9. Open questions (required)
Mark N/A with rationale if none.
