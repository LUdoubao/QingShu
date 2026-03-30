# 方法重载和重写演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-method-feature.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "方法重载和重写演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/method"

# 测试 1：演示方法重载
Write-Host "【测试 1】演示方法重载 (OVERLOAD)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"OVERLOAD"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：演示方法重写
Write-Host "【测试 2】演示方法重写 (OVERRIDE)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"OVERRIDE"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：错误的特性类型（测试兜底逻辑）
Write-Host "【测试 3】错误的特性类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"INVALID_TYPE"}'
    $response3 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
