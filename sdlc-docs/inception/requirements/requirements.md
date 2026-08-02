<!-- template_id: requirements-template.md -->

# Requirements — TinyURL URL Shortener

- **template_id**: requirements-template.md
- **run_id**: run-20260801T232309Z (Phase 1, approved) · run-20260802T150051Z (Phase 2, approved) · run-20260802T170000Z (Phase 3, in draft)
- **node_id**: requirements
- **authored_by**: inception
- **status**: Phase 1 approved · Phase 2 approved · Phase 3 (§9) pending exit-gate approval

> This is a cumulative requirements document. **Phase 1 (REQ-001..REQ-010)** and **Phase 2
> (REQ-011..REQ-020)** are approved and implemented. **Phase 3 (REQ-021..REQ-025)** in §9 optimizes
> the database write pattern of bulk creation (intake INTAKE-20260802T170000Z-performance-improvement).

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

---

## 8. Phase 2 — Optional expiry column + Bulk creation (run-20260802T150051Z)

**Intake**: INTAKE-20260802T150051Z-expiry-and-bulk-creation · **Source**: user request +
`idea-refinement.md` Phase 2 section. All decisions below reflect the human-approved defaults
**D1–D9** (verbatim approval: _"Accept all defaults"_).

### 8.1 Scope

- **In scope**:
  - An **optional** expiry timestamp on a short link (nullable column; null = never expires).
  - Enforcement of expiry on redirect (expired links are treated as not found).
  - **Bulk creation** of many short links in a single API request, with per-item results.
  - An optional expiry picker on the existing submission UI.
- **Out of scope** (unchanged / explicitly deferred):
  - Deleting/deactivating links, or editing/extending an existing link's expiry after creation.
  - A background reaper/cleanup job for expired rows (expiry is enforced lazily on read).
  - A dedicated bulk-entry UI and CSV/file import (bulk is **API-only** in Phase 2).
  - Reclaiming an expired alias (an expired alias stays permanently reserved).
  - User accounts / analytics (still out of scope).

### 8.2 Functional requirements

| ID | Requirement | Priority | Acceptance criteria | Decision |
|----|-------------|----------|---------------------|----------|
| REQ-011 | Optional expiry on single create | must | Given a valid create request with an optional `expiresAt` (ISO-8601 absolute UTC instant), when submitted, then the link is stored with that expiry and the response echoes `expiresAt`. When `expiresAt` is absent/null, the link never expires (Phase 1 behavior preserved). | D2 |
| REQ-012 | Reject non-future expiry | must | Given `expiresAt` that is null → accept; given `expiresAt` ≤ current server time (UTC), when submitted, then the API returns HTTP 400 with a consistent `ErrorResponse` and no link is created. | D3 |
| REQ-013 | Expired link redirect returns 404 | must | Given a stored link whose `expiresAt` ≤ now, when `GET /{code}` is requested, then the service returns HTTP 404 (identical to an unknown code — existence is not leaked); given `expiresAt` is null or in the future, redirect behaves per REQ-005. Expiry check is `now >= expiresAt ⇒ expired`. | D1 |
| REQ-014 | Expired alias permanently reserved | must | Given an expired link, when a later create requests the same code/alias, then it is rejected (409 for alias / uniqueness retry for generated code); expired rows are retained (lazy expiry, no reaper) so the code/alias is never reused. | D4 |
| REQ-015 | Bulk creation endpoint | must | Given `POST /api/links/bulk` with a JSON array of `{ url, alias?, expiresAt? }` items, when submitted, then each item is created under the same rules as REQ-001/002/004/011/012 and the response is an ordered array of per-item results. | D5 |
| REQ-016 | Best-effort partial success | must | Given a bulk request where some items are valid and some invalid, when processed, then valid items are persisted and each result reports either success (code + short URL + echoed `expiresAt`) or an error (item index + reason + error code); one invalid item does not fail the whole batch. | D6 |
| REQ-017 | Bulk size bounds | must | Given a bulk request with 0 items or more than 100 items, when submitted, then the API returns HTTP 400 with a clear message and creates nothing; 1–100 items are accepted. | D7 |
| REQ-018 | Intra-batch alias collision | must | Given two items in the same batch requesting the same custom alias (or an alias that collides with an existing link), when processed, then at most one succeeds and the other(s) report a taken-alias error in their per-item result. | D6 |
| REQ-019 | Bulk honors rate limiting | should | Given per-IP creation rate limiting (REQ-009), when a bulk request of N items is processed, then it consumes N rate-limit tokens (one per item) so bulk cannot bypass the limit; if the remaining budget is exhausted mid-batch, remaining items receive a rate-limit error (or the request returns 429 per the design decided in architecture). | D8 |
| REQ-020 | Optional expiry picker in UI | should | Given the submission UI, when a Visitor optionally selects an expiry date/time, then it is sent as `expiresAt` on create and the created link's expiry is shown; leaving it blank creates a never-expiring link. Bulk creation is **not** exposed in the UI in Phase 2. | D9 |

### 8.3 Non-functional requirements (Phase 2)

- **Data model**: add a **nullable** `expires_at` timestamp (UTC `Instant`) column to `short_link`.
  Existing rows have `NULL` (never expires); the migration must be backward compatible.
- **Performance**: redirect remains the hot path (REQ-005 p95 ≤ 100 ms); the added expiry check is a
  single in-memory comparison on the already-loaded row — no extra query. Bulk create p95 scales
  roughly linearly with item count; target p95 ≤ 300 ms per item-equivalent under nominal local load.
- **Security** (security-standard, OWASP A05 / API abuse): bulk is a DoS amplification surface —
  enforce the max-100 cap (REQ-017) **before** processing and count N rate-limit tokens (REQ-019);
  validate every item's URL/alias/expiry with the same rules as single create; return generic
  `ErrorResponse` (no stack traces); do not leak link existence on expiry (REQ-013 → 404).
- **Compatibility**: `expiresAt` is serialized as an ISO-8601 UTC instant (e.g. `2026-12-31T23:59:59Z`).
  The single-create contract stays backward compatible (`expiresAt` optional).

### 8.4 Assumptions & constraints (Phase 2)

- **A9**: Expiry is an **absolute UTC instant**, optional/nullable; null = never expires (D2).
- **A10**: Expiry is enforced **lazily at redirect time**; no scheduled deletion of expired rows (D4).
- **A11**: Bulk is a **new endpoint** `POST /api/links/bulk`, JSON array in, array of results out (D5).
- **A12**: Bulk uses **best-effort partial success**, not a single transaction (D6).
- **A13**: Max **100** items per bulk request (D7).
- **A14**: A bulk request consumes **N** rate-limit tokens (D8).
- **A15**: Bulk creation stays **API-only** in Phase 2; only the expiry picker is added to the UI (D9).
- **Constraint**: stack fixed by `AGENTS.md`; no new third-party libraries without a flag.

### 8.5 Open questions (Phase 2)

**RESOLVED** — human approval (verbatim): _"Accept all defaults"_ confirms D1–D9. No open questions
remain. See `idea-refinement.md` Phase 2 section for the mapping of each default to a decision.

### 8.6 Traceability (Phase 2)

- Requirement IDs defined here: REQ-011..REQ-020.
- Upstream: user request (expiry + bulk) + `idea-refinement.md` (run-20260802T150051Z) + defaults D1–D9.
- Downstream: to be linked to ADR- (architecture-design Phase 2) and UNIT- (unit-decomposition).
- Existing units likely impacted: UNIT-001 (backend API) for REQ-011..REQ-019; UNIT-002 (frontend) for
  REQ-020.

---

## 9. Phase 3 — Bulk creation database-query optimization (run-20260802T170000Z)

**Intake**: INTAKE-20260802T170000Z-performance-improvement · **Source**: user request _"I want to
improve performance"_, narrowed by the human to _"the simple piece of updating the bulk urls on db
queries"_ · **Critique**: `idea-refinement.md` Phase 3 section.

### 9.1 Problem baseline (measured from current code)

The bulk path (`LinkService.createBulk`) processes items **one at a time**, and each item performs an
individual `existsByCode` **SELECT** followed by an individual, **non-batched** `save` **INSERT**. For
a batch of N new items this is roughly **2N separate DB statements** with no JDBC batching. This is the
inefficiency this run targets. The public bulk API contract and its best-effort semantics (REQ-015..
REQ-019) are **not** changing — only the persistence access pattern behind them.

### 9.2 Scope

- **In scope**: reduce the number of database round trips / statements incurred by
  `POST /api/links/bulk` for a batch of N items, behind the existing contract.
- **Out of scope**: any change to the single-create path (`POST /api/links`), the redirect path, the
  request/response schema, the 100-item cap, the N-token rate limiting, the frontend, and the data
  model (no new columns/tables). No caching layer, no async/queueing, no new third-party library.

### 9.3 Functional / quality requirements

| ID | Requirement | Priority | Acceptance criteria | Decision |
|----|-------------|----------|---------------------|----------|
| REQ-021 | Reduce bulk DB round trips | must | Given a bulk request of N valid new items, when processed, then the total number of DB statements is materially lower than the current per-item baseline (~2N individual statements): existence checks are consolidated and inserts are JDBC-batched, so insert round trips are ~⌈N/batch_size⌉ rather than N. | P1, P2 |
| REQ-022 | Preserve the bulk API contract | must | Given the same bulk request, when processed by the optimized path, then the HTTP response (ordered per-item results, success fields, and per-item error codes) is byte-for-byte equivalent to the current behavior for every mix of valid/invalid/duplicate items (REQ-015/016/017/018 still hold). | P3 |
| REQ-023 | Preserve best-effort partial success | must | Given a batch containing both valid and invalid items (bad URL, past expiry, taken/intra-batch-duplicate alias), when processed, then all valid items are persisted and each invalid item reports its per-item error; one failing item must not roll back or drop any valid item. | P4 |
| REQ-024 | Prove the improvement with a regression test | must | Given a representative batch, when executed under test, then a test asserts the reduced DB statement/round-trip count (e.g., via Hibernate `Statistics`) against the documented baseline, and fails if the per-item pattern regresses. | P6 |
| REQ-025 | Stay within stack + config guardrails | must | Given `AGENTS.md`, when implementing, then no new third-party library is added; enabling Hibernate JDBC batching in `application.yml` (`spring.jpa.properties.hibernate.jdbc.batch_size`, `order_inserts`) requires explicit human approval before that config is changed. | P5 |

### 9.4 Non-functional requirements (Phase 3)

- **Performance**: fewer DB round trips for bulk; wall-clock bulk-create latency should improve or hold
  under a representative batch (e.g., 100 items) on the authoritative datastore. No regression to the
  redirect or single-create hot paths.
- **Correctness/consistency**: identical externally-observable results to today (REQ-022/023); the
  optimization is purely an internal access-pattern change.
- **Security** (security-standard): unchanged surface — the 100-item cap and N-token rate limit still
  bound the batch; each item is still fully validated. No new inputs, endpoints, or data exposure.
- **Compatibility**: no schema change; no API change; backward compatible.

### 9.5 Assumptions & constraints — proposed defaults (P1–P6)

Recommended defaults, adopted **pending exit-gate confirmation** (they answer the still-open
`idea-refinement.md` Phase 3 questions):

- **P1** (Q1 scope): optimize **only** the bulk write path; single-create is untouched.
- **P2** (Q2 target): reduce statement count via (a) **consolidated existence checks** (batch lookup of
  the batch's explicit aliases instead of one `existsByCode` per item) and (b) **JDBC-batched inserts**
  (`saveAll` + Hibernate batch settings). Baseline = ~2N individual statements; target = a bounded
  number of existence queries + ~⌈N/batch_size⌉ insert round trips.
- **P3** (contract): the public bulk request/response, ordering, and per-item error codes are **frozen**.
- **P4** (semantics): **best-effort partial success is preserved** — valid items still commit even if
  others fail; failures are isolated per item.
- **P5** (Q6 guardrails): **no new library**. New repository query method(s) are allowed. Enabling
  Hibernate JDBC batching in `application.yml` is **flagged as requiring explicit approval** (AGENTS.md).
- **P6** (Q7 acceptance): a **measured regression test** (Hibernate `Statistics` statement count) is
  the acceptance gate, not best-effort tuning.

- **Environment note** (Q5): the authoritative measurement target is **PostgreSQL** (JDBC batching
  behavior differs from H2); tests may run on H2 but the statement-count assertion is the portable gate.

### 9.6 Open questions (Phase 3)

Proposed defaults P1–P6 above resolve `idea-refinement.md` Q1, Q2, Q5, Q6, Q7. The following still
want an explicit human decision at the exit gate:

1. **(Q6 / P5, REQ-025)** Do you approve changing `application.yml` to enable Hibernate JDBC batching
   (`hibernate.jdbc.batch_size`, `order_inserts`)? If **no**, the optimization is limited to
   consolidating the existence-check SELECTs (still a real reduction, but inserts stay per-row).
2. **(Q4 load profile)** Is optimizing for a **full 100-item batch** the right design point, or do you
   expect typically small batches (then the win is mostly the batched inserts on large batches)?

### 9.7 Traceability (Phase 3)

- Requirement IDs defined here: REQ-021..REQ-025.
- Upstream: user request (performance → bulk DB queries) + `idea-refinement.md` (run-20260802T170000Z)
  + defaults P1–P6.
- Downstream: to be linked to ADR- (architecture-design Phase 3) and UNIT- (unit-decomposition).
- Unit likely impacted: **UNIT-001** (backend) only — `LinkService.createBulk`, `LinkRepository`, and
  (pending Q1 approval) `application.yml`. No frontend change (UNIT-002 untouched).
