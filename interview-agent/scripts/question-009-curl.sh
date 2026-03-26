#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/009/query"

echo "[CASE 1] mutex/single-flight hot key"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"hotKey":"hot-mutex","clientId":"q009-mutex"}'
echo

echo "[CASE 2] logical-expire hot key"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"hotKey":"hot-logical","clientId":"q009-logical"}'
echo

echo "[CASE 3] never-expire + active-refresh hot key"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"hotKey":"hot-never-expire","clientId":"q009-never-expire"}'
echo

echo "[CASE 4] rate-limit burst"
for i in {1..10}; do
  curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"hotKey":"hot-mutex","clientId":"q009-rate-limit"}'
  echo
done
