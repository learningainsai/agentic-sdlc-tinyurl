# TinyURL Backend (UNIT-001)

Spring Boot 3.3.4 / Java 17 REST API for the TinyURL Phase 1 MVP. Traces to REQ-001..006, REQ-008..010
and ADR-001, ADR-003..009.

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
{ "url": "https://example.com/very/long/path", "alias": "my-alias" }
```

- `url` — required, `http`/`https`, ≤ 2048 chars, not a self-host.
- `alias` — optional, `^[A-Za-z0-9_-]{3,30}$`, not a reserved word.

Response `201 Created`:

```json
{ "code": "abc1234", "shortUrl": "http://localhost:8080/abc1234", "originalUrl": "https://example.com/very/long/path" }
```

Errors (`ErrorResponse { timestamp, status, error, message }`):

| Status | When |
|--------|------|
| `400 Bad Request` | Invalid URL or alias (validation / reserved word). |
| `409 Conflict` | Custom alias already in use. |
| `429 Too Many Requests` | Per-IP rate limit exceeded. |

### Redirect

`GET /{code}` → `302 Found` with a `Location` header pointing at the original URL, or `404 Not Found`
(`CodeNotFoundException`) for an unknown code.

## Architecture (layered)

`controller` → `service` → `repository` → `entity`, with `dto` for all request/response bodies (JPA
entities are never exposed over REST). `CodeGenerator` produces 7-character Base62 codes using
`SecureRandom`. `RateLimitFilter` (registered via `WebConfig`) applies a per-IP fixed-window limit to
`POST /api/links` only. `GlobalExceptionHandler` maps domain exceptions to the consistent
`ErrorResponse` shape.

## Known limitations

- **RISK-004** (accepted): the rate limiter is in-memory and per-instance; a shared store is required
  before horizontal scaling.
