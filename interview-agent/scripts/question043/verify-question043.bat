@echo off
chcp 65001 >nul
echo ========================================
echo Spring @Scheduled 任务调度演示验证脚本
echo 问题 043 - 定时任务原理与多场景示例
echo ========================================
echo.

REM 设置服务地址
SET BASE_URL=http://localhost:9510/interview-agent/question043

echo [1] 健康检查...
echo.
curl -s "%BASE_URL%/health"
echo.
echo.

echo [2] 获取基础用法详解（fixedRate vs fixedDelay vs cron）...
echo.
curl -s "%BASE_URL%/basic/explanation"
echo.
echo.

echo [3] 获取高级特性说明（异步、超时、分布式锁）...
echo.
curl -s "%BASE_URL%/advanced/explanation"
echo.
echo.

echo [4] 获取任务执行统计...
echo.
curl -s "%BASE_URL%/statistics"
echo.
echo.

echo ========================================
echo 验证完成
echo ========================================
echo.
echo 【观察日志】
echo 启动服务后，请观察控制台日志：
echo - FixedRate 任务：每 5 秒执行一次，耗时约 2 秒
echo - FixedDelay 任务：每次执行完后等 5 秒，耗时约 3 秒
echo - Cron 任务：每 10 秒执行一次（精确到秒）
echo - Async 任务：异步执行，不阻塞其他任务
echo - TimeoutTask: 有 8 秒超时控制
echo - ConcurrencyControl: 单机并发控制
echo - DistributedLock: 分布式锁演示
echo.
