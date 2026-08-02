<!-- template_id: requirements-template.md -->

# Requirements — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: requirements-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: requirements
- **authored_by**: inception
- **status**: approved

## 1. Context (required)

Users need a way to convert long, unwieldy URLs into short, shareable links. This is Phase 1 (MVP) of
a TinyURL-style URL shortener. The MVP delivers the smallest safe, end-to-end slice: create a short
link (optionally with a custom alias), follow a short link to its destination, and a simple web UI to
submit URLs.

- **Problem**: Long URLs are hard to share, remember, and embed.
- **Primary user**: Anonymous public visitor who wants to shorten and share a link.
- **Stakeholders**: End users (link creators and clickers), the product owner, the delivery team.
- **Tech context** (per `AGENTS.md`): Spring Boot (Java 17) REST API backend + Angular SPA frontend;
  PostgreSQL for persistence (H2 for local/dev).

## 2. Scope (required)

- **In scope**:
  - Create a short link from a long URL, **without** a custom alias (system-generated short code).
  - Create a short link **with** a user-supplied custom alias.
  - Redirect from a short link to its original destination URL.
  - A web UI to submit a URL (with optional custom alias) and display the resulting short link.
  - Baseline safety controls: input validation (scheme allowlist, max length) and per-IP rate
    limiting on creation.
- **Out of scope** (deferred to later phases):
  - User accounts / authentication / ownership of links.
  - Link expiration, editing, and deletion.
  - Click analytics / tracking / dashboards.
  - Custom / per-tenant short-link domains.
  - QR codes, link previews, and bulk import.

## 3. Functional requirements (required)

Each requirement carries a `REQ-` id (v2 §9). Priorities use MoSCoW.

| ID | Requirement | Priority | Acceptance criteria |
|----|-------------|----------|---------------------|
| REQ-001 | Shorten a URL without a custom alias | must | Given a valid `http`/`https` URL, when the user submits it without an alias, then the system persists it and returns a unique short code (Base62, 7 chars) and the full short URL. |
| REQ-002 | Shorten a URL with a custom alias | must | Given a valid URL and an available alias matching `^[A-Za-z0-9_-]{3,30}$`, when submitted, then the system persists the mapping and returns the short URL using that alias. |
| REQ-003 | Reject an unavailable/invalid custom alias | must | Given an alias that is already taken, reserved, or fails the pattern, when submitted, then the API returns HTTP 409 (taken) or 400 (invalid) with a consistent `ErrorResponse` body and no link is created. |
| REQ-004 | Validate the input URL | must | Given input that is empty, whitespace-only, exceeds 2048 chars, or uses a scheme other than `http`/`https`, when submitted, then the API returns HTTP 400 with a clear validation message and no link is created. |
| REQ-005 | Redirect via short URL | must | Given an existing short code, when a client requests `GET /{code}`, then the service responds `302 Found` with `Location` set to the original URL. |
| REQ-006 | Handle unknown short code | must | Given a short code that does not exist, when requested, then the service returns HTTP 404 with a clear "not found" response. |
| REQ-007 | Submission UI | must | Given the web UI, when a user enters a URL (and optionally an alias) and submits, then the resulting short URL is displayed with a copy-to-clipboard control; validation/collision errors are shown inline. |
| REQ-008 | Guarantee short-code uniqueness | must | Given concurrent creation requests, when codes/aliases are assigned, then a uniqueness constraint prevents duplicates; a generated-code collision triggers a retry so every stored code is unique. |
| REQ-009 | Rate-limit creation | should | Given repeated creation requests from one client IP, when a configurable per-IP threshold is exceeded within a time window, then further creation requests receive HTTP 429 until the window resets. Redirect requests are not rate-limited. |
| REQ-010 | Prevent redirect loops / self-shortening | should | Given a URL whose host is the shortener's own domain, when submitted, then the API rejects it with HTTP 400. |

## 4. Non-functional requirements (required)

- **Performance**: Redirect (`GET /{code}`) is the hot path — target p95 latency ≤ 100 ms server-side
  under nominal local/dev load. Creation target p95 ≤ 300 ms.
- **Security** (security-standard, OWASP A01/A05): accept only `http`/`https` schemes; enforce max URL
  length (2048); parameterized persistence (no SQL string concatenation); do not follow or fetch the
  target URL server-side (avoid SSRF); return a generic `ErrorResponse` without stack traces;
  per-IP rate limiting on creation; validate/sanitize custom aliases against an allowlist pattern and
  a reserved-word list (`api`, `admin`, `health`, `actuator`, `assets`).
- **Availability**: Best-effort single-instance for MVP; no formal SLA. State persists across
  restarts via the datastore.
- **Observability**: Structured logs for creation and redirect events (short code, outcome) with **no
  logging of PII or secrets**; basic error logging with correlation where available.
- **Compatibility**: REST/JSON API; short codes are URL-safe (Base62).

## 5. Assumptions & constraints (required)

Recommended defaults from `idea-refinement.md`, adopted pending exit-gate confirmation:

- **A1**: Only `http` and `https` input schemes are accepted (REQ-004).
- **A2**: Custom alias pattern `^[A-Za-z0-9_-]{3,30}$`, case-sensitive, with reserved-word exclusions
  (REQ-002/003).
- **A3**: On custom-alias collision the API **rejects with 409** (no silent fallback) (REQ-003).
- **A4**: Redirect uses **HTTP 302 (Found)** to preserve future flexibility and avoid aggressive
  browser caching (REQ-005).
- **A5**: A duplicate original URL **without** an alias always mints a **new** short code (MVP
  simplicity) (REQ-001).
- **A6**: Link creation is **anonymous/public** in the MVP — no authentication (scope §2).
- **A7**: Baseline safety controls (max length + per-IP rate limit) are **in scope** (REQ-004/009).
- **A8**: The submission UI shows the single result + copy control (no per-session link history)
  (REQ-007).
- **Constraint**: Stack is fixed by `AGENTS.md` (Spring Boot + Angular + PostgreSQL/H2); no new
  third-party libraries without a flag.

## 6. Open questions (required)

**RESOLVED** at the requirements exit gate (run-20260801T232309Z). Human decision (verbatim):
_"Reviewed default recomendations and approve"_ — all 8 recommended defaults (A1–A8) are confirmed.
No open questions remain.

The following mirror `idea-refinement.md`; each default below is now an approved decision:

1. Accepted input schemes — confirmed: `http`/`https` only (A1).
2. Custom-alias policy (charset/length/reserved) — confirmed: `^[A-Za-z0-9_-]{3,30}$` + reserved words
   (A2).
3. Alias-collision behavior — confirmed: reject with **409** (A3).
4. Redirect status — confirmed: **302** (A4).
5. Duplicate original URL (no alias) — confirmed: mint a **new** code (A5).
6. Anonymous creation vs. auth — confirmed: **anonymous** (A6).
7. Rate limiting + max URL length in MVP — confirmed: **yes** (A7).
8. UI shows single result vs. session link list — confirmed: **single result + copy** (A8).

## 7. Traceability (required)

- Requirement IDs defined here: REQ-001, REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-007,
  REQ-008, REQ-009, REQ-010.
- Upstream source: user request (Phase 1 MVP) + `idea-refinement.md` (run-20260801T232309Z).
- Downstream: to be linked to ADR- (architecture-design) and UNIT- (unit-decomposition).
