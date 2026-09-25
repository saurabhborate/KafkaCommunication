#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
docker compose -f "$ROOT/docker/docker-compose.yml" down
printf 'Services stopped. Named database and Kafka volumes are preserved. Use docker compose down -v to erase local data.\n'
