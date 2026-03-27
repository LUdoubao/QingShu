#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
SEND_API="${BASE_URL}/interview-agent/questions/rabbitmq/q016-delay/send"
INSPECT_API="${BASE_URL}/interview-agent/questions/rabbitmq/q016-delay/inspect"

echo "[CASE 1] ttl-dlx delay 3000ms"
curl -sS -X POST "${SEND_API}" -H "Content-Type: application/json" -d '{"bizId":"delay-1001","payload":"order-timeout","delayMillis":3000,"mode":"ttl-dlx"}'
echo

echo "[CASE 2] inspect immediately"
curl -sS "${INSPECT_API}?bizId=delay-1001"
echo

sleep 4
echo "[CASE 3] inspect after delay"
curl -sS "${INSPECT_API}?bizId=delay-1001"
echo

echo "[CASE 4] plugin mode demo"
curl -sS -X POST "${SEND_API}" -H "Content-Type: application/json" -d '{"bizId":"delay-2001","payload":"plugin-delay","delayMillis":2000,"mode":"plugin"}'
echo
