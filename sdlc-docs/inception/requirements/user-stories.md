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

## Notes / open questions

N/A — stories restate approved requirements; no open questions.
