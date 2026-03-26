#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9510}"
API="${BASE_URL}/interview-agent/questions/q016-persistence/compare"

echo "[CASE 1] fsync=everysec"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"everysec"}'
echo

echo "[CASE 2] fsync=always"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"always"}'
echo

echo "[CASE 3] fsync=no"
curl -sS -X POST "${API}" -H "Content-Type: application/json" -d '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"no"}'
echo
