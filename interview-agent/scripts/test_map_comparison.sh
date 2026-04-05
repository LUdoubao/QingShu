#!/bin/bash
# Map比较API测试脚本
# Linux兼容版本

echo "Testing Map Comparison APIs..."

echo ""
echo "1. Testing ordering comparison:"
curl -X POST "http://localhost:9510/collection/map-comparison/ordering" \
-H "Content-Type: application/json" \
-d '{"banana": "yellow", "apple": "red", "cherry": "red", "date": "brown", "elderberry": "purple"}'
echo -e "\n"

echo "2. Testing key uniqueness comparison:"
curl -X POST "http://localhost:9510/collection/map-comparison/key-uniqueness" \
-H "Content-Type: application/json" \
-d '[{"key":"apple","value":"fruit1"},{"key":"banana","value":"fruit2"},{"key":"apple","value":"fruit3"},{"key":"cherry","value":"fruit4"}]'
echo -e "\n"

echo "3. Testing TreeMap range operations:"
curl -X POST "http://localhost:9510/collection/map-comparison/treemap-range-operations" \
-H "Content-Type: application/json" \
-d '{"5": "five", "2": "two", "8": "eight", "1": "one", "9": "nine", "3": "three", "7": "seven", "4": "four", "6": "six"}'
echo -e "\n"

echo "4. Testing performance comparison (default 10000 elements):"
curl -X GET "http://localhost:9510/collection/map-comparison/performance-comparison?count=10000"
echo -e "\n"

echo "5. Testing null handling:"
curl -X GET "http://localhost:9510/collection/map-comparison/null-handling"
echo -e "\n"

echo "6. Testing LinkedHashMap access order:"
curl -X GET "http://localhost:9510/collection/map-comparison/linkedhashmap-access-order"
echo -e "\n"

echo "Tests completed!"