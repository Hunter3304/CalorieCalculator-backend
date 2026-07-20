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
docker compose --env-file .env.production -f docker-compose.production.yml ps
```

## Operate

```sh
# Logs
docker compose --env-file .env.production -f docker-compose.production.yml logs -f --tail=200

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