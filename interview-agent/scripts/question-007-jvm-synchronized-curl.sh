#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/jvm/q007-synchronized/run"

echo "[CASE] synchronized demo"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"threadCount":20,"incrementPerThread":10000}'
echo
