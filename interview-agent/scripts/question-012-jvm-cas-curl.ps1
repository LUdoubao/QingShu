$OutputEncoding = [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
[Console]::InputEncoding = New-Object System.Text.UTF8Encoding($false)
chcp 65001 > $null

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$Api = "$BaseUrl/interview-agent/questions/jvm/q012-cas/run"
$Body = '{"threads":8,"incrementsPerThread":1000,"enableSpinHint":true}'

Write-Host "[CASE 1] CAS counter demo"
curl.exe -sS -X POST $Api `
  -H "Content-Type: application/json; charset=utf-8" `
  -H "Accept: application/json; charset=utf-8" `
  --data-raw $Body
Write-Host ""
