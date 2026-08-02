<!-- template_id: security-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by security-standard (§6); part of release readiness. -->

# Security Review — run-20260801T232309Z (TinyURL Phase 1 MVP)

- **template_id**: security-review-template.md
- **run_id**: run-20260801T232309Z
- **status**: approved

## 1. Findings (required)

| SEC id | Area | Severity | Finding | Status | Evidence |
|--------|------|----------|---------|--------|----------|
| SEC-001 | input-validation | — | URL & alias validated via `@Valid` (`@NotBlank`, `@Size ≤2048`, `@Pattern ^[A-Za-z0-9_-]{3,30}$`) plus service-side scheme/self-host checks; rejects non-http(s) and self-host targets (REQ-005/010). | resolved | LinkServiceTest, GlobalExceptionHandler |
| SEC-002 | injection | — | Persistence via Spring Data JPA (parameterized); no string-concatenated SQL/JPQL. No SSRF (backend only stores the URL and issues a 302 `Location`; it does not fetch the target). | resolved | LinkRepository, ADR-003 |
| SEC-003 | secrets | — | No secrets in source. Dev DB credentials are docker-compose local-only; prod credentials injected via env (`DB_URL/DB_USERNAME/DB_PASSWORD`), `prod` profile. | resolved | application-prod.yml, docker-compose.yml |
| SEC-004 | availability | low | Per-IP fixed-window rate limiter on `POST /api/links` is in-memory/per-instance (RISK-004). Adequate for single-instance MVP; not a shared limiter. | accepted | RateLimitFilter, WebConfig |
| SEC-005 | auditability / error-handling | — | Uniform `ErrorResponse` (no stack traces or internal details to clients); domain exceptions mapped to 400/404/409, rate limit to 429. | resolved | GlobalExceptionHandler |
| SEC-006 | authn-authz | — | Phase 1 MVP is intentionally unauthenticated (public shorten/redirect); no privileged operations or user data exposed. Documented assumption, in scope per SPEC. | accepted | architecture-design.md |
| SEC-007 | dependency-risk | low | Frontend `npm install` reports transitive advisories in the Angular 17 dev toolchain (build/test only, not shipped to runtime). No runtime third-party additions. Backend uses managed Spring Boot 3.3.4 BOM versions. | accepted | package.json (devDependencies) |
| SEC-008 | frontend-xss | — | Angular default contextual escaping used; no `innerHTML`/`bypassSecurityTrust`. User content rendered via interpolation/`href` binding only. | resolved | shorten.component.html |

## 2. Coverage checklist (required)

- [x] Input validation — bean validation + service checks; tested.
- [x] Secret handling — env-injected in prod; none in source.
- [x] Authn/Authz assumptions — documented (public MVP, no protected resources).
- [x] Dependency risk — runtime deps managed; dev-only advisories noted/accepted.
- [x] Auditability — consistent error contract; no internal detail leakage.

## 3. Traceability (required)

- SEC-001 ↔ REQ-005/010 · SEC-002 ↔ ADR-003 · SEC-003 ↔ ADR-005 · SEC-004 ↔ REQ-009/RISK-004 ·
  SEC-005 ↔ REQ-006 · SEC-006 ↔ ADR-001 · SEC-007 ↔ UNIT-002 · SEC-008 ↔ UNIT-002.

## 4. Unresolved risks (required)

- No unresolved high/critical findings. SEC-004 and SEC-007 are **accepted low-severity** items for the
  Phase 1 MVP (single-instance rate limiting; dev-only toolchain advisories). Gate not blocked (§7).
