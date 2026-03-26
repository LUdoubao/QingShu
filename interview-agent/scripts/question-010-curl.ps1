# PowerShell script for testing interview-agent question-010
# Usage: .\question-010-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/q010-avalanche/query"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] local/redis multi-level cache"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"goods-1001","clientId":"q010-normal","simulateRedisDown":false}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] redis unavailable -> single-flight rebuild + degrade path"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"goods-1001","clientId":"q010-down","simulateRedisDown":true}'
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] rate limit"
for ($i = 1; $i -le 12; $i++) {
    $r = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"goods-1002","clientId":"q010-rate","simulateRedisDown":false}'
    Print-Json -Data $r
    Write-Host ""
}
