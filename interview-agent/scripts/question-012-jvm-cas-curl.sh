#!/usr/bin/env bash
set -euo pipefail

export LANG="${LANG:-C.UTF-8}"
export LC_ALL="${LC_ALL:-C.UTF-8}"

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/jvm/q012-cas/run"

echo "[CASE 1] CAS counter demo"
curl -sS -X POST "${API}" \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "Accept: application/json; charset=utf-8" \
  --data-raw '{"threads":8,"incrementsPerThread":1000,"enableSpinHint":true}'
echo
