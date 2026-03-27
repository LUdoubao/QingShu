#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/jvm/q009-reentrant/run"

echo "[CASE 1] synchronized reentrant"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"mode":"synchronized","depth":5}'
echo

echo "[CASE 2] reentrantlock reentrant"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"mode":"reentrant","depth":5}'
echo
