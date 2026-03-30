@REM final、finally、finalize 区别演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-finalkeyword-comparison.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo final、finally、finalize 区别演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/finalkeyword

echo 【测试 1】final 关键字演示 ^(FINAL^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"keywordType\":\"FINAL\"}"
echo.
echo.

echo 【测试 2】finally 关键字演示 ^(FINALLY^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"keywordType\":\"FINALLY\"}"
echo.
echo.

echo 【测试 3】finalize 关键字演示 ^(FINALIZE^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"keywordType\":\"FINALIZE\"}"
echo.
echo.

echo 【测试 4】错误的类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"keywordType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
