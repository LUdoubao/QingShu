#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
SEND_API="${BASE_URL}/interview-agent/questions/rabbitmq/q015-dlq/send"
INSPECT_API="${BASE_URL}/interview-agent/questions/rabbitmq/q015-dlq/inspect"
REPLAY_API="${BASE_URL}/interview-agent/questions/rabbitmq/q015-dlq/replay"

echo "[CASE 1] send fail message to trigger DLQ"
curl -sS -X POST "${SEND_API}" -H "Content-Type: application/json" -d '{"bizId":"dlq-1001","payload":"force-fail","forceFail":true}'
echo
sleep 1

echo "[CASE 2] inspect should be IN_DLQ"
curl -sS "${INSPECT_API}?bizId=dlq-1001"
echo

echo "[CASE 3] replay from DLQ"
curl -sS -X POST "${REPLAY_API}?bizId=dlq-1001"
echo
sleep 1

echo "[CASE 4] inspect after replay"
curl -sS "${INSPECT_API}?bizId=dlq-1001"
echo
