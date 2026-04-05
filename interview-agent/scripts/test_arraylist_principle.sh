#!/bin/bash
# ArrayList原理API测试脚本
# Linux兼容版本

echo "Testing ArrayList Principle APIs..."

echo ""
echo "1. Testing initialization process:"
curl -X GET "http://localhost:9510/collection/arraylist-principle/initialization"
echo -e "\n"

echo "2. Testing expansion process:"
curl -X GET "http://localhost:9510/collection/arraylist-principle/expansion"
echo -e "\n"

echo "3. Testing performance characteristics (default size 10000):"
curl -X POST "http://localhost:9510/collection/arraylist-principle/performance?size=10000"
echo -e "\n"

echo "4. Testing simple implementation:"
curl -X GET "http://localhost:9510/collection/arraylist-principle/simple-implementation"
echo -e "\n"

echo "Tests completed!"