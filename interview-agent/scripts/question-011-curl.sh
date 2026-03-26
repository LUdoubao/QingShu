#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
UPDATE_API="${BASE_URL}/interview-agent/questions/q011-consistency/update"
QUERY_API="${BASE_URL}/interview-agent/questions/q011-consistency/query"

echo "[CASE 1] baseline query"
curl -sS -X POST "${QUERY_API}" -H "Content-Type: application/json" -d '{"dataId":"profile-1001"}'
echo

echo "[CASE 2] update with normal delete-cache"
curl -sS -X POST "${UPDATE_API}" -H "Content-Type: application/json" -d '{"dataId":"profile-1001","newValue":"????V2","simulateDeleteFail":false}'
echo

sleep 1
echo "[CASE 3] query after update"
curl -sS -X POST "${QUERY_API}" -H "Content-Type: application/json" -d '{"dataId":"profile-1001"}'
echo

echo "[CASE 4] update with simulated delete failure (retry + delayed double delete + MQ)"
curl -sS -X POST "${UPDATE_API}" -H "Content-Type: application/json" -d '{"dataId":"profile-1002","newValue":"????V2","simulateDeleteFail":true}'
echo

sleep 1
echo "[CASE 5] query after compensation"
curl -sS -X POST "${QUERY_API}" -H "Content-Type: application/json" -d '{"dataId":"profile-1002"}'
echo
