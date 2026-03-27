# PowerShell script for question-013 RabbitMQ order demo
# Usage: .\question-013-rabbitmq-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$SEND_API = "$BASE_URL/interview-agent/questions/rabbitmq/q013-order/send"
$INSPECT_API = "$BASE_URL/interview-agent/questions/rabbitmq/q013-order/inspect?bizKey=order-1001"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] send seq=1"
$r1 = Invoke-RestMethod -Uri $SEND_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"order-1001","seq":1,"payload":"created"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] send seq=2"
$r2 = Invoke-RestMethod -Uri $SEND_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizKey":"order-1001","seq":2,"payload":"paid"}'
Print-Json -Data $r2
Write-Host ""
Start-Sleep -Seconds 1

Write-Host "[CASE 3] inspect"
$r3 = Invoke-RestMethod -Uri $INSPECT_API -Method Get
Print-Json -Data $r3
Write-Host ""
