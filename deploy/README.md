# Production deployment
## Body-circumference release record

The 2026-07-27 circumference release used the non-destructive migration:

```text
src/main/resources/sql/migrations/2026-07-27-create-body-circumference-records.sql
```

Before migration, create and verify a PostgreSQL custom-format backup and tag the previous backend image for rollback. Keep the exact backup path, image identifier, and production secrets out of public logs and documentation.

Only the backend service was recreated with `docker compose up -d --no-deps backend`; PostgreSQL and Nginx remained in place. Acceptance covered health, empty snapshot, partial save, independent carry-forward, all-blank no-op, trend, clear fallback, delete, cleanup, import blocking, and closed public ports. Never include real user measurements or production secret values in logs or release documentation.

This deployment runs Nginx, Spring Boot, and PostgreSQL on one Docker Compose host. Only Nginx is published to the host network. PostgreSQL and Spring Boot remain on a private Docker network.

## Requirements

- Ubuntu 24.04 LTS
- Docker Engine with the Compose plugin
- Inbound TCP 22, 80, and 443 allowed by the cloud firewall
- Trusted certificates for caloriecalculator.top/www.caloriecalculator.top and api.caloriecalculator.top

## Configure TLS

Install certificates outside the repository with root-only private-key permissions:

    /etc/calorie-calculator/tls/root/fullchain.crt
    /etc/calorie-calculator/tls/root/private.key
    /etc/calorie-calculator/tls/api/fullchain.crt
    /etc/calorie-calculator/tls/api/private.key

The Nginx container mounts /etc/calorie-calculator/tls read-only. Never copy certificate private keys into the repository or Docker image. Free certificates must be replaced before expiry, followed by an Nginx configuration test and reload or recreation.

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
curl --fail https://caloriecalculator.top/healthz
curl --fail https://api.caloriecalculator.top/healthz
curl --fail "http://localhost/api/foods/page?page=1&size=10"
curl --fail "https://api.caloriecalculator.top/api/foods/page?page=1&size=10"
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

- Public: ports 80 and 443
- Administrative: port 22 restricted to trusted source IPs when practical
- Private only: PostgreSQL 5432 and Spring Boot 8080
- Blocked by Nginx: `/api/import/*`

The raw-IP HTTP endpoint is retained temporarily for infrastructure verification and experience-version rollback. Formal Mini Program traffic uses https://api.caloriecalculator.top/api, and the filed root domain serves a minimal public landing page over HTTPS.

## Calendar release verification

After deploying a calendar-enabled revision, verify all of the following:

- `/healthz` returns HTTP 200.
- `/api/foods/page?page=1&size=10` still reports the expected catalog total.
- `/api/records/calendar?month=yyyy-MM` returns `minDate`, `maxDate`, and `recordedDates`.
- PostgreSQL was not recreated and the existing named volume remains attached.
- `/api/import/*` remains unavailable through public Nginx.
- Backend and PostgreSQL containers report `healthy` before considering the deployment complete.
