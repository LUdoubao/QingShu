#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/008/check"

echo "[CASE 1] cache hit (first DB_HIT, second CACHE_HIT)"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"dataId":"item-1","clientId":"demo-cache"}'
echo
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"dataId":"item-1","clientId":"demo-cache"}'
echo

echo "[CASE 2] penetration guard (Bloom filter + empty cache)"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"dataId":"ghost-999999","clientId":"demo-penetration"}'
echo

echo "[CASE 3] rate limit (burst to trigger RATE_LIMIT)"
for i in {1..7}; do
  curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"dataId":"item-2","clientId":"demo-rate-limit"}'
  echo
done
