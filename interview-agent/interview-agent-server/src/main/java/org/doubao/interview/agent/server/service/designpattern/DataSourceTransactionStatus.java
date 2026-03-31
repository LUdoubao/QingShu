package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.TransactionStatus;

/**
 * 数据源事务状态实现
 * 
 * 模拟 Spring 的 DefaultTransactionStatus
 * 保存事务的当前状态信息
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class DataSourceTransactionStatus implements TransactionStatus {
    
    /**
     * 是否为新事务
     */
    private boolean newTransaction;
    
    /**
     * 是否已标记回滚
     */
    private boolean rollbackOnly = false;
    
    /**
     * 事务是否已完成
     */
    private boolean completed = false;
    
    @Override
    public boolean isNewTransaction() {
        return newTransaction;
    }
    
    public void setNewTransaction(boolean newTransaction) {
        this.newTransaction = newTransaction;
    }
    
    @Override
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }
    
    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
    
    @Override
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    @Override
    public String toString() {
        return String.format("DataSourceTransactionStatus{new=%s, rollbackOnly=%s, completed=%s}",
                           newTransaction, rollbackOnly, completed);
    }
}
