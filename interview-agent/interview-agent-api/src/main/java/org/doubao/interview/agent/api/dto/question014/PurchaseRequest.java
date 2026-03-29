package org.doubao.interview.agent.api.dto.question014;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 购买请求 DTO - 用于演示 InnoDB 锁机制
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：封装购买请求参数，演示不同锁模式下的并发控制
 * 边界：仅用于演示场景，简化了真实购买的复杂流程
 * 线程安全：不可变对象，线程安全
 * 幂等性：通过 orderNo 保证幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class PurchaseRequest {

    /**
     * 用户 ID - 必须为正整数
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 商品 ID - 必须为正整数
     */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    /**
     * 购买数量 - 必须大于 0
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于 0")
    private Integer quantity;

    /**
     * 订单编号 - 可选，用于保证幂等性
     * 如果不传，系统会自动生成 UUID
     */
    private String orderNo;

    /**
     * 锁模式 - 用于演示不同的锁类型
     * 可选值：X(排他锁，默认), S(共享锁), Gap(间隙锁), NextKey(临键锁)
     */
    private String lockMode = "X";

    /**
     * 是否走索引 - 用于演示锁的粒度与索引的关系
     * true: 使用索引查询（行锁）
     * false: 全表扫描（可能升级为表锁或锁住更多记录）
     */
    private Boolean useIndex = true;

    /**
     * 备注说明
     */
    private String remark;
}
