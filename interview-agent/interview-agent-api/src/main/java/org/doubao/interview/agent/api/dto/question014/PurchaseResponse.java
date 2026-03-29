package org.doubao.interview.agent.api.dto.question014;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购买响应 DTO - 展示锁的使用情况
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：返回购买操作的结果和使用的锁信息
 * 边界：仅展示锁的信息，不涉及底层实现细节
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    /**
     * 购买是否成功
     */
    private Boolean success;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 消息提示
     */
    private String message;

    /**
     * 商品 ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 购买前库存
     */
    private Integer stockBefore;

    /**
     * 购买后库存
     */
    private Integer stockAfter;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 使用的锁信息
     */
    private LockInfo lockInfo;

    /**
     * 锁信息类
     * 
     * 【类注释】
     * 职责：展示 InnoDB 锁的类型和特点
     * 
     * InnoDB 锁可以从两个维度分类：
     * 1. 按粒度：表级锁、行级锁
     * 2. 按语义：共享锁 (S)、排他锁 (X)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockInfo {
        /**
         * 锁的类型
         * Record Lock: 记录锁（锁住索引记录）
         * Gap Lock: 间隙锁（锁住间隙，不包含记录本身）
         * Next-Key Lock: 临键锁（Record Lock + Gap Lock）
         * Intention Lock: 意向锁（表级锁，IS/IX）
         */
        private String lockType;

        /**
         * 锁的模式
         * S: 共享锁（读锁），允许其他事务读，不允许写
         * X: 排他锁（写锁），不允许其他事务读写
         * IS: 意向共享锁
         * IX: 意向排他锁
         */
        private String lockMode;

        /**
         * 锁的粒度
         * 表级锁：锁定整个表或部分表
         * 行级锁：锁定特定的行记录
         * 页级锁：锁定数据页（InnoDB 不使用）
         */
        private String lockGranularity;

        /**
         * 是否依赖索引
         * true: 使用索引，锁住特定记录
         * false: 全表扫描，可能锁住更多记录
         */
        private Boolean dependsOnIndex;

        /**
         * 锁定的范围说明
         */
        private String scopeDescription;

        /**
         * 并发影响说明
         */
        private String concurrencyImpact;
    }
}
