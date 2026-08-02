<!-- template_id: implementation-plan-template.md -->
<!-- v2 §3/§4 mandatory template. Code-generation plan step of the per-unit loop. -->

# Implementation Plan — UNIT-002

- **template_id**: implementation-plan-template.md
- **plan_id**: PLAN-002
- **unit_id**: UNIT-002
- **run_id**: run-20260801T232309Z
- **status**: approved

## 1. Approach (required)

Angular 17 standalone SPA providing the submission form (ADR-002; components never touch `HttpClient`):

1. **Bootstrap** — `main.ts` with `provideHttpClient()`; `AppComponent` shell hosting `<app-shorten>`.
2. **Core** — `core/link.models.ts` (`CreateLinkRequest`, `LinkResponse`, `ApiErrorResponse` mirroring
   the backend contract) and `core/link.service.ts` (`LinkService.createLink()` — the only HTTP caller,
   `catchError` mapping backend `ErrorResponse.message` to a display message).
3. **Feature** — `feature/shorten/shorten.component.ts` standalone reactive-form component using signals
   (`result`, `errorMessage`, `submitting`, `copied`), URL + optional alias validators matching the API
   contract, `takeUntilDestroyed(destroyRef)` for subscription cleanup, and copy-to-clipboard.
4. **Template/styles** — accessible form (labels, `aria-invalid`, `role="alert"`, `aria-live`), inline
   field + API error display, short-URL result with Copy button.
5. **Tests** — `link.service.spec.ts` (`HttpTestingController`) and `shorten.component.spec.ts`
   (TEST-008 renders short link; TEST-009 inline error on 409; plus invalid-form guard).
6. **Tooling** — `angular.json` (esbuild application builder), Karma headless config, `proxy.conf.json`
   forwarding `/api` → `http://localhost:8080`.

## 2. Target files (required)

`tiny-url-creator/frontend/**` — matches unit registry `target_files` (disjoint from UNIT-001 `backend/**`).

Key files: `package.json`, `angular.json`, `tsconfig*.json`, `karma.conf.js`, `proxy.conf.json`,
`src/{index.html,main.ts,styles.css}`, `src/app/app.component.ts`,
`src/app/core/{link.models.ts,link.service.ts}`,
`src/app/feature/shorten/{shorten.component.ts,shorten.component.html,shorten.component.css}`, plus specs.

## 3. Dependencies & sequencing (required)

- Depends on units: — (none; builds against the fixed API contract, runs in parallel with UNIT-001)
- External dependencies: Angular 17 toolchain (dev only), RxJS. No runtime third-party additions.

## 4. Risk & compensating actions (required)

- Clipboard API unavailable / permission denied: handled gracefully (`copied` stays false; no crash).
- No non-reversible side-effects (client-only). N/A for rollback.

## 5. Traceability (required)

- PLAN-002 ↔ UNIT-002 ↔ REQ-007 ↔ ADR-002 ↔ TEST-008, TEST-009.
