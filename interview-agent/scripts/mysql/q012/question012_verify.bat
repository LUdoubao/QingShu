@echo off
REM =====================================================
REM 问题 012: undo log、redo log、binlog 的区别 - Windows CMD 验证脚本
REM =====================================================
REM 对应面试知识点：问题 012 - 三种日志的区别
REM 创建时间：2026-03-29
REM 
REM 【说明】
REM 1. 本脚本用于演示和验证 MySQL 三种日志的工作机制
REM 2. 兼容 Windows CMD，保证输出不乱码
REM 3. 执行前确保服务已启动：http://localhost:9510
REM =====================================================

chcp 65001 >nul
setlocal enabledelayedexpansion

echo =====================================================
echo 问题 012: undo log, redo log, binlog 的区别 - 验证脚本
echo =====================================================
echo.

REM 设置基础 URL
set BASE_URL=http://localhost:9510/interview-agent/question012

REM 步骤 1: 初始化演示数据
echo [步骤 1] 初始化演示数据...
echo 命令：POST %BASE_URL%/init
echo.

curl -s -X POST "%BASE_URL%/init" > "%TEMP%\q012_init.json"
type "%TEMP%\q012_init.json"
echo.

REM 从响应中提取账户 ID（简化处理，使用默认值）
set ACCOUNT1_ID=1
set ACCOUNT2_ID=2

REM 步骤 2: 查询账户信息
echo.
echo =====================================================
echo [步骤 2] 查询账户信息...
echo 命令：GET %BASE_URL%/account/%ACCOUNT1_ID%
echo.

curl -s "%BASE_URL%/account/%ACCOUNT1_ID%"
echo.
echo.

REM 步骤 3: 执行正常转账操作
echo.
echo =====================================================
echo [步骤 3] 执行正常转账操作（演示三种日志的完整流程）...
echo 命令：POST %BASE_URL%/transfer
echo 参数：fromAccountId=%ACCOUNT1_ID%, toAccountId=%ACCOUNT2_ID%, amount=100
echo.

echo {"fromAccountId":%ACCOUNT1_ID%,"toAccountId":%ACCOUNT2_ID%,"amount":100,"remark":"正常转账演示"} > "%TEMP%\q012_transfer.json"

curl -s -X POST -H "Content-Type: application/json" -d @"%TEMP%\q012_transfer.json" "%BASE_URL%/transfer" > "%TEMP%\q012_transfer_response.json"
type "%TEMP%\q012_transfer_response.json"
echo.

REM 步骤 4: 再次查询账户信息
echo.
echo =====================================================
echo [步骤 4] 查询转账后的账户信息...
echo.

echo 转出账户信息:
curl -s "%BASE_URL%/account/%ACCOUNT1_ID%"
echo.
echo.

echo 转入账户信息:
curl -s "%BASE_URL%/account/%ACCOUNT2_ID%"
echo.

REM 步骤 5: 模拟异常转账
echo.
echo =====================================================
echo [步骤 5] 模拟异常转账（演示 undo log 的回滚功能）...
echo 命令：POST %BASE_URL%/transfer
echo 参数：simulateException=true
echo.

echo {"fromAccountId":%ACCOUNT1_ID%,"toAccountId":%ACCOUNT2_ID%,"amount":50,"simulateException":true,"remark":"模拟异常 - 测试 undo log 回滚"} > "%TEMP%\q012_exception.json"

curl -s -X POST -H "Content-Type: application/json" -d @"%TEMP%\q012_exception.json" "%BASE_URL%/transfer" > "%TEMP%\q012_exception_response.json"
type "%TEMP%\q012_exception_response.json"
echo.
echo 【说明】由于 simulateException=true，事务会回滚
echo undo log 会将数据恢复到事务开始前的状态
echo.

REM 步骤 6: 验证回滚后的账户余额
echo.
echo =====================================================
echo [步骤 6] 验证回滚后的账户余额（应该与步骤 4 相同）...
echo.

echo 转出账户信息（应保持不变）:
curl -s "%BASE_URL%/account/%ACCOUNT1_ID%"
echo.
echo.

echo 转入账户信息（应保持不变）:
curl -s "%BASE_URL%/account/%ACCOUNT2_ID%"
echo.

REM 步骤 7: 获取三种日志的详细对比说明
echo.
echo =====================================================
echo [步骤 7] 获取三种日志的详细对比说明...
echo 命令：GET %BASE_URL%/comparison
echo.

curl -s "%BASE_URL%/comparison"
echo.
echo.

REM 步骤 8: 总结
echo =====================================================
echo 验证完成！
echo =====================================================
echo.
echo 【关键知识点总结】
echo 1. undo log: 记录反向操作，用于事务回滚（步骤 5 演示）
echo 2. redo log: 记录物理修改，用于崩溃恢复（WAL 技术）
echo 3. binlog: 记录 SQL 语句，用于主从复制（追加写入）
echo.
echo 【两阶段提交流程】
echo Prepare: 写 redo log -^> Commit: 写 binlog -^> Commit: 提交 redo log
echo.
echo 【观察实际日志的方法】
echo - undo log: InnoDB 自动管理，无法直接查看
echo - redo log: 查看 ib_logfile0, ib_logfile1 文件
echo - binlog: 使用 mysqlbinlog 工具查看
echo   示例：mysqlbinlog C:\ProgramData\MySQL\MySQL Server X.X\Data\mysql-bin.000001
echo.

REM 清理临时文件
del "%TEMP%\q012_*.json" >nul 2>&1

endlocal
