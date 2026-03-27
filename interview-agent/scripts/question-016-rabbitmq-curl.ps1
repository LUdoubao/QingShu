# PowerShell script for question-016 RabbitMQ delay demo
# Usage: .\question-016-rabbitmq-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$SEND_API = "$BASE_URL/interview-agent/questions/rabbitmq/q016-delay/send"
$INSPECT_NOW = "$BASE_URL/interview-agent/questions/rabbitmq/q016-delay/inspect?bizId=delay-1001"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] ttl-dlx delay 3000ms"
$r1 = Invoke-RestMethod -Uri $SEND_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizId":"delay-1001","payload":"order-timeout","delayMillis":3000,"mode":"ttl-dlx"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] inspect immediately"
$r2 = Invoke-RestMethod -Uri $INSPECT_NOW -Method Get
Print-Json -Data $r2
Write-Host ""

Start-Sleep -Seconds 4
Write-Host "[CASE 3] inspect after delay"
$r3 = Invoke-RestMethod -Uri $INSPECT_NOW -Method Get
Print-Json -Data $r3
Write-Host ""

Write-Host "[CASE 4] plugin mode demo"
$r4 = Invoke-RestMethod -Uri $SEND_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizId":"delay-2001","payload":"plugin-delay","delayMillis":2000,"mode":"plugin"}'
Print-Json -Data $r4
Write-Host ""
