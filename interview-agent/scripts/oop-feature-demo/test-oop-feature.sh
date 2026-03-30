#!/bin/bash
# 面向对象三大特征演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-oop-feature.sh
#   Windows Git Bash: ./test-oop-feature.sh
#   Windows CMD/PowerShell: bash test-oop-feature.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/oop"

echo "=========================================="
echo "面向对象三大特征演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：演示封装特性
echo "【测试 1】演示封装特性 (ENCAPSULATION)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"ENCAPSULATION\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"ENCAPSULATION\"}"
echo ""
echo ""

# 测试 2：演示继承特性
echo "【测试 2】演示继承特性 (INHERITANCE)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"INHERITANCE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"INHERITANCE\"}"
echo ""
echo ""

# 测试 3：演示多态特性
echo "【测试 3】演示多态特性 (POLYMORPHISM)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"POLYMORPHISM\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"featureType\":\"POLYMORPHISM\"}"
echo ""
echo ""

# 测试 4：错误的特征类型（测试兜底逻辑）
echo "【测试 4】错误的特征类型（测试兜底逻辑）"
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
