# Salary Management

Web application for ACME's HR Manager to manage salary data for **10,000 employees across 7 countries** and answer questions about how the organisation pays people – replacing spreadsheets.

- **Live app:** <LIVE_URL>  *(free hosting: the first request after inactivity can take ~1 minute while the server wakes up)*
- **Video demo:** <VIDEO_URL>

## What the HR Manager can do

| Need | Where |
|---|---|
| Find employees – search by name, filter by country and job title, sort, page through results | **Employees** page |
| Add, edit and delete employees, with validation and clear error messages | **Employees** page → *Add employee* / *Edit* / *Delete* |
| Record why a salary changed and see an employee's full salary history | *Edit* (reason field) → *History* |
| See headcount and min / average / max salary per country | **Insights** page |
| See pay by job title within a country | **Insights** page → click a country |

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation |
| Database | SQLite (Hibernate community dialect) |
| Frontend | Angular 22 (standalone components, signals, zoneless), Angular Material |
| Tests | JUnit 5, AssertJ, Mockito, MockMvc (backend) · Vitest (frontend) |
| Build & deploy | Multi-stage Dockerfile → single container on Render |

## Project structure

```
backend/    Spring Boot application (API + serves the built Angular app)
frontend/   Angular application
docs/       Requirements, design, performance notes
AI_USAGE.md How AI tools were used, and what I accepted or changed
Dockerfile  Builds UI + API into one container
```

## Run locally

**Prerequisites:** JDK 21, Node.js 22+ (LTS), npm.

```bash
# 1. Backend (http://localhost:8080) – seeds 10,000 employees on first start
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# 2. Frontend (http://localhost:4200) – proxies /api to the backend
cd frontend
npm install
npm start
```

Open **http://localhost:4200**.

## Run the tests

```bash
cd backend && ./mvnw test        # Windows: mvnw.cmd test
cd frontend && npx ng test --watch=false
```

The backend suite includes a **performance test** that loads 10,000 employees and fails if any insight query takes 500 ms or more.

## API overview

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/employees?name=&country=&jobTitle=&page=&size=&sortBy=&direction=` | Search, filter, sort, paginate |
| GET / POST / PUT / DELETE | `/api/employees/{id}` | Read, create, update, delete |
| GET | `/api/employees/{id}/salary-history` | Salary revisions, newest first |
| GET | `/api/insights/countries` | Salary stats per country |
| GET | `/api/insights/countries/{country}` | Country stats + breakdown by job title |
| GET | `/api/insights/job-titles` | Job titles for filters |

Full details, including error responses: [docs/DESIGN.md](docs/DESIGN.md).

## Key decisions (short version)

- **Modular monolith, not microservices** – one HR persona and 10k rows; modules (`employee`, `insights`, `salaryhistory`, `seed`) keep boundaries clean so one could be extracted later.
- **Salaries stay in local currency; insights are per country** – averaging INR and USD together would be meaningless without live exchange rates.
- **Aggregations run in SQL** (`GROUP BY`), so the database returns a few summary rows instead of 10,000 employees.
- **Salary changes publish an event** (`SalaryChangedEvent`); salary history listens. New behaviour (approvals, notifications) becomes a new listener rather than an edit to the employee service.
- **SQLite** keeps setup and hosting simple; the code uses JPA, so moving to PostgreSQL is a configuration change.

More: [Requirements](docs/REQUIREMENTS.md) · [Design](docs/DESIGN.md) · [Performance](docs/PERFORMANCE.md) · [AI usage](AI_USAGE.md)

## Performance (10,000 employees)

| Operation | Time |
|---|---|
| Seed 10,000 employees on first start | 833 ms |
| Salary stats for all countries | 240 ms |
| One country by job title | 96 ms |

Details and scaling path: [docs/PERFORMANCE.md](docs/PERFORMANCE.md).

## Known limitations

- **Demo data resets on redeploy.** SQLite lives inside the container on free hosting; production would use a managed PostgreSQL.
- **No login yet.** Single HR persona in this version; role-based access is the first item on the roadmap.
- **No currency conversion** and no cross-country salary comparison (see requirements for reasoning).

## Roadmap

1. Role-based access (Spring Security + company SSO)
2. Approval workflow for salary changes above a threshold
3. Full audit log of every field change
4. Excel import / export to ease migration from spreadsheets

## Development approach

Built test-first: most features have a `test:` commit followed by a `feat:` commit – see the git history. How AI tools were used, and what I reviewed or changed, is documented in [AI_USAGE.md](AI_USAGE.md).
