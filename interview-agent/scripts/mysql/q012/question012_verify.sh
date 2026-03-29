#!/bin/bash
# =====================================================
# 问题 012: undo log、redo log、binlog 的区别 - 验证脚本
# =====================================================
# 对应面试知识点：问题 012 - 三种日志的区别
# 创建时间：2026-03-29
# 
# 【说明】
# 1. 本脚本用于演示和验证 MySQL 三种日志的工作机制
# 2. 兼容 Windows CMD/PowerShell 和 Linux Bash
# 3. 执行前确保服务已启动：http://localhost:9510
# =====================================================

# 设置编码，防止乱码（Linux/Mac）
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/question012"

echo "====================================================="
echo "问题 012: undo log、redo log、binlog 的区别 - 验证脚本"
echo "====================================================="
echo ""

# 步骤 1: 初始化演示数据
echo "[步骤 1] 初始化演示数据..."
echo "命令：POST ${BASE_URL}/init"
echo ""

INIT_RESPONSE=$(curl -s -X POST "${BASE_URL}/init")

if [ $? -eq 0 ]; then
    echo "响应结果:"
    echo "${INIT_RESPONSE}"
    echo ""
    
    # 提取账户 ID（简单处理，实际可能需要 jq 工具）
    ACCOUNT1_ID=$(echo "${INIT_RESPONSE}" | grep -oP 'ID=\K\d+' | head -1)
    ACCOUNT2_ID=$(echo "${INIT_RESPONSE}" | grep -oP 'ID=\K\d+' | tail -1)
    
    if [ -z "$ACCOUNT1_ID" ] || [ -z "$ACCOUNT2_ID" ]; then
        # 如果无法提取，使用默认值
        ACCOUNT1_ID=1
        ACCOUNT2_ID=2
    fi
else
    echo "错误：初始化失败，请检查服务是否启动"
    exit 1
fi

# 步骤 2: 查询账户信息
echo ""
echo "====================================================="
echo "[步骤 2] 查询账户信息..."
echo "命令：GET ${BASE_URL}/account/${ACCOUNT1_ID}"
echo ""

curl -s "${BASE_URL}/account/${ACCOUNT1_ID}" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/account/${ACCOUNT1_ID}"

echo ""

# 步骤 3: 执行正常转账操作
echo ""
echo "====================================================="
echo "[步骤 3] 执行正常转账操作（演示三种日志的完整流程）..."
echo "命令：POST ${BASE_URL}/transfer"
echo "参数：fromAccountId=${ACCOUNT1_ID}, toAccountId=${ACCOUNT2_ID}, amount=100"
echo ""

TRANSFER_DATA="{\"fromAccountId\":${ACCOUNT1_ID},\"toAccountId\":${ACCOUNT2_ID},\"amount\":100,\"remark\":\"正常转账演示\"}"

TRANSFER_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "${TRANSFER_DATA}" \
  "${BASE_URL}/transfer")

echo "响应结果:"
echo "${TRANSFER_RESPONSE}" | python3 -m json.tool 2>/dev/null || \
echo "${TRANSFER_RESPONSE}"

echo ""

# 步骤 4: 再次查询账户信息（验证余额变化）
echo ""
echo "====================================================="
echo "[步骤 4] 查询转账后的账户信息..."
echo "命令：GET ${BASE_URL}/account/${ACCOUNT1_ID} 和 ${BASE_URL}/account/${ACCOUNT2_ID}"
echo ""

echo "转出账户信息:"
curl -s "${BASE_URL}/account/${ACCOUNT1_ID}" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/account/${ACCOUNT1_ID}"
echo ""
echo ""

echo "转入账户信息:"
curl -s "${BASE_URL}/account/${ACCOUNT2_ID}" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/account/${ACCOUNT2_ID}"
echo ""

# 步骤 5: 模拟异常转账（演示 undo log 回滚）
echo ""
echo "====================================================="
echo "[步骤 5] 模拟异常转账（演示 undo log 的回滚功能）..."
echo "命令：POST ${BASE_URL}/transfer"
echo "参数：simulateException=true"
echo ""

EXCEPTION_DATA="{\"fromAccountId\":${ACCOUNT1_ID},\"toAccountId\":${ACCOUNT2_ID},\"amount\":50,\"simulateException\":true,\"remark\":\"模拟异常 - 测试 undo log 回滚\"}"

EXCEPTION_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "${EXCEPTION_DATA}" \
  "${BASE_URL}/transfer")

echo "响应结果:"
echo "${EXCEPTION_RESPONSE}" | python3 -m json.tool 2>/dev/null || \
echo "${EXCEPTION_RESPONSE}"

echo ""
echo "【说明】由于 simulateException=true，事务会回滚"
echo "undo log 会将数据恢复到事务开始前的状态"
echo ""

# 步骤 6: 验证回滚后的账户余额
echo ""
echo "====================================================="
echo "[步骤 6] 验证回滚后的账户余额（应该与步骤 4 相同）..."
echo ""

echo "转出账户信息（应保持不变）:"
curl -s "${BASE_URL}/account/${ACCOUNT1_ID}" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/account/${ACCOUNT1_ID}"
echo ""
echo ""

echo "转入账户信息（应保持不变）:"
curl -s "${BASE_URL}/account/${ACCOUNT2_ID}" | python3 -m json.tool 2>/dev/null || \
curl -s "${BASE_URL}/account/${ACCOUNT2_ID}"
echo ""

# 步骤 7: 获取三种日志的详细对比说明
echo ""
echo "====================================================="
echo "[步骤 7] 获取三种日志的详细对比说明..."
echo "命令：GET ${BASE_URL}/comparison"
echo ""

curl -s "${BASE_URL}/comparison"

echo ""
echo ""

# 步骤 8: 总结
echo "====================================================="
echo "验证完成！"
echo "====================================================="
echo ""
echo "【关键知识点总结】"
echo "1. undo log: 记录反向操作，用于事务回滚（步骤 5 演示）"
echo "2. redo log: 记录物理修改，用于崩溃恢复（WAL 技术）"
echo "3. binlog: 记录 SQL 语句，用于主从复制（追加写入）"
echo ""
echo "【两阶段提交流程】"
echo "Prepare: 写 redo log -> Commit: 写 binlog -> Commit: 提交 redo log"
echo ""
echo "【观察实际日志的方法】"
echo "- undo log: InnoDB 自动管理，无法直接查看"
echo "- redo log: 查看 ib_logfile0, ib_logfile1 文件"
echo "- binlog: 使用 mysqlbinlog 工具查看"
echo "  示例：mysqlbinlog /var/lib/mysql/mysql-bin.000001 | less"
echo ""
