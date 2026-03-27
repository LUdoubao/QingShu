#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/jvm/q011-lock-mode/run"

echo "[CASE 1] pessimistic lock"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"lockType":"pessimistic","threads":8,"incrementsPerThread":1000}'
echo

echo "[CASE 2] optimistic lock (CAS)"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"lockType":"optimistic","threads":8,"incrementsPerThread":1000}'
echo
