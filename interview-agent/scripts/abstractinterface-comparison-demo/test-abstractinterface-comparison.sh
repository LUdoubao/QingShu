#!/bin/bash
# 接口和抽象类区别演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-abstractinterface-comparison.sh
#   Windows Git Bash: ./test-abstractinterface-comparison.sh
#   Windows CMD/PowerShell: bash test-abstractinterface-comparison.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/abstractinterface"

echo "=========================================="
echo "接口和抽象类区别演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：抽象类演示
echo "【测试 1】抽象类演示 (ABSTRACT_CLASS)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"ABSTRACT_CLASS\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"ABSTRACT_CLASS\"}"
echo ""
echo ""

# 测试 2：接口演示
echo "【测试 2】接口演示 (INTERFACE)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"INTERFACE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"INTERFACE\"}"
echo ""
echo ""

# 测试 3：错误的类型（测试兜底逻辑）
echo "【测试 3】错误的类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"targetType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
