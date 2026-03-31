package org.doubao.interview.agent.api.service.designpattern;

/**
 * 平台事务管理器接口 - 策略模式
 * 
 * 模拟 Spring 的 PlatformTransactionManager
 * 定义事务管理的顶级策略接口
 * 
 * 设计要点：
 * 1. 定义事务管理的标准操作（getTransaction/commit/rollback）
 * 2. 不同的实现类代表不同的事务策略
 * 3. 使用时可以根据场景灵活切换实现
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface PlatformTransactionManager {
    
    /**
     * 获取事务
     * 
     * @param definition 事务定义（隔离级别、传播行为等）
     * @return 事务状态对象
     */
    TransactionStatus getTransaction(TransactionDefinition definition);
    
    /**
     * 提交事务
     * 
     * @param status 事务状态
     */
    void commit(TransactionStatus status);
    
    /**
     * 回滚事务
     * 
     * @param status 事务状态
     */
    void rollback(TransactionStatus status);
}
