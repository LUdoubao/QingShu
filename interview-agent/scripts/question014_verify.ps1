# PowerShell 版本验证脚本 - 问题 014: InnoDB 锁的分类
# =====================================================
# 对应面试知识点：问题 014 - InnoDB 锁的分类
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示和验证 InnoDB 各种锁的使用机制
# 2. 兼容 Windows PowerShell，保证输出不乱码
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置 UTF-8 编码，防止乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "====================================================="
Write-Host "问题 014: InnoDB 锁的分类 - 验证脚本"
Write-Host "====================================================="
Write-Host ""

# 设置基础 URL
$BASE_URL = "http://localhost:9510/interview-agent/question014"

# 步骤 1: 初始化演示数据
Write-Host "[步骤 1] 初始化演示数据..."
Write-Host "命令：POST $BASE_URL/init"
Write-Host ""

try {
    $initResponse = Invoke-RestMethod -Uri "$BASE_URL/init" -Method Post -ContentType "application/json"
    Write-Host $initResponse
    Write-Host ""
    
    # 提取商品 ID（简化处理，使用默认值）
    $product1Id = 1
    $product2Id = 2
} catch {
    Write-Host "错误：初始化失败，请检查服务是否启动" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

# 步骤 2: 查询商品信息
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 2] 查询商品信息..."
Write-Host "命令：GET $BASE_URL/product/$product1Id"
Write-Host ""

try {
    $productInfo = Invoke-RestMethod -Uri "$BASE_URL/product/$product1Id" -Method Get
    Write-Host $productInfo
} catch {
    Write-Host "查询失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 3: 执行正常购买操作（使用索引）
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 3] 执行正常购买操作（演示行锁 - Record Lock）..."
Write-Host "命令：POST $BASE_URL/purchase"
Write-Host "参数：userId=1, productId=$product1Id, quantity=1, useIndex=true"
Write-Host ""

$purchaseData = @{
    userId = 1
    productId = $product1Id
    quantity = 1
    lockMode = "X"
    useIndex = $true
    remark = "演示行锁 - 使用索引"
} | ConvertTo-Json

try {
    $purchaseResponse = Invoke-RestMethod -Uri "$BASE_URL/purchase" -Method Post -Body $purchaseData -ContentType "application/json"
    $purchaseResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Host "购买失败：$_" -ForegroundColor Red
}

Write-Host ""

# 步骤 4: 执行不走索引的购买（演示表锁）
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 4] 执行不走索引的购买（演示表级锁）..."
Write-Host "命令：POST $BASE_URL/purchase"
Write-Host "参数：useIndex=false"
Write-Host ""

$noIndexData = @{
    userId = 1
    productId = $product1Id
    quantity = 1
    lockMode = "X"
    useIndex = $false
    remark = "演示表锁 - 全表扫描"
} | ConvertTo-Json

try {
    $noIndexResponse = Invoke-RestMethod -Uri "$BASE_URL/purchase" -Method Post -Body $noIndexData -ContentType "application/json"
    $noIndexResponse | ConvertTo-Json -Depth 10
} catch {
    Write-Host "购买失败：$_" -ForegroundColor Red
}

Write-Host ""
Write-Host "【说明】不走索引时，会锁住整个表的所有记录" -ForegroundColor Yellow
Write-Host ""

# 步骤 5: 验证库存变化
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 5] 验证库存变化..."
Write-Host ""

Write-Host "商品 1 信息:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/product/$product1Id" -Method Get
} catch {}

Write-Host ""
Write-Host "商品 2 信息:"
try {
    Invoke-RestMethod -Uri "$BASE_URL/product/$product2Id" -Method Get
} catch {}

Write-Host ""

# 步骤 6: 获取 InnoDB 锁的详细分类说明
Write-Host ""
Write-Host "====================================================="
Write-Host "[步骤 6] 获取 InnoDB 锁的详细分类说明..."
Write-Host "命令：GET $BASE_URL/classification"
Write-Host ""

try {
    $classification = Invoke-RestMethod -Uri "$BASE_URL/classification" -Method Get
    Write-Host $classification
} catch {
    Write-Host "获取分类说明失败：$_" -ForegroundColor Red
}

Write-Host ""
Write-Host ""

# 步骤 7: 总结
Write-Host "====================================================="
Write-Host "验证完成！"
Write-Host "====================================================="
Write-Host ""
Write-Host "【关键知识点总结】" -ForegroundColor Cyan
Write-Host "1. 按语义分：共享锁 (S)、排他锁 (X)、意向锁 (IS/IX)" -ForegroundColor White
Write-Host "2. 按粒度分：表级锁、行级锁（InnoDB 不支持页级锁）" -ForegroundColor White
Write-Host "3. 行级锁细分：Record Lock、Gap Lock、Next-Key Lock" -ForegroundColor White
Write-Host "4. 行锁必须依赖索引，否则退化为表锁" -ForegroundColor Yellow
Write-Host "5. 意向锁是表级锁，由 InnoDB 自动添加，无需人工干预" -ForegroundColor White
Write-Host ""
Write-Host "【实际观察锁的方法】" -ForegroundColor Cyan
Write-Host "- information_schema.INNODB_TRX: 查看事务信息"
Write-Host "- performance_schema.data_locks: MySQL 8.0+ 查看锁信息"
Write-Host "- SHOW ENGINE INNODB STATUS: 查看死锁信息"
Write-Host ""
