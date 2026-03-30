@REM String、StringBuffer、StringBuilder 区别演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-stringbuilder-comparison.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo String、StringBuffer、StringBuilder 区别演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/stringbuilder

echo 【测试 1】String 类演示 ^(STRING^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"stringType\":\"STRING\"}"
echo.
echo.

echo 【测试 2】StringBuffer 类演示 ^(STRINGBUFFER^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"stringType\":\"STRINGBUFFER\"}"
echo.
echo.

echo 【测试 3】StringBuilder 类演示 ^(STRINGBUILDER^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"stringType\":\"STRINGBUILDER\"}"
echo.
echo.

echo 【测试 4】错误的字符串类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"stringType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
