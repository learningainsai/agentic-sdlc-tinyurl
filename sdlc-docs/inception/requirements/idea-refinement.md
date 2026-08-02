# Idea Refinement — TinyURL URL Shortener (Phase 1 MVP)

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
