#!/usr/bin/env bash
# Build (Maven) and run the sql-transactions jar entirely inside Docker.
#
# Usage:
#   ./run.sh            # build & run
#   ./run.sh --build    # force a clean rebuild even if the jar already exists
#
# The Java app connects to localhost:5432.  When it runs inside Docker we share
# the pgdemo container's network namespace so "localhost" resolves correctly.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

MAVEN_IMAGE="maven:3.9-eclipse-temurin-11"
PG_CONTAINER="pgdemo"
JAR="target/sql-transactions.jar"
FORCE_BUILD=false

# ── argument parsing ──────────────────────────────────────────────────────────
for arg in "$@"; do
  case "$arg" in
    --build) FORCE_BUILD=true ;;
    *)
      echo "Unknown argument: $arg"
      echo "Usage: ./run.sh [--build]"
      exit 1
      ;;
  esac
done

# ── pre-flight: pgdemo must be running ────────────────────────────────────────
if ! docker inspect --format '{{.State.Running}}' "$PG_CONTAINER" 2>/dev/null | grep -q true; then
  echo "ERROR: PostgreSQL container '$PG_CONTAINER' is not running."
  echo ""
  echo "Start it with:"
  echo "  docker run --name $PG_CONTAINER \\"
  echo "    -e POSTGRES_PASSWORD=postgres \\"
  echo "    -p 5432:5432 \\"
  echo "    -d postgres:16"
  echo ""
  echo "Then load the schema and seed data:"
  echo "  docker exec -i $PG_CONTAINER psql -U postgres -c \"CREATE DATABASE projectdb;\""
  echo "  docker exec -i $PG_CONTAINER psql -U postgres -d projectdb < sql/schema.sql"
  echo "  docker exec -i $PG_CONTAINER psql -U postgres -d projectdb < sql/data.sql"
  exit 1
fi

# ── build phase ───────────────────────────────────────────────────────────────
if [ "$FORCE_BUILD" = true ] || [ ! -f "${SCRIPT_DIR}/${JAR}" ]; then
  echo "Building project with Maven (${MAVEN_IMAGE})..."
  docker run --rm \
    -v "${SCRIPT_DIR}:/app" \
    -v "${HOME}/.m2:/root/.m2" \
    -w /app \
    "${MAVEN_IMAGE}" \
    mvn clean package -q
  echo "Build complete → ${JAR}"
else
  echo "Jar already exists (${JAR}). Skipping build. Use --build to force rebuild."
fi

# ── run phase ─────────────────────────────────────────────────────────────────
echo ""
echo "Running sql-transactions (connected to '$PG_CONTAINER' network)..."
echo "────────────────────────────────────────────────────────────────"

# --network container:pgdemo shares pgdemo's network namespace, so
# "localhost:5432" inside this container resolves to the Postgres process.
docker run -it --rm \
  --network "container:${PG_CONTAINER}" \
  -v "${SCRIPT_DIR}:/app" \
  -w /app \
  "${MAVEN_IMAGE}" \
  java -jar "${JAR}"
