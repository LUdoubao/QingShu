# PowerShell 版本验证脚本 - 问题 005: BeanFactory 和 ApplicationContext 区别
# =====================================================
# 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示和对比两种 Spring 容器的功能差异
# 2. 兼容 Windows PowerShell，保证输出不乱码
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置 UTF-8 编码，防止乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "====================================================="
Write-Host "问题 005: BeanFactory vs ApplicationContext - 验证脚本"
Write-Host "====================================================="
Write-Host ""

# 设置基础 URL
$BASE_URL = "http://localhost:9510/interview-agent/question005"

# 步骤 1: 演示 ApplicationContext 功能
Write-Host "[步骤 1] 演示 ApplicationContext（高级容器）功能..."
Write-Host "命令：POST $BASE_URL/demo"
Write-Host "参数：containerType=ApplicationContext"
Write-Host ""

$appContextData = @{
    containerType = "ApplicationContext"
    lazyInit = $false
    remark = "演示企业级应用标准容器"
} | ConvertTo-Json

try {
    $appContextResponse = Invoke-RestMethod -Uri "$BASE_URL/demo" -Method Post -Body $appContextData -ContentType "application/json"
    Write-Host "容器类型：$($appContextResponse.containerType)"
    Write-Host "Bean 数量：$($appContextResponse.beanList.Count)"
    Write-Host ""
    Write-Host "功能对比:" -ForegroundColor Cyan
    Write-Host "  初始化时机：$($appContextResponse.comparison.initializationTiming)"
    Write-Host "  生命周期管理：$($appContextResponse.comparison.lifecycleManagement)"
    Write-Host "  国际化支持：$($appContextResponse.comparison.i18nSupport)"
    Write-Host "  事件机制：$($appContextResponse.comparison.eventMechanism)"
    Write-Host "  AOP 支持：$($appContextResponse.comparison.aopSupport)"
    Write-Host "  资源访问：$($appContextResponse.comparison.resourceAccess)"
    Write-Host "  父容器支持：$($appContextResponse.comparison.parentContainerSupport)"
    Write-Host ""
    Write-Host "额外功能:" -ForegroundColor Green
    $appContextResponse.additionalFeatures.GetEnumerator() | ForEach-Object {
        Write-Host "  $($_.Key): $($_.Value)"
    }
} catch {
    Write-Host "演示失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 2: 演示 BeanFactory 功能
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 2] 演示 BeanFactory（基础容器）功能..."
Write-Host "命令：POST $BASE_URL/demo"
Write-Host "参数：containerType=BeanFactory"
Write-Host ""

$beanFactoryData = @{
    containerType = "BeanFactory"
    lazyInit = $true
    remark = "演示基础容器（仅用于对比）"
} | ConvertTo-Json

try {
    $beanFactoryResponse = Invoke-RestMethod -Uri "$BASE_URL/demo" -Method Post -Body $beanFactoryData -ContentType "application/json"
    Write-Host "容器类型：$($beanFactoryResponse.containerType)"
    Write-Host "Bean 数量：$($beanFactoryResponse.beanList.Count)"
    Write-Host ""
    Write-Host "【说明】BeanFactory 是基础容器，不支持高级功能" -ForegroundColor Yellow
    Write-Host "额外功能：$($beanFactoryResponse.additionalFeatures['note'])"
} catch {
    Write-Host "演示失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 3: 获取详细对比说明
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 3] 获取详细对比说明..."
Write-Host "命令：GET $BASE_URL/comparison"
Write-Host ""

try {
    $comparison = Invoke-RestMethod -Uri "$BASE_URL/comparison" -Method Get
    Write-Host $comparison
} catch {
    Write-Host "获取说明失败：$_" -ForegroundColor Red
}

Write-Host ""
Write-Host ""

# 步骤 4: 总结
Write-Host "====================================================="
Write-Host "验证完成！"
Write-Host "====================================================="
Write-Host ""
Write-Host "【关键知识点总结】" -ForegroundColor Cyan
Write-Host "1. ApplicationContext 继承自 BeanFactory，是其超集" -ForegroundColor White
Write-Host "2. BeanFactory 延迟加载，ApplicationContext 启动预加载" -ForegroundColor White
Write-Host "3. ApplicationContext 提供完整的企业级功能" -ForegroundColor Green
Write-Host "4. 企业开发永远优先使用 ApplicationContext" -ForegroundColor Green
Write-Host "5. BeanFactory 仅用于资源极度受限的特殊场景" -ForegroundColor Yellow
Write-Host ""
Write-Host "【核心区别】" -ForegroundColor Cyan
Write-Host "- 初始化时机：延迟加载 vs 启动预加载"
Write-Host "- 生命周期管理：手动 vs 自动"
Write-Host "- 高级功能：无 vs 完整（国际化、事件、AOP、资源访问）"
Write-Host "- 使用场景：特殊场景 vs 企业开发标准"
Write-Host ""
