# PowerShell script for testing interview-agent question-029
# Usage: .\question-029-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/q029-watch/increment"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] basic watch-cas"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"key":"stock-1","delta":1,"maxRetry":5}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] another increment"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"key":"stock-1","delta":2,"maxRetry":5}'
Print-Json -Data $r2
Write-Host ""
