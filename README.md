# CalorieCalculator Backend

REST API for the CalorieCalculator application. It stores the food catalog, daily food records, and body-weight history in PostgreSQL and calculates calories from protein, carbohydrates, and fat using the 4-4-9 formula.

Project handoff, iteration history, and iteration plans are indexed in [`doc/README.md`](doc/README.md).

## Technology stack

- Java 21
- Spring Boot 4.0.6
- Spring MVC
- MyBatis 4.0.1
- PostgreSQL
- Maven Wrapper
- Lombok

## Features

- List, search, and paginate foods
- Calculate nutrition for an arbitrary food weight
- Create, update, list, and delete custom foods
- Add, update, delete, and summarize daily food records
- Navigate records by date, backfill historical days, and plan up to seven days ahead
- Return natural-month calendar metadata, selectable bounds, and recorded-date markers
- Record, edit, delete, and carry forward one body-weight entry per date
- Return rolling body-weight trend points with real-record markers
- Record six independently optional body-circumference values per date
- Carry each circumference forward independently and return selectable single-measurement trends
- Import the bundled Chinese food composition JSON files
- Configure the database and server port through environment variables

## Prerequisites

- JDK 21 or newer
- PostgreSQL 14 or newer

Maven does not need to be installed globally because the repository includes Maven Wrapper.

## Local database setup

Create the database with PostgreSQL tools:

```sql
CREATE DATABASE calorie_calculator;
```

Then initialize it from the backend directory:

```powershell
psql -U postgres -d calorie_calculator -f src/main/resources/sql/schema.sql
```

Warning: `schema.sql` drops and recreates `food_items`. Run it only for initial setup or when you intentionally want to reset the food catalog. Automatic SQL initialization is disabled by default to protect local data.

For an existing database, apply the non-destructive body-weight migration instead of rerunning `schema.sql`:

```powershell
psql -U postgres -d calorie_calculator -f src/main/resources/sql/migrations/2026-07-22-create-body-weight-records.sql
```
Apply the circumference migration in the same non-destructive way:

```powershell
psql -U postgres -d calorie_calculator -f src/main/resources/sql/migrations/2026-07-27-create-body-circumference-records.sql
```

The default connection settings are:

| Setting | Default | Environment variable |
| --- | --- | --- |
| Host | `localhost` | `PGHOST` |
| Port | `5432` | `PGPORT` |
| Database | `calorie_calculator` | `PGDATABASE` |
| User | `postgres` | `PGUSER` |
| Password | `123456` | `PGPASSWORD` |
| HTTP port | `8080` | `PORT` |
| Spring SQL initialization | `never` | `SQL_INIT_MODE` |

Example for the current PowerShell session:

```powershell
$env:PGHOST = "localhost"
$env:PGPORT = "5432"
$env:PGDATABASE = "calorie_calculator"
$env:PGUSER = "postgres"
$env:PGPASSWORD = "your-postgres-password"
```

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080/api`.

Quick check after startup:

```powershell
Invoke-RestMethod "http://localhost:8080/api/foods/page?page=1&size=10"
```

## Build and test

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Tests that start the Spring context require a reachable PostgreSQL database unless a test-specific datasource is configured.

The current suite contains 14 tests, including eight body-weight service tests covering validation, effective-record lookup, trend behavior, update, and deletion.

## API overview

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/foods` | List all foods |
| GET | `/api/foods/page?page=1&size=10` | List foods by page |
| GET | `/api/foods/search?keyword=apple` | Search Chinese food names |
| GET | `/api/foods/{id}/calculate?weight=100` | Calculate nutrition by weight |
| GET | `/api/foods/custom` | List custom foods |
| POST | `/api/foods/custom` | Create a custom food |
| PUT | `/api/foods/custom/{id}` | Update a custom food |
| DELETE | `/api/foods/custom/{id}` | Delete a custom food |
| POST | `/api/records` | Add a daily record |
| GET | `/api/records/{yyyy-MM-dd}` | Get a daily summary |
| GET | `/api/records/calendar?month=yyyy-MM` | Get selectable bounds and recorded dates for a natural month |
| GET | `/api/weights/{yyyy-MM-dd}` | Get the selected date body weight or its carried-forward source |
| PUT | `/api/weights/{yyyy-MM-dd}` | Create or replace that date body-weight record |
| PUT | `/api/weights/records/{id}` | Update a real body-weight record |
| DELETE | `/api/weights/records/{id}` | Delete a real body-weight record |
| GET | `/api/weights/trend?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd` | Get up to 365 daily trend points |
| GET | `/api/circumferences/{yyyy-MM-dd}` | Get six effective circumference values and their source dates |
| PUT | `/api/circumferences/{yyyy-MM-dd}` | Create, update, or clear sparse selected-date circumference values |
| DELETE | `/api/circumferences/records/{id}` | Delete a selected-date circumference record |
| GET | `/api/circumferences/trend?type=waist&startDate=yyyy-MM-dd&endDate=yyyy-MM-dd` | Get one circumference measurement's daily trend |
| PUT | `/api/records/{id}` | Update record weight |
| DELETE | `/api/records/{id}` | Delete a record |
| GET | `/api/import/json` | Import bundled food JSON files |

Example daily-record request:

```json
{
  "date": "2026-07-22",
  "foodId": 1,
  "weight": 150
}
```

The JSON import endpoint is intended for controlled initialization. Calling it repeatedly can insert duplicate foods.

### Record date rules

- The earliest selectable date is the later of the first recorded date and one year before the server's current date.
- When there are no records, or the first record is a future plan, today remains selectable.
- The latest selectable date is seven days after the server's current date.
- `POST /api/records` rejects dates outside this range with HTTP 400.
- `GET /api/records/calendar` returns `minDate`, `maxDate`, and the distinct `recordedDates` within the requested month.

Example calendar response:

```json
{
  "minDate": "2026-07-21",
  "maxDate": "2026-07-29",
  "recordedDates": ["2026-07-21"]
}
```

## Project structure

```text
src/main/java/.../
  controller/   REST controllers
  dto/          API response models
  entity/       Persistent food, daily-record, body-weight, and circumference entities
  mapper/       MyBatis SQL mappers
  service/      Business and nutrition logic
src/main/resources/
  application.properties
  data/foods/   Bundled food composition JSON files
  sql/schema.sql
  sql/migrations/  Non-destructive upgrades for existing databases
```

## Production deployment

The production stack runs on Tencent Cloud Lighthouse with Docker Compose:

- Nginx is the only public container and currently publishes HTTP port 80.
- Spring Boot listens only on the private Compose network at port 8080.
- PostgreSQL 17 listens only on the private Compose network at port 5432.
- PostgreSQL data is stored in the named `postgres_data` volume.
- `/api/import/*` is blocked by Nginx.

Deployment and rollback commands are documented in [`deploy/README.md`](deploy/README.md). Never print or commit `.env.production`, and never run `docker compose down --volumes` in production.

The calendar and body-weight backends were deployed and verified on 2026-07-22. The weight release used a pre-migration PostgreSQL backup, retained the previous backend image for rollback, and recreated only the backend container; the existing PostgreSQL container and volume remained in place. Health, snapshot, carry-forward, trend, delete, cleanup, and public import-blocking checks passed.

The HTTP IP endpoint remains suitable only for development and experience-version testing. Formal Mini Program release still requires an ICP-filed domain, HTTPS, and a configured WeChat request domain.

## Troubleshooting

- `Connection refused`: start PostgreSQL and verify port `5432`.
- `password authentication failed`: set `PGUSER` and `PGPASSWORD` to valid PostgreSQL credentials.
- Port `8080` is occupied: set `$env:PORT = "8081"` and update the frontend API URL accordingly.
- `mvn` is not recognized: use `.\mvnw.cmd`; global Maven is optional.