---
name: breakdown-feature-implementation
description: 'Create detailed feature implementation plans from a Feature PRD, following Epoch monorepo structure. Use when asked to break down a feature, write an implementation plan, map frontend and backend work, define API and database design, or produce a Mermaid-rich plan saved under docs/ways-of-work/plan.'
argument-hint: 'Provide the epic name, feature name, and Feature PRD content or path'
user-invocable: true
---

# Feature Implementation Plan

Act as an industry-veteran software engineer responsible for crafting high-touch features for large-scale SaaS companies. Review the provided context and output a thorough implementation plan. Do not write production code unless it is pseudocode for technical situations.

## What This Skill Produces
- A complete feature implementation plan in Markdown
- A plan structured for Epoch monorepo conventions
- System architecture and data model diagrams in Mermaid
- Concrete frontend, backend, API, data, security, and deployment planning guidance

## When To Use
- A developer has a Feature PRD and needs a detailed implementation plan
- A team needs a feature breakdown before coding begins
- A feature spans frontend, backend, API, database, and infrastructure concerns
- A project needs a consistent implementation planning format under `docs/ways-of-work/plan/`

## Output Contract
- Output a complete implementation plan in Markdown format.
- Save the result to `/docs/ways-of-work/plan/{epic-name}/{feature-name}/implementation-plan.md`.
- Do not write implementation code in the output unless it is narrowly scoped pseudocode.

## Epoch Monorepo Structure

```text
apps/
  [app-name]/
services/
  [service-name]/
packages/
  [package-name]/
```

## Procedure
1. Read the Feature PRD and identify the epic name, feature name, user-facing goal, system impacts, and constraints.
2. Map the feature onto the Epoch monorepo structure by identifying which apps, services, and packages are affected.
3. Write a concise feature goal in 3 to 5 sentences.
4. Expand the PRD into a detailed requirements list, including functional behavior, edge cases, and implementation-specific expectations.
5. Produce a system architecture overview using Mermaid with subgraphs for frontend, API, business logic, data, and infrastructure layers.
6. Document technology stack selection, integration points, deployment architecture, and scalability considerations.
7. Produce a Mermaid entity-relationship diagram and describe table specifications, indexes, foreign keys, and migration strategy.
8. Define API design, including endpoints, request and response formats, authentication, error handling, rate limiting, and caching.
9. Define frontend architecture, including component hierarchy, state flow, reusable component expectations, and state management patterns.
10. Document security and performance considerations, including validation, sanitization, caching, and optimization strategy.
11. Save the finished plan to `/docs/ways-of-work/plan/{epic-name}/{feature-name}/implementation-plan.md`.

## Decision Points
1. If the epic name is missing, derive it from the PRD theme and make the assumption explicit.
2. If the feature name is missing, derive a stable slug from the PRD title and make the assumption explicit.
3. If the PRD lacks technical detail, expand the plan with clearly labeled assumptions instead of leaving sections empty.
4. If the feature impacts multiple applications or services, split responsibilities clearly by `apps/`, `services/`, and `packages/` boundaries.
5. If data persistence is involved, include migration and indexing strategy even when the PRD does not mention it.
6. If external APIs or infrastructure components are involved, include integration and deployment architecture explicitly.
7. If the requested output would drift into implementation code, stop at design-level detail and use pseudocode only where necessary.

## Required Output Structure

### Goal
- Describe the feature goal in 3 to 5 sentences.

### Requirements
- Detailed feature requirements as a bulleted list
- Implementation-plan-specific notes and assumptions

### Technical Considerations

#### System Architecture Overview
- Create a comprehensive Mermaid system architecture diagram.
- Include these layers as subgraphs:
  - Frontend Layer: user interface components, state management, and client-side logic
  - API Layer: tRPC endpoints, authentication middleware, input validation, and request routing
  - Business Logic Layer: service classes, business rules, workflow orchestration, and event handling
  - Data Layer: database interactions, caching mechanisms, and external API integrations
  - Infrastructure Layer: Docker containers, background services, and deployment components
- Show data flow between layers with labeled arrows for request and response patterns, data transformations, and event flows.
- Include feature-specific components, services, or data structures unique to the implementation.
- Document technology stack selection and rationale for each layer.
- Define integration points and communication protocols.
- Define Docker-oriented deployment architecture.
- Describe horizontal and vertical scaling considerations.

#### Database Schema Design
- Create a Mermaid entity-relationship diagram for the feature data model.
- Document table specifications with fields, types, and constraints.
- Document indexing strategy and rationale.
- Document foreign key relationships and referential integrity rules.
- Document database migration strategy.

#### API Design
- Define endpoints with full specifications.
- Include request and response formats with TypeScript types.
- Describe authentication and authorization with Stack Auth.
- Describe error handling strategies and status codes.
- Describe rate limiting and caching strategies.

#### Frontend Architecture

##### Component Hierarchy Documentation
- Base the component structure on `shadcn/ui` when applicable for consistent and accessible UI foundations.
- Document a concrete component hierarchy for the feature page or flow.
- Include a Mermaid state flow diagram for component state management.
- Document reusable component library expectations.
- Document state management patterns using Zustand and React Query where appropriate.
- Define TypeScript interfaces and types at the planning level.

Use the following structure pattern when relevant:

```text
Recipe Library Page
├── Header Section (shadcn: Card)
│   ├── Title (shadcn: Typography `h1`)
│   ├── Add Recipe Button (shadcn: Button with DropdownMenu)
│   │   ├── Manual Entry (DropdownMenuItem)
│   │   ├── Import from URL (DropdownMenuItem)
│   │   └── Import from PDF (DropdownMenuItem)
│   └── Search Input (shadcn: Input with icon)
├── Main Content Area (flex container)
│   ├── Filter Sidebar (aside)
│   │   ├── Filter Title (shadcn: Typography `h4`)
│   │   ├── Category Filters (shadcn: Checkbox group)
│   │   ├── Cuisine Filters (shadcn: Checkbox group)
│   │   └── Difficulty Filters (shadcn: RadioGroup)
│   └── Recipe Grid (main)
│       └── Recipe Card (shadcn: Card)
│           ├── Recipe Image (img)
│           ├── Recipe Title (shadcn: Typography `h3`)
│           ├── Recipe Tags (shadcn: Badge)
│           └── Quick Actions (shadcn: Button - View, Edit)
```

#### Security Performance
- Document authentication and authorization requirements.
- Document data validation and sanitization requirements.
- Document performance optimization strategies.
- Document caching mechanisms.

## Context Template
- Feature PRD: the content of the feature PRD markdown file

## Completion Checks
Before finalizing, verify:
1. The plan is saved under `/docs/ways-of-work/plan/{epic-name}/{feature-name}/implementation-plan.md`.
2. The plan contains all required top-level sections.
3. The plan includes both a system architecture Mermaid diagram and a database Mermaid diagram.
4. The plan maps work to `apps/`, `services/`, and `packages/` where applicable.
5. The plan covers API, frontend, data, security, performance, and deployment considerations.
6. The output stays at planning level and does not drift into full implementation code.

## Example Prompts
- Break down this Feature PRD into a full implementation plan for our Epoch monorepo.
- Create a Mermaid-rich implementation plan for this feature and save it under docs/ways-of-work/plan.
- Turn this SaaS feature PRD into a backend, frontend, API, and database implementation plan.

## Related Customizations
- Add a prompt file that pre-fills epic name, feature name, and PRD path.
- Add workspace instructions for preferred plan naming, Mermaid conventions, or Stack Auth patterns.
- Add reference files with house standards for tRPC, Zustand, React Query, Docker, and schema migrations.