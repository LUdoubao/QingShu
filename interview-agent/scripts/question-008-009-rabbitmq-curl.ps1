# PowerShell script for testing question 008 & 009 RabbitMQ demos
# Usage: .\question-008-009-rabbitmq-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$Q008_API = "$BASE_URL/interview-agent/questions/q008-rabbitmq-persist/send"
$Q009_SEND = "$BASE_URL/interview-agent/questions/q009-rabbitmq-reliable/send"
$Q009_INSPECT = "$BASE_URL/interview-agent/questions/q009-rabbitmq-reliable/inspect?bizId=biz-9001"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[Q008] send persistent message"
$r1 = Invoke-RestMethod -Uri $Q008_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"message":"persistent-demo"}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[Q009] send reliable message"
$r2 = Invoke-RestMethod -Uri $Q009_SEND -Method Post -ContentType "application/json; charset=utf-8" -Body '{"bizId":"biz-9001","payload":"order-created"}'
Print-Json -Data $r2
Write-Host ""
Start-Sleep -Seconds 1

Write-Host "[Q009] inspect status"
$r3 = Invoke-RestMethod -Uri $Q009_INSPECT -Method Get
Print-Json -Data $r3
Write-Host ""
