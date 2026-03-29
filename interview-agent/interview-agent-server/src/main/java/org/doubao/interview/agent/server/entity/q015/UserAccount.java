package org.doubao.interview.agent.server.entity.q015;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户账户实体类 - 用于演示 Next-Key Lock 和幻读问题
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：表示用户账户信息，用于模拟查询和插入场景，展示幻读现象及 Next-Key Lock 的作用
 * 边界：仅用于演示，不包含真实金融系统的复杂业务逻辑
 * 线程安全：非线程安全，依赖数据库锁和事务保证并发安全
 * 幂等性：无特殊幂等设计，依赖业务层控制
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("user_account_015")
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 账户状态：0-冻结，1-正常，2-注销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
