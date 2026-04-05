@echo off
REM Set比较API测试脚本
REM Windows兼容版本

echo Testing Set Comparison APIs...

echo.
echo 1. Testing ordering comparison:
curl -X POST "http://localhost:9510/collection/set-comparison/ordering" ^
-H "Content-Type: application/json" ^
-d "[\"banana\", \"apple\", \"cherry\", \"date\", \"elderberry\"]"
echo.
echo.

echo 2. Testing deduplication comparison:
curl -X POST "http://localhost:9510/collection/set-comparison/deduplication" ^
-H "Content-Type: application/json" ^
-d "[\"apple\", \"banana\", \"apple\", \"cherry\", \"banana\", \"date\"]"
echo.
echo.

echo 3. Testing TreeSet range operations:
curl -X POST "http://localhost:9510/collection/set-comparison/treeset-range-operations" ^
-H "Content-Type: application/json" ^
-d "[5, 2, 8, 1, 9, 3, 7, 4, 6]"
echo.
echo.

echo 4. Testing performance comparison (default 10000 elements):
curl -X GET "http://localhost:9510/collection/set-comparison/performance-comparison?count=10000"
echo.
echo.

echo 5. Testing null handling:
curl -X GET "http://localhost:9510/collection/set-comparison/null-handling"
echo.
echo.

echo Tests completed!
pause