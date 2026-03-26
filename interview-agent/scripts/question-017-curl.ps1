# PowerShell script for testing interview-agent question-017
# Usage: .\question-017-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/q017-aof-rewrite/run"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] rewrite without incremental writes"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"incrTimes":100,"simulateIncrementalWrites":false}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] rewrite with incremental writes"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"incrTimes":100,"simulateIncrementalWrites":true}'
Print-Json -Data $r2
Write-Host ""
