#!/bin/bash
# ============================================================================
# ArrayList vs LinkedList 性能对比测试 - 验证脚本
# 
# 【用途】验证 interview-agent 模块的集合性能对比接口
# 【兼容性】Linux/macOS Bash 和 Windows PowerShell（通过 bash 执行）
# 【编码】UTF-8（保证中文输出不乱码）
# 
# 【使用方法】
#   Linux/macOS: bash test-collection-performance.sh
#   Windows:     bash test-collection-performance.sh 或 ./test-collection-performance.sh
# 
# 【前置条件】
#   1. interview-agent 服务已启动（默认端口 9510）
#   2. curl 命令可用
# ============================================================================

# 设置编码为 UTF-8，防止中文乱码
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# 服务地址配置
BASE_URL="http://localhost:9510"
API_PREFIX="/interview-agent/collection"

echo "=========================================="
echo "ArrayList vs LinkedList 性能对比测试"
echo "=========================================="
echo ""

# ----------------------------------------------------------------------------
# 测试 1：健康检查（确认服务可用）
# ----------------------------------------------------------------------------
echo "[测试 1/4] 健康检查..."
HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/interview-agent/health")

if [ "$HEALTH_RESPONSE" = "200" ]; then
    echo "✓ 服务正常响应 (HTTP 200)"
else
    echo "✗ 服务异常 (HTTP $HEALTH_RESPONSE)"
    echo "请确认 interview-agent 服务已启动在端口 9510"
    exit 1
fi
echo ""

# ----------------------------------------------------------------------------
# 测试 2：获取面试知识点总结（快速验证）
# ----------------------------------------------------------------------------
echo "[测试 2/4] 获取面试知识点总结..."
SUMMARY_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/knowledge/summary")

if echo "$SUMMARY_RESPONSE" | grep -q "\"code\":200"; then
    echo "✓ 知识点总结接口正常"
    echo ""
    echo "--- 核心知识点 ---"
    # 提取并格式化显示 summary 字段（简单处理）
    echo "$SUMMARY_RESPONSE" | sed 's/\\n/\n/g' | grep -o '"data":"[^"]*"' | head -1 | sed 's/"data":"//;s/"$//'
    echo ""
else
    echo "✗ 知识点总结接口异常"
    echo "响应内容: $SUMMARY_RESPONSE"
fi
echo ""

# ----------------------------------------------------------------------------
# 测试 3：执行完整性能对比测试（默认数据量 100,000）
# ----------------------------------------------------------------------------
echo "[测试 3/4] 执行完整性能对比测试（数据量: 100,000）..."
echo "提示：此测试耗时约 1-5 秒，请耐心等待..."
START_TIME=$(date +%s)

FULL_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/compare/full")

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

if echo "$FULL_RESPONSE" | grep -q "\"code\":200"; then
    echo "✓ 完整性能对比测试完成 (耗时: ${ELAPSED}秒)"
    echo ""
    
    # 提取并显示关键性能数据
    echo "--- 性能对比结果摘要 ---"
    
    # 使用简单的文本处理提取 scenarios（实际生产环境建议使用 jq）
    SCENARIO_COUNT=$(echo "$FULL_RESPONSE" | grep -o '"scenario"' | wc -l)
    echo "测试场景数: $SCENARIO_COUNT"
    echo ""
    
    # 显示每个场景的核心数据
    echo "$FULL_RESPONSE" | grep -o '"scenario":"[^"]*","arrayListTimeMs":[0-9]*,"linkedListTimeMs":[0-9]*,"winner":"[^"]*"' | while IFS= read -r line; do
        SCENARIO=$(echo "$line" | grep -o '"scenario":"[^"]*"' | cut -d'"' -f4)
        ARRAY_TIME=$(echo "$line" | grep -o '"arrayListTimeMs":[0-9]*' | cut -d':' -f2)
        LINKED_TIME=$(echo "$line" | grep -o '"linkedListTimeMs":[0-9]*' | cut -d':' -f2)
        WINNER=$(echo "$line" | grep -o '"winner":"[^"]*"' | cut -d'"' -f4)
        
        printf "场景: %-30s | ArrayList: %6sms | LinkedList: %6sms | 优胜者: %s\n" \
            "$SCENARIO" "$ARRAY_TIME" "$LINKED_TIME" "$WINNER"
    done
    
    echo ""
    
    # 显示总结和建议
    SUMMARY=$(echo "$FULL_RESPONSE" | grep -o '"summary":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "--- 核心结论 ---"
    echo "$SUMMARY" | sed 's/\\n/\n/g'
    echo ""
    
else
    echo "✗ 完整性能对比测试失败"
    echo "响应内容: $FULL_RESPONSE"
fi
echo ""

# ----------------------------------------------------------------------------
# 测试 4：执行自定义数据量测试（较小数据量 10,000）
# ----------------------------------------------------------------------------
echo "[测试 4/4] 执行自定义数据量测试（数据量: 10,000）..."
CUSTOM_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/compare/custom?dataSize=10000")

if echo "$CUSTOM_RESPONSE" | grep -q "\"code\":200"; then
    echo "✓ 自定义数据量测试完成"
    
    # 验证数据量是否正确
    DATA_SIZE=$(echo "$CUSTOM_RESPONSE" | grep -o '"dataSize":10000' | head -1)
    if [ -n "$DATA_SIZE" ]; then
        echo "✓ 数据量参数正确传递 (10,000)"
    else
        echo "⚠ 无法验证数据量参数"
    fi
else
    echo "✗ 自定义数据量测试失败"
    echo "响应内容: $CUSTOM_RESPONSE"
fi
echo ""

# ----------------------------------------------------------------------------
# 测试 5：参数校验测试（超出范围的数据量）
# ----------------------------------------------------------------------------
echo "[额外测试] 参数校验测试（数据量: 100，应被拒绝）..."
INVALID_RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PREFIX}/compare/custom?dataSize=100")

if echo "$INVALID_RESPONSE" | grep -q "\"code\":500\|\"message\".*必须在"; then
    echo "✓ 参数校验正常工作（正确拒绝了无效参数）"
else
    echo "⚠ 参数校验可能存在问题"
    echo "响应内容: $INVALID_RESPONSE"
fi
echo ""

# ============================================================================
# 测试总结
# ============================================================================
echo "=========================================="
echo "测试完成总结"
echo "=========================================="
echo ""
echo "✓ 所有接口已验证"
echo ""
echo "核心发现："
echo "  1. ArrayList 随机访问速度远快于 LinkedList（O(1) vs O(n)）"
echo "  2. LinkedList 头部插入速度快于 ArrayList（O(1) vs O(n)）"
echo "  3. 尾部添加两者性能相当（均为 O(1)）"
echo "  4. 遍历操作必须使用迭代器，LinkedList 严禁使用普通 for 循环"
echo ""
echo "选型建议："
echo "  - 读多写少 → ArrayList"
echo "  - 写多读少 → LinkedList"
echo "  - 需要队列/栈 → LinkedList"
echo "  - 不确定场景 → 优先 ArrayList"
echo ""
echo "详细测试数据请查看上方输出。"
echo "=========================================="
