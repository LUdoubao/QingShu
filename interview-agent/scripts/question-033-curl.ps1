# PowerShell script for testing interview-agent question-033
# Usage: .\question-033-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$CHECK_API = "$BASE_URL/interview-agent/questions/q033-bloom/check"
$REBUILD_API = "$BASE_URL/interview-agent/questions/q033-bloom/rebuild"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] known key -> maybe exist -> db hit"
$r1 = Invoke-RestMethod -Uri $CHECK_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"user-1001"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] unknown key -> blocked by bloom"
$r2 = Invoke-RestMethod -Uri $CHECK_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"user-9999"}'
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] rebuild bloom"
$r3 = Invoke-RestMethod -Uri $REBUILD_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{}'
Print-Json -Data $r3
Write-Host ""
