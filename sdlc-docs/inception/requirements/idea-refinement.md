# Idea Refinement — TinyURL URL Shortener

> Cumulative pre-implementation critique log. The **current run** (Phase 2) appears first; the prior
> Phase 1 MVP critique is preserved below for lineage. Gate path is fixed by the workflow graph.

---

## Run run-20260802T170000Z — Phase 3 (Performance improvement)

- **run_id**: run-20260802T170000Z
- **node_id**: requirements
- **skill**: idea-refiner
- **authored_by**: inception
- **review_mode**: pre-implementation critique (no code)
- **intake**: INTAKE-20260802T170000Z-performance-improvement
- **source**: User request (verbatim) — "I want to improve performance"

This report interrogates the request **before** any requirements are drafted or code is written.
The request as stated is a classic **untestable requirement**: it names no component, no metric, no
baseline, and no acceptance threshold. A verdict and numbered open questions appear at the end;
**these must be agreed with the human before `requirements.md` is drafted.**

### Ambiguities

1. **"performance"** — Undefined dimension. Could mean redirect latency, create latency, bulk
   throughput, cold-start time, frontend load/render time, DB query time, memory footprint, or
   build/CI time. Each implies a different change and a different measurement method.
2. **"improve"** — No baseline and no target. "Improve" is unverifiable without a current measured
   number and a goal (e.g., "redirect p95 from X ms to ≤ Y ms"). The Phase 1 design already asserts
   NFR targets (redirect p95 ≤ 100 ms, create p95 ≤ 300 ms) — is the complaint that these are being
   missed, or a request to tighten them?
3. **Trigger** — Is this reactive (an observed slowness / incident / profiling result) or proactive
   (pre-emptive hardening)? If reactive, what evidence exists (numbers, logs, load conditions)?

### Missing Edge Cases

1. **Load profile** — Target throughput (req/s), concurrency, and payload sizes are unstated. Bulk
   creation (max 100 items, Phase 2) and the in-memory per-IP rate limiter behave very differently
   under load; "performance" under 1 user vs. 1000 concurrent users is a different problem.
2. **Measurement environment** — Local H2 vs. PostgreSQL, single instance vs. replicas. The current
   rate limiter is in-memory/per-instance (RISK-004); any throughput work interacts with that.
3. **Regression guardrails** — Is a performance/load test harness expected as an acceptance artifact,
   or is this "best-effort tuning" with no measured gate?

### Unstated Assumptions

1. That there **is** a measurable problem — no profiling or benchmark data has been provided.
2. That the change is allowed to touch product source. Note: `AGENTS.md` forbids changing dependency
   versions, `application.yml`, or adding libraries without explicit approval — so caching layers
   (e.g., Redis/Caffeine), connection-pool tuning, or an APM agent would each need a separate green-light.
3. That scope is code-level, not infrastructure (JVM flags, container sizing, DB indexing/hardware).

### Scope Risks

1. **Unbounded scope** — Without a named target, this could sprawl across backend, frontend, DB, and
   CI. It must be narrowed to one measurable objective per run.
2. **Premature optimization** — Optimizing without a baseline risks complexity for no verifiable gain
   and could regress the clean layered architecture the standards require.
3. **New-dependency temptation** — Common perf fixes (caching libs, async frameworks) collide with the
   "no new library without flagging" rule; each needs an explicit high-impact decision.

### Open Questions

1. Which **component/flow** should get faster: redirect `GET /{code}`, create `POST /api/links`, bulk
   `POST /api/links/bulk`, the Angular frontend load, DB queries, or build/CI time?
2. What is the **current measured baseline** (a number, and how it was measured), and what is the
   **target** (e.g., redirect p95 ≤ 50 ms)?
3. Is this **reactive** (you observed slowness — please share the evidence) or **proactive** hardening?
4. What **load profile** must it hold under (concurrent users / requests-per-second / payload size)?
5. Which **environment** is authoritative for the measurement — local H2, or PostgreSQL on a
   representative instance/replica count?
6. Are you open to changes that need approval per `AGENTS.md` — e.g., **adding a cache library**,
   tuning the **connection pool / `application.yml`**, or adding an **index**/schema change — or must
   the fix stay within existing code and dependencies?
7. Is a **performance/load-test harness** expected as an acceptance artifact (a measured gate), or is
   best-effort tuning acceptable without an automated regression test?

### Recommendations

- Narrow to **one measurable objective** (component + baseline + target + load profile) before drafting
  requirements. Everything else stays out of scope for this run.
- Establish the **baseline first** (measure), so "improve" becomes a verifiable delta.
- Pre-agree the **guardrails**: whether new dependencies / config / schema changes are permitted, since
  the most effective perf levers here (caching, indexing, pool tuning) all trip `AGENTS.md` approval rules.

### Verdict

**7 open questions before implementation should start.** The request is currently untestable and
unscoped; `requirements.md` for run-20260802T170000Z must not be drafted until Q1–Q7 are answered and
a single measurable objective is agreed with the human.

---

## Run run-20260802T150051Z — Phase 2 (Expiry column + Bulk creation)

- **run_id**: run-20260802T150051Z
- **node_id**: requirements
- **skill**: idea-refiner
- **authored_by**: inception
- **review_mode**: pre-implementation critique (no code)
- **intake**: INTAKE-20260802T150051Z-expiry-and-bulk-creation
- **source**: User request (verbatim) — "I want to add the feature of having an additional column for
  having optional expiry date column. Also want to add bulk creation of short urls."

This report interrogates the two requested capabilities **before** any requirements are drafted or
code is written. Findings are grounded in the existing Phase 1 implementation (`ShortLink` entity,
`LinkService`, `LinkController`, `RedirectController`). A verdict and numbered open questions appear at
the end of this section; **these must be agreed with the human before `requirements.md` is drafted.**

### Ambiguities

1. **"optional expiry date column"** — Column vs. behavior conflated. An expiry *column* is a storage
   change; the meaningful requirement is *what happens when a link is expired*. Unspecified: does an
   expired code return **404** (as if never existed) or **410 Gone** (existed, now expired)? Should the
   UI expose the expiry, and should the create response echo it back?
2. **Expiry granularity & type** — "expiry date" vs. "date/time". Is the value a calendar **date**
   (expires at end of day, in which timezone?) or a precise **timestamp/instant** (UTC)? Phase 1 stores
   `createdAt` as `Instant` (UTC) — consistency suggests an absolute UTC instant.
3. **How expiry is supplied** — Absolute timestamp (e.g. `2026-12-31T23:59:59Z`) supplied by the
   client, or a relative **TTL/duration** (e.g. "7 days") the server converts to an absolute instant?
   These are different API contracts.
4. **Bulk creation shape** — "bulk creation" does not define the request/response contract. Is it an
   array of `{url, alias?, expiresAt?}` objects returning an array of results? Is it a new endpoint
   (`POST /api/links/bulk`) or an overload of the existing `POST /api/links`?
5. **Partial success semantics** — In a bulk request where some items are valid and some invalid
   (bad URL, taken alias, past expiry), is the outcome **all-or-nothing** (transactional, reject the
   whole batch) or **best-effort** (persist the good ones, report per-item errors)? This is the single
   biggest design decision and is completely unspecified.

### Missing Edge Cases

1. **Expiry in the past / present** — What if `expiresAt` is already in the past or equals "now" at
   creation time? Reject with 400, or accept a link that is born expired?
2. **Expiry = null** — Confirm null/absent means **never expires** (Phase 1 behavior preserved).
3. **Far-future / bogus dates** — Extreme values (year 9999), malformed date strings, non-ISO formats.
4. **Redirect on the boundary** — Race between the expiry instant and an in-flight redirect. Expiry is
   checked in `resolve()`; the check must be inclusive/exclusive-defined (`now >= expiresAt` ⇒ expired).
5. **Expired-code reuse** — Once a code/alias is expired, can that alias be **reclaimed** by a new
   create request, or is it permanently burned? Affects the uniqueness constraint and cleanup.
6. **Bulk size limits** — No max batch size ⇒ a single request could create thousands of links (DoS /
   memory / DB pressure). What is the maximum items per bulk request?
7. **Duplicate aliases *within* one bulk request** — Two items in the same batch requesting the same
   custom alias. Must be detected intra-batch, not only against the database.
8. **Bulk + rate limiting** — Phase 1 rate-limits per creation request per IP. Does a bulk request
   count as **1** request or **N**? Otherwise bulk trivially bypasses REQ-009 rate limiting.
9. **Empty bulk array** — `[]` or missing items ⇒ 400, or 200 with empty result?

### Unstated Assumptions

1. **Storage/migration** — Adding a column to `short_link` implies a schema migration. Assumed nullable
   `expires_at TIMESTAMP` so existing rows (never-expire) remain valid.
2. **Clock source** — Server wall-clock (UTC) is the authority for expiry evaluation.
3. **No background reaper** — Assumed expiry is enforced lazily at redirect time (filter on read), not
   by a scheduled deletion job, unless stated otherwise. Expired rows remain in the DB.
4. **Bulk auth** — Creation remains anonymous/public (Phase 1 A6); bulk does not introduce accounts.
5. **Frontend scope** — Unclear whether the Angular UI must add an expiry picker and a bulk-entry
   screen, or whether these are API-only for Phase 2.

### Scope Risks

1. **Expiry cleanup / analytics out of scope?** — Confirm no reaper job, no "expired links" listing,
   no metrics are required now.
2. **Bulk import from file (CSV) out of scope?** — "Bulk creation" could be interpreted as file upload;
   assumed JSON array only.
3. **Editing/extending expiry out of scope?** — No update endpoint to change an expiry after creation.
4. **Delete/deactivate explicitly excluded** — The withdrawn prior intake bundled deactivate; this run
   carries **only** expiry + bulk. Confirm delete stays out of scope.

### Open Questions (must be answered before requirements are drafted)

1. **Expired behavior:** return **404** or **410 Gone** for an expired code?
2. **Expiry type:** absolute UTC **timestamp** from the client, a **relative TTL** (e.g. days), or
   **both**?
3. **Past/`now` expiry at creation:** reject with 400, or allow?
4. **Alias reclaim:** may an expired alias be reused by a later create, or is it permanently reserved?
5. **Bulk contract:** new `POST /api/links/bulk` taking a JSON array, returning per-item results?
6. **Partial success:** **best-effort per-item** (recommended) or **all-or-nothing transactional**?
7. **Max batch size:** what limit (e.g. 100 items)?
8. **Bulk vs. rate limiting:** does one bulk request consume **N** rate-limit tokens (recommended) or 1?
9. **Frontend:** add an expiry picker and a bulk-entry UI in Phase 2, or API-only?

### Recommended defaults (proposed — pending human confirmation)

- **D1**: Expired code ⇒ **404** (consistent with Phase 1 "unknown code" and does not leak existence).
- **D2**: Expiry is an **absolute UTC timestamp** (`expiresAt`, ISO-8601 `Instant`); optional/nullable
  ⇒ null means never expires.
- **D3**: `expiresAt` **must be in the future** at creation; past/now ⇒ **400**.
- **D4**: An expired alias is **permanently reserved** (row retained; lazy expiry only, no reaper).
- **D5**: Bulk = **new** endpoint `POST /api/links/bulk` accepting a JSON array of
  `{url, alias?, expiresAt?}`.
- **D6**: **Best-effort** partial success — persist valid items, return HTTP **207-style** per-item
  results (each item: created or error with reason); the batch itself returns 200/201.
- **D7**: **Max 100** items per bulk request; over-limit ⇒ **400**.
- **D8**: A bulk request consumes **N** rate-limit tokens (one per item) so bulk cannot bypass REQ-009.
- **D9**: Frontend adds an **optional expiry picker** on the existing form; bulk is **API-only** for
  Phase 2 (revisit UI later) — unless you want the bulk UI now.

### Verdict

**NOT READY to draft requirements.** Nine open questions materially change the API contract, data
model, and failure semantics. Proceeding on assumptions would risk building the wrong behavior
(especially partial-success and rate-limit interaction). Awaiting human answers (accept the D1–D9
defaults or override each).

---

## Run run-20260801T232309Z — Phase 1 MVP

- **run_id**: run-20260801T232309Z
- **node_id**: requirements
- **skill**: idea-refiner
- **authored_by**: inception
- **review_mode**: pre-implementation critique (no code)
- **source**: User request — "Create a TinyURL URL shortener service. Phase 1 MVP: (1) Shorten a URL with and without a custom alias; (2) Redirect via short URL; (3) UI for the user to submit URLs."

This report critically interrogates the request before any requirements are approved. Findings are
grounded in the exact request text. A verdict and numbered open questions appear at the end.

## Ambiguities

1. **"Shorten a URL"** — The request does not state what constitutes a valid input URL. Schemes
   (`http`/`https` only? `ftp`? `mailto`?), maximum length, whether internationalized domain names
   (IDN/Punycode) are accepted, and whether the service must reject its own domain (to prevent
   redirect loops) are all undefined.
2. **"custom alias"** — The allowed alphabet, length bounds, case sensitivity, reserved words
   (e.g. `api`, `admin`, `health`), and collision behavior (reject vs. auto-suffix) are unspecified.
3. **Short code generation (no alias)** — Length, alphabet (Base62 vs. Base58), and whether codes
   must be non-sequential/unpredictable are not defined. This is a hidden architecture decision.
4. **"Redirect via short URL"** — The HTTP redirect status is unspecified (301 permanent vs. 302/307
   temporary). This materially affects analytics, caching, and the ability to change a target later.
5. **UI scope** — "UI for the user to submit URLs" does not say whether the UI also lists previously
   created links, supports copy-to-clipboard, or shows errors — only "submit" is explicit.

## Missing Edge Cases

1. **Duplicate original URL** — Should submitting the same long URL twice return the same short code
   or mint a new one? Affects storage and user expectations.
2. **Custom alias already taken** — Required behavior on collision (409 error with a clear message vs.
   silent fallback to a generated code) is undefined.
3. **Malformed / empty / whitespace-only input** — Rejection behavior and error contract undefined.
4. **Unknown short code on redirect** — Behavior for a non-existent code (404 page vs. redirect to
   home) is undefined.
5. **Extremely long URLs** — No maximum length is stated; unbounded input is a resource/DoS risk.
6. **Reserved-path collision** — A custom alias could shadow an application route (e.g. `/api`,
   `/health`). No reservation policy is stated.
7. **Concurrency** — Two simultaneous requests for the same custom alias, or a generated-code
   collision under load, need a defined resolution (unique constraint + retry).

## Unstated Assumptions

1. **Persistence** — Assumes a datastore survives restarts. AGENTS.md indicates PostgreSQL (H2 for
   local/dev); this should be stated explicitly rather than implied.
2. **No authentication in MVP** — The request never mentions users/accounts; assumed anonymous,
   public creation. This must be confirmed because it changes abuse-control needs.
3. **Single deployment / no custom domains** — Assumes one short-link domain, not per-tenant domains.
4. **Analytics out of scope** — Click counts/tracking are not requested; assumed Phase 2.
5. **Tech stack** — AGENTS.md establishes Spring Boot (Java 17) backend + Angular frontend; assumed
   to apply here rather than an unspecified stack.

## Scope Risks

1. **Abuse / safety** — A public, anonymous shortener is a known vector for phishing/malware
   distribution. No rate limiting, blocklist, or SSRF-safe validation is mentioned. At minimum,
   input validation and rate limiting should be in-scope for a safe MVP (security-standard applies).
2. **Expiration / deletion** — Not requested. Explicitly out of scope for Phase 1, but should be
   named so it is a deliberate decision, not an omission.
3. **Analytics & custom domains** — Attractive scope creep; must be fenced out of Phase 1 explicitly.

## Non-Functional Gaps

1. **Performance** — No latency target for redirect (the hot path) or creation. Redirect should be
   fast; a measurable target is needed.
2. **Availability** — No uptime expectation stated for the MVP.
3. **Security** — No statement on HTTPS-only, input sanitization, open-redirect/SSRF prevention, or
   rate limiting (all relevant to OWASP A01/A05 per the security standard).
4. **Observability** — No logging/metrics requirement for creation and redirect events.

## Open Questions

1. Which URL schemes are accepted on input (recommend: `http` and `https` only)?
2. What is the custom-alias policy — allowed characters, length bounds, and reserved words?
3. On a custom-alias collision, should the API reject with 409, or silently fall back to a generated
   code? (Recommend: reject with 409.)
4. What redirect status should be used — 301 (permanent) or 302/307 (temporary)? (Recommend: 302 to
   keep future flexibility and avoid aggressive browser caching.)
5. For a duplicate original URL with no alias, return the existing short code or always mint a new
   one? (Recommend: always mint a new code for MVP simplicity.)
6. Is creation anonymous/public in the MVP, or is authentication required? (Recommend: anonymous.)
7. Should the MVP include rate limiting and a maximum URL length as safety controls? (Recommend: yes
   — lightweight per-IP rate limit + max length.)
8. Should the submission UI also list the user's created links in the same session, or only show the
   single result after submission? (Recommend: show the single result + copy button for MVP.)

## Recommendations

- Adopt the recommended defaults above so requirements are testable and the MVP stays lean while
  remaining safe. Each becomes an explicit assumption/decision in `requirements.md`, subject to the
  human exit-gate approval.
- Keep expiration, deletion, analytics, accounts, and custom domains explicitly **out of scope** for
  Phase 1.
- Add lightweight security NFRs (scheme allowlist, max length, per-IP rate limit) — a public
  shortener without them is an abuse vector.

## Verdict

**8 open questions before implementation should start.** The idea is sound and buildable, but the
questions above (especially alias policy, collision behavior, and redirect semantics) must be
resolved or the recommended defaults confirmed before the requirements can be approved at the exit
gate.
