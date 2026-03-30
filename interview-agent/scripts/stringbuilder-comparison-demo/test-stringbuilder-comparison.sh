#!/bin/bash
# String、StringBuffer、StringBuilder 区别演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-stringbuilder-comparison.sh
#   Windows Git Bash: ./test-stringbuilder-comparison.sh
#   Windows CMD/PowerShell: bash test-stringbuilder-comparison.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/stringbuilder"

echo "=========================================="
echo "String、StringBuffer、StringBuilder 区别演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：String 类演示
echo "【测试 1】String 类演示 (STRING)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRING\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRING\"}"
echo ""
echo ""

# 测试 2：StringBuffer 类演示
echo "【测试 2】StringBuffer 类演示 (STRINGBUFFER)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRINGBUFFER\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRINGBUFFER\"}"
echo ""
echo ""

# 测试 3：StringBuilder 类演示
echo "【测试 3】StringBuilder 类演示 (STRINGBUILDER)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRINGBUILDER\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"STRINGBUILDER\"}"
echo ""
echo ""

# 测试 4：错误的字符串类型（测试兜底逻辑）
echo "【测试 4】错误的字符串类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"stringType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
