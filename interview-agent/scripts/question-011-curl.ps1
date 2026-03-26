# PowerShell script for testing interview-agent question-011
# Usage: .\question-011-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$UPDATE_API = "$BASE_URL/interview-agent/questions/q011-consistency/update"
$QUERY_API = "$BASE_URL/interview-agent/questions/q011-consistency/query"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] baseline query"
$q1 = Invoke-RestMethod -Uri $QUERY_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"profile-1001"}'
Print-Json -Data $q1
Write-Host ""

Write-Host "[CASE 2] update with normal delete-cache"
$u1 = Invoke-RestMethod -Uri $UPDATE_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"profile-1001","newValue":"用户资料V2","simulateDeleteFail":false}'
Print-Json -Data $u1
Write-Host ""

Start-Sleep -Seconds 1
Write-Host "[CASE 3] query after update"
$q2 = Invoke-RestMethod -Uri $QUERY_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"profile-1001"}'
Print-Json -Data $q2
Write-Host ""

Write-Host "[CASE 4] update with simulated delete failure (retry + delayed double delete + MQ)"
$u2 = Invoke-RestMethod -Uri $UPDATE_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"profile-1002","newValue":"用户资料V2","simulateDeleteFail":true}'
Print-Json -Data $u2
Write-Host ""

Start-Sleep -Seconds 1
Write-Host "[CASE 5] query after compensation"
$q3 = Invoke-RestMethod -Uri $QUERY_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"profile-1002"}'
Print-Json -Data $q3
Write-Host ""
