#!/bin/bash
# Set比较API测试脚本
# Linux兼容版本

echo "Testing Set Comparison APIs..."

echo ""
echo "1. Testing ordering comparison:"
curl -X POST "http://localhost:9510/collection/set-comparison/ordering" \
-H "Content-Type: application/json" \
-d '["banana", "apple", "cherry", "date", "elderberry"]'
echo -e "\n"

echo "2. Testing deduplication comparison:"
curl -X POST "http://localhost:9510/collection/set-comparison/deduplication" \
-H "Content-Type: application/json" \
-d '["apple", "banana", "apple", "cherry", "banana", "date"]'
echo -e "\n"

echo "3. Testing TreeSet range operations:"
curl -X POST "http://localhost:9510/collection/set-comparison/treeset-range-operations" \
-H "Content-Type: application/json" \
-d '[5, 2, 8, 1, 9, 3, 7, 4, 6]'
echo -e "\n"

echo "4. Testing performance comparison (default 10000 elements):"
curl -X GET "http://localhost:9510/collection/set-comparison/performance-comparison?count=10000"
echo -e "\n"

echo "5. Testing null handling:"
curl -X GET "http://localhost:9510/collection/set-comparison/null-handling"
echo -e "\n"

echo "Tests completed!"