package org.doubao.interview.agent.api.service.question014;

import org.doubao.interview.agent.api.dto.question014.PurchaseRequest;
import org.doubao.interview.agent.api.dto.question014.PurchaseResponse;

/**
 * InnoDB 锁演示服务接口
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：提供购买功能，展示 InnoDB 各种锁的使用场景和特点
 * 边界：仅用于演示和学习，不包含真实电商系统的复杂处理
 * 线程安全：接口本身不保证线程安全，实现类通过数据库锁保证并发安全
 * 幂等性：通过 orderNo 保证同一订单的幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface InnodbLockDemoService {

    /**
     * 执行购买操作 - 演示 InnoDB 锁的使用
     * 
     * 【方法注释】
     * 输入约束：userId 和 productId 必须存在；quantity 必须大于 0
     * 输出语义：返回购买结果，包含使用的锁信息
     * 异常场景：
     *   - 商品不存在时抛出 BusinessException
     *   - 库存不足时抛出 BusinessException
     *   - 并发冲突时可能抛出 DeadlockException
     * 性能注意点：涉及数据库事务和锁操作，避免长事务持有锁
     * 
     * @param request 购买请求参数
     * @return 购买响应，包含锁的详细信息
     */
    PurchaseResponse purchase(PurchaseRequest request);

    /**
     * 查询商品信息
     * 
     * 【方法注释】
     * 输入约束：productId 必须为正整数
     * 输出语义：返回商品的详细信息（JSON 格式）
     * 异常场景：商品不存在时返回 null
     * 
     * @param productId 商品 ID
     * @return 商品信息（JSON 字符串）
     */
    String getProductInfo(Long productId);

    /**
     * 初始化演示数据
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：创建测试商品数据
     * 异常场景：如果表已存在数据则先清空
     * 
     * @return 初始化结果描述
     */
    String initDemoData();

    /**
     * 获取 InnoDB 锁的详细分类说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 InnoDB 锁的分类对比和详细说明
     * 异常场景：无
     * 
     * @return InnoDB 锁的分类说明（Markdown 格式）
     */
    String getLockClassification();
}
