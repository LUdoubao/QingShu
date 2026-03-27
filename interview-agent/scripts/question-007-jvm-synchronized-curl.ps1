# PowerShell script for question-007 JVM synchronized demo
# 修复版 - 强制 UTF-8 解码 HTTP 响应
# Usage: .\question-007-jvm-synchronized-curl.ps1

# ========== 控制台编码设置 ==========
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null 2>&1 } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/jvm/q007-synchronized/run"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

Write-Host "[CASE] synchronized demo"

# ========== 备选方案 2: 如果方案 1 仍乱码，取消下面的注释使用 ==========

$request = [System.Net.HttpWebRequest]::Create($API)
$request.Method = "POST"
$request.ContentType = "application/json; charset=utf-8"
$request.Accept = "application/json"

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes('{"threadCount":20,"incrementPerThread":10000}')
$request.ContentLength = $bodyBytes.Length

$stream = $request.GetRequestStream()
$stream.Write($bodyBytes, 0, $bodyBytes.Length)
$stream.Close()

$response = $request.GetResponse()
$reader = New-Object System.IO.StreamReader($response.GetResponseStream(), [System.Text.Encoding]::UTF8)
$content = $reader.ReadToEnd()
$reader.Close()

$r = $content | ConvertFrom-Json
Print-Json -Data $r
Write-Host ""

