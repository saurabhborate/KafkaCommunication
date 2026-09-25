#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
docker info >/dev/null
docker compose -f "$ROOT/docker/docker-compose.yml" up --build -d --wait
docker compose -f "$ROOT/docker/docker-compose.yml" ps
printf '\nServices started:\n  Acceptance:   http://localhost:18181\n  Order:        http://localhost:18183/actuator/health\n  Notification: http://localhost:18182\n  Swagger:      http://localhost:18181/swagger-ui/index.html\n'
