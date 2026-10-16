# Salary Management – Requirements

## Goal
Replace ACME's Excel-based salary tracking with a web app that lets the HR Manager **manage salary data for ~10,000 employees across countries** and **answer questions about how the org pays people** in a few clicks.

## User persona
**HR Manager** – owns salary data for the whole org, not technical, today works in spreadsheets. Needs to find, update and compare salaries quickly.

## Questions the HR Manager must be able to answer
- What is the minimum, maximum and average salary in a given country?
- What is the average salary for a given job title in a given country?
- How many employees do we have in each country?
- Who are the employees in a given country / job title, and what are they paid?

## In scope (features)
1. **Employee directory**
   - Paginated list of employees (server-side paging – 10k rows must not be loaded at once)
   - Search by name; filter by country and job title; sort by salary
2. **Manage employees**
   - Add, edit and delete an employee
   - Fields: full name, email (unique), job title, department, country, currency, annual salary, hire date
   - Validation: required fields, salary > 0, valid email, no duplicate email
3. **Salary insights**
   - Per country: headcount, min / max / average salary
   - Per job title within a country: headcount and average salary
   - Org overview: headcount by country
4. **Salary revision history**
   - Every salary change is recorded: old salary, new salary, reason, date of change
   - HR can view the full salary history of an employee
5. **Seed data** – script that loads 10,000 realistic employees across multiple countries and job titles
6. **Performance-tested analytics** – automated test that runs the insight queries against 10,000 employees and asserts they stay under the response-time target; results documented in `docs/PERFORMANCE.md`
7. **Login and roles** *(added after deployment – see change log below)*
   - Sign in with **Google SSO**, or with demo username/password accounts for reviewers
   - **HR Manager** can view and change salary data; **HR Viewer** can only view; anyone else who signs in sees "No access"
   - Google users get a role from an email allowlist; all secrets come from environment variables

## Change log
- **Login and roles moved into scope.** The original plan left login out for a single-persona prototype. Once the app was deployed on a public URL, salary data – which is sensitive – was readable and editable by anyone with the link, so basic role-based access became a must-have rather than a roadmap item.

## Key decisions
- **Salaries stay in local currency; insights are always per country.** Comparing salaries across countries needs exchange rates, which change daily. Mixing currencies would give misleading averages, so cross-country salary comparison is out of scope.
- **Aggregations run in the database (SQL GROUP BY), not in Java** – fast at 10k rows and keeps working as data grows.
- **Single deployable app** – Spring Boot serves the Angular build; one URL, no CORS, simple deployment.
- **SQLite** – zero-setup relational DB, enough for a single HR user and 10k rows.

## Deliberately left out (and why)
| Left out | Why |
|---|---|
| Currency conversion | Needs a live FX source; per-country insights answer the core questions without it |
| Payroll, tax, net pay | Different problem (payroll processing), country-specific rules |
| Excel import/export | Messy-data validation and per-row error reporting make it a large feature; seed script covers initial data for now |
| Charts / dashboards | Tables answer the questions; charts are polish, not core |

## Prioritised roadmap (next, in order)
1. **Company identity provider** – point SSO at ACME's own identity provider and take roles from directory groups instead of an email allowlist; remove the demo accounts.
2. **Approval workflow for salary changes** – a revision above a threshold needs approval by a second HR Manager before it takes effect. Roles now exist, so this is unblocked.
3. **Full audit logging** – who changed which field and when, for every entity. Salary changes are already covered by revision history; with a signed-in user, each entry can now record *who* made the change.
4. **Excel import/export** – HR's current data lives in Excel, so this eases migration.

## Non-functional requirements
- Employee list and insights respond in < 500 ms with 10,000 employees
- Indexes on `country` and `job_title` (the main filter and group-by columns)
- Unit tests that are fast, deterministic and cover the core salary logic and APIs

## Success criteria
The HR Manager can answer "What is the average salary of a Software Engineer in India?" in under 3 clicks, and can add or correct an employee's salary in under a minute.
