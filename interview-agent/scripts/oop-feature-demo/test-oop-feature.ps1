# 面向对象三大特征演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-oop-feature.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "面向对象三大特征演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/oop"

# 测试 1：演示封装特性
Write-Host "【测试 1】演示封装特性 (ENCAPSULATION)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"ENCAPSULATION"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：演示继承特性
Write-Host "【测试 2】演示继承特性 (INHERITANCE)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"INHERITANCE"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：演示多态特性
Write-Host "【测试 3】演示多态特性 (POLYMORPHISM)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"POLYMORPHISM"}'
$response3 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 4：错误的特征类型（测试兜底逻辑）
Write-Host "【测试 4】错误的特征类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"featureType":"INVALID_TYPE"}'
    $response4 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
