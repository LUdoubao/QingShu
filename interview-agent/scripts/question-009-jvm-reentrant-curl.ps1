# PowerShell script for question-009 JVM reentrant lock demo
# 修复版 - 强制 UTF-8 解码
# Usage: .\question-009-jvm-reentrant-curl.ps1

# ========== 编码设置 ==========
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null 2>&1 } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/jvm/q009-reentrant/run"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

function Invoke-Api {
    param([string]$Body)
    # 使用 HttpWebRequest 强制 UTF-8 解码响应
    $request = [System.Net.HttpWebRequest]::Create($API)
    $request.Method = "POST"
    $request.ContentType = "application/json; charset=utf-8"
    $request.Accept = "application/json"

    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Body)
    $request.ContentLength = $bytes.Length

    $stream = $request.GetRequestStream()
    $stream.Write($bytes, 0, $bytes.Length)
    $stream.Close()

    $response = $request.GetResponse()
    $reader = New-Object System.IO.StreamReader($response.GetResponseStream(), [System.Text.Encoding]::UTF8)
    $content = $reader.ReadToEnd()
    $reader.Close()
    $response.Close()

    return $content | ConvertFrom-Json
}

Write-Host "[CASE 1] synchronized reentrant"
$r1 = Invoke-Api -Body '{"mode":"synchronized","depth":5}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] reentrantlock reentrant"
$r2 = Invoke-Api -Body '{"mode":"reentrant","depth":5}'
Print-Json -Data $r2
Write-Host ""