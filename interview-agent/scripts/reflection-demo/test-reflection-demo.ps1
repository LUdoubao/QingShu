# Java 反射机制演示 - PowerShell 验证脚本
# 适用于 Windows PowerShell
# 使用方法：.\test-reflection-demo.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Java 反射机制演示 - 验证脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:9510/interview-agent/reflection"

# 测试 1：获取 Class 对象
Write-Host "【测试 1】获取 Class 对象 (GET_CLASS)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response1 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"GET_CLASS"}'
$response1 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 2：创建实例
Write-Host "【测试 2】创建实例 (CREATE_INSTANCE)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response2 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"CREATE_INSTANCE"}'
$response2 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 3：调用方法
Write-Host "【测试 3】调用方法 (INVOKE_METHOD)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response3 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"INVOKE_METHOD"}'
$response3 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 4：访问属性
Write-Host "【测试 4】访问属性 (ACCESS_FIELD)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response4 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"ACCESS_FIELD"}'
$response4 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 5：获取构造器
Write-Host "【测试 5】获取构造器 (GET_CONSTRUCTOR)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$response5 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"GET_CONSTRUCTOR"}'
$response5 | ConvertTo-Json -Depth 10
Write-Host ""
Write-Host ""

# 测试 6：错误的类型（测试兜底逻辑）
Write-Host "【测试 6】错误的类型（测试兜底逻辑）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
try {
    $response6 = Invoke-RestMethod -Uri "$baseUrl/demonstrate" -Method Post -ContentType "application/json" -Body '{"operationType":"INVALID_TYPE"}'
    $response6 | ConvertTo-Json -Depth 10
} catch {
    Write-Host "请求失败：$_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
