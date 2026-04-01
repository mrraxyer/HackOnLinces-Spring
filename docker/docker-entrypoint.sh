#!/usr/bin/env sh
set -eu

RUNTIME_ENV_FILE="/app/.runtime-generated.env"

# Reuse previously generated values for this container filesystem.
if [ -f "$RUNTIME_ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$RUNTIME_ENV_FILE"
  set +a
fi

generated_any=0

if [ -z "${APP_JWT_SECRET:-}" ] || [ "$APP_JWT_SECRET" = "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_KEY_AT_LEAST_32_CHARS" ]; then
  APP_JWT_SECRET="$(tr -dc 'A-Za-z0-9' </dev/urandom | head -c 64)"
  export APP_JWT_SECRET
  generated_any=1
fi

if [ -z "${SPRING_DATASOURCE_PASSWORD:-}" ] || [ "$SPRING_DATASOURCE_PASSWORD" = "CHANGE_ME_DB_PASSWORD" ]; then
  SPRING_DATASOURCE_PASSWORD="$(tr -dc 'A-Za-z0-9' </dev/urandom | head -c 32)"
  export SPRING_DATASOURCE_PASSWORD
  generated_any=1
fi

if [ "$generated_any" -eq 1 ]; then
  {
    printf 'APP_JWT_SECRET=%s\n' "$APP_JWT_SECRET"
    printf 'SPRING_DATASOURCE_PASSWORD=%s\n' "$SPRING_DATASOURCE_PASSWORD"
  } > "$RUNTIME_ENV_FILE"
  chmod 600 "$RUNTIME_ENV_FILE"
echo "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://db:5432/hackonlinces}"
  echo "SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-postgres}"
  echo "SPRING_DATASOURCE_PASSWORD=$SPRING_DATASOURCE_PASSWORD"
  echo "SPRING_JPA_HIBERNATE_DDL_AUTO=${SPRING_JPA_HIBERNATE_DDL_AUTO:-update}"
  echo "SPRING_JPA_SHOW_SQL=${SPRING_JPA_SHOW_SQL:-false}"
  echo ''
  echo '# Postgres container'
  echo "POSTGRES_DB=${POSTGRES_DB:-hackonlinces}"
  echo '================================================='
fi

exec sh -c "java ${JAVA_OPTS:-} -jar /app/app.jar"
