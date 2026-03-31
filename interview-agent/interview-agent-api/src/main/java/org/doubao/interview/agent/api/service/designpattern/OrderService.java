package org.doubao.interview.agent.api.service.designpattern;

/**
 * 代理服务接口
 * 
 * 定义需要被代理的业务接口
 * 对应 Spring AOP 中的目标对象接口
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 
     * @param orderId 订单 ID
     * @param amount 订单金额
     * @return 创建结果
     */
    String createOrder(String orderId, double amount);
    
    /**
     * 查询订单
     * 
     * @param orderId 订单 ID
     * @return 订单信息
     */
    String getOrder(String orderId);
    
    /**
     * 删除订单
     * 
     * @param orderId 订单 ID
     */
    void deleteOrder(String orderId);
}
