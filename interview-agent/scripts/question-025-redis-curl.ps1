# PowerShell script for testing interview-agent question-025 Redis version
# Usage: .\question-025-redis-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/q025-distributed-lock/run-redis"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[REDIS CASE 1] normal lock/unlock"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"lockKey":"order-lock-r1","clientId":"redis-client-a","lockTtlMillis":500,"bizWorkMillis":200,"enableWatchdog":false}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[REDIS CASE 2] long work with watchdog"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"lockKey":"order-lock-r2","clientId":"redis-client-b","lockTtlMillis":300,"bizWorkMillis":1200,"enableWatchdog":true}'
Print-Json -Data $r2
Write-Host ""
