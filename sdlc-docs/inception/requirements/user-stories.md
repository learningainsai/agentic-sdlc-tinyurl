<!-- template_id: user-story-template.md -->

# User Stories — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: user-story-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: requirements
- **status**: approved
- **note**: Authored to materialize the `user-story-template.md` artifact declared by the
  `requirements` node. These stories restate the already-approved REQ-001..REQ-010 in story form;
  no acceptance criteria or decisions are changed.

Roles: **Visitor** (anonymous end user who shortens/clicks links).

---

## US-001 — Shorten a URL without a custom alias (REQ-001)

> As a **Visitor**, I want **to paste a long URL and get a short link**, so that **I can share it
> easily**.

### Acceptance criteria
- [ ] Given a valid `http`/`https` URL, when I submit it without an alias, then I receive a unique
      7-char Base62 short code and the full short URL.

### Traceability
- Parent requirement(s): REQ-001 · Related units: UNIT-001

## US-002 — Shorten a URL with a custom alias (REQ-002)

> As a **Visitor**, I want **to choose my own alias for the short link**, so that **the link is
> memorable and branded**.

### Acceptance criteria
- [ ] Given a valid URL and an available alias matching `^[A-Za-z0-9_-]{3,30}$`, when I submit, then
      the short URL uses my alias.

### Traceability
- Parent requirement(s): REQ-002 · Related units: UNIT-001

## US-003 — Clear feedback on an unavailable/invalid alias (REQ-003)

> As a **Visitor**, I want **a clear error when my alias is taken or invalid**, so that **I can pick
> another one**.

### Acceptance criteria
- [ ] Given a taken alias, when I submit, then I get HTTP 409 with a clear message and no link is
      created.
- [ ] Given an alias failing the pattern/reserved-word rules, when I submit, then I get HTTP 400.

### Traceability
- Parent requirement(s): REQ-003 · Related units: UNIT-001

## US-004 — Reject invalid URLs (REQ-004)

> As a **Visitor**, I want **the service to reject bad input**, so that **only usable links are
> created**.

### Acceptance criteria
- [ ] Given empty/whitespace-only input, a URL over 2048 chars, or a non-`http`/`https` scheme, when
      I submit, then I get HTTP 400 and no link is created.

### Traceability
- Parent requirement(s): REQ-004 · Related units: UNIT-001

## US-005 — Redirect via short URL (REQ-005)

> As a **Visitor**, I want **clicking a short link to take me to the original page**, so that **the
> short link is useful**.

### Acceptance criteria
- [ ] Given an existing short code, when I request `GET /{code}`, then I receive `302 Found` with
      `Location` set to the original URL.

### Traceability
- Parent requirement(s): REQ-005 · Related units: UNIT-001

## US-006 — Helpful response for unknown short codes (REQ-006)

> As a **Visitor**, I want **a clear "not found" when a short link doesn't exist**, so that **I know
> the link is invalid**.

### Acceptance criteria
- [ ] Given a non-existent code, when I request it, then I receive HTTP 404 with a clear message.

### Traceability
- Parent requirement(s): REQ-006 · Related units: UNIT-001

## US-007 — Submit URLs from a web UI (REQ-007)

> As a **Visitor**, I want **a simple web form to shorten a URL**, so that **I don't need to use the
> API directly**.

### Acceptance criteria
- [ ] Given the web UI, when I enter a URL (and optional alias) and submit, then the short URL is
      shown with a copy-to-clipboard control.
- [ ] Given an API validation/collision error, when I submit, then the error is shown inline.

### Traceability
- Parent requirement(s): REQ-007 · Related units: UNIT-002

## US-008 — Unique short codes under load (REQ-008)

> As a **Visitor**, I want **every short link to resolve to exactly one destination**, so that **links
> are reliable**.

### Acceptance criteria
- [ ] Given concurrent creation requests, when codes/aliases are assigned, then a uniqueness
      constraint prevents duplicates and generated-code collisions trigger a retry.

### Traceability
- Parent requirement(s): REQ-008 · Related units: UNIT-001

## US-009 — Abuse protection via rate limiting (REQ-009)

> As a **service operator**, I want **creation requests rate-limited per IP**, so that **the service
> resists abuse**.

### Acceptance criteria
- [ ] Given repeated creations from one IP beyond the configured threshold in a window, when the
      limit is exceeded, then further creations receive HTTP 429; redirects are unaffected.

### Traceability
- Parent requirement(s): REQ-009 · Related units: UNIT-001

## US-010 — Prevent redirect loops / self-shortening (REQ-010)

> As a **service operator**, I want **the shortener to reject its own domain as a target**, so that
> **redirect loops are avoided**.

### Acceptance criteria
- [ ] Given a URL whose host is the shortener's own domain, when I submit, then I get HTTP 400.

### Traceability
- Parent requirement(s): REQ-010 · Related units: UNIT-001

---

# Phase 2 — Expiry + Bulk creation (run-20260802T150051Z)

Additive stories restating approved REQ-011..REQ-020 (defaults D1–D9 confirmed by the human).
New role: **Operator** (integrator/script creating many links via the API).

## US-011 — Set an optional expiry on a short link (REQ-011)

> As a **Visitor**, I want **to optionally set when my short link expires**, so that **it stops working
> after a date I choose**.

### Acceptance criteria
- [ ] Given a valid create request with an ISO-8601 UTC `expiresAt`, when I submit, then the link is
      stored with that expiry and the response echoes `expiresAt`.
- [ ] Given no `expiresAt`, when I submit, then the link never expires (Phase 1 behavior unchanged).

### Traceability
- Parent requirement(s): REQ-011 · Related units: UNIT-001

## US-012 — Reject an expiry that is not in the future (REQ-012)

> As a **Visitor**, I want **a clear error if I pick an expiry in the past**, so that **I don't create a
> link that is born expired**.

### Acceptance criteria
- [ ] Given `expiresAt` ≤ now (UTC), when I submit, then I get HTTP 400 and no link is created.

### Traceability
- Parent requirement(s): REQ-012 · Related units: UNIT-001

## US-013 — Expired links stop redirecting (REQ-013)

> As a **Visitor/Operator**, I want **an expired short link to stop working**, so that **it no longer
> sends people to the destination**.

### Acceptance criteria
- [ ] Given a link with `expiresAt` ≤ now, when `GET /{code}` is requested, then I receive HTTP 404
      (identical to an unknown code; existence is not leaked).
- [ ] Given `expiresAt` null or in the future, when requested, then redirect works per REQ-005.

### Traceability
- Parent requirement(s): REQ-013 · Related units: UNIT-001

## US-014 — Expired aliases are not reused (REQ-014)

> As an **Operator**, I want **an expired alias to stay reserved**, so that **an old link's alias can't
> silently point somewhere new**.

### Acceptance criteria
- [ ] Given an expired link, when a later create requests the same alias, then it is rejected (409) and
      the expired row is retained.

### Traceability
- Parent requirement(s): REQ-014 · Related units: UNIT-001

## US-015 — Create many links in one request (REQ-015)

> As an **Operator**, I want **to submit many URLs at once**, so that **I can shorten links in bulk
> without one request each**.

### Acceptance criteria
- [ ] Given `POST /api/links/bulk` with a JSON array of `{ url, alias?, expiresAt? }`, when submitted,
      then I receive an ordered array of per-item results.

### Traceability
- Parent requirement(s): REQ-015 · Related units: UNIT-001

## US-016 — Bulk succeeds partially with per-item results (REQ-016, REQ-018)

> As an **Operator**, I want **valid items to succeed even when some fail**, so that **one bad URL
> doesn't waste the whole batch**.

### Acceptance criteria
- [ ] Given a batch with valid and invalid items, when processed, then valid items are created and each
      result reports success (code + short URL + `expiresAt`) or an error (index + reason + code).
- [ ] Given two items requesting the same alias, when processed, then at most one succeeds and the
      other reports a taken-alias error.

### Traceability
- Parent requirement(s): REQ-016, REQ-018 · Related units: UNIT-001

## US-017 — Bulk requests are bounded (REQ-017)

> As a **service operator**, I want **bulk requests capped in size**, so that **the service resists
> abuse and overload**.

### Acceptance criteria
- [ ] Given a bulk request with 0 or >100 items, when submitted, then I get HTTP 400 and nothing is
      created; 1–100 items are accepted.

### Traceability
- Parent requirement(s): REQ-017 · Related units: UNIT-001

## US-018 — Bulk cannot bypass rate limiting (REQ-019)

> As a **service operator**, I want **bulk to consume rate-limit budget per item**, so that **bulk
> can't sidestep per-IP limits**.

### Acceptance criteria
- [ ] Given per-IP rate limiting, when a bulk request of N items is processed, then it consumes N
      tokens; when the budget is exhausted, remaining items receive a rate-limit error (or 429 per the
      architecture decision).

### Traceability
- Parent requirement(s): REQ-019 · Related units: UNIT-001

## US-019 — Choose an expiry from the web UI (REQ-020)

> As a **Visitor**, I want **an optional expiry picker on the form**, so that **I can set an expiry
> without using the API**.

### Acceptance criteria
- [ ] Given the submission form, when I optionally pick an expiry and submit, then it is sent as
      `expiresAt` and the created link's expiry is shown; leaving it blank creates a never-expiring
      link. Bulk creation is not exposed in the UI in Phase 2.

### Traceability
- Parent requirement(s): REQ-020 · Related units: UNIT-002

## Notes / open questions

Phase 1: N/A. Phase 2: open questions resolved by human approval "Accept all defaults" (D1–D9).
