package org.doubao.interview.agent.api.dto.question012;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 转账请求 DTO
 * 
 * 对应面试知识点：问题 012 - 三种日志的区别
 * 
 * 【类注释】
 * 职责：封装转账请求参数，用于演示事务日志的工作机制
 * 边界：仅用于演示场景，不包含真实银行系统的复杂校验（如风控、限额等）
 * 线程安全：不可变对象，线程安全
 * 幂等性：通过 transactionNo 保证幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class TransferRequest {

    /**
     * 转出账户 ID - 必须为正整数
     */
    @NotNull(message = "转出账户 ID 不能为空")
    private Long fromAccountId;

    /**
     * 转入账户 ID - 必须为正整数
     */
    @NotNull(message = "转入账户 ID 不能为空")
    private Long toAccountId;

    /**
     * 转账金额 - 必须大于 0.01 元
     */
    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额必须大于 0.01 元")
    private BigDecimal amount;

    /**
     * 交易流水号 - 可选，用于保证幂等性
     * 如果不传，系统会自动生成 UUID
     */
    private String transactionNo;

    /**
     * 备注说明 - 可选
     */
    private String remark;

    /**
     * 是否模拟异常 - 用于演示 undo log 回滚
     * true: 在转账过程中抛出异常，触发回滚
     * false: 正常完成转账
     */
    private Boolean simulateException = false;

    /**
     * 是否延迟提交 - 用于演示 redo log 的崩溃恢复
     * true: 模拟写入 redo log 后延迟提交
     * false: 立即提交
     */
    private Boolean delayCommit = false;


    public @NotNull(message = "转出账户 ID 不能为空") Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(@NotNull(message = "转出账户 ID 不能为空") Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public @NotNull(message = "转账金额不能为空") @DecimalMin(value = "0.01", message = "转账金额必须大于 0.01 元") BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "转账金额不能为空") @DecimalMin(value = "0.01", message = "转账金额必须大于 0.01 元") BigDecimal amount) {
        this.amount = amount;
    }

    public @NotNull(message = "转入账户 ID 不能为空") Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(@NotNull(message = "转入账户 ID 不能为空") Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Boolean getSimulateException() {
        return simulateException;
    }

    public void setSimulateException(Boolean simulateException) {
        this.simulateException = simulateException;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getDelayCommit() {
        return delayCommit;
    }

    public void setDelayCommit(Boolean delayCommit) {
        this.delayCommit = delayCommit;
    }
}
