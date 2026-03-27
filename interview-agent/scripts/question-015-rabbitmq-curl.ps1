# PowerShell script for question-015 RabbitMQ DLQ demo
# Usage: .\question-015-rabbitmq-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$SEND_API = "$BASE_URL/interview-agent/questions/rabbitmq/q015-dlq/send"
$INSPECT_API = "$BASE_URL/interview-agent/questions/rabbitmq/q015-dlq/inspect?bizId=dlq-1001"
$REPLAY_API = "$BASE_URL/interview-agent/questions/rabbitmq/q015-dlq/replay?bizId=dlq-1001"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] send fail message to trigger DLQ"
$r1 = Invoke-RestMethod -Uri $SEND_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizId":"dlq-1001","payload":"force-fail","forceFail":true}'
Print-Json -Data $r1
Write-Host ""
Start-Sleep -Seconds 1

Write-Host "[CASE 2] inspect should be IN_DLQ"
$r2 = Invoke-RestMethod -Uri $INSPECT_API -Method Get
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] replay from DLQ"
$r3 = Invoke-RestMethod -Uri $REPLAY_API -Method Post
Print-Json -Data $r3
Write-Host ""
Start-Sleep -Seconds 1

Write-Host "[CASE 4] inspect after replay"
$r4 = Invoke-RestMethod -Uri $INSPECT_API -Method Get
Print-Json -Data $r4
Write-Host ""
