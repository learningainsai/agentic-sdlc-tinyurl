<!-- template_id: documentation-plan-template.md -->
<!-- v2 §4 mandatory template. Enforced by documentation-standard (§6). -->

# Documentation Plan — run-20260802T170000Z (TinyURL Phase 3 — bulk DB-query optimization)

- **template_id**: documentation-plan-template.md
- **run_id**: run-20260802T170000Z
- **node_id**: documentation
- **status**: passed
- **supersedes**: run-20260802T150051Z (Phase 2) documentation plan

## 0. Phase 3 documentation artifacts (required)

| DOC id | Audience | Artifact | Location | Traces to |
|--------|----------|----------|----------|-----------|
| DOC-005 | developer / operator | Backend README — Phase 3 note: `POST /api/links/bulk` now uses a two-pass batched write (single `findExistingCodes` SELECT + batched `saveAll`), `ShortLink` uses a `short_link_seq` sequence, and Hibernate JDBC batching (`jdbc.batch_size: 50`) is enabled. **Operator caveat**: under `prod` `ddl-auto: validate`, `short_link_seq` must exist in the schema before deploy (RISK-023). API request/response contract is unchanged. | tiny-url-creator/backend/README.md | REQ-021..025 · UNIT-001 · ADR-017..021 |

## 0a. Phase 3 freshness (required)

- DOC-005 documents the code as built and verified this run (`./mvnw -o verify` green: 31 unit/slice
  + 5 IT incl. TEST-019/020). The public API contract did not change, so no client-facing API doc
  update is required beyond the operator batching/sequence note.
- Regeneration trigger: update when `application.yml` batching keys, the id-generation strategy, or
  the bulk algorithm (ADR-017..021) change.

---

# Documentation Plan — run-20260802T150051Z (TinyURL Phase 2 — expiry + bulk creation)

- **template_id**: documentation-plan-template.md
- **run_id**: run-20260802T150051Z
- **node_id**: documentation
- **status**: passed (Phase 2 baseline)
- **supersedes**: run-20260801T232309Z (Phase 1 MVP) documentation plan

## 1. Documentation artifacts (required)

| DOC id | Audience | Artifact | Location | Traces to |
|--------|----------|----------|----------|-----------|
| DOC-003 | developer / operator | Backend README — Phase 2 API updates (`expiresAt` on create, `POST /api/links/bulk`, expiry-aware redirect, rate-limit N-token, `InvalidExpiryException`→400 / `RateLimitExceededException`→429) | tiny-url-creator/backend/README.md | REQ-011..019 · UNIT-001 · ADR-010..015 |
| DOC-004 | developer / user | Frontend README — optional expiry picker + `datetime-local`→UTC conversion usage | tiny-url-creator/frontend/README.md | REQ-020 · UNIT-002 · ADR-016 |
| DOC-001 | developer / operator | Backend README (Phase 1 baseline, updated in place) | tiny-url-creator/backend/README.md | REQ-001..006, REQ-008..010 · UNIT-001 |
| DOC-002 | developer / user | Frontend README (Phase 1 baseline, updated in place) | tiny-url-creator/frontend/README.md | REQ-007 · UNIT-002 |

## 2. Freshness (required)

- DOC-003 and DOC-004 were authored against the Phase 2 code as built and verified in this run
  (backend `./mvnw -o verify` green: 31 unit/slice + 3 IT; frontend `npm run build` + `npm run test:ci`
  9 specs green).
- Regeneration: update the READMEs when the API contract in
  `sdlc-docs/inception/architecture-design/architecture-design.md` (ADR-010..016) changes, or when
  `application.yml` config keys change. No generated (tool-emitted) docs are used, so no build step is
  required to keep them current.

## 3. Traceability (required)

- **Phase 2**: DOC-003 ↔ UNIT-001 ↔ REQ-011..019 ↔ ADR-010..015; DOC-004 ↔ UNIT-002 ↔ REQ-020 ↔ ADR-016.
- **Phase 1 baseline**: DOC-001 ↔ UNIT-001 ↔ REQ-001..006, REQ-008..010 ↔ ADR-001/003..009;
  DOC-002 ↔ UNIT-002 ↔ REQ-007 ↔ ADR-002.

## 4. Open gaps (required)

- Bulk creation is documented as an API only (no UI, ADR-016) — an end-user bulk workflow is deferred.
- An end-user landing/help page remains deferred (out of scope; the UI is self-explanatory with
  inline guidance).
