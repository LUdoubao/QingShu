#!/bin/bash
# ==和 equals() 区别演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-equals-comparison.sh
#   Windows Git Bash: ./test-equals-comparison.sh
#   Windows CMD/PowerShell: bash test-equals-comparison.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/equals"

echo "=========================================="
echo "==和 equals() 区别演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：基本类型比较
echo "【测试 1】基本类型比较 (BASIC)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"BASIC\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"BASIC\"}"
echo ""
echo ""

# 测试 2：字符串比较
echo "【测试 2】字符串比较 (STRING)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"STRING\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"STRING\"}"
echo ""
echo ""

# 测试 3：包装类比较
echo "【测试 3】包装类比较 (WRAPPER)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"WRAPPER\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"WRAPPER\"}"
echo ""
echo ""

# 测试 4：自定义对象比较
echo "【测试 4】自定义对象比较 (CUSTOM)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"CUSTOM\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"CUSTOM\"}"
echo ""
echo ""

# 测试 5：错误的比较类型（测试兜底逻辑）
echo "【测试 5】错误的比较类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"comparisonType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
