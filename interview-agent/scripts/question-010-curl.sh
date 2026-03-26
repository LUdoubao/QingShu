#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/q010-avalanche/query"

echo "[CASE 1] local/redis multi-level cache"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"bizKey":"goods-1001","clientId":"q010-normal","simulateRedisDown":false}'
echo

echo "[CASE 2] redis unavailable -> single-flight rebuild + degrade path"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"bizKey":"goods-1001","clientId":"q010-down","simulateRedisDown":true}'
echo

echo "[CASE 3] rate limit"
for i in {1..12}; do
  curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"bizKey":"goods-1002","clientId":"q010-rate","simulateRedisDown":false}'
  echo
done
