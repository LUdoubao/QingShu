# String、StringBuffer、StringBuilder 区别演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-stringbuilder-comparison.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "String、StringBuffer、StringBuilder 区别演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/stringbuilder"

# 测试 1：String 类演示
Write-Host "【测试 1】String 类演示 (STRING)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"stringType":"STRING"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：StringBuffer 类演示
Write-Host "【测试 2】StringBuffer 类演示 (STRINGBUFFER)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"stringType":"STRINGBUFFER"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：StringBuilder 类演示
Write-Host "【测试 3】StringBuilder 类演示 (STRINGBUILDER)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"stringType":"STRINGBUILDER"}'
$response3 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 4：错误的字符串类型（测试兜底逻辑）
Write-Host "【测试 4】错误的字符串类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"stringType":"INVALID_TYPE"}'
    $response4 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
