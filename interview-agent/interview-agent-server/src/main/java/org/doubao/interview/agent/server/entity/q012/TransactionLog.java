package org.doubao.interview.agent.server.entity.q012;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易日志实体类 - 记录转账操作的详细日志
 * 
 * 对应面试知识点：问题 012 - 三种日志的区别
 * 
 * 【类注释】
 * 职责：记录每笔交易的详细信息，包括 undo log、redo log、binlog 的状态
 * 边界：仅用于演示和说明，不作为真实审计依据
 * 线程安全：非线程安全
 * 幂等性：通过交易流水号保证幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("transaction_log_012")
public class TransactionLog {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易流水号 - 全局唯一标识一次交易
     */
    private String transactionNo;

    /**
     * 转出账户 ID
     */
    private Long fromAccountId;

    /**
     * 转入账户 ID
     */
    private Long toAccountId;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额 - 用于演示 undo log 的回滚能力
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额 - 用于演示 redo log 的持久化能力
     */
    private BigDecimal balanceAfter;

    /**
     * 交易状态：0-失败，1-成功，2-处理中
     */
    private Integer status;

    /**
     * undo log 记录 ID - 指向回滚日志
     */
    private String undoLogId;

    /**
     * redo log 记录 ID - 指向重做日志
     */
    private String redoLogId;

    /**
     * binlog 记录 ID - 指向归档日志
     */
    private String binlogId;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUndoLogId() {
        return undoLogId;
    }

    public void setUndoLogId(String undoLogId) {
        this.undoLogId = undoLogId;
    }

    public String getRedoLogId() {
        return redoLogId;
    }

    public void setRedoLogId(String redoLogId) {
        this.redoLogId = redoLogId;
    }

    public String getBinlogId() {
        return binlogId;
    }

    public void setBinlogId(String binlogId) {
        this.binlogId = binlogId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
