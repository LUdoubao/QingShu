#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/jvm/q008-lock-choice/run"

echo "[CASE 1] synchronized"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"mode":"synchronized","threadCount":20,"incrementPerThread":5000,"fairLock":false,"tryLockTimeoutMillis":10}'
echo

echo "[CASE 2] reentrant fair lock"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"mode":"reentrant","threadCount":20,"incrementPerThread":5000,"fairLock":true,"tryLockTimeoutMillis":10}'
echo
