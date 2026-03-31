package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务感知数据源代理 - 装饰器模式实现
 * 
 * 模拟 Spring 的 TransactionAwareDataSourceProxy
 * 职责：在基础数据源之上添加事务管理功能
 * 
 * 设计要点：
 * 1. 持有 DataSource 的引用（被装饰对象）
 * 2. 实现相同的接口（DataSource）
 * 3. 在 getConnection() 中添加事务逻辑
 * 4. 可以嵌套多层装饰（如：缓冲 + 事务 + 日志）
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class TransactionAwareDataSource implements DataSource {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionAwareDataSource.class);
    
    /**
     * 被装饰的数据源（核心成员变量）
     * 可以是 SimpleDataSource，也可以是其他装饰器
     */
    private final DataSource targetDataSource;
    
    /**
     * 当前事务状态
     */
    private boolean transactionActive = false;
    
    /**
     * 构造函数
     * 
     * @param targetDataSource 目标数据源（被装饰的对象）
     */
    public TransactionAwareDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
        log.info("创建事务感知数据源代理，包装：{}", targetDataSource.getName());
    }
    
    /**
     * 获取数据库连接（增强版本）
     * 
     * 在基础功能之上添加了事务管理逻辑：
     * 1. 检查是否有活跃事务
     * 2. 如果没有，开启新事务
     * 3. 将连接绑定到当前线程（ThreadLocal）
     * 4. 返回连接
     * 
     * @return 数据库连接
     */
    @Override
    public Object getConnection() {
        log.info("[事务装饰器] 开始获取事务感知的连接");
        
        // 1. 检查当前是否有活跃事务
        if (!transactionActive) {
            log.info("[事务装饰器] 检测到无活跃事务，开启新事务");
            beginTransaction();
        }
        
        // 2. 从被装饰的数据源获取基础连接
        Object connection = targetDataSource.getConnection();
        
        // 3. 将连接绑定到当前线程（模拟 ThreadLocal 绑定）
        bindConnectionToThread(connection);
        
        log.info("[事务装饰器] 返回事务感知的连接：{}", connection.getClass().getSimpleName());
        return connection;
    }
    
    @Override
    public String getName() {
        return targetDataSource.getName() + " (TransactionAware)";
    }
    
    /**
     * 开启事务
     * 
     * 模拟 Spring 的 PlatformTransactionManager.getTransaction()
     */
    private void beginTransaction() {
        log.info("[事务管理] ========== 开启数据库事务 ==========");
        log.info("[事务管理] 设置自动提交为 false");
        log.info("[事务管理] 隔离级别：READ_COMMITTED");
        log.info("[事务管理] 传播行为：REQUIRED");
        transactionActive = true;
    }
    
    /**
     * 提交事务
     * 
     * 模拟 Spring 的 PlatformTransactionManager.commit()
     */
    public void commit() {
        if (transactionActive) {
            log.info("[事务管理] ========== 提交数据库事务 ==========");
            log.info("[事务管理] 执行 COMMIT 操作");
            transactionActive = false;
            clearThreadConnection();
        } else {
            log.warn("[事务管理] 没有活跃的事务，无法提交");
        }
    }
    
    /**
     * 回滚事务
     * 
     * 模拟 Spring 的 PlatformTransactionManager.rollback()
     */
    public void rollback() {
        if (transactionActive) {
            log.error("[事务管理] ========== 回滚数据库事务 ==========");
            log.error("[事务管理] 执行 ROLLBACK 操作");
            transactionActive = false;
            clearThreadConnection();
        } else {
            log.warn("[事务管理] 没有活跃的事务，无法回滚");
        }
    }
    
    /**
     * 判断是否有活跃事务
     * 
     * @return true-有活跃事务，false-无事务
     */
    public boolean isTransactionActive() {
        return transactionActive;
    }
    
    /**
     * 将连接绑定到当前线程
     * 
     * 模拟 Spring 的 DataSourceUtils.bindConnectionToThread()
     * 实际实现会使用 ThreadLocal<Map<String, Connection>>
     * 
     * @param connection 数据库连接
     */
    private void bindConnectionToThread(Object connection) {
        String threadName = Thread.currentThread().getName();
        log.info("[事务管理] 绑定连接到当前线程：thread={}, connection={}",
                 threadName, connection.getClass().getSimpleName());
        // 简化实现，实际会使用 ThreadLocal 存储
    }
    
    /**
     * 清除当前线程绑定的连接
     * 
     * 模拟 Spring 的 DataSourceUtils.unbindConnectionFromThread()
     */
    private void clearThreadConnection() {
        String threadName = Thread.currentThread().getName();
        log.debug("[事务管理] 清除线程绑定的连接：thread={}", threadName);
    }
}
