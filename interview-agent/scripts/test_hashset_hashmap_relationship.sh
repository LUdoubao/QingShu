#!/bin/bash
# HashSet和HashMap关系API测试脚本
# Linux兼容版本

echo "Testing HashSet and HashMap Relationship APIs..."

echo ""
echo "1. Testing duplicate handling:"
curl -X POST "http://localhost:9510/collection/relationship/duplicate-handling" \
-H "Content-Type: application/json" \
-d '["apple", "banana", "apple", "cherry", "banana"]'
echo -e "\n"

echo "2. Testing null handling:"
curl -X GET "http://localhost:9510/collection/relationship/null-handling"
echo -e "\n"

echo "3. Testing implementation principle explanation:"
curl -X GET "http://localhost:9510/collection/relationship/implementation-principle"
echo -e "\n"

echo "4. Testing performance comparison:"
curl -X GET "http://localhost:9510/collection/relationship/performance-comparison"
echo -e "\n"

echo "5. Testing iterator behavior comparison:"
curl -X GET "http://localhost:9510/collection/relationship/compare-iterator-behavior"
echo -e "\n"

echo "Tests completed!"