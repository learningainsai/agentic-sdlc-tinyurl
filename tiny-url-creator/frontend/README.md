# TinyURL Frontend (UNIT-002)

Angular 17 standalone SPA providing the URL submission form for TinyURL. Traces to REQ-007, REQ-020
and ADR-002, ADR-016. Phase 2 adds an optional link-expiry picker.

## Prerequisites

- Node.js 18+ and npm. (Verified on Node 26 with the Angular 17 toolchain.)
- A Chrome/Chromium browser is required to run the Karma tests.

## Install

```bash
cd tiny-url-creator/frontend
npm install
```

## Run (dev)

```bash
npm start
# App served at http://localhost:4200
```

`proxy.conf.json` forwards `/api` → `http://localhost:8080`, so run the backend alongside the SPA.

## Test

```bash
# Interactive (uses your default Chrome):
npm test

# Headless CI run (TEST-008, TEST-009 and service/contract specs):
CHROME_BIN="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" npm run test:ci
```

## Build

```bash
npm run build   # output in dist/tiny-url-frontend
```

## Usage

1. Enter a long URL (`http://` or `https://`).
2. Optionally enter a custom alias (3–30 chars: letters, digits, `-`, `_`).
3. Optionally pick an expiry date/time. The `datetime-local` value is entered in your local time
   zone and converted to an absolute UTC instant before it is sent to the API (ADR-016). Leave it
   blank for a link that never expires.
4. Submit — the short URL appears with a **Copy** button; if the link has an expiry, it is shown
   beneath the short URL.
5. Validation errors show inline under each field; API errors (e.g. alias taken, expiry in the past)
   show as an alert.

## Structure

- `core/link.service.ts` — the only component-facing HTTP caller (ADR-002); maps backend
  `ErrorResponse.message` to a display message via `catchError`.
- `core/link.models.ts` — request/response/error interfaces mirroring the backend contract.
- `feature/shorten/` — standalone reactive-form component (signals for state, `takeUntilDestroyed`
  for cleanup, copy-to-clipboard, accessible inline errors).
