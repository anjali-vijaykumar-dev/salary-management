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

## 2. Backend features (CRUD, insights, salary history)
- Asked: test-first implementation of each module, one feature at a time
- Reviewed/decided: monolith over microservices; event for salary changes; aggregation in SQL; page-size cap
- Verified: 45 tests passing; insight queries 96–240 ms on 10k employees