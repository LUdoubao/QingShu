-- =====================================================
-- 问题 015: Next-Key Lock 与幻读 - 数据库表结构
-- =====================================================
-- 对应面试知识点：问题 015 - Next-Key Lock 与幻读
-- 创建时间：2026-03-29
-- 
-- 【说明】
-- 1. 本脚本用于创建演示 Next-Key Lock 和幻读现象所需的表结构
-- 2. 执行后需调用 /interview-agent/question015/init 接口初始化测试数据
-- 3. 所有操作均会产生 Next-Key Lock 或快照读
-- =====================================================

-- 使用数据库（根据实际情况修改）
USE interview_agent;

-- =====================================================
-- 表 1: 用户账户表 - 用于演示幻读现象
-- =====================================================
DROP TABLE IF EXISTS `user_account_015`;

CREATE TABLE `user_account_015` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '账户 ID，主键自增',
  `user_name` VARCHAR(100) NOT NULL COMMENT '用户名称',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '账户状态：0-冻结，1-正常，2-注销',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表 - 用于演示 Next-Key Lock';

-- =====================================================
-- 表 2: 查询日志表 - 记录查询操作和幻读检测结果
-- =====================================================
DROP TABLE IF EXISTS `query_log_015`;

CREATE TABLE `query_log_015` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志 ID，主键自增',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话 ID（标识同一个事务）',
  `query_type` VARCHAR(50) NOT NULL COMMENT '查询类型：SNAPSHOT_READ/CURRENT_READ',
  `query_condition` VARCHAR(500) NOT NULL COMMENT '查询条件',
  `result_count` INT(11) NOT NULL COMMENT '查询结果数量',
  `is_phantom` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否发生幻读：0-否，1-是',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查询日志表 - 记录幻读检测';

-- =====================================================
-- 初始化数据（可选，也可以通过 API 接口初始化）
-- =====================================================
-- 注释掉，建议通过 /init 接口进行初始化，避免重复插入
/*
INSERT INTO user_account_015 (user_name, balance, status) VALUES 
('张三', 1000.00, 1),
('李四', 2000.00, 1),
('王五', 3000.00, 1);
*/

-- =====================================================
-- Next-Key Lock 相关配置参数说明
-- =====================================================
-- 以下参数可在 my.cnf 或 my.ini 中查看和修改
--
-- 【隔离级别相关】
-- tx_isolation = 'REPEATABLE-READ'    -- MySQL 默认隔离级别
--                                      -- 支持 Next-Key Lock，能避免幻读
-- tx_isolation = 'READ-COMMITTED'     -- 读已提交
--                                      -- 只使用 Record Lock，可能发生幻读
--
-- 【锁监控相关】
-- information_schema.INNODB_TRX       -- 当前运行的所有事务信息
-- performance_schema.data_locks       -- MySQL 8.0+ 的锁信息视图
-- performance_schema.data_lock_waits  -- MySQL 8.0+ 的锁等待视图
--
-- =====================================================
-- 查看 Next-Key Lock 的 SQL 命令
-- =====================================================
-- -- 查看当前事务
-- SELECT * FROM information_schema.INNODB_TRX;
--
-- -- 查看当前持有的锁（MySQL 8.0+）
-- SELECT 
--     lock_type,
--     lock_mode,
--     lock_table,
--     lock_index,
--     lock_data,
--     lock_table_schema
-- FROM performance_schema.data_locks;
--
-- -- 查看锁等待
-- SELECT * FROM performance_schema.data_lock_waits;
--
-- -- 查看死锁信息
-- SHOW ENGINE INNODB STATUS\G
--
-- =====================================================
-- 手动测试 Next-Key Lock 的示例
-- =====================================================
-- -- 会话 1
-- BEGIN;
-- SELECT * FROM user_account_015 WHERE id > 1 AND id < 5 FOR UPDATE;
-- -- 此时会锁定：
-- --   1. id=2,3,4 的记录（Record Lock）
-- --   2. (1,2), (2,3), (3,4), (4,5) 的间隙（Gap Lock）
--
-- -- 会话 2
-- INSERT INTO user_account_015 (id, user_name, balance) VALUES (2.5, '测试用户', 1500);
-- -- ❌ 被阻塞！因为 id=2.5 落在 (2,3) 间隙中，已被锁定
--
-- -- 会话 1 提交
-- COMMIT;
-- -- 此时会话 2 的插入才能成功
--
-- =====================================================
-- 验证脚本使用说明
-- =====================================================
-- 1. 执行本 SQL 脚本创建表结构
-- 2. 启动服务后调用：POST /interview-agent/question015/init
-- 3. 执行查询操作：POST /interview-agent/question015/query
-- 4. 查看 Next-Key Lock 说明：GET /interview-agent/question015/explanation
--
-- 实际观察 Next-Key Lock 的方法：
-- a) 开启两个会话，一个执行 FOR UPDATE，另一个尝试插入
-- b) 使用 performance_schema.data_locks 查看锁信息
-- c) 使用 SHOW ENGINE INNODB STATUS 查看详细的锁状态
