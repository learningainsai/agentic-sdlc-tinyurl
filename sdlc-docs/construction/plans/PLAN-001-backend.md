<!-- template_id: implementation-plan-template.md -->
<!-- v2 §3/§4 mandatory template. Code-generation plan step of the per-unit loop. -->

# Implementation Plan — UNIT-001

- **template_id**: implementation-plan-template.md
- **plan_id**: PLAN-001
- **unit_id**: UNIT-001
- **run_id**: run-20260801T232309Z
- **status**: approved

## 1. Approach (required)

Backend URL-shortener REST API on Spring Boot 3.3.4 / Java 17, layered per `AGENTS.md`:

1. **Persistence** — `ShortLink` JPA entity (`short_link`: id, unique `code`, `original_url`,
   `custom_alias`, `created_at`) + `LinkRepository` (`findByCode`, `existsByCode`).
2. **Config** — `AppProperties` (`app.base-url`, `app.self-hosts`, `app.rate-limit`) bound via
   `@ConfigurationPropertiesScan`; `application.yml` (H2 in-memory for MVP, PostgreSQL runtime dep).
3. **Code generation** — `CodeGenerator` (SecureRandom Base62, 7 chars) satisfying REQ-002.
4. **Service** — `LinkService.createLink()` validates URL (http/https, ≤2048, non-self-host),
   handles custom alias (reserved-word + collision → 409) or generated code (retry on collision,
   catch `DataIntegrityViolationException`); `resolve()` maps code → original URL or 404.
5. **Web** — `LinkController` (`POST /api/links` → 201 `LinkResponse`), `RedirectController`
   (`GET /{code}` → 302 `Location`), `GlobalExceptionHandler` (400/404/409 → `ErrorResponse`).
6. **Rate limiting** — `RateLimitFilter` (per-IP fixed window on `POST /api/links`) registered via
   `WebConfig` `FilterRegistrationBean` (kept out of MVC test slices).
7. **Tests** — `LinkServiceTest` (Mockito), `CodeGeneratorTest`, `@WebMvcTest` controller slices,
   `LinkFlowIT` (`@SpringBootTest` create→redirect).

## 2. Target files (required)

`tiny-url-creator/backend/**` — matches unit registry `target_files` (no cross-unit conflict; UNIT-002
owns `frontend/**`).

Key files: `pom.xml`, `src/main/resources/application.yml`,
`entity/ShortLink.java`, `repository/LinkRepository.java`, `dto/{CreateLinkRequest,LinkResponse,ErrorResponse}.java`,
`config/{AppProperties,WebConfig,RateLimitFilter,GlobalExceptionHandler}.java`,
`service/{CodeGenerator,LinkService}.java` + service exceptions,
`controller/{LinkController,RedirectController}.java`; tests under `src/test/java/...`.

## 3. Dependencies & sequencing (required)

- Depends on units: — (none; builds against the fixed API contract in ADR-003)
- External dependencies: Spring Boot 3.3.4 starters (web, data-jpa, validation), H2 (runtime),
  PostgreSQL driver (runtime), JUnit 5. No new libraries beyond the approved architecture.

## 4. Risk & compensating actions (required)

- **RISK-004** (accepted): in-memory per-IP rate limiter is per-instance; acceptable for single-instance
  Phase 1 MVP. Compensating action deferred: swap to a shared store (e.g. Redis) when horizontally scaled.
- Alias collision under concurrency: mitigated by DB unique constraint + `DataIntegrityViolationException`
  catch and bounded retry (generated codes) / 409 (custom alias). No non-reversible side-effects.

## 5. Traceability (required)

- PLAN-001 ↔ UNIT-001 ↔ REQ-001..006, REQ-008..010 ↔ ADR-001, ADR-003..009 ↔ TEST-001..007.
