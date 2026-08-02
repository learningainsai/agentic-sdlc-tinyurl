<!-- template_id: security-review-template.md -->
<!-- v2 §4 mandatory template. Enforced by security-standard (§6); part of release readiness. -->

# Security Review — run-20260802T150051Z (TinyURL Phase 2 — expiry + bulk creation)

- **template_id**: security-review-template.md
- **run_id**: run-20260802T150051Z
- **status**: passed
- **supersedes**: run-20260801T232309Z (Phase 1 MVP) security review

## 1. Findings (required)

### Phase 2 findings (this run)

| SEC id | Area | Severity | Finding | Status | Evidence |
|--------|------|----------|---------|--------|----------|
| SEC-010 | availability / DoS | — | Bulk create bounded to 1–100 items (`@Valid @Size`) and gated by a shared N-token rate limiter that reserves one token per item; an over-budget batch is rejected wholesale (429) and creates nothing (ADR-014). Mitigates request-amplification DoS (A05, RISK-011). | resolved | LinkControllerTest (429/400), RateLimiter |
| SEC-011 | input-validation | — | Optional `expiresAt` parsed as a strict ISO instant; a past/present value is rejected (400, `InvalidExpiryException`) at create and per-item in bulk. No unvalidated user value reaches persistence. | resolved | LinkServiceTest (TEST-010/011/016b) |
| SEC-012 | access-control | — | Lazy expiry makes an expired alias resolve to 404 (ADR-011) and the alias stays permanently reserved (ADR-015), preventing reuse/takeover of an expired code. | resolved | LinkServiceTest (TEST-012/013) |
| SEC-013 | error-handling | — | Bulk returns structured per-item error codes (`INVALID_URL/INVALID_ALIAS/ALIAS_TAKEN/INVALID_EXPIRY/ERROR`) with no stack traces or internal detail; batch-level errors use the uniform `ErrorResponse`. | resolved | BulkItemResult, GlobalExceptionHandler |
| SEC-014 | frontend-xss | — | The new expiry value is rendered via Angular interpolation with the `date` pipe (no `innerHTML`/`bypassSecurityTrust`); the `datetime-local` value is converted, not echoed as HTML. | resolved | shorten.component.html |

### Phase 1 findings (baseline)

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

- **Phase 2**: SEC-010 ↔ REQ-014..017/ADR-013/014 · SEC-011 ↔ REQ-012/ADR-012 · SEC-012 ↔ REQ-013/ADR-011/015 · SEC-013 ↔ REQ-016 · SEC-014 ↔ REQ-020/ADR-016.
- **Phase 1 baseline**: SEC-001 ↔ REQ-005/010 · SEC-002 ↔ ADR-003 · SEC-003 ↔ ADR-005 · SEC-004 ↔ REQ-009/RISK-004 · SEC-005 ↔ REQ-006 · SEC-006 ↔ ADR-001 · SEC-007 ↔ UNIT-002 · SEC-008 ↔ UNIT-002.

## 4. Unresolved risks (required)

- No unresolved high/critical findings. Phase 2 introduces no new high-severity risk; bulk DoS is
  mitigated (SEC-010). SEC-004 and SEC-007 remain **accepted low-severity** baseline items
  (single-instance rate limiting; dev-only toolchain advisories). RISK-012 (no expired-row reaper) is
  **accepted**. Gate not blocked (§7).
