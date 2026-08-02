<!-- template_id: architecture-design-template.md -->

# Architecture & Design — TinyURL URL Shortener

- **template_id**: architecture-design-template.md
- **run_id**: run-20260801T232309Z (Phase 1, approved) · run-20260802T150051Z (Phase 2, in review)
- **node_id**: architecture-design
- **status**: Phase 1 approved · Phase 2 (§9) pending high-impact durable confirmation

> Cumulative design. **Phase 1** (§1–§8, ADR-001..ADR-009) is approved and implemented.
> **Phase 2** (§9, ADR-010..ADR-016) adds the optional expiry column and bulk creation.

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

---

## 9. Phase 2 — Optional expiry column + Bulk creation (run-20260802T150051Z)

Additive design for intake INTAKE-20260802T150051Z. Traces to REQ-011..REQ-020 (defaults D1–D9).

### 9.1 Architecture decisions (Phase 2)

| ID | Decision | Rationale | Alternatives considered | Traces to |
|----|----------|-----------|-------------------------|-----------|
| ADR-010 | Add a **nullable** `expires_at TIMESTAMP` (UTC `Instant`) column to `short_link`; `NULL` = never expires. Backward-compatible additive migration (existing rows → `NULL`). | Minimal schema change; preserves Phase 1 links; matches `created_at` type | Separate `link_expiry` table (over-normalized); non-null with sentinel date (leaky) | REQ-011, REQ-013 |
| ADR-011 | Enforce expiry **lazily at read time** in `LinkService.resolve(code)`: if `expiresAt != null && now >= expiresAt`, throw `CodeNotFoundException` → **404** (same path as unknown code). No background reaper; expired rows are retained. | Confirmed D1/D4; no leak of existence; zero extra queries on the hot path; simplest ops | 410 Gone (leaks existence); scheduled deletion job (infra + data loss risk) | REQ-013, REQ-014 |
| ADR-012 | `CreateLinkRequest` gains an **optional** `expiresAt` (ISO-8601 UTC `Instant`). Validate in `LinkService`: `null` → accept; `expiresAt <= Instant.now()` → throw `InvalidExpiryException` → **400**. | Confirmed D2/D3; validate at the boundary; consistent `ErrorResponse` | Relative TTL/duration (extra parsing, ambiguity); accept past dates (born-expired links) | REQ-011, REQ-012 |
| ADR-013 | Bulk = **new endpoint** `POST /api/links/bulk` taking `BulkCreateRequest { items: CreateLinkRequest[] }` (1–100 items), returning `BulkCreateResponse { results: BulkItemResult[] }` in request order. Each `BulkItemResult` is either `{ index, code, shortUrl, originalUrl, expiresAt }` (created) or `{ index, error: { code, message } }`. **Best-effort partial success** — each item processed in its own transaction; one failure does not roll back others. `items` size validated **before** processing (0 or >100 → 400). Intra-batch alias collisions resolved first-wins via the DB unique constraint (loser gets a taken-alias item error). | Confirmed D5/D6/D7; predictable ordered contract; reuses single-create validation per item; bounded to cap DoS | Overload `POST /api/links` (ambiguous contract); all-or-nothing transaction (one bad URL wastes batch); unbounded batch (DoS) | REQ-015, REQ-016, REQ-017, REQ-018 |
| ADR-014 | **N-token** rate limiting for bulk. Extract the per-IP fixed-window counter into a shared `RateLimiter` component used by both `RateLimitFilter` (single create = 1 token) and the bulk path. `POST /api/links/bulk` is excluded from the filter; the bulk service atomically requests **N tokens** (N = item count) up front — if the remaining per-IP budget < N, the **whole bulk request is rejected with 429** (nothing created). | Confirmed D8; prevents bulk bypassing REQ-009; whole-request 429 is simpler and more predictable than partial per-item rate errors; filters can't cheaply re-read the JSON body | Per-item rate-limit errors (complex partial accounting); leave filter as-is (bulk bypasses limit) | REQ-019 |
| ADR-015 | Expired code/alias stays **permanently reserved**: `existsByCode` and the unique constraint operate on all rows regardless of expiry, so an expired alias cannot be reclaimed. No cleanup job. | Confirmed D4; avoids silent re-pointing of a known alias; no extra logic | Free expired aliases for reuse (surprising, security-sensitive) | REQ-014 |
| ADR-016 | Frontend adds an **optional** expiry control (native `datetime-local`, converted to a UTC ISO instant) to the existing `feature/shorten` form via `LinkService`; blank → omit `expiresAt`. Created link's expiry is displayed. **Bulk is not exposed in the UI** in Phase 2. | Confirmed D9; smallest UI change; no new dependency | Full bulk-entry UI (out of scope); separate expiry page (unneeded) | REQ-020 |

### 9.2 API additions

| Method | Path | Request | Success | Errors |
|--------|------|---------|---------|--------|
| POST | `/api/links` (extended) | `CreateLinkRequest { url, alias?, expiresAt? }` | `201 Created` + `LinkResponse { code, shortUrl, originalUrl, expiresAt? }` | 400 invalid url/alias/**expiry (past/now)**, 409 alias taken, 429 rate limited |
| POST | `/api/links/bulk` | `BulkCreateRequest { items: [ { url, alias?, expiresAt? }, … ] }` (1–100) | `200 OK` + `BulkCreateResponse { results: [ created \| error, … ] }` (ordered) | 400 empty/>100 items or malformed body; 429 if per-IP budget < N (nothing created) |
| GET | `/{code}` (extended) | path var `code` | `302 Found` when not expired | **404 when expired** or unknown code |

`LinkResponse` gains an optional `expiresAt` (echoed; omitted/null when the link never expires).
New: `BulkCreateRequest`, `BulkCreateResponse`, `BulkItemResult` DTOs; `InvalidExpiryException`
mapped to 400 in `GlobalExceptionHandler`; shared `RateLimiter` component (refactor of `RateLimitFilter`).

### 9.3 Data model change

`short_link` gains one column:

| Column | Type | Constraints |
|--------|------|-------------|
| expires_at | TIMESTAMP | NULL (nullable; NULL = never expires) |

No new index required: expiry is evaluated on the row already fetched by the unique `code` lookup, so
the redirect hot path stays a single indexed read (REQ-013 performance preserved).

### 9.4 Security & privacy (Phase 2)

- **SEC-INPUT-01 (bulk DoS)**: enforce the **max-100** cap (REQ-017) and per-IP **N-token** budget
  (REQ-019) *before* processing items → bounds memory, DB writes, and abuse (OWASP A05/API abuse).
- **SEC-INPUT-01 (per item)**: every bulk item runs the same URL/alias/expiry validation as single
  create (ADR-007/012) — no relaxed path via bulk.
- **SEC-AUDIT-01**: do not leak link existence on expiry — expired → 404, identical to unknown (D1).
- **SEC-ERR-01**: per-item errors and expiry errors use the standard `ErrorResponse` shape (no stack
  traces); bulk item errors carry a machine-readable `code` + message only.

### 9.5 High-impact classification (Phase 2)

- **Classification**: **critical_design**
- **Rationale**: Phase 2 changes the public API contract (extended create response, new bulk endpoint,
  new redirect failure mode), the persistence schema (new column + migration), and the rate-limiting
  architecture (shared limiter, N-token accounting). These are core, hard-to-reverse decisions with
  direct security/abuse implications → durable human confirmation required (spec S3, v2 §8).
- **Approval record**: sdlc-docs/approvals/run-20260802T150051Z/architecture-design/architecture-design-approval.yaml

### 9.6 Traceability (Phase 2)

- ADR-010 → REQ-011/013 · ADR-011 → REQ-013/014 · ADR-012 → REQ-011/012 ·
  ADR-013 → REQ-015/016/017/018 · ADR-014 → REQ-019 · ADR-015 → REQ-014 · ADR-016 → REQ-020.
- Upstream handoff: sdlc-docs/handoffs/run-20260802T150051Z/requirements.yaml.
- Likely units: UNIT-001 (backend, ADR-010..ADR-015), UNIT-002 (frontend, ADR-016).

### 9.7 Open questions & risks (Phase 2)

- **Open questions**: None — resolved by human approval "Accept all defaults" (D1–D9).
- **Risks**:
  - RISK-011 (medium, mitigated): bulk DoS amplification — mitigated by max-100 cap + N-token limit.
  - RISK-012 (low, accepted): expired rows retained indefinitely (no reaper) → table growth; cleanup
    deferred to a future phase.
  - RISK-013 (low, mitigated): best-effort partial success requires callers to inspect per-item
    results — mitigated by explicit ordered per-item error codes.
  - RISK-014 (low, accepted): in-memory shared `RateLimiter` remains per-instance (inherits RISK-004);
    N-token accounting is per-replica for the single-instance MVP.

### 9.8 NFR coverage (Phase 2)

- **Performance**: redirect hot path unchanged (single indexed read + one in-memory comparison);
  bulk scales ~linearly, bounded by the 100-item cap.
- **Security**: OWASP A05/API-abuse addressed via cap + N-token limiting + per-item validation.
- **Availability/Observability**: unchanged; add structured logs for bulk outcomes (counts of
  created/failed) and expiry-triggered 404s, with no PII.
