#!/bin/bash
# 方法重载和重写演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-method-feature.sh
#   Windows Git Bash: ./test-method-feature.sh
#   Windows CMD/PowerShell: bash test-method-feature.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/method"

echo "=========================================="
echo "方法重载和重写演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：演示方法重载
echo "【测试 1】演示方法重载 (OVERLOAD)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"OVERLOAD\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"OVERLOAD\"}"
echo ""
echo ""

# 测试 2：演示方法重写
echo "【测试 2】演示方法重写 (OVERRIDE)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"OVERRIDE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"OVERRIDE\"}"
echo ""
echo ""

# 测试 3：错误的特性类型（测试兜底逻辑）
echo "【测试 3】错误的特性类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
