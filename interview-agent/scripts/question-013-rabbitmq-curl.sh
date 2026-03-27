#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
SEND_API="${BASE_URL}/interview-agent/questions/rabbitmq/q013-order/send"
INSPECT_API="${BASE_URL}/interview-agent/questions/rabbitmq/q013-order/inspect"

echo "[CASE 1] send seq=1"
curl -sS -X POST "${SEND_API}" -H "Content-Type: application/json" -d '{"bizKey":"order-1001","seq":1,"payload":"created"}'
echo

echo "[CASE 2] send seq=2"
curl -sS -X POST "${SEND_API}" -H "Content-Type: application/json" -d '{"bizKey":"order-1001","seq":2,"payload":"paid"}'
echo
sleep 1

echo "[CASE 3] inspect"
curl -sS "${INSPECT_API}?bizKey=order-1001"
echo
