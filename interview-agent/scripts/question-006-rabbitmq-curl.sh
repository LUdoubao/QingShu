#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
ONE_API="${BASE_URL}/interview-agent/questions/q006-rabbitmq-confirm/send-one"
BATCH_API="${BASE_URL}/interview-agent/questions/q006-rabbitmq-confirm/send-batch?count=10"

echo "[CASE 1] async confirm"
curl -sS -X POST "${ONE_API}" -H "Content-Type: application/json" -d '{"message":"hello-confirm-async","routingKey":"q006.confirm.key","waitSync":false}'
echo

echo "[CASE 2] sync confirm"
curl -sS -X POST "${ONE_API}" -H "Content-Type: application/json" -d '{"message":"hello-confirm-sync","routingKey":"q006.confirm.key","waitSync":true}'
echo

echo "[CASE 3] batch async confirm"
curl -sS -X POST "${BATCH_API}"
echo
