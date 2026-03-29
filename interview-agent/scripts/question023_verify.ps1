# PowerShell 版本验证脚本 - 问题 023: Spring 事件机制
# =====================================================
# 对应面试知识点：问题 023 - Spring 事件机制
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示 Spring 事件的发布和监听机制
# 2. 兼容 Windows PowerShell，保证输出不乱码
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置 UTF-8 编码，防止乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "====================================================="
Write-Host "问题 023: Spring 事件机制 - 验证脚本"
Write-Host "====================================================="
Write-Host ""

# 设置基础 URL
$BASE_URL = "http://localhost:9510/interview-agent/question023"

# 步骤 1: 同步方式注册用户（触发事件）
Write-Host "[步骤 1] 同步方式注册用户（触发事件监听）..."
Write-Host "命令：POST $BASE_URL/register"
Write-Host "参数：userName=张三，email=zhangsan@example.com, async=false"
Write-Host ""

$syncRegisterData = @{
    userName = "张三"
    email = "zhangsan@example.com"
    async = $false
    remark = "同步方式演示事件机制"
} | ConvertTo-Json

try {
    $syncResponse = Invoke-RestMethod -Uri "$BASE_URL/register" -Method Post -Body $syncRegisterData -ContentType "application/json"
    Write-Host "注册成功！" -ForegroundColor Green
    Write-Host "用户 ID: $($syncResponse.userId)"
    Write-Host "用户名称：$($syncResponse.userName)"
    Write-Host ""
    Write-Host "事件处理详情:" -ForegroundColor Cyan
    
    foreach ($info in $syncResponse.eventProcessInfos) {
        Write-Host ""
        Write-Host "  监听器：$($info.listenerName)" -ForegroundColor Yellow
        Write-Host "  处理方式：$($info.processType)"
        Write-Host "  状态：$($info.status)"
        Write-Host "  耗时：$($info.processTime)ms"
        Write-Host "  描述：$($info.description)"
    }
} catch {
    Write-Host "注册失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 2: 异步方式注册用户（触发异步事件）
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 2] 异步方式注册用户（@Async 异步监听）..."
Write-Host "命令：POST $BASE_URL/register"
Write-Host "参数：userName=李四，email=lisi@example.com, async=true"
Write-Host ""

$asyncRegisterData = @{
    userName = "李四"
    email = "lisi@example.com"
    async = $true
    remark = "异步方式演示事件机制"
} | ConvertTo-Json

try {
    $asyncResponse = Invoke-RestMethod -Uri "$BASE_URL/register" -Method Post -Body $asyncRegisterData -ContentType "application/json"
    Write-Host "注册成功！" -ForegroundColor Green
    Write-Host "用户 ID: $($asyncResponse.userId)"
    Write-Host "用户名称：$($asyncResponse.userName)"
    Write-Host ""
    Write-Host "事件处理详情:" -ForegroundColor Cyan
    
    foreach ($info in $asyncResponse.eventProcessInfos) {
        Write-Host ""
        Write-Host "  监听器：$($info.listenerName)" -ForegroundColor Yellow
        Write-Host "  处理方式：$($info.processType)"
        Write-Host "  状态：$($info.status)"
        Write-Host "  耗时：$($info.processTime)ms"
        Write-Host "  描述：$($info.description)"
    }
} catch {
    Write-Host "注册失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 3: 获取详细说明文档
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 3] 获取详细说明文档..."
Write-Host "命令：GET $BASE_URL/explanation"
Write-Host ""

try {
    $explanation = Invoke-RestMethod -Uri "$BASE_URL/explanation" -Method Get
    Write-Host $explanation
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
Write-Host "1. Spring 事件机制基于观察者模式（发布/订阅）" -ForegroundColor White
Write-Host "2. 三要素：事件、发布器（ApplicationEventPublisher）、监听器（@EventListener）" -ForegroundColor White
Write-Host "3. 支持同步和异步两种处理方式" -ForegroundColor Green
Write-Host "4. 适用于应用内模块解耦，不等同于 MQ" -ForegroundColor Yellow
Write-Host "5. 可配合@Async 实现异步处理，提升性能" -ForegroundColor Green
Write-Host ""
Write-Host "【使用场景】" -ForegroundColor Cyan
Write-Host "- 模块解耦：如用户注册后发送邮件、积分、统计等"
Write-Host "- 异步处理：耗时的后台操作"
Write-Host "- 扩展功能：新增监听器无需修改原有代码"
Write-Host ""
