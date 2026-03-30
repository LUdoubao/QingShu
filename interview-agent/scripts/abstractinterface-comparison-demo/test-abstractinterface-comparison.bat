@REM 接口和抽象类区别演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-abstractinterface-comparison.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo 接口和抽象类区别演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/abstractinterface

echo 【测试 1】抽象类演示 ^(ABSTRACT_CLASS^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"targetType\":\"ABSTRACT_CLASS\"}"
echo.
echo.

echo 【测试 2】接口演示 ^(INTERFACE^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"targetType\":\"INTERFACE\"}"
echo.
echo.

echo 【测试 3】错误的类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"targetType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
