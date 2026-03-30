# final、finally、finalize 区别演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-finalkeyword-comparison.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "final、finally、finalize 区别演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/finalkeyword"

# 测试 1：final 演示
Write-Host "【测试 1】final 关键字演示 (FINAL)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"keywordType":"FINAL"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：finally 演示
Write-Host "【测试 2】finally 关键字演示 (FINALLY)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"keywordType":"FINALLY"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：finalize 演示
Write-Host "【测试 3】finalize 关键字演示 (FINALIZE)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"keywordType":"FINALIZE"}'
$response3 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 4：错误的类型（测试兜底逻辑）
Write-Host "【测试 4】错误的类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"keywordType":"INVALID_TYPE"}'
    $response4 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
