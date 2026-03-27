# PowerShell script for question-008 JVM lock choice demo
# 修复版 - 强制 UTF-8 解码
# Usage: .\question-008-jvm-lock-choice-curl.ps1

# ========== 编码设置 ==========
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try { cmd /c chcp 65001 > $null 2>&1 } catch {}

$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$API = "$BASE_URL/interview-agent/questions/jvm/q008-lock-choice/run"

function Print-Json {
    param([Parameter(Mandatory = $true)] $Data)
    $json = $Data | ConvertTo-Json -Depth 10
    Write-Host $json
}

function Invoke-Api {
    param([string]$Body)
    # 使用 WebRequest 获取原始字节流，强制 UTF-8 解码
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

    return $content | ConvertFrom-Json
}

Write-Host "[CASE 1] synchronized"
$r1 = Invoke-Api -Body '{"mode":"synchronized","threadCount":20,"incrementPerThread":5000,"fairLock":false,"tryLockTimeoutMillis":10}'
Print-Json -Data $r1
Write-Host ""

Write-Host "[CASE 2] reentrant fair lock"
$r2 = Invoke-Api -Body '{"mode":"reentrant","threadCount":20,"incrementPerThread":5000,"fairLock":true,"tryLockTimeoutMillis":10}'
Print-Json -Data $r2
Write-Host ""