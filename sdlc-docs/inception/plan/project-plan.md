<!-- template_id: project-plan-template.md -->

# Project Plan — TinyURL URL Shortener (Phase 1 MVP)

- **template_id**: project-plan-template.md
- **run_id**: run-20260801T232309Z
- **node_id**: plan
- **authored_by**: inception
- **status**: draft
- **skill**: breakdown-plan
- **sources**: requirements.md (REQ-001..010), user-stories.md (US-001..010),
  architecture-design.md (ADR-001..009), unit-registry.yaml (UNIT-001/002)

## 1. Project overview (required)

- **Summary & business value**: Deliver an MVP URL shortener that lets anonymous users convert long
  URLs into short shareable links (with optional custom alias) and redirect via the short link,
  through a simple web UI. Value: faster sharing, memorable links, foundation for future analytics.
- **Success criteria / KPIs**:
  - 100% of REQ-001..REQ-010 acceptance criteria pass automated tests.
  - Redirect p95 ≤ 100 ms; creation p95 ≤ 300 ms (NFR).
  - Zero open high/critical security findings at release readiness.
- **Key milestones** (no timelines):
  - M1 Backend API functional (create + redirect + validation + rate limit).
  - M2 Frontend submission UI wired to the API.
  - M3 Tests green (unit + integration) and docs complete.
  - M4 Release-readiness review passed.
- **Risk assessment**:
  - RISK-002 (medium, accepted): public redirect abuse — mitigated by scheme allowlist, max length,
    self-host rejection, per-IP rate limiting.
  - RISK-004 (low): in-memory rate limiter is per-instance (single-instance MVP).
  - RISK-005 (low): frontend/backend integration verified in the testing node.

## 2. Work item hierarchy (required)

```mermaid
graph TD
    E[Epic EPIC-1: URL Shortener MVP] --> F1[Feature FEAT-1: Link Creation]
    E --> F2[Feature FEAT-2: Redirection]
    E --> F3[Feature FEAT-3: Submission UI]
    E --> F4[Feature FEAT-4: Safety and Abuse Controls]

    F1 --> S1[Story US-001: Shorten without alias]
    F1 --> S2[Story US-002: Shorten with alias]
    F1 --> S3[Story US-003: Alias error feedback]
    F1 --> S4[Story US-004: Reject invalid URLs]
    F1 --> EN1[Enabler EN-1: Spring Boot project + short_link schema]
    F1 --> EN2[Enabler EN-2: Base62 code generator + retry]

    F2 --> S5[Story US-005: Redirect 302]
    F2 --> S6[Story US-006: Unknown code 404]

    F3 --> S7[Story US-007: Submission UI + copy]
    F3 --> EN3[Enabler EN-3: Angular app + LinkService]

    F4 --> S8[Story US-008: Unique codes under load]
    F4 --> S9[Story US-009: Per-IP rate limit]
    F4 --> S10[Story US-010: Reject self-host]
    F4 --> EN4[Enabler EN-4: Rate-limit filter + ErrorResponse advice]

    S1 --> T1[Test TEST-001..007: Backend API tests]
    S7 --> T2[Test TEST-008..009: UI tests]
```

## 3. Issues breakdown (required)

### Epic — EPIC-1: URL Shortener MVP
- **Description**: Anonymous URL shortening + redirect + submission UI (Phase 1).
- **Acceptance criteria**: REQ-001..REQ-010 satisfied; NFRs met; release-readiness passed.
- **Features**: FEAT-1, FEAT-2, FEAT-3, FEAT-4.
- **Labels**: `epic`, `priority-critical`, `value-high` · **Estimate**: M (8–20 pts) · **Traces**: all REQ/ADR/UNIT.

### Feature — FEAT-1: Link Creation (UNIT-001)
- **Stories**: US-001, US-002, US-003, US-004 · **Enablers**: EN-1, EN-2
- **Acceptance**: create with/without alias; validation + collision errors correct.
- **Blocked by**: EN-1 · **Labels**: `feature`, `priority-high`, `value-high`, `backend` · **Estimate**: S
- **Traces**: REQ-001/002/003/004; ADR-003/004/006/007/009.

### Feature — FEAT-2: Redirection (UNIT-001)
- **Stories**: US-005, US-006 · **Acceptance**: 302 for known, 404 for unknown.
- **Blocked by**: EN-1 · **Labels**: `feature`, `priority-high`, `value-high`, `backend` · **Estimate**: XS
- **Traces**: REQ-005/006; ADR-005/009.

### Feature — FEAT-3: Submission UI (UNIT-002)
- **Stories**: US-007 · **Enablers**: EN-3 · **Acceptance**: form submits, shows short URL + copy, inline errors.
- **Blocked by**: none at build time (fixed API contract); integration verified in testing.
- **Labels**: `feature`, `priority-high`, `value-high`, `frontend` · **Estimate**: S · **Traces**: REQ-007; ADR-002.

### Feature — FEAT-4: Safety & Abuse Controls (UNIT-001)
- **Stories**: US-008, US-009, US-010 · **Enablers**: EN-4
- **Acceptance**: unique codes under concurrency; 429 on rate limit; 400 on self-host.
- **Labels**: `feature`, `priority-high`, `value-medium`, `backend`, `security` · **Estimate**: S
- **Traces**: REQ-008/009/010; ADR-004/007/008/009.

### User Stories (INVEST)
| Story | Statement (abbrev.) | AC source | Labels | Est |
|-------|---------------------|-----------|--------|-----|
| US-001 | Shorten without alias | REQ-001 | `user-story`,`backend`,`P1` | 2 |
| US-002 | Shorten with alias | REQ-002 | `user-story`,`backend`,`P1` | 2 |
| US-003 | Alias error feedback | REQ-003 | `user-story`,`backend`,`P1` | 2 |
| US-004 | Reject invalid URLs | REQ-004 | `user-story`,`backend`,`P1` | 2 |
| US-005 | Redirect 302 | REQ-005 | `user-story`,`backend`,`P0` | 1 |
| US-006 | Unknown code 404 | REQ-006 | `user-story`,`backend`,`P1` | 1 |
| US-007 | Submission UI + copy | REQ-007 | `user-story`,`frontend`,`P1` | 3 |
| US-008 | Unique codes under load | REQ-008 | `user-story`,`backend`,`P1` | 2 |
| US-009 | Per-IP rate limit | REQ-009 | `user-story`,`backend`,`P2` | 3 |
| US-010 | Reject self-host | REQ-010 | `user-story`,`backend`,`P2` | 1 |

### Technical Enablers
| Enabler | Description | Enables | Labels | Est |
|---------|-------------|---------|--------|-----|
| EN-1 | Spring Boot project, `short_link` entity + repo + H2/Postgres config | US-001..006,008 | `enabler`,`backend`,`database`,`P0` | 3 |
| EN-2 | Base62 `SecureRandom` code generator + collision retry | US-001,008 | `enabler`,`backend`,`P1` | 2 |
| EN-3 | Angular app scaffold + `LinkService` HTTP client | US-007 | `enabler`,`frontend`,`P1` | 3 |
| EN-4 | Per-IP rate-limit filter + `@RestControllerAdvice` `ErrorResponse` | US-003,004,009,010 | `enabler`,`backend`,`security`,`P1` | 3 |

### Tests
| Test | Covers | Labels |
|------|--------|--------|
| TEST-001..007 | Backend create/redirect/validation/rate-limit (UNIT-001) | `test`,`backend` |
| TEST-008..009 | UI submit + error display (UNIT-002) | `test`,`frontend` |

## 4. Priority & value matrix (required)

| Item | Priority | Value | Labels |
|------|----------|-------|--------|
| US-005 (redirect), EN-1 | P0 | High | `priority-critical`,`value-high` |
| FEAT-1/2/3, US-001..004/006/007/008, EN-2/3/4 | P1 | High | `priority-high`,`value-high` |
| US-009/010 (safety) | P2 | Medium | `priority-medium`,`value-medium` |

## 5. Estimation (required)

- **Total story points**: ~30 (stories 19 + enablers 11). Epic size **M**.
- Fibonacci per item as tabulated above.

## 6. Dependency management & critical path (required)

```mermaid
graph LR
    EN1[EN-1 Project+Schema] --> EN2[EN-2 Code gen]
    EN1 --> S5[US-005 Redirect]
    EN1 --> EN4[EN-4 Rate limit+Errors]
    EN2 --> S1[US-001 Create]
    EN4 --> S3[US-003 Alias errors]
    EN4 --> S9[US-009 Rate limit]
    EN3[EN-3 Angular+Service] --> S7[US-007 UI]
```

- **Critical path**: EN-1 → EN-2 → US-001/002 → US-003/004 → US-008 (backend core).
- **Parallel**: UNIT-002 (EN-3 → US-007) runs concurrently with UNIT-001 (disjoint files).
- **Prerequisite**: EN-1 blocks all backend stories.

## 7. Sprint / board configuration (required)

- **Board columns**: Backlog → Sprint Ready → In Progress → In Review → Testing → Done.
- **Custom fields**: Priority (P0–P3), Value, Component (frontend/backend/security/database),
  Estimate, Sprint, Assignee, Epic.
- **Capacity**: single delivery increment; 20% buffer; focus factor 70–80%.
- **Sprint 1 goal**: EN-1, EN-2, US-001..006 (backend create + redirect).
- **Sprint 2 goal**: EN-3, EN-4, US-007..010 (UI + safety) + tests/docs.
- **Automation (optional)**: `github-script` to open issues from this plan; PR open → In Review,
  merged → Done.

## 8. Traceability (required)

| Work item | REQ | US | ADR | UNIT |
|-----------|-----|----|-----|------|
| FEAT-1 | 001–004 | 001–004 | 003,004,006,007,009 | UNIT-001 |
| FEAT-2 | 005,006 | 005,006 | 005,009 | UNIT-001 |
| FEAT-3 | 007 | 007 | 002 | UNIT-002 |
| FEAT-4 | 008,009,010 | 008,009,010 | 004,007,008,009 | UNIT-001 |
| EN-1 | 001,005,008 | — | 003 | UNIT-001 |
| EN-2 | 001,008 | 001,008 | 004 | UNIT-001 |
| EN-3 | 007 | 007 | 002 | UNIT-002 |
| EN-4 | 003,004,009,010 | 003,004,009,010 | 007,008,009 | UNIT-001 |
| TEST-001..007 | 001–006,008,009,010 | — | — | UNIT-001 |
| TEST-008..009 | 007 | 007 | 002 | UNIT-002 |

## 9. Open questions (required)

N/A — the plan is derived entirely from approved inception artifacts; no open questions remain.

---

## 10. Phase 2 plan — Expiry + Bulk creation (run-20260802T150051Z)

- **skill**: breakdown-plan · **sources**: requirements.md (REQ-011..020), user-stories.md
  (US-011..019), architecture-design.md (ADR-010..016), unit-registry.yaml (UNIT-001/002 Phase 2).

### 10.1 Overview

- **Business value**: let creators time-box links (expiry) and shorten many URLs in one call (bulk),
  broadening the product beyond one-at-a-time anonymous shortening.
- **Success criteria**: 100% of REQ-011..REQ-020 acceptance criteria pass automated tests; redirect
  hot path stays p95 ≤ 100 ms; bulk bounded to 100 items; zero new high/critical security findings.
- **Milestones**: M5 backend expiry (column + lazy 404 + validation); M6 backend bulk endpoint +
  N-token limiter; M7 frontend expiry picker; M8 tests green + docs; M9 release-readiness passed.

### 10.2 Work item hierarchy

```mermaid
graph TD
    E2[Epic EPIC-2: Link lifecycle & bulk creation] --> F5[Feature FEAT-5: Link expiry]
    E2 --> F6[Feature FEAT-6: Bulk creation]
    E2 --> F7[Feature FEAT-7: Expiry in UI]

    F5 --> S11[US-011 Optional expiry]
    F5 --> S12[US-012 Reject non-future expiry]
    F5 --> S13[US-013 Expired stops redirecting]
    F5 --> S14[US-014 Expired alias reserved]
    F5 --> EN5[Enabler EN-5: expires_at column + migration + lazy expiry]

    F6 --> S15[US-015 Bulk create]
    F6 --> S16[US-016 Partial success + per-item results]
    F6 --> S17[US-017 Bulk size bounds]
    F6 --> S18[US-018 Bulk honors rate limit]
    F6 --> EN6[Enabler EN-6: bulk DTOs + endpoint]
    F6 --> EN7[Enabler EN-7: shared RateLimiter + N-token accounting]

    F7 --> S19[US-019 Expiry picker]
    F7 --> EN8[Enabler EN-8: datetime-local control in LinkService form]

    S11 --> T10[TEST-010..016 Backend expiry+bulk]
    S19 --> T17[TEST-017..018 UI expiry]
```

### 10.3 Issues breakdown

**Epic EPIC-2: Link lifecycle & bulk creation** — REQ-011..020 satisfied; NFRs met; release-readiness
passed. Labels: `epic`,`priority-high`,`value-high`. Traces: all Phase 2 REQ/ADR/UNIT.

| Feature | Unit | Stories | Enablers | Acceptance | Labels | Est |
|---------|------|---------|----------|------------|--------|-----|
| FEAT-5 Link expiry | UNIT-001 | US-011,012,013,014 | EN-5 | optional expiry stored/echoed; past/now→400; expired→404; expired alias reserved | `feature`,`backend`,`priority-high` | S |
| FEAT-6 Bulk creation | UNIT-001 | US-015,016,017,018 | EN-6, EN-7 | array in→ordered per-item results; partial success; 0/>100→400; N-token 429 | `feature`,`backend`,`security`,`priority-high` | M |
| FEAT-7 Expiry in UI | UNIT-002 | US-019 | EN-8 | optional picker→expiresAt; blank→never; created expiry shown | `feature`,`frontend`,`priority-medium` | S |

**Stories (INVEST)**

| Story | Statement (abbrev.) | AC source | Labels | Est |
|-------|---------------------|-----------|--------|-----|
| US-011 | Optional expiry on create | REQ-011 | `user-story`,`backend`,`P1` | 2 |
| US-012 | Reject non-future expiry | REQ-012 | `user-story`,`backend`,`P1` | 1 |
| US-013 | Expired stops redirecting (404) | REQ-013 | `user-story`,`backend`,`P1` | 2 |
| US-014 | Expired alias reserved | REQ-014 | `user-story`,`backend`,`P2` | 1 |
| US-015 | Bulk create | REQ-015 | `user-story`,`backend`,`P1` | 3 |
| US-016 | Partial success + per-item results | REQ-016,018 | `user-story`,`backend`,`P1` | 3 |
| US-017 | Bulk size bounds | REQ-017 | `user-story`,`backend`,`P1` | 1 |
| US-018 | Bulk honors rate limit (N tokens) | REQ-019 | `user-story`,`backend`,`security`,`P2` | 2 |
| US-019 | Expiry picker in UI | REQ-020 | `user-story`,`frontend`,`P2` | 2 |

**Technical enablers**

| Enabler | Description | Enables | Labels | Est |
|---------|-------------|---------|--------|-----|
| EN-5 | `expires_at` nullable column + backward-compatible migration + lazy expiry in `resolve()` + `InvalidExpiryException`→400 | US-011,012,013,014 | `enabler`,`backend`,`database`,`P1` | 3 |
| EN-6 | `BulkCreateRequest`/`BulkCreateResponse`/`BulkItemResult` DTOs + `POST /api/links/bulk` controller + best-effort per-item service loop | US-015,016,017 | `enabler`,`backend`,`P1` | 3 |
| EN-7 | Extract shared `RateLimiter` bean from `RateLimitFilter`; N-token bulk accounting; exclude bulk path from filter | US-018 | `enabler`,`backend`,`security`,`P2` | 2 |
| EN-8 | Optional `datetime-local` control in the shorten form; UTC ISO conversion via `LinkService` | US-019 | `enabler`,`frontend`,`P2` | 2 |

**Tests**

| Test | Covers | Labels |
|------|--------|--------|
| TEST-010..016 | Backend expiry (create/validate/404/reserved) + bulk (happy/partial/bounds/rate-limit) (UNIT-001) | `test`,`backend` |
| TEST-017..018 | UI expiry picker sends/omits `expiresAt`; shows expiry (UNIT-002) | `test`,`frontend` |

### 10.4 Estimation & critical path

- **Total Phase 2 points**: ~27 (stories 17 + enablers 10). Epic size **M**.
- **Critical path**: EN-5 → US-011/012/013 → US-014; EN-6 → US-015/016/017 → EN-7 → US-018.
- **Parallel**: UNIT-002 (EN-8 → US-019) runs concurrently with UNIT-001 (disjoint files).
- **Prerequisite**: EN-5 (schema) precedes expiry stories; EN-6 precedes bulk stories; EN-7 after EN-6.

### 10.5 Risk assessment (Phase 2)

- RISK-011 (medium, mitigated): bulk DoS — max-100 cap + N-token limit (EN-7).
- RISK-012 (low, accepted): expired rows retained (no reaper) — table growth deferred.
- RISK-013 (low, mitigated): partial-success requires per-item inspection — explicit error codes.

### 10.6 Traceability (Phase 2)

| Work item | REQ | US | ADR | UNIT |
|-----------|-----|----|-----|------|
| FEAT-5 | 011–014 | 011–014 | 010,011,012,015 | UNIT-001 |
| FEAT-6 | 015–019 | 015–018 | 013,014 | UNIT-001 |
| FEAT-7 | 020 | 019 | 016 | UNIT-002 |
| EN-5 | 011,012,013,014 | — | 010,011,012,015 | UNIT-001 |
| EN-6 | 015,016,017 | 015,016,017 | 013 | UNIT-001 |
| EN-7 | 019 | 018 | 014 | UNIT-001 |
| EN-8 | 020 | 019 | 016 | UNIT-002 |
| TEST-010..016 | 011–019 | 011–018 | 010–015 | UNIT-001 |
| TEST-017..018 | 020 | 019 | 016 | UNIT-002 |
