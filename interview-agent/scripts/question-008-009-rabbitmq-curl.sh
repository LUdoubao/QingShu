#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
Q008_API="${BASE_URL}/interview-agent/questions/q008-rabbitmq-persist/send"
Q009_SEND="${BASE_URL}/interview-agent/questions/q009-rabbitmq-reliable/send"
Q009_INSPECT="${BASE_URL}/interview-agent/questions/q009-rabbitmq-reliable/inspect"

echo "[Q008] send persistent message"
curl -sS -X POST "${Q008_API}" -H "Content-Type: application/json" -d '{"message":"persistent-demo"}'
echo

echo "[Q009] send reliable message"
curl -sS -X POST "${Q009_SEND}" -H "Content-Type: application/json" -d '{"bizId":"biz-9001","payload":"order-created"}'
echo
sleep 1
echo "[Q009] inspect status"
curl -sS "${Q009_INSPECT}?bizId=biz-9001"
echo
