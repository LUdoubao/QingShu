package org.doubao.interview.agent.server.entity.q014;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类 - 用于演示 InnoDB 锁机制
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：表示商品信息，用于模拟库存扣减场景，展示各种锁的使用
 * 边界：仅用于演示，不包含真实电商系统的复杂业务逻辑
 * 线程安全：非线程安全，依赖数据库锁和事务保证并发安全
 * 幂等性：无特殊幂等设计，依赖业务层控制
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("product_014")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 版本号 - 用于乐观锁演示
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
}
