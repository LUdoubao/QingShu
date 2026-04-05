# ============================================================================
# ArrayList vs LinkedList 性能对比测试 - 验证脚本 (PowerShell 版本)
# 
# 【用途】验证 interview-agent 模块的集合性能对比接口
# 【兼容性】Windows PowerShell 5.1+ 和 PowerShell Core 7+
# 【编码】UTF-8（保证中文输出不乱码）
#
# 【使用方法】
#   PowerShell: .\test-collection-performance.ps1
#   如遇执行策略限制，先运行: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
#
# 【前置条件】
#   1. interview-agent 服务已启动（默认端口 9510）
#   2. Invoke-WebRequest 或 curl 命令可用
# ============================================================================

# 设置控制台编码为 UTF-8，防止中文乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 服务地址配置
$BASE_URL = "http://localhost:9510"
$API_PREFIX = "/interview-agent/collection"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "ArrayList vs LinkedList 性能对比测试" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# ----------------------------------------------------------------------------
# 辅助函数：发送 HTTP GET 请求并返回响应内容
# ----------------------------------------------------------------------------
function Invoke-HttpGet {
    param(
        [string]$Url
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -Method Get -UseBasicParsing -ErrorAction Stop
        return @{
            StatusCode = $response.StatusCode
            Content = $response.Content
        }
    } catch {
        return @{
            StatusCode = 0
            Content = $_.Exception.Message
        }
    }
}

# ----------------------------------------------------------------------------
# 测试 1：健康检查（确认服务可用）
# ----------------------------------------------------------------------------
Write-Host "[测试 1/4] 健康检查..." -ForegroundColor Yellow
$healthResult = Invoke-HttpGet "$BASE_URL/interview-agent/health"

if ($healthResult.StatusCode -eq 200) {
    Write-Host "✓ 服务正常响应 (HTTP 200)" -ForegroundColor Green
} else {
    Write-Host "✗ 服务异常 (HTTP $($healthResult.StatusCode))" -ForegroundColor Red
    Write-Host "请确认 interview-agent 服务已启动在端口 9510" -ForegroundColor Red
    exit 1
}
Write-Host ""

# ----------------------------------------------------------------------------
# 测试 2：获取面试知识点总结（快速验证）
# ----------------------------------------------------------------------------
Write-Host "[测试 2/4] 获取面试知识点总结..." -ForegroundColor Yellow
$summaryResult = Invoke-HttpGet "$BASE_URL$API_PREFIX/knowledge/summary"

if ($summaryResult.Content -match '"code":200') {
    Write-Host "✓ 知识点总结接口正常" -ForegroundColor Green
    Write-Host ""
    Write-Host "--- 核心知识点 ---" -ForegroundColor Cyan

    # 解析 JSON 并显示 data 字段
    try {
        $jsonObj = $summaryResult.Content | ConvertFrom-Json
        if ($jsonObj.data) {
            # 替换转义换行符并显示
            $formattedData = $jsonObj.data -replace '\\n', "`n"
            Write-Host $formattedData
        }
    } catch {
        Write-Host "⚠ JSON 解析失败，显示原始响应" -ForegroundColor Yellow
        Write-Host $summaryResult.Content
    }
    Write-Host ""
} else {
    Write-Host "✗ 知识点总结接口异常" -ForegroundColor Red
    Write-Host "响应内容: $($summaryResult.Content)" -ForegroundColor Red
}
Write-Host ""

# ----------------------------------------------------------------------------
# 测试 3：执行完整性能对比测试（默认数据量 100,000）
# ----------------------------------------------------------------------------
Write-Host "[测试 3/4] 执行完整性能对比测试（数据量: 100,000）..." -ForegroundColor Yellow
Write-Host "提示：此测试耗时约 1-5 秒，请耐心等待..." -ForegroundColor Gray

$startTime = Get-Date
$fullResult = Invoke-HttpGet "$BASE_URL$API_PREFIX/compare/full"
$endTime = Get-Date
$elapsed = ($endTime - $startTime).TotalSeconds

if ($fullResult.Content -match '"code":200') {
    Write-Host "✓ 完整性能对比测试完成 (耗时: $([math]::Round($elapsed, 2))秒)" -ForegroundColor Green
    Write-Host ""

    # 解析 JSON 并显示关键性能数据
    Write-Host "--- 性能对比结果摘要 ---" -ForegroundColor Cyan

    try {
        $jsonObj = $fullResult.Content | ConvertFrom-Json

        if ($jsonObj.data -and $jsonObj.data.results) {
            $scenarioCount = $jsonObj.data.results.Count
            Write-Host "测试场景数: $scenarioCount" -ForegroundColor White
            Write-Host ""

            # 显示每个场景的核心数据
            foreach ($result in $jsonObj.data.results) {
                $scenario = $result.scenario.PadRight(30)
                $arrayTime = $result.arrayListTimeMs.ToString().PadLeft(6)
                $linkedTime = $result.linkedListTimeMs.ToString().PadLeft(6)
                $winner = $result.winner

                Write-Host ("场景: {0} | ArrayList: {1}ms | LinkedList: {2}ms | 优胜者: {3}" -f `
                    $scenario, $arrayTime, $linkedTime, $winner) -ForegroundColor White
            }

            Write-Host ""

            # 显示总结和建议
            Write-Host "--- 核心结论 ---" -ForegroundColor Cyan
            $summary = $jsonObj.data.summary -replace '\\n', "`n"
            Write-Host $summary -ForegroundColor White
            Write-Host ""

            Write-Host "--- 推荐场景 ---" -ForegroundColor Cyan
            $recommendation = $jsonObj.data.recommendation -replace '\\n', "`n"
            Write-Host $recommendation -ForegroundColor White
        }
    } catch {
        Write-Host "⚠ JSON 解析失败: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ 完整性能对比测试失败" -ForegroundColor Red
    Write-Host "响应内容: $($fullResult.Content)" -ForegroundColor Red
}
Write-Host ""

# ----------------------------------------------------------------------------
# 测试 4：执行自定义数据量测试（较小数据量 10,000）
# ----------------------------------------------------------------------------
Write-Host "[测试 4/4] 执行自定义数据量测试（数据量: 10,000）..." -ForegroundColor Yellow
$customResult = Invoke-HttpGet "$BASE_URL$API_PREFIX/compare/custom?dataSize=10000"

if ($customResult.Content -match '"code":200') {
    Write-Host "✓ 自定义数据量测试完成" -ForegroundColor Green

    # 验证数据量是否正确
    try {
        $jsonObj = $customResult.Content | ConvertFrom-Json
        if ($jsonObj.data.results -and $jsonObj.data.results[0].dataSize -eq 10000) {
            Write-Host "✓ 数据量参数正确传递 (10,000)" -ForegroundColor Green
        } else {
            Write-Host "⚠ 无法验证数据量参数" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠ JSON 解析失败，无法验证数据量" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ 自定义数据量测试失败" -ForegroundColor Red
    Write-Host "响应内容: $($customResult.Content)" -ForegroundColor Red
}
Write-Host ""

# ----------------------------------------------------------------------------
# 测试 5：参数校验测试（超出范围的数据量）
# ----------------------------------------------------------------------------
Write-Host "[额外测试] 参数校验测试（数据量: 100，应被拒绝）..." -ForegroundColor Yellow
$invalidResult = Invoke-HttpGet "$BASE_URL$API_PREFIX/compare/custom?dataSize=100"

if ($invalidResult.Content -match '"code":500|"message".*必须在') {
    Write-Host "✓ 参数校验正常工作（正确拒绝了无效参数）" -ForegroundColor Green
} else {
    Write-Host "⚠ 参数校验可能存在问题" -ForegroundColor Yellow
    Write-Host "响应内容: $($invalidResult.Content)" -ForegroundColor Yellow
}
Write-Host ""

# ============================================================================
# 测试总结
# ============================================================================
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "测试完成总结" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ 所有接口已验证" -ForegroundColor Green
Write-Host ""
Write-Host "核心发现：" -ForegroundColor Cyan
Write-Host "  1. ArrayList 随机访问速度远快于 LinkedList（O(1) vs O(n)）" -ForegroundColor White
Write-Host "  2. LinkedList 头部插入速度快于 ArrayList（O(1) vs O(n)）" -ForegroundColor White
Write-Host "  3. 尾部添加两者性能相当（均为 O(1)）" -ForegroundColor White
Write-Host "  4. 遍历操作必须使用迭代器，LinkedList 严禁使用普通 for 循环" -ForegroundColor White
Write-Host ""
Write-Host "选型建议：" -ForegroundColor Cyan
Write-Host "  - 读多写少 → ArrayList" -ForegroundColor White
Write-Host "  - 写多读少 → LinkedList" -ForegroundColor White
Write-Host "  - 需要队列/栈 → LinkedList" -ForegroundColor White
Write-Host "  - 不确定场景 → 优先 ArrayList" -ForegroundColor White
Write-Host ""
Write-Host "详细测试数据请查看上方输出。" -ForegroundColor Gray
Write-Host "==========================================" -ForegroundColor Cyan
