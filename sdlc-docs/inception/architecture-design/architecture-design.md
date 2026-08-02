<!-- template_id: architecture-design-template.md -->

# Architecture & Design — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: architecture-design-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: architecture-design
- **status**: approved

## 1. Overview (required)

A two-tier web application implementing a URL shortener MVP:

- **Frontend**: Angular SPA (standalone components) providing a single submission form. It calls the
  backend REST API only through an Angular service (no direct `HttpClient` use in components).
- **Backend**: Spring Boot (Java 17) REST API with a layered structure
  (`controller → service → repository`, with `dto`/`entity`), persisting short-link mappings to
  PostgreSQL (H2 in local/dev).

Two hot flows:
1. **Create** — `POST /api/links` validates input, generates or accepts a code, persists the mapping,
   returns the short URL.
2. **Redirect** — `GET /{code}` looks up the code and issues an HTTP 302 to the original URL.

```mermaid
flowchart LR
  U[User / Browser] -->|submit URL| A[Angular SPA]
  A -->|POST /api/links| C[LinkController]
  C --> S[LinkService]
  S --> R[LinkRepository]
  R --> DB[(PostgreSQL / H2)]
  U -->|GET /{code}| C
  C -->|302 Location| U
```

## 2. Architecture decisions (required)

| ID | Decision | Rationale | Alternatives considered | Traces to |
|----|----------|-----------|-------------------------|-----------|
| ADR-001 | Layered Spring Boot REST API (`controller/service/repository/dto/entity`); controllers hold no business logic | Matches `AGENTS.md` conventions; testable, separation of concerns | Single-class app; hexagonal (overkill for MVP) | REQ-001..REQ-006, REQ-008 |
| ADR-002 | Angular standalone-component SPA; all HTTP via a `LinkService` | Matches `AGENTS.md` frontend conventions; keeps components thin | NgModule-based; server-rendered form | REQ-007 |
| ADR-003 | Single `short_link` table in PostgreSQL (H2 for local/dev), unique constraint on `code` | Simplest durable model; DB enforces uniqueness | In-memory map (not durable); NoSQL (unneeded) | REQ-001, REQ-002, REQ-008 |
| ADR-004 | System code = 7-char Base62 from a CSPRNG (`SecureRandom`); on unique-constraint violation, retry (bounded) | Non-sequential/unpredictable codes; ~3.5T space; retry resolves rare collisions | Sequential id + Base62 (enumerable); hashing (collision handling) | REQ-001, REQ-008 |
| ADR-005 | Redirect returns **HTTP 302 (Found)** with `Location` header | Confirmed default A4; preserves flexibility, avoids aggressive browser caching | 301 permanent (hard to change later) | REQ-005 |
| ADR-006 | Custom alias validated against `^[A-Za-z0-9_-]{3,30}$` + reserved-word list; taken alias → **409**, invalid → **400** | Confirmed defaults A2/A3; prevents route shadowing & ambiguous input | Silent fallback to generated code (surprising) | REQ-002, REQ-003 |
| ADR-007 | URL validation: accept only `http`/`https`, max 2048 chars, reject the shortener's own host; **never** fetch the target server-side | Confirmed A1/A7; prevents SSRF (OWASP A05) and redirect loops | Fetch to verify liveness (SSRF risk) | REQ-004, REQ-010 |
| ADR-008 | Per-IP fixed-window rate limit on creation via an in-memory counter in a servlet `Filter` (no new dependency) | Confirmed A7; lightweight abuse control without adding a library (`AGENTS.md`) | Bucket4j / Redis token bucket (deferred — needs dependency + infra) | REQ-009 |
| ADR-009 | Consistent error contract via `@RestControllerAdvice` returning an `ErrorResponse` DTO (no stack traces) | Uniform errors across endpoints; avoids info leak (OWASP A10) | Ad-hoc per-controller errors | REQ-003, REQ-004, REQ-006 |

## 3. Components & interfaces (required)

**Backend packages** (`com.example.tinyurl`): `controller`, `service`, `repository`, `dto`, `entity`,
`config` (filter/advice).

**REST API**

| Method | Path | Request | Success | Errors |
|--------|------|---------|---------|--------|
| POST | `/api/links` | `CreateLinkRequest { url: string, alias?: string }` | `201 Created` + `LinkResponse { code, shortUrl, originalUrl }` | 400 invalid URL/alias, 409 alias taken, 429 rate limited |
| GET | `/{code}` | path var `code` | `302 Found`, `Location: <originalUrl>` | 404 unknown code |

- `LinkController` — thin; delegates to `LinkService`; maps outcomes to HTTP.
- `LinkService` — validation, code generation, collision retry, alias/reserved-word checks.
- `LinkRepository` — `JpaRepository<ShortLink, Long>`; `Optional<ShortLink> findByCode(String)`,
  `existsByCode(String)`.
- `RateLimitFilter` — per-IP fixed-window counter for `POST /api/links` only.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — maps exceptions to `ErrorResponse`.

**Frontend** (`src/app`): `feature/shorten` (standalone component + template), `core/link.service.ts`
(HTTP), `shared` (models). Uses reactive form; shows result + copy-to-clipboard; inline error display.

## 4. Data model (required)

`short_link`:

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, generated |
| code | VARCHAR(30) | UNIQUE, NOT NULL (system 7-char Base62 or custom alias) |
| original_url | VARCHAR(2048) | NOT NULL |
| custom_alias | BOOLEAN | NOT NULL, default false |
| created_at | TIMESTAMP | NOT NULL, default now |

Index: unique index on `code` (enforces ADR-004/006 uniqueness; backs redirect lookup).

## 5. Security & privacy considerations (required)

Cross-references security-standard rule IDs:

- **SEC-INPUT-01**: All input validated at the boundary — scheme allowlist (`http`/`https`), max
  length 2048, alias regex + reserved words, self-host rejection (ADR-006/007). Persistence uses JPA
  parameter binding (no SQL string concatenation → OWASP A05).
- **SEC-INPUT-01 (SSRF)**: The service never issues an outbound request to the submitted URL; it only
  stores/redirects (ADR-007) → avoids OWASP A01 SSRF.
- **SEC-AUTHZ-01**: Trust boundary is the public internet → the API. MVP is intentionally anonymous
  (A6); no protected resources exist, so no authz beyond input hardening + rate limiting.
- **SEC-SECRET-01**: No secrets in code; DB credentials via environment/config, never logged.
- **SEC-AUDIT-01**: Structured logs for create/redirect outcomes (code + result), **no PII/secrets**.
- **Open redirect note**: Redirect targets are user-supplied by design (core feature). Risk is
  contained by scheme allowlist + self-host rejection; browser shows the destination. Domain
  blocklisting for phishing is deferred to Phase 2 (RISK-002).

## 6. High-impact classification (required)

- **Classification**: **critical_design**
- **Rationale**: This node fixes the core architecture, the public API contract, the persistence data
  model, the code-generation scheme, and redirect semantics for the whole product. These are
  expensive to change later and directly shape security posture — a critical design decision requiring
  durable human confirmation (spec S3, v2 §8).
- **Approval record**: sdlc-docs/approvals/run-20260801T232309Z/architecture-design/architecture-design-approval.yaml

## 7. Traceability (required)

- ADR-001 → REQ-001..006, REQ-008 · ADR-002 → REQ-007 · ADR-003 → REQ-001/002/008 ·
  ADR-004 → REQ-001/008 · ADR-005 → REQ-005 · ADR-006 → REQ-002/003 · ADR-007 → REQ-004/010 ·
  ADR-008 → REQ-009 · ADR-009 → REQ-003/004/006.
- Upstream handoff: sdlc-docs/handoffs/run-20260801T232309Z/requirements.yaml.

## 8. Open questions & risks (required)

- **Open questions**: None — all requirement-level questions resolved at the requirements gate (A1–A8).
- **Risks**:
  - RISK-002 (medium, **accepted for MVP**): Public redirect to arbitrary user-supplied URLs enables
    phishing/malware sharing. Mitigations in scope: scheme allowlist, max length, self-host rejection,
    per-IP rate limiting. Domain/threat blocklisting deferred to Phase 2.
  - RISK-003 (low, mitigated): Generated-code collision under concurrency — mitigated by DB unique
    constraint + bounded retry (ADR-004).
  - RISK-004 (low, accepted): In-memory rate limiter (ADR-008) is per-instance only; not shared across
    replicas. Acceptable for single-instance MVP; distributed limiter deferred.

### NFR coverage (nfr-requirements-template.md / nfr-design-template.md)

- **Performance**: redirect p95 ≤ 100 ms (indexed single-key lookup); creation p95 ≤ 300 ms.
- **Security**: as §5 (OWASP A01/A05/A10 addressed).
- **Availability**: single-instance MVP, state durable in DB; no formal SLA.
- **Observability**: structured create/redirect logs, no PII; error logging via `ErrorResponse`.
- **Scalability design**: stateless API (except in-memory rate-limit counter) → horizontally
  scalable later once the rate limiter is externalized (RISK-004).
