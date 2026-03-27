# PowerShell script for question-006 JVM volatile demo
# 终极修复版 - 强制 UTF-8 解码响应
# Usage: .\question-006-jvm-volatile-curl.ps1

# ========== 编码设置 ==========
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null 2>&1 } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/jvm/q006-volatile/run"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE] volatile demo"

# ========== 关键修复：强制 UTF-8 解码 ==========
# 使用 WebRequest 获取原始字节，然后强制 UTF-8 解码
$request = [System.Net.WebRequest]::Create($API)
$request.Method = "POST"
$request.ContentType = "application/json; charset=utf-8"

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes('{"threadCount":20,"incrementPerThread":10000}')
$request.ContentLength = $bodyBytes.Length

$stream = $request.GetRequestStream()
$stream.Write($bodyBytes, 0, $bodyBytes.Length)
$stream.Close()

$response = $request.GetResponse()
$reader = New-Object System.IO.StreamReader($response.GetResponseStream(), [System.Text.Encoding]::UTF8)
$content = $reader.ReadToEnd()
$reader.Close()

# 解析 JSON 并输出
$r = $content | ConvertFrom-Json
Print-Json -Data $r
Write-Host ""