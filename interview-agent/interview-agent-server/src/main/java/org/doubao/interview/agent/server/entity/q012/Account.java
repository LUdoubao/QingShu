package org.doubao.interview.agent.server.entity.q012;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类 - 用于演示 undo log、redo log、binlog
 * 
 * 对应面试知识点：问题 012 - 三种日志的区别
 * 
 * 【类注释】
 * 职责：表示银行账户信息，用于模拟转账场景，展示事务日志的工作机制
 * 边界：仅用于演示，不包含真实银行系统的复杂校验逻辑
 * 线程安全：非线程安全，依赖 Service 层事务保证并发安全
 * 幂等性：无特殊幂等设计，依赖数据库唯一约束
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("account_012")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户余额 - 使用 BigDecimal 避免精度丢失
     */
    private BigDecimal balance;

    /**
     * 版本号 - 用于乐观锁演示（可选）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
