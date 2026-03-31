package org.doubao.interview.agent.api.service.designpattern;

/**
 * 事务定义接口
 * 
 * 模拟 Spring 的 TransactionDefinition
 * 定义事务的属性（隔离级别、传播行为等）
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface TransactionDefinition {
    
    /**
     * 隔离级别：默认
     */
    int ISOLATION_DEFAULT = -1;
    
    /**
     * 隔离级别：读未提交
     */
    int ISOLATION_READ_UNCOMMITTED = 1;
    
    /**
     * 隔离级别：读已提交
     */
    int ISOLATION_READ_COMMITTED = 2;
    
    /**
     * 隔离级别：可重复读
     */
    int ISOLATION_REPEATABLE_READ = 4;
    
    /**
     * 隔离级别：串行化
     */
    int ISOLATION_SERIALIZABLE = 8;
    
    /**
     * 传播行为：必需
     */
    int PROPAGATION_REQUIRED = 0;
    
    /**
     * 传播行为：支持当前事务
     */
    int PROPAGATION_SUPPORTS = 1;
    
    /**
     * 获取隔离级别
     * 
     * @return 隔离级别
     */
    int getIsolationLevel();
    
    /**
     * 获取传播行为
     * 
     * @return 传播行为
     */
    int getPropagationBehavior();
    
    /**
     * 获取超时时间（秒）
     * 
     * @return 超时时间
     */
    int getTimeout();
}
