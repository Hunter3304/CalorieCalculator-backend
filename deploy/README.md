# Production deployment

This deployment runs Nginx, Spring Boot, and PostgreSQL on one Docker Compose host. Only Nginx is published to the host network. PostgreSQL and Spring Boot remain on a private Docker network.

## Requirements

- Ubuntu 24.04 LTS
- Docker Engine with the Compose plugin
- Inbound TCP 22 and 80 allowed by the cloud firewall
- TCP 443 will be required after a domain and TLS certificate are configured

## Configure secrets

```sh
cp .env.production.example .env.production
chmod 600 .env.production
```

Replace `POSTGRES_PASSWORD` with a long random value. Never commit `.env.production`.

## Apply the body-weight migration

Existing installations must create the body-weight table before deploying a weight-enabled backend. Back up PostgreSQL first, then apply only the non-destructive migration:

```sh
docker compose --env-file .env.production -f docker-compose.production.yml exec -T postgres \
  sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom' > calorie_calculator-before-weight.dump

docker compose --env-file .env.production -f docker-compose.production.yml exec -T postgres \
  sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
  < src/main/resources/sql/migrations/2026-07-22-create-body-weight-records.sql
```

Do not rerun `schema.sql` on an existing installation because it recreates the food catalog table.
## Start

```sh
docker compose --env-file .env.production -f docker-compose.production.yml up -d --build
sh deploy/import-foods.sh
```

The import script is idempotent: it imports bundled JSON only when the table contains no more than the four schema seed rows.

## Verify

```sh
curl --fail http://localhost/healthz
curl --fail "http://localhost/api/foods/page?page=1&size=10"
curl --fail "http://localhost/api/records/calendar?month=$(date +%Y-%m)"
docker compose --env-file .env.production -f docker-compose.production.yml ps
```

## Operate

```sh
# Logs
docker compose --env-file .env.production -f docker-compose.production.yml logs -f --tail=200

# Keep a rollback tag before replacing a known-good backend image
docker image tag calorie-calculator-backend:latest calorie-calculator-backend:pre-deploy

# Deploy a new revision
docker compose --env-file .env.production -f docker-compose.production.yml up -d --build

# Stop without deleting data
docker compose --env-file .env.production -f docker-compose.production.yml down

# Back up PostgreSQL
docker compose --env-file .env.production -f docker-compose.production.yml exec -T postgres \
  sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom' > calorie_calculator.dump
```

Do not run `docker compose down --volumes` in production because it deletes the PostgreSQL volume.

## Public exposure

- Public: ports 80 and, after TLS setup, 443
- Administrative: port 22 restricted to trusted source IPs when practical
- Private only: PostgreSQL 5432 and Spring Boot 8080
- Blocked by Nginx: `/api/import/*`

The IP-based HTTP deployment is suitable for infrastructure verification only. A WeChat Mini Program production release requires an approved HTTPS API domain.

## Calendar release verification

After deploying a calendar-enabled revision, verify all of the following:

- `/healthz` returns HTTP 200.
- `/api/foods/page?page=1&size=10` still reports the expected catalog total.
- `/api/records/calendar?month=yyyy-MM` returns `minDate`, `maxDate`, and `recordedDates`.
- PostgreSQL was not recreated and the existing named volume remains attached.
- `/api/import/*` remains unavailable through public Nginx.
- Backend and PostgreSQL containers report `healthy` before considering the deployment complete.
