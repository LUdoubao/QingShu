@REM ==和 equals() 区别演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-equals-comparison.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo ==和 equals() 区别演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/equals

echo 【测试 1】基本类型比较 ^(BASIC^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"comparisonType\":\"BASIC\"}"
echo.
echo.

echo 【测试 2】字符串比较 ^(STRING^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"comparisonType\":\"STRING\"}"
echo.
echo.

echo 【测试 3】包装类比较 ^(WRAPPER^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"comparisonType\":\"WRAPPER\"}"
echo.
echo.

echo 【测试 4】自定义对象比较 ^(CUSTOM^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"comparisonType\":\"CUSTOM\"}"
echo.
echo.

echo 【测试 5】错误的比较类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"comparisonType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
