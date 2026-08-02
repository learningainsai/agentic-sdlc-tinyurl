---
name: angular-design-reviewer
user-invocable: true
description: >
  Visually inspect a running Angular application and audit component code for
  Angular-specific conventions, accessibility, and UI/design issues. In this SDLC
  it is a construction-phase review skill applied to the frontend unit (UNIT-002).
allowed-tools: [read, edit]
---

# Angular Design Reviewer

Use this skill when you want to review a running Angular app for UI issues and
Angular best-practice violations in the same pass.

> **SDLC integration**: This is a **construction-phase** skill used during the `implementation`
> node (and again before `code-review`) for the **frontend unit (UNIT-002)**. Findings and fixes
> must respect the unit's target files (`tiny-url-creator/frontend/**`), preserve REQ-/US-/ADR-/UNIT-
> traceability, and be recorded in the unit's code-review artifact (CR-002).

## Prerequisites

- The target Angular app must be running (local dev server, staging, or
  production for read-only review).
- Browser automation must be available (screenshot capture, page navigation,
  DOM inspection).
- Source code must be accessible in the workspace to make fixes.

## Process

1. **Information Gathering** — Identify the running app's URL, the
   component(s) or page(s) in scope, and whether this is a fix pass or a
   read-only audit.
2. **Visual Inspection** — Screenshot the target page(s). Check for layout
   issues, spacing/alignment problems, responsive breakage, and visual
   inconsistency with the rest of the app.
3. **Angular Convention Check** — While reviewing the underlying component
   code, verify:
   - Standalone components used (not NgModule-based, unless the surrounding
     module already uses NgModules)
   - Angular Signals used for reactive state where applicable
     (`signal()`, `computed()`, `input()`/`output()`) rather than older
     decorator patterns, unless the codebase is pre-19
   - `strict` mode compliance — no untyped `any` without a justifying comment
   - Typed reactive forms (`FormGroup`, `FormControl`), not untyped forms
   - RxJS error handling present (`catchError`) on any HTTP-driven state
   - No component subscribing to an Observable without cleanup (missing
     `takeUntilDestroyed` or an async pipe in the template)
   - Accessibility: WCAG 2.1 basics — alt text, label associations, contrast,
     keyboard navigation
4. **Issue Fixing** — Fix identified issues directly in source, following:
   - Minimal changes: only what's needed to resolve the issue
   - Respect existing patterns in the surrounding code
   - Avoid unrelated breaking changes
   - Add a comment explaining non-obvious fixes
5. **Re-verification** — Reload/screenshot again (or wait for HMR) and
   confirm the issue is resolved before moving to the next one.

## Rules

- Do not restyle working components purely on personal preference — only fix
  genuine issues (broken layout, accessibility failures, convention
  violations), not stylistic taste.
- Do not silently convert a whole legacy NgModule-based codebase to
  standalone components in one pass — flag it as a larger, separate task.
- Every fix must be re-verified visually before being reported as resolved.
- If a visual issue and a convention violation both exist in the same
  component, fix both, but report them as two separate findings.

## Output Structure

Produce a completion report with:

- **Visual Issues Found & Fixed**
- **Angular Convention Issues Found & Fixed**
- **Remaining Concerns** (anything flagged but not auto-fixed, with why)

## Example

Input: "Review the user profile page."

Output should include: a screenshot-based note on any layout issue, a check
of whether the profile component uses signals vs. plain properties for
reactive fields, confirmation that any HTTP call in the component's service
has `catchError` handling, and an accessibility check on form labels if the
page includes an edit form.
