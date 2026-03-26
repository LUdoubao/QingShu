# 兼容 Windows PowerShell 5：统一输出为 UTF-8，减少中文乱码
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/008/check"

function Print-Json {
    param(
        [Parameter(Mandatory = $true)]
        $Data
    )
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

# 修复中文乱码的解码函数
function Decode-Response {
    param($Response)
    # 获取原始字节流，然后用 UTF-8 解码
    $rawBytes = $Response.RawContentStream.ToArray()
    $utf8 = [System.Text.Encoding]::UTF8
    $jsonString = $utf8.GetString($rawBytes)
    return $jsonString | ConvertFrom-Json
}

Write-Host "[CASE 1] cache hit (first DB_HIT, second CACHE_HIT)"

# 使用 Invoke-WebRequest 而不是 Invoke-RestMethod
$resp1 = Invoke-WebRequest -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"item-1","clientId":"demo-cache"}'
$response1 = Decode-Response -Response $resp1
Print-Json -Data $response1
Write-Host ""

$resp2 = Invoke-WebRequest -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"item-1","clientId":"demo-cache"}'
$response2 = Decode-Response -Response $resp2
Print-Json -Data $response2
Write-Host ""

Write-Host "[CASE 2] penetration guard (Bloom filter + empty cache)"
$resp3 = Invoke-WebRequest -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"ghost-999999","clientId":"demo-penetration"}'
$response3 = Decode-Response -Response $resp3
Print-Json -Data $response3
Write-Host ""

Write-Host "[CASE 3] rate limit (burst to trigger RATE_LIMIT)"
for ($i = 1; $i -le 7; $i++) {
    $resp = Invoke-WebRequest -Uri $API -Method Post -ContentType "application/json; charset=utf-8" -Body '{"dataId":"item-2","clientId":"demo-rate-limit"}'
    $response = Decode-Response -Response $resp
    Print-Json -Data $response
    Write-Host ""
}