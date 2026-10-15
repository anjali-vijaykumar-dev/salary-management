# Performance

## Target
From the requirements: the employee list and salary insights must respond in **under 500 ms with 10,000 employees**.

## How it is measured
- **Automated test:** `InsightsPerformanceTest` loads 10,000 generated employees into SQLite, runs the insight queries, and **fails the build** if any query takes 500 ms or more. The requirement is checked on every test run, not measured once by hand.
- **Seeding:** timed by `EmployeeSeeder` and logged on application startup.
- **Environment:** developer laptop, Windows 11, JDK 21, SQLite (in-memory for tests, file-based for the app).
- Timings are **cold** – the first call after startup, including Hibernate compiling the query – so they are the worst case a user would see.

## Results (10,000 employees)

| Operation | Time | Target | Result |
|---|---|---|---|
| Seed 10,000 employees on first startup | 833 ms | – (one-time) | – |
| Salary stats for all countries (headcount, min, max, avg) | 240 ms | < 500 ms | Pass |
| One country broken down by job title | 96 ms | < 500 ms | Pass |

## Why it is fast

1. **Aggregation happens in the database.** Insights use SQL `GROUP BY` with `COUNT`, `MIN`, `MAX` and `AVG`. The database returns about 7 summary rows (one per country) instead of sending 10,000 employee rows to Java to loop over.
2. **Indexes on the filter and group-by columns.**
   - `idx_employee_country` – filtering and grouping by country
   - `idx_employee_country_job_title` – the job-title breakdown within a country
   - `idx_salary_revision_employee` – loading one employee's salary history
3. **Server-side pagination.** The employee list never returns all rows; page size is capped at 100, so no request can ask for the whole table.
4. **Read-only transactions** for all queries (`@Transactional(readOnly = true)`), which lets Hibernate skip dirty-checking.
5. **Stable paging.** A secondary sort by `id` keeps page boundaries consistent without extra queries.

## Known limits and trade-offs

| Limit | Impact today | What I would do at larger scale |
|---|---|---|
| SQLite allows one writer at a time | Fine for a few HR users | Move to PostgreSQL (config change only – JPA keeps the code database-independent) |
| Name search uses `LIKE '%text%'`, which cannot use an index | Fast at 10k rows | Full-text search (PostgreSQL `tsvector`, or a search engine) |
| `IDENTITY` ids stop Hibernate from batching inserts | Seeding 10k still takes < 1 s | `JdbcTemplate.batchUpdate` or sequence-based ids for bulk loads |
| Insights are recomputed on every request | 96–240 ms is acceptable | Cache results and evict on salary change, or keep a summary table updated by the `SalaryChangedEvent` |
| Timings are from one laptop | Indicative, not a benchmark | Load test (e.g. Gatling/JMeter) against the deployed app |

## Scaling path

| Employees | Change needed |
|---|---|
| 10k (now) | None |
| 100k – 1M | PostgreSQL; keep indexes and pagination |
| 1M+ with heavy reporting | Cache insights or maintain pre-aggregated summary tables |
| Many concurrent users | Run several stateless app instances behind a load balancer |
