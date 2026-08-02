# TinyURL Backend (UNIT-001)

Spring Boot 3.3.4 / Java 17 REST API for TinyURL. Traces to REQ-001..019, ADR-001, ADR-003..015.
Phase 2 adds optional link expiry and bulk creation (REQ-011..019, ADR-010..015).

## Prerequisites

- JDK 17+ (verified with JDK 21). The Maven wrapper is included.
- No external database required for local/dev — an in-memory H2 database is used. A PostgreSQL driver is
  bundled for production configuration.

## Build & test

```bash
cd tiny-url-creator/backend
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"   # ensure a supported JDK
./mvnw -B clean test      # unit tests (Surefire)
./mvnw -B verify          # unit + integration tests (Failsafe: *IT.java)
```

## Run

```bash
./mvnw spring-boot:run
# API available at http://localhost:8080
```

## Configuration (`src/main/resources/application.yml`)

| Key | Default | Purpose |
|-----|---------|---------|
| `app.base-url` | `http://localhost:8080` | Prefix used to build the returned `shortUrl`. |
| `app.self-hosts` | `[localhost, 127.0.0.1]` | Hosts rejected as shorten targets (prevents self-referential loops). |
| `app.rate-limit.window-seconds` | `60` | Fixed-window size for per-IP rate limiting. |
| `app.rate-limit.max-requests` | `20` | Max `POST /api/links` requests per IP per window. |

## API reference

### Create a short link

`POST /api/links`

Request body:

```json
{ "url": "https://example.com/very/long/path", "alias": "my-alias", "expiresAt": "2026-12-31T23:59:59Z" }
```

- `url` — required, `http`/`https`, ≤ 2048 chars, not a self-host.
- `alias` — optional, `^[A-Za-z0-9_-]{3,30}$`, not a reserved word.
- `expiresAt` — optional absolute UTC instant (ISO-8601). Omit for a link that never expires
  (ADR-010). Must be strictly in the future; a past/present value is rejected `400` (ADR-012).

Response `201 Created` (`expiresAt` is present only when the link has an expiry):

```json
{ "code": "abc1234", "shortUrl": "http://localhost:8080/abc1234", "originalUrl": "https://example.com/very/long/path", "expiresAt": "2026-12-31T23:59:59Z" }
```

Errors (`ErrorResponse { timestamp, status, error, message }`):

| Status | When |
|--------|------|
| `400 Bad Request` | Invalid URL or alias (validation / reserved word), or expiry not in the future. |
| `409 Conflict` | Custom alias already in use. |
| `429 Too Many Requests` | Per-IP rate limit exceeded. |

### Bulk-create short links

`POST /api/links/bulk`

Request body — 1 to 100 items (ADR-013), each item identical to the single-create body:

```json
{ "items": [
  { "url": "https://example.com/a" },
  { "url": "https://example.com/b", "alias": "b-alias", "expiresAt": "2026-12-31T23:59:59Z" }
] }
```

Best-effort partial success: valid items are created even if others fail. The response returns one
result per input item **in the same order**, each either a success or an error (ADR-013):

Response `200 OK`:

```json
{ "results": [
  { "index": 0, "code": "abc1234", "shortUrl": "http://localhost:8080/abc1234", "originalUrl": "https://example.com/a" },
  { "index": 1, "error": { "code": "ALIAS_TAKEN", "message": "Alias already in use" } }
] }
```

Per-item error `code` values: `INVALID_URL`, `INVALID_ALIAS`, `ALIAS_TAKEN`, `INVALID_EXPIRY`, `ERROR`.

| Status | When |
|--------|------|
| `400 Bad Request` | `items` empty or larger than 100 (batch-level `@Valid` `@Size`). |
| `429 Too Many Requests` | Insufficient rate budget for the whole batch — **nothing is created** (ADR-014). |

Rate limiting uses a shared N-token bucket: a bulk request consumes one token per item and is rejected
wholesale (429) if the per-IP budget cannot cover the entire batch.

> **Phase 3 — batched writes (ADR-017..021).** Bulk creation persists in two passes: it validates and
> assigns all codes in memory, resolves collisions with persisted rows in a **single**
> `SELECT ... WHERE code IN (...)`, then inserts survivors with **one** batched `saveAll` (a per-item
> fallback on a rare race preserves best-effort partial success). `ShortLink` ids come from the
> `short_link_seq` sequence and Hibernate JDBC batching is enabled (`jdbc.batch_size: 50`), so a
> 100-item batch issues a bounded number of statements instead of ~2 per item. The request/response
> contract is unchanged.
>
> **Operator note:** the `prod` profile runs `ddl-auto: validate` with no bundled migration tooling, so
> the `short_link_seq` sequence must exist in the schema before deploy (dev `update` and test
> `create-drop` create it automatically).

### Redirect

`GET /{code}` → `302 Found` with a `Location` header pointing at the original URL, or `404 Not Found`
(`CodeNotFoundException`) for an unknown **or expired** code. Expiry is evaluated lazily at resolve
time (ADR-011); an expired alias stays permanently reserved (ADR-015).

## Architecture (layered)

`controller` → `service` → `repository` → `entity`, with `dto` for all request/response bodies (JPA
entities are never exposed over REST). `CodeGenerator` produces 7-character Base62 codes using
`SecureRandom`. A shared `RateLimiter` bean (registered via `WebConfig`) provides per-IP fixed-window,
N-token limiting; `RateLimitFilter` consumes one token for `POST /api/links`, while the bulk endpoint
reserves one token per item up front. `LinkService` takes an injectable `Clock` so expiry boundaries
are deterministically testable. `GlobalExceptionHandler` maps domain exceptions (including
`InvalidExpiryException` → 400 and `RateLimitExceededException` → 429) to the consistent
`ErrorResponse` shape.

## Known limitations

- **RISK-004** (accepted): the rate limiter is in-memory and per-instance; a shared store is required
  before horizontal scaling.
- **RISK-012** (accepted): expired links are not reaped; rows are retained and the alias stays
  permanently reserved (ADR-015). A background cleanup job is deferred.
