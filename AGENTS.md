# Agent Instructions

Instructions for  AI coding agents working with this repository.

## Repository Overview

This is a full-stack application with a **Spring Boot REST API** backend and an **Angular SPA** frontend, built using a spec-driven development workflow. Requirements live in `SPEC.md`; this file governs *how* agents should behave while implementing them.

### Key Components

- **backend/**: Spring Boot REST API (Java 17, Maven)
- **frontend/**: Angular single-page application (TypeScript)
- **SPEC.md**: Feature requirements, API contracts, and acceptance criteria — always check this before implementing anything
- **.github/skills/**: Reusable, task-specific agent skills

### Technology Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Data JPA, Maven, JUnit 5, PostgreSQL (H2 for local/dev)
- **Frontend**: Angular 17+, TypeScript (strict mode), RxJS, Angular CLI, Jasmine/Karma
- **API contract**: REST/JSON; OpenAPI spec maintained alongside controllers where present

## General

- Always check `SPEC.md/PRD.md` before implementing a new feature. Do not invent requirements that aren't stated there — flag ambiguity instead of guessing.
- Never modify `pom.xml` or `package.json` dependency versions unless explicitly asked.
- Never change `application.yml` / `application.properties` or environment configs without confirmation.
- Do not introduce a new third-party library without flagging it first.
- Make only high-confidence suggestions when reviewing code changes.

## Backend Conventions (Java / Spring Boot)

- Java 17, 4-space indentation, no wildcard imports.
- Layered package structure: `controller / service / repository / dto / entity`. Controllers must not contain business logic — that belongs in the service layer.
- Use constructor injection exclusively; never field injection (`@Autowired` on fields).
- DTOs for all request/response bodies. Never expose JPA entities directly over REST.
- Every new endpoint requires a corresponding JUnit test (`@WebMvcTest` for controllers, `@SpringBootTest` for integration).
- Validate all inputs with `@Valid` and Bean Validation annotations. Return a consistent error response shape across all endpoints (see `ErrorResponse` DTO).
- Use `Optional<T>` for nullable repository results; a service method should never return `null` directly.

## Frontend Conventions (Angular)

- TypeScript strict mode is on. No `any` without a comment explaining why it's unavoidable.
- Prefer standalone components over NgModules for new code, unless the surrounding module already uses NgModules.
- Services own all HTTP calls. Components must never call `HttpClient` directly.
- Use RxJS operators (`switchMap`, `map`, `catchError`) instead of nested `.subscribe()` calls.
- Every new component requires a corresponding `.spec.ts` test.
- Follow the existing folder convention: `feature/`, `shared/`, `core/`.

## Code Review Instructions

- Flag any controller method containing business logic that belongs in a service class.
- Flag any endpoint missing input validation.
- Flag any Angular component that subscribes to an Observable without unsubscribing (missing `takeUntilDestroyed` or an async pipe in the template).
- Don't comment on formatting already enforced by Prettier/Checkstyle — assume that's handled separately.

## Code Comments

- Comment **why**, not what — explain non-obvious business rules, workarounds, or constraints.
- Don't add comments that just restate the code.
- When a piece of code implements a specific rule from `SPEC.md`, reference which acceptance criterion it satisfies.

## Building

- **Backend**: `cd backend && ./mvnw clean install`
- **Frontend**: `cd frontend && npm install && npm run build`
- **Full stack** (if Docker Compose is configured): `docker-compose up --build`

## Testing

- **Backend**: `./mvnw test` — all new endpoints require passing tests before merge.
- **Frontend**: `npm test` (Karma/Jasmine, or Jest if configured).
- Never comment out a failing test to force a build to pass — fix it or flag it explicitly.

## Project Layout

```
repo-root/
├── backend/                          # Spring Boot REST API
│   └── src/main/java/.../{controller,service,repository,dto,entity}
├── frontend/                         # Angular SPA
│   └── src/app/{feature,shared,core}
├── SPEC.md                           # Feature requirements & acceptance criteria
├── AGENTS.md                         # This file
└── .github/skills/                   # Reusable agent skills
```

## Available Skills

- **breakdown-feature-implementation**: Create detailed feature implementation plans.
- **breakdown-plan**: Generate comprehensive project plans with Epic > Feature > Story/Enabler > Test hierarchy.
- **breakdown-test**: Create test strategies and quality validation plans.
- **create-github-action-workflow-specification**: Specify existing GitHub Actions CI/CD workflows.
- **create-github-issues-feature-from-implementation-plan**: Create GitHub issues from implementation plans.
- **create-github-issues-for-unmet-specification-requirements**: Create issues for missing spec requirements.
- **create-implementation-plan**: Generate implementation plan files for new features or refactors.
- **create-specification**: Generate specification files optimized for AI consumption.
- **create-spring-boot-java-project**: Scaffold a Spring Boot project skeleton with Docker support.
- **devops-rollout-plan**: Generate deployment rollout plans with rollback and verification steps.
- **documentation-writer**: Create high-quality project documentation using Diátaxis.
- **excel-to-pdf**: Convert Excel files to PDF.
- **github-actions-efficiency**: Audit GitHub Actions workflow efficiency.
- **github-actions-hardening**: Review GitHub Actions workflows for security risks.
- **grill-me**: Interrogate `SPEC.md` for ambiguities and gaps before implementation.
- **java-springboot**: Provide Spring Boot best practices.
- **java-springboot-bestpractices**: Provide Spring Boot development best practices.
- **multi-stage-dockerfile**: Generate optimized multi-stage Dockerfiles.
- **openapi-to-application-code**: Generate application code from OpenAPI specifications.
- **quality-playbook**: Run a quality engineering audit on the codebase.
- **security-review**: Scan the codebase for security vulnerabilities and risks.
- **spring-boot-testing**: Provide Spring Boot testing guidance.
- **word-to-pdf**: Convert Word documents to PDF.

## Pattern-Based Instructions

Pattern | Applies
--- | ---
`backend/**/*.java` | Java / Spring Boot conventions above
`frontend/**/*.ts` | Angular / TypeScript conventions above
`**/*.spec.ts`, `**/*Test.java` | Testing conventions above
