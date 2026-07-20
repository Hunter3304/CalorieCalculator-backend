#!/usr/bin/env sh
set -eu
compose_file="docker-compose.production.yml"
env_file=".env.production"
food_count="$(docker compose --env-file "$env_file" -f "$compose_file" exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "SELECT COUNT(*) FROM food_items"' | tr -d '[:space:]')"
if [ "$food_count" -gt 4 ]; then
  echo "Food import skipped: food_items already contains $food_count rows."
  exit 0
fi
docker compose --env-file "$env_file" -f "$compose_file" exec -T backend curl --fail --silent --show-error http://localhost:8080/api/import/json
echo
final_count="$(docker compose --env-file "$env_file" -f "$compose_file" exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "SELECT COUNT(*) FROM food_items"' | tr -d '[:space:]')"
echo "Food import completed. food_items contains $final_count rows."