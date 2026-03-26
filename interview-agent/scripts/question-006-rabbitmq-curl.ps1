# PowerShell script for testing interview-agent question-006 (RabbitMQ confirm)
# Usage: .\question-006-rabbitmq-curl.ps1

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$ONE_API = "$BASE_URL/interview-agent/questions/q006-rabbitmq-confirm/send-one"
$BATCH_API = "$BASE_URL/interview-agent/questions/q006-rabbitmq-confirm/send-batch?count=10"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE 1] async confirm"
$r1 = Invoke-RestMethod -Uri $ONE_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"message":"hello-confirm-async","routingKey":"q006.confirm.key","waitSync":false}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] sync confirm"
$r2 = Invoke-RestMethod -Uri $ONE_API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"message":"hello-confirm-sync","routingKey":"q006.confirm.key","waitSync":true}'
Print-Json -Data $r2
Write-Host ""

Write-Host "[CASE 3] batch async confirm"
$r3 = Invoke-RestMethod -Uri $BATCH_API -Method Post
Print-Json -Data $r3
Write-Host ""
