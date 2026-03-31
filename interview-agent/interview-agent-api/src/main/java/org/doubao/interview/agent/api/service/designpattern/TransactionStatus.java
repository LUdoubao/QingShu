package org.doubao.interview.agent.api.service.designpattern;

/**
 * 事务状态接口
 * 
 * 模拟 Spring 的 TransactionStatus
 * 表示当前事务的运行状态
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface TransactionStatus {
    
    /**
     * 判断是否是新事务
     * 
     * @return true-新事务，false-加入现有事务
     */
    boolean isNewTransaction();
    
    /**
     * 标记事务为回滚
     */
    void setRollbackOnly();
    
    /**
     * 判断是否已标记回滚
     * 
     * @return true-已标记回滚，false-未标记
     */
    boolean isRollbackOnly();
    
    /**
     * 判断事务是否已完成
     * 
     * @return true-已完成，false-进行中
     */
    boolean isCompleted();
}
