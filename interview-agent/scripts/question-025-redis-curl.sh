#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/q025-distributed-lock/run-redis"

echo "[REDIS CASE 1] normal lock/unlock"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"lockKey":"order-lock-r1","clientId":"redis-client-a","lockTtlMillis":500,"bizWorkMillis":200,"enableWatchdog":false}'
echo

echo "[REDIS CASE 2] long work with watchdog"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"lockKey":"order-lock-r2","clientId":"redis-client-b","lockTtlMillis":300,"bizWorkMillis":1200,"enableWatchdog":true}'
echo
