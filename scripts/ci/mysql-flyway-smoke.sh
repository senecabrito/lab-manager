#!/usr/bin/env bash

set -Eeuo pipefail

temp_dir="${AGENT_TEMPDIRECTORY:-${TMPDIR:-/tmp}}"
build_id="${BUILD_BUILDID:-local-$$}"
mysql_image="${CI_MYSQL_IMAGE:-mysql:8.4}"
mysql_container="${CI_MYSQL_CONTAINER:-lab-manager-ci-mysql-${build_id}}"
api_port="${CI_API_PORT:-18082}"
api_pid_file="${temp_dir}/lab-manager-api.pid"
api_log="${temp_dir}/lab-manager-api.log"
openapi_output="${temp_dir}/lab-manager-openapi.json"

database_name='lab_manager_ci'
database_user='lab_manager_ci'
database_password="$(openssl rand -hex 24)"
root_password="$(openssl rand -hex 24)"
jwt_key="$(openssl rand -base64 64 | tr -d '\n')"

api_pid=''

cleanup() {
  exit_code=$?

  if [[ -n "${api_pid}" ]]; then
    kill "${api_pid}" >/dev/null 2>&1 || true
    wait "${api_pid}" >/dev/null 2>&1 || true
  elif [[ -f "${api_pid_file}" ]]; then
    saved_pid="$(cat "${api_pid_file}")"
    kill "${saved_pid}" >/dev/null 2>&1 || true
  fi

  docker rm -f "${mysql_container}" >/dev/null 2>&1 || true
  rm -f "${api_pid_file}" "${api_log}" "${openapi_output}"
  unset database_password root_password jwt_key

  return "${exit_code}"
}

show_diagnostics() {
  echo 'MySQL diagnostics:' >&2
  docker logs "${mysql_container}" --tail 80 >&2 || true
  echo 'API diagnostics:' >&2
  tail -n 120 "${api_log}" >&2 || true
}

trap cleanup EXIT INT TERM

docker rm -f "${mysql_container}" >/dev/null 2>&1 || true
docker run --detach \
  --name "${mysql_container}" \
  --publish 127.0.0.1::3306 \
  --tmpfs /var/lib/mysql:rw,noexec,nosuid,size=1g \
  --env MYSQL_ROOT_PASSWORD="${root_password}" \
  --env MYSQL_DATABASE="${database_name}" \
  --env MYSQL_USER="${database_user}" \
  --env MYSQL_PASSWORD="${database_password}" \
  --health-cmd='mysqladmin ping -h 127.0.0.1 -uroot --password="$MYSQL_ROOT_PASSWORD" --silent' \
  --health-interval=2s \
  --health-timeout=5s \
  --health-retries=30 \
  "${mysql_image}" >/dev/null

mysql_ready='false'
for _ in $(seq 1 60); do
  health_status="$(docker inspect --format '{{.State.Health.Status}}' "${mysql_container}" 2>/dev/null || true)"
  if [[ "${health_status}" == 'healthy' ]]; then
    mysql_ready='true'
    break
  fi
  if [[ "${health_status}" == 'unhealthy' ]]; then
    break
  fi
  sleep 2
done

if [[ "${mysql_ready}" != 'true' ]]; then
  echo 'MySQL did not become healthy before the 120 second timeout.' >&2
  show_diagnostics
  exit 1
fi

mysql_port="$(docker port "${mysql_container}" 3306/tcp | awk -F: 'NR == 1 { print $NF }')"
if [[ ! "${mysql_port}" =~ ^[0-9]+$ ]]; then
  echo 'Could not determine the temporary MySQL port.' >&2
  show_diagnostics
  exit 1
fi

mapfile -t jars < <(find target -maxdepth 1 -type f -name '*.jar' | sort)
if [[ "${#jars[@]}" -ne 1 ]]; then
  echo "Expected exactly one executable JAR; found ${#jars[@]}." >&2
  exit 1
fi

DB_URL="jdbc:mysql://127.0.0.1:${mysql_port}/${database_name}" \
DB_USERNAME="${database_user}" \
DB_PASSWORD="${database_password}" \
JWT_KEY="${jwt_key}" \
JWT_EXPIRATION='900000' \
CORS_ALLOWED_ORIGINS='http://localhost:3000' \
SERVER_PORT="${api_port}" \
java -jar "${jars[0]}" >"${api_log}" 2>&1 &
api_pid=$!
printf '%s' "${api_pid}" > "${api_pid_file}"

api_ready='false'
for _ in $(seq 1 90); do
  if ! kill -0 "${api_pid}" >/dev/null 2>&1; then
    echo 'The API process stopped before becoming ready.' >&2
    show_diagnostics
    exit 1
  fi

  if curl --fail --silent \
    "http://127.0.0.1:${api_port}/v3/api-docs" \
    --output "${openapi_output}"; then
    api_ready='true'
    break
  fi
  sleep 2
done

if [[ "${api_ready}" != 'true' ]]; then
  echo 'The API did not expose OpenAPI before the 180 second timeout.' >&2
  show_diagnostics
  exit 1
fi

schema_version="$(docker exec \
  --env MYSQL_PWD="${database_password}" \
  "${mysql_container}" \
  mysql --batch --skip-column-names \
  --user="${database_user}" "${database_name}" \
  --execute='SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success = 1;')"

migration_count="$(docker exec \
  --env MYSQL_PWD="${database_password}" \
  "${mysql_container}" \
  mysql --batch --skip-column-names \
  --user="${database_user}" "${database_name}" \
  --execute='SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1 AND version IS NOT NULL;')"

if [[ "${schema_version}" != '7' || "${migration_count}" != '7' ]]; then
  echo "Unexpected Flyway state: version=${schema_version}, migrations=${migration_count}." >&2
  show_diagnostics
  exit 1
fi

if ! grep -q 'Started Application' "${api_log}"; then
  echo 'Spring Boot startup confirmation was not found.' >&2
  show_diagnostics
  exit 1
fi

echo "MySQL ${mysql_image} is healthy."
echo "Flyway schema version: ${schema_version}; successful migrations: ${migration_count}."
echo 'Hibernate schema validation and Spring Boot startup completed.'
echo 'GET /v3/api-docs returned HTTP 200.'
