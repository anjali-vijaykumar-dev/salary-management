# AI Usage Log

How I used AI tools while building this project, what I accepted, and what I changed.

## 1. Requirements
- **Tool:** Claude (chat)
- **Asked:** Draft a one-page requirements document from the assessment brief (goal, scope, out of scope with reasons).
- **Reviewed:** Checked each decision against the brief – HR Manager persona, 10,000 employees, multiple countries.
- **What I changed:**
  - Moved **salary revision history** into scope – HR needs to know when and why a salary changed, and it is a small addition (one table).
  - Added **performance-tested analytics** – an automated test against 10,000 employees so the response-time target is proven, not assumed.
  - Turned the "left out" list into a **prioritised roadmap**: role-based access → approval workflow → full audit logging → Excel import.
- **Why:** Approval workflow and audit logging both depend on knowing *who* the user is, so role-based access must come first. Building them without auth in the time available would be half-done; documenting the order shows the path instead.

## 2. Seed data
- **Tool:** Claude (chat)
- **Asked:** A generator for 10,000 realistic employees and an idempotent seeder, test-first.
- **Reviewed / decided:** Fixed random seed for reproducible data; currency always matches the country; `count() > 0` check so restarts never duplicate data.
- **Verified:** 10,000 rows seeded in 833 ms; restart logs "skipping seed".

## 3. Backend features (CRUD, insights, salary history)
- **Tool:** Claude (chat)
- **Asked:** Test-first implementation of each module, one feature at a time.
- **Reviewed / decided:**
  - Kept a **modular monolith** – I considered microservices, but one HR persona and 10k rows do not justify network calls and separate deployments.
  - Salary changes publish an **event**; salary history listens, so the employee module does not depend on it.
  - Aggregations run in **SQL** (`GROUP BY`), not in Java.
  - Page size capped at 100 and sort fields whitelisted.
- **Verified:** Backend tests green; insight queries 96–240 ms on 10,000 employees.

## 4. Frontend (Angular)
- **Tool:** Claude (chat)
- **Context:** Frontend is not my background, so I relied on AI more here, as agreed with the recruiter.
- **Asked:** An Angular 22 UI (standalone components, signals, Angular Material) for the employee list, add/edit form, salary history and insights pages, built against my existing REST API.
- **Reviewed / decided:**
  - Kept paging, sorting and filtering on the server; the browser only ever holds one page.
  - `switchMap` so a slow older search cannot overwrite a newer one; 300 ms debounce on name search.
  - Currency is set from the country in the form, so HR cannot pair India with USD.
- **Problems I hit and fixed:** files copied into the wrong place left the default welcome page in `app.html`; I traced it with the compiler error and replaced the files. Missing `DecimalPipe` import when I added number formatting.
- **Verified:** Vitest unit tests for the API client and the app shell; manual check of every flow in the browser.

## 5. Deployment
- **Asked:** A way to deploy UI and API as one unit.
- **Decided:** Multi-stage Dockerfile (Angular build → Maven build with the UI inside → slim Java runtime) on Render; `SpaController` forwards Angular routes so a page refresh works.
- **Verified:** Live API returns 10,000 employees across 7 countries; refresh on `/insights` stays on the page.

## 6. Security (login and roles)
- **Why:** After deploying I noticed the public URL let anyone read and edit salary data. This was my call – login had been out of scope in the original plan.
- **Options considered:** basic login only; Google SSO only; Google SSO plus demo accounts. I chose **SSO + demo accounts**: SSO is the production pattern, and demo accounts guarantee reviewers can always sign in (with SSO only, a reviewer without an allowed Google account would be locked out).
- **Asked:** Spring Security config with form login + Google OIDC, roles HR_MANAGER / HR_VIEWER from an email allowlist, CSRF that works with Angular, secrets from environment variables; UI that hides edit actions for viewers.
- **Bug found and fixed (test-first):** after logout the browser showed a 404. I wrote a failing test that reproduced it, found that setting the logout URL explicitly disabled Spring's built-in logged-out page, removed that line, and the test passed.
- **Verified:** security tests for 401 / 403 / CSRF / role mapping; manual check of both demo roles locally and on Render.
