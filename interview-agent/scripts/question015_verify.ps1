# PowerShell 版本验证脚本 - 问题 015: Next-Key Lock 与幻读
# =====================================================
# 对应面试知识点：问题 015 - Next-Key Lock 与幻读
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示和验证 Next-Key Lock 如何避免幻读
# 2. 兼容 Windows PowerShell，保证输出不乱码
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置 UTF-8 编码，防止乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "====================================================="
Write-Host "问题 015: Next-Key Lock 与幻读 - 验证脚本"
Write-Host "====================================================="
Write-Host ""

# 设置基础 URL
$BASE_URL = "http://localhost:9510/interview-agent/question015"

# 步骤 1: 初始化演示数据
Write-Host "[步骤 1] 初始化演示数据..."
Write-Host "命令：POST $BASE_URL/init"
Write-Host ""

try {
    $initResponse = Invoke-RestMethod -Uri "$BASE_URL/init" -Method Post -ContentType "application/json"
    Write-Host $initResponse
    Write-Host ""
} catch {
    Write-Host "错误：初始化失败，请检查服务是否启动" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

# 步骤 2: 使用当前读（Next-Key Lock）查询
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 2] 使用当前读查询（触发 Next-Key Lock）..."
Write-Host "命令：POST $BASE_URL/query"
Write-Host "参数：sessionId=session1, queryType=CURRENT_READ, minId=1, maxId=5"
Write-Host ""

$currentReadData = @{
    sessionId = "session1"
    queryType = "CURRENT_READ"
    minId = 1
    maxId = 5
    useNextKeyLock = $true
    remark = "演示 Next-Key Lock 避免幻读"
} | ConvertTo-Json

try {
    $currentReadResponse = Invoke-RestMethod -Uri "$BASE_URL/query" -Method Post -Body $currentReadData -ContentType "application/json"
    Write-Host "查询结果数量：$($currentReadResponse.resultCount)"
    Write-Host "锁类型：$($currentReadResponse.lockInfo.lockType)"
    Write-Host "锁定范围：$($currentReadResponse.lockInfo.lockRange)"
    Write-Host "能否防止幻读：$($currentReadResponse.lockInfo.preventsPhantom)"
    Write-Host ""
    Write-Host "【说明】使用 FOR UPDATE 触发 Next-Key Lock，锁定范围内的所有记录和间隙" -ForegroundColor Green
} catch {
    Write-Host "查询失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 3: 使用快照读查询（不使用锁）
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 3] 使用快照读查询（可能发生幻读）..."
Write-Host "命令：POST $BASE_URL/query"
Write-Host "参数：sessionId=session2, queryType=SNAPSHOT_READ"
Write-Host ""

$snapshotReadData = @{
    sessionId = "session2"
    queryType = "SNAPSHOT_READ"
    minId = 1
    maxId = 5
    useNextKeyLock = $false
    remark = "演示快照读可能发生幻读"
} | ConvertTo-Json

try {
    $snapshotReadResponse = Invoke-RestMethod -Uri "$BASE_URL/query" -Method Post -Body $snapshotReadData -ContentType "application/json"
    Write-Host "查询结果数量：$($snapshotReadResponse.resultCount)"
    Write-Host "锁类型：$($snapshotReadResponse.lockInfo.lockType)"
    Write-Host "能否防止幻读：$($snapshotReadResponse.lockInfo.preventsPhantom)"
    Write-Host ""
    Write-Host "【说明】普通 SELECT 不加锁，通过 MVCC 读取，可能发生幻读" -ForegroundColor Yellow
} catch {
    Write-Host "查询失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 4: 模拟插入操作（测试锁的效果）
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 4] 模拟插入操作（测试 Next-Key Lock 的阻塞效果）..."
Write-Host "命令：POST $BASE_URL/insert"
Write-Host "参数：sessionId=session3, userName=测试用户，balance=1500"
Write-Host ""

try {
    $insertResponse = Invoke-RestMethod -Uri "$BASE_URL/insert?sessionId=session3&userName=测试用户&balance=1500" -Method Post
    Write-Host $insertResponse
    Write-Host ""
    Write-Host "【说明】如果在事务 A 执行 FOR UPDATE 后插入，会被阻塞" -ForegroundColor Cyan
} catch {
    Write-Host "插入失败（可能是被锁阻塞）：$_" -ForegroundColor Yellow
}

Write-Host ""

# 步骤 5: 获取 Next-Key Lock 的详细说明
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 5] 获取 Next-Key Lock 的详细说明..."
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

# 步骤 6: 总结
Write-Host "====================================================="
Write-Host "验证完成！"
Write-Host "====================================================="
Write-Host ""
Write-Host "【关键知识点总结】" -ForegroundColor Cyan
Write-Host "1. Next-Key Lock = Record Lock + Gap Lock" -ForegroundColor White
Write-Host "2. Record Lock 锁记录，Gap Lock 锁间隙" -ForegroundColor White
Write-Host "3. Next-Key Lock 能彻底杜绝幻读" -ForegroundColor Green
Write-Host "4. 只在 REPEATABLE READ 的当前读场景下使用" -ForegroundColor White
Write-Host "5. 是以牺牲并发度为代价的一致性保证" -ForegroundColor Yellow
Write-Host ""
Write-Host "【实际观察 Next-Key Lock 的方法】" -ForegroundColor Cyan
Write-Host "- information_schema.INNODB_TRX: 查看事务信息"
Write-Host "- performance_schema.data_locks: MySQL 8.0+ 查看锁信息"
Write-Host "- SHOW ENGINE INNODB STATUS: 查看详细的锁状态"
Write-Host ""
Write-Host "【手动测试建议】" -ForegroundColor Cyan
Write-Host "开启两个 MySQL 会话："
Write-Host "会话 1: BEGIN; SELECT ... FOR UPDATE;"
Write-Host "会话 2: INSERT INTO ... (在锁定范围内)"
Write-Host "观察会话 2 是否被阻塞"
Write-Host ""
