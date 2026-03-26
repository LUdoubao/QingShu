#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/q017-aof-rewrite/run"

echo "[CASE 1] rewrite without incremental writes"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"incrTimes":100,"simulateIncrementalWrites":false}'
echo

echo "[CASE 2] rewrite with incremental writes"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"incrTimes":100,"simulateIncrementalWrites":true}'
echo
