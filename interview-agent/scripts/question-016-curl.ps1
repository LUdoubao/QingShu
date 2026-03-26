# PowerShell script for testing interview-agent question-016
# Usage: .\question-016-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/q016-persistence/compare"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] fsync=everysec"
$r1 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"everysec"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] fsync=always"
$r2 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"always"}'
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] fsync=no"
$r3 = Invoke-RestMethod -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"writeOps":3000,"rdbSnapshotSeconds":300,"aofFsyncPolicy":"no"}'
Print-Json -Data $r3
Write-Host ""
