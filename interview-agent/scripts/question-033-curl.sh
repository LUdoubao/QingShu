#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
CHECK_API="${BASE_URL}/interview-agent/questions/q033-bloom/check"
REBUILD_API="${BASE_URL}/interview-agent/questions/q033-bloom/rebuild"

echo "[CASE 1] known key -> maybe exist -> db hit"
curl -sS -X POST "${CHECK_API}" -H "Content-Type: application/json" -d '{"bizKey":"user-1001"}'
echo

echo "[CASE 2] unknown key -> blocked by bloom"
curl -sS -X POST "${CHECK_API}" -H "Content-Type: application/json" -d '{"bizKey":"user-9999"}'
echo

echo "[CASE 3] rebuild bloom"
curl -sS -X POST "${REBUILD_API}" -H "Content-Type: application/json" -d '{}'
echo
