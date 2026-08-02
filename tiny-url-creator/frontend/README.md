# TinyURL Frontend (UNIT-002)

Angular 17 standalone SPA providing the URL submission form for the TinyURL Phase 1 MVP. Traces to
REQ-007 and ADR-002.

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
3. Submit — the short URL appears with a **Copy** button.
4. Validation errors show inline under each field; API errors (e.g. alias taken) show as an alert.

## Structure

- `core/link.service.ts` — the only component-facing HTTP caller (ADR-002); maps backend
  `ErrorResponse.message` to a display message via `catchError`.
- `core/link.models.ts` — request/response/error interfaces mirroring the backend contract.
- `feature/shorten/` — standalone reactive-form component (signals for state, `takeUntilDestroyed`
  for cleanup, copy-to-clipboard, accessible inline errors).
