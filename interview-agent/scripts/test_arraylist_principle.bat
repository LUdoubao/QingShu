@echo off
REM ArrayList原理API测试脚本
REM Windows兼容版本

echo Testing ArrayList Principle APIs...

echo.
echo 1. Testing initialization process:
curl -X GET "http://localhost:9510/collection/arraylist-principle/initialization"
echo.
echo.

echo 2. Testing expansion process:
curl -X GET "http://localhost:9510/collection/arraylist-principle/expansion"
echo.
echo.

echo 3. Testing performance characteristics (default size 10000):
curl -X POST "http://localhost:9510/collection/arraylist-principle/performance?size=10000"
echo.
echo.

echo 4. Testing simple implementation:
curl -X GET "http://localhost:9510/collection/arraylist-principle/simple-implementation"
echo.
echo.

echo Tests completed!
pause