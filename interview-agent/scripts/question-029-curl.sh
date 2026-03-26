#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/q029-watch/increment"

echo "[CASE 1] basic watch-cas"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"key":"stock-1","delta":1,"maxRetry":5}'
echo

echo "[CASE 2] another increment"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"key":"stock-1","delta":2,"maxRetry":5}'
echo
