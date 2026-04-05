@echo off
REM HashSet and HashMap Relationship API Test Script
REM Windows Compatible Version

echo Testing HashSet and HashMap Relationship APIs...

echo.
echo 1. Testing duplicate handling:
curl -X POST "http://localhost:9510/collection/relationship/duplicate-handling" ^
-H "Content-Type: application/json" ^
-d "[\"apple\", \"banana\", \"apple\", \"cherry\", \"banana\"]"
echo.
echo.

echo 2. Testing null handling:
curl -X GET "http://localhost:9510/collection/relationship/null-handling"
echo.
echo.

echo 3. Testing implementation principle explanation:
curl -X GET "http://localhost:9510/collection/relationship/implementation-principle"
echo.
echo.

echo 4. Testing performance comparison:
curl -X GET "http://localhost:9510/collection/relationship/performance-comparison"
echo.
echo.

echo Tests completed!
pause