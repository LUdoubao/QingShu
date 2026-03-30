@REM 方法重载和重写演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-method-feature.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo 方法重载和重写演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/method

echo 【测试 1】演示方法重载 ^(OVERLOAD^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"OVERLOAD\"}"
echo.
echo.

echo 【测试 2】演示方法重写 ^(OVERRIDE^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"OVERRIDE\"}"
echo.
echo.

echo 【测试 3】错误的特性类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
