# PowerShell script for testing interview-agent question-009
# Usage: .\question-009-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/009/query"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] mutex/single-flight hot key"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"hotKey":"hot-mutex","clientId":"q009-mutex"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] logical-expire hot key"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"hotKey":"hot-logical","clientId":"q009-logical"}'
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] never-expire + active-refresh hot key"
$r3 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"hotKey":"hot-never-expire","clientId":"q009-never-expire"}'
Print-Json -Data $r3
Write-Host ""

Write-Host "[CASE 4] rate-limit burst"
for ($i = 1; $i -le 10; $i++) {
    $r = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"hotKey":"hot-mutex","clientId":"q009-rate-limit"}'
    Print-Json -Data $r
    Write-Host ""
}
