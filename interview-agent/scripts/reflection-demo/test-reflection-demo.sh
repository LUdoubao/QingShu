#!/bin/bash
# Java 反射机制演示 - 验证脚本
# 适用于 Linux/Mac 和 Windows（Git Bash）
# 使用方法：
#   Linux/Mac: ./test-reflection-demo.sh
#   Windows Git Bash: ./test-reflection-demo.sh
#   Windows CMD/PowerShell: bash test-reflection-demo.sh

# 设置基础 URL
BASE_URL="http://localhost:9510/interview-agent/reflection"

echo "=========================================="
echo "Java 反射机制演示 - 验证脚本"
echo "=========================================="
echo ""

# 测试 1：获取 Class 对象
echo "【测试 1】获取 Class 对象 (GET_CLASS)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"GET_CLASS\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"GET_CLASS\"}"
echo ""
echo ""

# 测试 2：创建实例
echo "【测试 2】创建实例 (CREATE_INSTANCE)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"CREATE_INSTANCE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"CREATE_INSTANCE\"}"
echo ""
echo ""

# 测试 3：调用方法
echo "【测试 3】调用方法 (INVOKE_METHOD)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"INVOKE_METHOD\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"INVOKE_METHOD\"}"
echo ""
echo ""

# 测试 4：访问属性
echo "【测试 4】访问属性 (ACCESS_FIELD)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"ACCESS_FIELD\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"ACCESS_FIELD\"}"
echo ""
echo ""

# 测试 5：获取构造器
echo "【测试 5】获取构造器 (GET_CONSTRUCTOR)"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"GET_CONSTRUCTOR\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"GET_CONSTRUCTOR\"}"
echo ""
echo ""

# 测试 6：错误的类型（测试兜底逻辑）
echo "【测试 6】错误的类型（测试兜底逻辑）"
echo "----------------------------------------"
curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"INVALID_TYPE\"}" | python3 -m json.tool 2>/dev/null || curl -X POST "${BASE_URL}/demonstrate" \
  -H "Content-Type: application/json" \
  -d "{\"operationType\":\"INVALID_TYPE\"}"
echo ""
echo ""

echo "=========================================="
echo "所有测试完成！"
echo "=========================================="
