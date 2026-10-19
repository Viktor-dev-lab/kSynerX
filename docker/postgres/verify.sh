#!/usr/bin/env bash
# A temporary database is created and dropped; the application database is never changed.
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$project_dir"

cat docker/postgres/init/001_cdms_schema.sql docker/postgres/tests/schema_test.sql |
  docker compose exec -T postgres sh -eu -c '
    verify_db="cdms_verify_$(date +%s)_$$"
    createdb -U "$POSTGRES_USER" "$verify_db"
    trap '\''dropdb -U "$POSTGRES_USER" "$verify_db"'\'' EXIT
    psql -X -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$verify_db"
  '
