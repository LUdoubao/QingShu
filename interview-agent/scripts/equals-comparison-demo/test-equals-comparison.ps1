# ==和 equals() 区别演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-equals-comparison.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "==和 equals() 区别演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/equals"

# 测试 1：基本类型比较
Write-Host "【测试 1】基本类型比较 (BASIC)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"comparisonType":"BASIC"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：字符串比较
Write-Host "【测试 2】字符串比较 (STRING)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"comparisonType":"STRING"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：包装类比较
Write-Host "【测试 3】包装类比较 (WRAPPER)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"comparisonType":"WRAPPER"}'
$response3 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 4：自定义对象比较
Write-Host "【测试 4】自定义对象比较 (CUSTOM)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response4 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"comparisonType":"CUSTOM"}'
$response4 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 5：错误的比较类型（测试兜底逻辑）
Write-Host "【测试 5】错误的比较类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response5 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"comparisonType":"INVALID_TYPE"}'
    $response5 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
