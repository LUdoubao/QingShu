# PowerShell 版本验证脚本 - 问题 012: undo log、redo log、binlog 的区别
# =====================================================
# 对应面试知识点：问题 012 - 三种日志的区别
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示和验证 MySQL 三种日志的工作机制
# 2. 兼容 Windows PowerShell，保证输出不乱码
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置 UTF-8 编码，防止乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "====================================================="
Write-Host "问题 012: undo log, redo log, binlog 的区别 - 验证脚本"
Write-Host "====================================================="
Write-Host ""

# 设置基础 URL
$BASE_URL = "http://localhost:9510/interview-agent/question012"

# 步骤 1: 初始化演示数据
Write-Host "[步骤 1] 初始化演示数据..."
Write-Host "命令：POST $BASE_URL/init"
Write-Host ""

try {
    $initResponse = Invoke-RestMethod -Uri "$BASE_URL/init" -Method Post -ContentType "application/json"
    Write-Host $initResponse
    Write-Host ""
    
    # 提取账户 ID（简化处理，使用默认值）
    $account1Id = 1
    $account2Id = 2
} catch {
    Write-Host "错误：初始化失败，请检查服务是否启动" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

# 步骤 2: 查询账户信息
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 2] 查询账户信息..."
Write-Host "命令：GET $BASE_URL/account/$account1Id"
Write-Host ""

try {
    $accountInfo = Invoke-RestMethod -Uri "$BASE_URL/account/$account1Id" -Method Get
    Write-Host $accountInfo
} catch {
    Write-Host "查询失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 3: 执行正常转账操作
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 3] 执行正常转账操作（演示三种日志的完整流程）..."
Write-Host "命令：POST $BASE_URL/transfer"
Write-Host "参数：fromAccountId=$account1Id, toAccountId=$account2Id, amount=100"
Write-Host ""

$transferData = @{
    fromAccountId = $account1Id
    toAccountId = $account2Id
    amount = 100
    remark = "正常转账演示"
} | ConvertTo-Json

try {
    $transferResponse = Invoke-RestMethod -Uri "$BASE_URL/transfer" -Method Post -Body $transferData -ContentType "application/json"
    $transferResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Host "转账失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 4: 再次查询账户信息
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 4] 查询转账后的账户信息..."
Write-Host ""

Write-Host "转出账户信息:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/account/$account1Id" -Method Get
} catch {}

Write-Host ""
Write-Host "转入账户信息:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/account/$account2Id" -Method Get
} catch {}

Write-Host ""

# 步骤 5: 模拟异常转账
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 5] 模拟异常转账（演示 undo log 的回滚功能）..."
Write-Host "命令：POST $BASE_URL/transfer"
Write-Host "参数：simulateException=true"
Write-Host ""

$exceptionData = @{
    fromAccountId = $account1Id
    toAccountId = $account2Id
    amount = 50
    simulateException = $true
    remark = "模拟异常 - 测试 undo log 回滚"
} | ConvertTo-Json

try {
    $exceptionResponse = Invoke-RestMethod -Uri "$BASE_URL/transfer" -Method Post -Body $exceptionData -ContentType "application/json"
    $exceptionResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Host "预期中的异常：事务回滚" -ForegroundColor Yellow
    Write-Host $_.Exception.Message
}

Write-Host ""
Write-Host "【说明】由于 simulateException=true，事务会回滚"
Write-Host "undo log 会将数据恢复到事务开始前的状态"
Write-Host ""

# 步骤 6: 验证回滚后的账户余额
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 6] 验证回滚后的账户余额（应该与步骤 4 相同）..."
Write-Host ""

Write-Host "转出账户信息（应保持不变）:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/account/$account1Id" -Method Get
} catch {}

Write-Host ""
Write-Host "转入账户信息（应保持不变）:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/account/$account2Id" -Method Get
} catch {}

Write-Host ""

# 步骤 7: 获取三种日志的详细对比说明
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 7] 获取三种日志的详细对比说明..."
Write-Host "命令：GET $BASE_URL/comparison"
Write-Host ""

try {
    $comparison = Invoke-RestMethod -Uri "$BASE_URL/comparison" -Method Get
    Write-Host $comparison
} catch {
    Write-Host "获取对比说明失败：$_" -ForegroundColor Red
}

Write-Host ""
Write-Host ""

# 步骤 8: 总结
Write-Host "====================================================="
Write-Host "验证完成！"
Write-Host "====================================================="
Write-Host ""
Write-Host "【关键知识点总结】" -ForegroundColor Cyan
Write-Host "1. undo log: 记录反向操作，用于事务回滚（步骤 5 演示）"
Write-Host "2. redo log: 记录物理修改，用于崩溃恢复（WAL 技术）"
Write-Host "3. binlog: 记录 SQL 语句，用于主从复制（追加写入）"
Write-Host ""
Write-Host "【两阶段提交流程】" -ForegroundColor Cyan
Write-Host "Prepare: 写 redo log -> Commit: 写 binlog -> Commit: 提交 redo log"
Write-Host ""
Write-Host "【观察实际日志的方法】" -ForegroundColor Cyan
Write-Host "- undo log: InnoDB 自动管理，无法直接查看"
Write-Host "- redo log: 查看 ib_logfile0, ib_logfile1 文件"
Write-Host "- binlog: 使用 mysqlbinlog 工具查看"
Write-Host "  示例：mysqlbinlog C:\ProgramData\MySQL\MySQL Server X.X\Data\mysql-bin.000001"
Write-Host ""
