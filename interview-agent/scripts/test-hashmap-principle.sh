#!/bin/bash

# HashMap JDK 1.8 原理演示 - 验证脚本
# 适用于 Linux/macOS 系统

echo "========================================"
echo "HashMap JDK 1.8 原理演示 - 验证脚本"
echo "========================================"
echo ""

BASE_URL="http://localhost:9510"

echo "[1/7] 测试健康检查..."
curl -s ${BASE_URL}/interview-agent/health
echo ""
echo ""

echo "[2/7] 演示底层数据结构..."
curl -s ${BASE_URL}/collection/hashmap-principle/structure | python3 -m json.tool 2>/dev/null || curl -s ${BASE_URL}/collection/hashmap-principle/structure
echo ""
echo ""

echo "[3/7] 演示哈希计算过程..."
curl -s "${BASE_URL}/collection/hashmap-principle/hash-calculation?testKey=hello" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/collection/hashmap-principle/hash-calculation?testKey=hello"
echo ""
echo ""

echo "[4/7] 演示put操作..."
curl -s -X POST ${BASE_URL}/collection/hashmap-principle/put-operation \
  -H "Content-Type: application/json" \
  -d '{"keys":["apple","banana","cherry"],"values":["苹果","香蕉","樱桃"]}' | python3 -m json.tool 2>/dev/null || curl -s -X POST ${BASE_URL}/collection/hashmap-principle/put-operation -H "Content-Type: application/json" -d '{"keys":["apple","banana","cherry"],"values":["苹果","香蕉","樱桃"]}'
echo ""
echo ""

echo "[5/7] 演示get操作..."
curl -s "${BASE_URL}/collection/hashmap-principle/get-operation?key=apple" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/collection/hashmap-principle/get-operation?key=apple"
echo ""
echo ""

echo "[6/7] 演示扩容过程..."
curl -s ${BASE_URL}/collection/hashmap-principle/expansion | python3 -m json.tool 2>/dev/null || curl -s ${BASE_URL}/collection/hashmap-principle/expansion
echo ""
echo ""

echo "[7/7] JDK版本对比..."
curl -s ${BASE_URL}/collection/hashmap-principle/jdk-comparison | python3 -m json.tool 2>/dev/null || curl -s ${BASE_URL}/collection/hashmap-principle/jdk-comparison
echo ""
echo ""

echo "========================================"
echo "所有测试完成！"
echo "========================================"
