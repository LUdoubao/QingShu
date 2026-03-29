-- =====================================================
-- 问题 012: undo log、redo log、binlog 的区别 - 数据库表结构
-- =====================================================
-- 对应面试知识点：问题 012 - 三种日志的区别
-- 创建时间：2026-03-29
-- 
-- 【说明】
-- 1. 本脚本用于创建演示 MySQL 三种日志工作机制所需的表结构
-- 2. 执行后需调用 /interview-agent/question012/init 接口初始化测试数据
-- 3. 所有操作均会产生 undo log、redo log 和 binlog
-- =====================================================

-- 使用数据库（根据实际情况修改）
USE interview_agent;

-- =====================================================
-- 表 1: 账户表 - 用于模拟转账场景
-- =====================================================
DROP TABLE IF EXISTS `account_012`;

CREATE TABLE `account_012` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '账户 ID，主键自增',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_name` (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表 - 用于演示三种日志';

-- =====================================================
-- 表 2: 交易日志表 - 记录转账操作的详细信息
-- =====================================================
DROP TABLE IF EXISTS `transaction_log_012`;

CREATE TABLE `transaction_log_012` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志 ID，主键自增',
  `transaction_no` VARCHAR(64) NOT NULL COMMENT '交易流水号（全局唯一）',
  `from_account_id` BIGINT(20) NOT NULL COMMENT '转出账户 ID',
  `to_account_id` BIGINT(20) NOT NULL COMMENT '转入账户 ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '交易前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '交易状态：0-失败，1-成功，2-处理中',
  `undo_log_id` VARCHAR(64) DEFAULT NULL COMMENT 'undo log 记录 ID',
  `redo_log_id` VARCHAR(64) DEFAULT NULL COMMENT 'redo log 记录 ID',
  `binlog_id` VARCHAR(64) DEFAULT NULL COMMENT 'binlog 记录 ID',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_from_account` (`from_account_id`),
  KEY `idx_to_account` (`to_account_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易日志表 - 记录三种日志信息';

-- =====================================================
-- 初始化数据（可选，也可以通过 API 接口初始化）
-- =====================================================
-- 注释掉，建议通过 /init 接口进行初始化，避免重复插入
/*
INSERT INTO account_012 (account_name, balance, version) VALUES 
('张三账户', 1000.00, 0),
('李四账户', 1000.00, 0);
*/

-- =====================================================
-- MySQL 三种日志配置参数说明
-- =====================================================
-- 以下参数可在 my.cnf 或 my.ini 中查看和修改
--
-- 【undo log 相关】
-- innodb_undo_directory = /var/lib/mysql/undo/  -- undo log 文件目录
-- innodb_undo_logs = 128                         -- undo log 数量
-- innodb_undo_tablespaces = 3                    -- undo 表空间数量
--
-- 【redo log 相关】
-- innodb_log_file_size = 48M                     -- redo log 文件大小（默认 48M）
-- innodb_log_files_in_group = 2                  -- redo log 文件组数量（默认 2 个）
-- innodb_log_buffer_size = 16M                   -- redo log 缓冲区大小
-- innodb_flush_log_at_trx_commit = 1             -- 刷盘策略：
--                                                  0: 每秒刷盘一次
--                                                  1: 每次事务提交时刷盘（最安全，默认值）
--                                                  2: 每次事务提交时写入 OS cache，每秒刷盘
--
-- 【binlog 相关】
-- log_bin = mysql-bin                            -- binlog 文件名前缀
-- server_id = 1                                  -- 服务器 ID（主从复制必需）
-- binlog_format = MIXED                          -- binlog 格式：STATEMENT/ROW/MIXED
-- expire_logs_days = 7                           -- binlog 保留天数
-- max_binlog_size = 100M                         -- binlog 最大文件大小
-- sync_binlog = 1                                -- binlog 刷盘策略：
--                                                  0: 不强制同步（性能最好，风险最高）
--                                                  1: 每次事务提交同步到磁盘（最安全，默认值）
--                                                  N: 每 N 次事务提交同步一次
--
-- =====================================================
-- 查看当前日志配置的 SQL 命令
-- =====================================================
-- SHOW VARIABLES LIKE '%log%';          -- 查看所有日志相关变量
-- SHOW VARIABLES LIKE 'innodb_log%';    -- 查看 redo log 配置
-- SHOW VARIABLES LIKE 'binlog%';        -- 查看 binlog 配置
-- SHOW VARIABLES LIKE 'undo%';          -- 查看 undo log 配置
-- SHOW BINARY LOGS;                     -- 查看所有 binlog 文件
-- SHOW MASTER STATUS;                   -- 查看当前 binlog 位置和 GTID
-- SHOW ENGINE INNODB STATUS\G           -- 查看 InnoDB 引擎状态（包含日志信息）

-- =====================================================
-- 验证脚本使用说明
-- =====================================================
-- 1. 执行本 SQL 脚本创建表结构
-- 2. 启动服务后调用：POST /interview-agent/question012/init
-- 3. 执行转账操作：POST /interview-agent/question012/transfer
-- 4. 查看三种日志对比：GET /interview-agent/question012/comparison
--
-- 实际观察 MySQL 日志的方法：
-- a) undo log: 无法直接查看，由 InnoDB 自动管理
-- b) redo log: 可通过 ibdata1 和 ib_logfile* 文件观察
-- c) binlog: 可通过 mysqlbinlog 工具查看
--    示例：mysqlbinlog /var/lib/mysql/mysql-bin.000001 | less
