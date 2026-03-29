package org.doubao.interview.agent.server.entity.q014;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类 - 记录购买操作，用于演示锁的粒度
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：记录用户的购买订单，演示不同锁模式下的并发控制
 * 边界：仅用于演示，简化了真实订单系统的复杂性
 * 线程安全：非线程安全，依赖数据库锁保证并发安全
 * 幂等性：通过订单号唯一索引保证
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("order_014")
public class Order {

    private static final long serialVersionUID = 1L;

    /**
     * 订单 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号 - 全局唯一
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 商品 ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态：0-待支付，1-已支付，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 锁类型说明 - 用于记录该订单使用的锁类型
     * S: 共享锁，X: 排他锁，Gap: 间隙锁，Next-Key: 临键锁
     */
    private String lockType;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
