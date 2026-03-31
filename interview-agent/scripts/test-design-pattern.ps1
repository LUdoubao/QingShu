# Spring 设计模式演示 - 验证脚本（PowerShell 版本）
# 用途：验证所有设计模式的演示接口

# 设置输出编码为UTF-8，解决中文乱码问题
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================"
Write-Host "Spring 设计模式演示 - 开始验证"
Write-Host "========================================"
Write-Host ""

# 设置服务地址
$BASE_URL = "http://localhost:9510/design-pattern"

# 检查服务是否可用
Write-Host "[检查] 测试服务连通性..."
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/health" -Method Get -ErrorAction Stop
    Write-Host "[成功] 服务连接正常" -ForegroundColor Green
} catch {
    Write-Host "[错误] 无法连接到服务，请确认服务已启动：$BASE_URL" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 测试所有设计模式接口的函数
function Test-Pattern {
    param(
        [string]$PatternName,
        [string]$Endpoint,
        [int]$TestNumber
    )

    Write-Host "========================================"
    Write-Host "[测试 $TestNumber/10] $PatternName" -ForegroundColor Cyan
    Write-Host "========================================"

    try {
        $response = Invoke-WebRequest -Uri "$BASE_URL/$Endpoint" -Method Get -ErrorAction Stop

        # 修复：强制使用 UTF-8 解码
        $content = [System.Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())

        Write-Host "状态码: $($response.StatusCode)"
        Write-Host "响应内容:" -ForegroundColor Yellow

        $jsonResponse = $content | ConvertFrom-Json
        $jsonResponse | ConvertTo-Json -Depth 10 | Write-Host

    } catch {
        Write-Host "调用失败: $PatternName" -ForegroundColor Red
        Write-Host "错误信息: $_" -ForegroundColor Red
    }

    Write-Host ""
}

# 测试所有设计模式
$patterns = @(
#     @{Name = "工厂模式 + 单例模式"; Endpoint = "factory-singleton"},
#     @{Name = "原型模式"; Endpoint = "prototype"},
#     @{Name = "建造者模式"; Endpoint = "builder"},
#     @{Name = "代理模式"; Endpoint = "proxy"},
#     @{Name = "装饰器模式"; Endpoint = "decorator"},
#     @{Name = "策略模式"; Endpoint = "strategy"},
#     @{Name = "模板方法模式"; Endpoint = "template-method"},
#     @{Name = "观察者模式"; Endpoint = "observer"},
#     @{Name = "责任链模式"; Endpoint = "chain-of-responsibility"},
#     @{Name = "委派模式"; Endpoint = "delegation"}
)

$testNumber = 1
foreach ($pattern in $patterns) {
    Test-Pattern -PatternName $pattern.Name -Endpoint $pattern.Endpoint -TestNumber $testNumber
    $testNumber++
}

Write-Host "========================================"
Write-Host "所有测试完成！" -ForegroundColor Green
Write-Host "========================================"
Read-Host "按Enter键继续..."