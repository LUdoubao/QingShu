#!/bin/bash
# final、finally、finalize 区别演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-finalkeyword-comparison.sh
#   Windows Git Bash: ./test-finalkeyword-comparison.sh
#   Windows CMD/PowerShell: bash test-finalkeyword-comparison.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/finalkeyword"

echo "=========================================="
echo "final、finally、finalize 区别演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：final 演示
echo "【测试 1】final 关键字演示 (FINAL)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINAL\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINAL\"}"
echo ""
echo ""

# 测试 2：finally 演示
echo "【测试 2】finally 关键字演示 (FINALLY)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINALLY\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINALLY\"}"
echo ""
echo ""

# 测试 3：finalize 演示
echo "【测试 3】finalize 关键字演示 (FINALIZE)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINALIZE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"FINALIZE\"}"
echo ""
echo ""

# 测试 4：错误的类型（测试兜底逻辑）
echo "【测试 4】错误的类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"keywordType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
