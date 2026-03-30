# 接口和抽象类区别演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-abstractinterface-comparison.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "接口和抽象类区别演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/abstractinterface"

# 测试 1：抽象类演示
Write-Host "【测试 1】抽象类演示 (ABSTRACT_CLASS)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"targetType":"ABSTRACT_CLASS"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：接口演示
Write-Host "【测试 2】接口演示 (INTERFACE)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"targetType":"INTERFACE"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：错误的类型（测试兜底逻辑）
Write-Host "【测试 3】错误的类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"targetType":"INVALID_TYPE"}'
    $response3 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
