-- =====================================================
-- 问题 014: InnoDB 锁的分类 - 数据库表结构
-- =====================================================
-- 对应面试知识点：问题 014 - InnoDB 锁的分类
-- 创建时间：2026-03-29
-- 
-- 【说明】
-- 1. 本脚本用于创建演示 InnoDB 锁机制所需的表结构
-- 2. 执行后需调用 /interview-agent/question014/init 接口初始化测试数据
-- 3. 所有操作均会产生各种类型的 InnoDB 锁
-- =====================================================

-- 使用数据库（根据实际情况修改）
USE interview_agent;

-- =====================================================
-- 表 1: 商品表 - 用于模拟库存扣减场景
-- =====================================================
DROP TABLE IF EXISTS `product_014`;

CREATE TABLE `product_014` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品 ID，主键自增',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品单价',
  `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表 - 用于演示 InnoDB 锁';

-- =====================================================
-- 表 2: 订单表 - 记录购买操作的日志
-- =====================================================
DROP TABLE IF EXISTS `order_014`;

CREATE TABLE `order_014` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单 ID，主键自增',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号（全局唯一）',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品 ID',
  `quantity` INT(11) NOT NULL COMMENT '购买数量',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '订单状态：0-待支付，1-已支付，2-已完成，3-已取消',
  `lock_type` VARCHAR(50) DEFAULT NULL COMMENT '使用的锁类型：X/S/Gap/NextKey',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表 - 记录锁的使用情况';

-- =====================================================
-- 初始化数据（可选，也可以通过 API 接口初始化）
-- =====================================================
-- 注释掉，建议通过 /init 接口进行初始化，避免重复插入
/*
INSERT INTO product_014 (product_name, price, stock, version) VALUES 
('iPhone 15', 9999.00, 100, 0),
('华为 Mate60', 6999.00, 200, 0);
*/

-- =====================================================
-- InnoDB 锁相关配置参数说明
-- =====================================================
-- 以下参数可在 my.cnf 或 my.ini 中查看和修改
--
-- 【隔离级别相关】
-- tx_isolation = 'REPEATABLE-READ'    -- 默认隔离级别：可重复读
--                                      -- 支持 Record Lock、Gap Lock、Next-Key Lock
-- tx_isolation = 'READ-COMMITTED'     -- 读已提交
--                                      -- 只使用 Record Lock，不使用 Gap Lock
-- tx_isolation = 'READ-UNCOMMITTED'   -- 读未提交（不推荐）
-- tx_isolation = 'SERIALIZABLE'       -- 串行化（最严格）
--
-- 【锁等待相关】
-- innodb_lock_wait_timeout = 50       -- 锁等待超时时间（秒），默认 50 秒
-- innodb_deadlock_detect = ON         -- 死锁检测开关（默认开启）
--
-- 【锁监控相关】
-- information_schema.INNODB_TRX       -- 当前运行的所有事务信息
-- information_schema.INNODB_LOCKS     -- 当前持有的锁信息（MySQL 8.0 已废弃）
-- information_schema.INNODB_LOCK_WAITS -- 锁等待信息（MySQL 8.0 已废弃）
-- performance_schema.data_locks       -- MySQL 8.0+ 的锁信息视图
-- performance_schema.data_lock_waits  -- MySQL 8.0+ 的锁等待视图
--
-- =====================================================
-- 查看当前锁状态的 SQL 命令
-- =====================================================
-- -- 查看所有事务
-- SELECT * FROM information_schema.INNODB_TRX;
--
-- -- 查看锁等待（MySQL 5.7）
-- SELECT * FROM information_schema.INNODB_LOCK_WAITS;
--
-- -- 查看锁等待（MySQL 8.0+）
-- SELECT * FROM performance_schema.data_lock_waits;
--
-- -- 查看正在运行的事务和锁
-- SELECT 
--     t.trx_id,
--     t.trx_state,
--     t.trx_query,
--     l.lock_type,
--     l.lock_mode,
--     l.lock_table,
--     l.lock_index,
--     l.lock_data
-- FROM information_schema.INNODB_TRX t
-- JOIN information_schema.INNODB_LOCKS l ON t.trx_id = l.lock_trx_id;
--
-- -- 查看死锁信息
-- SHOW ENGINE INNODB STATUS\G
--
-- =====================================================
-- 验证脚本使用说明
-- =====================================================
-- 1. 执行本 SQL 脚本创建表结构
-- 2. 启动服务后调用：POST /interview-agent/question014/init
-- 3. 执行购买操作：POST /interview-agent/question014/purchase
-- 4. 查看锁分类说明：GET /interview-agent/question014/classification
--
-- 实际观察 InnoDB 锁的方法：
-- a) 开启两个会话，手动加锁测试
-- b) 使用 information_schema 视图查看锁信息
-- c) 使用 SHOW ENGINE INNODB STATUS 查看死锁信息
--
-- 示例：手动测试行锁
-- 会话 1:
-- BEGIN;
-- UPDATE product_014 SET stock = stock - 1 WHERE id = 1;
-- -- 此时 id=1 的记录被加上 X 锁
--
-- 会话 2:
-- BEGIN;
-- UPDATE product_014 SET stock = stock - 1 WHERE id = 1;
-- -- 会被阻塞，等待会话 1 提交或回滚
