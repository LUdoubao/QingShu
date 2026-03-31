package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源事务管理器 - 策略模式实现
 * 
 * 模拟 Spring 的 DataSourceTransactionManager
 * 针对 JDBC 数据源的事务管理策略
 * 
 * 设计要点：
 * 1. 实现 PlatformTransactionManager 接口
 * 2. 使用 JDBC Connection 管理事务
 * 3. 通过 ThreadLocal 绑定事务到当前线程
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class DataSourceTransactionManager implements PlatformTransactionManager {
    
    private static final Logger log = LoggerFactory.getLogger(DataSourceTransactionManager.class);
    
    /**
     * 数据源（用于获取连接）
     */
    private final javax.sql.DataSource dataSource;
    
    /**
     * 构造函数
     * 
     * @param dataSource 数据源
     */
    public DataSourceTransactionManager(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("初始化数据源事务管理器");
    }
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        log.info("========== 获取事务 ==========");
        log.info("隔离级别：{}", getIsolationLevelName(definition.getIsolationLevel()));
        log.info("传播行为：{}", getPropagationBehaviorName(definition.getPropagationBehavior()));
        log.info("超时时间：{}秒", definition.getTimeout());
        
        // 创建事务状态
        DataSourceTransactionStatus status = new DataSourceTransactionStatus();
        status.setNewTransaction(true);
        
        log.info("创建新事务，状态：{}", status);
        return status;
    }
    
    @Override
    public void commit(TransactionStatus status) {
        if (status.isCompleted()) {
            log.warn("事务已完成，无法再次提交");
            return;
        }
        
        log.info("========== 提交事务 ==========");
        log.info("事务状态：isNew={}, rollbackOnly={}", 
                status.isNewTransaction(), status.isRollbackOnly());
        
        if (status.isRollbackOnly()) {
            log.warn("事务已标记为回滚，执行回滚操作");
            rollback(status);
            return;
        }
        
        // 模拟 JDBC 提交
        try {
            log.info("执行数据库 COMMIT 操作");
            // connection.commit();
            
            ((DataSourceTransactionStatus) status).setCompleted(true);
            log.info("事务提交成功");
            
        } catch (Exception e) {
            log.error("事务提交失败", e);
            throw new RuntimeException("Commit failed", e);
        }
    }
    
    @Override
    public void rollback(TransactionStatus status) {
        if (status.isCompleted()) {
            log.warn("事务已完成，无法回滚");
            return;
        }
        
        log.error("========== 回滚事务 ==========");
        log.info("事务状态：isNew={}, rollbackOnly={}", 
                status.isNewTransaction(), status.isRollbackOnly());
        
        try {
            log.info("执行数据库 ROLLBACK 操作");
            // connection.rollback();
            
            ((DataSourceTransactionStatus) status).setCompleted(true);
            log.info("事务回滚完成");
            
        } catch (Exception e) {
            log.error("事务回滚失败", e);
            throw new RuntimeException("Rollback failed", e);
        }
    }
    
    /**
     * 获取隔离级别名称
     */
    private String getIsolationLevelName(int level) {
        switch (level) {
            case TransactionDefinition.ISOLATION_READ_UNCOMMITTED:
                return "READ_UNCOMMITTED";
            case TransactionDefinition.ISOLATION_READ_COMMITTED:
                return "READ_COMMITTED";
            case TransactionDefinition.ISOLATION_REPEATABLE_READ:
                return "REPEATABLE_READ";
            case TransactionDefinition.ISOLATION_SERIALIZABLE:
                return "SERIALIZABLE";
            default:
                return "DEFAULT";
        }
    }
    
    /**
     * 获取传播行为名称
     */
    private String getPropagationBehaviorName(int behavior) {
        switch (behavior) {
            case TransactionDefinition.PROPAGATION_REQUIRED:
                return "REQUIRED";
            case TransactionDefinition.PROPAGATION_SUPPORTS:
                return "SUPPORTS";
            default:
                return "UNKNOWN";
        }
    }
}
