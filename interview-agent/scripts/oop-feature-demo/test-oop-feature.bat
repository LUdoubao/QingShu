@REM 面向对象三大特征演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-oop-feature.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo 面向对象三大特征演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/oop

echo 【测试 1】演示封装特性 ^(ENCAPSULATION^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"ENCAPSULATION\"}"
echo.
echo.

echo 【测试 2】演示继承特性 ^(INHERITANCE^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"INHERITANCE\"}"
echo.
echo.

echo 【测试 3】演示多态特性 ^(POLYMORPHISM^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"POLYMORPHISM\"}"
echo.
echo.

echo 【测试 4】错误的特征类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"featureType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
