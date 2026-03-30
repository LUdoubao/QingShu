@REM Java 反射机制演示 - 验证脚本 (Windows CMD 版本)
@REM 适用于 Windows 命令提示符
@REM 使用方法：test-reflection-demo.bat

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo Java 反射机制演示 - 验证脚本
echo ==========================================
echo.

set BASE_URL=http://localhost:9510/interview-agent/reflection

echo 【测试 1】获取 Class 对象 ^(GET_CLASS^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"GET_CLASS\"}"
echo.
echo.

echo 【测试 2】创建实例 ^(CREATE_INSTANCE^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"CREATE_INSTANCE\"}"
echo.
echo.

echo 【测试 3】调用方法 ^(INVOKE_METHOD^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"INVOKE_METHOD\"}"
echo.
echo.

echo 【测试 4】访问属性 ^(ACCESS_FIELD^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"ACCESS_FIELD\"}"
echo.
echo.

echo 【测试 5】获取构造器 ^(GET_CONSTRUCTOR^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"GET_CONSTRUCTOR\"}"
echo.
echo.

echo 【测试 6】错误的类型 ^(测试兜底逻辑^)
echo ----------------------------------------
curl -X POST "%BASE_URL%/demonstrate" -H "Content-Type: application/json" -d "{\"operationType\":\"INVALID_TYPE\"}"
echo.
echo.

echo ==========================================
echo 所有测试完成！
echo ==========================================

endlocal
