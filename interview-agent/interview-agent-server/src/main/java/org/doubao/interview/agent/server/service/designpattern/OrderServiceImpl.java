package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单服务实现类（被代理的目标对象）
 * 
 * 这是真实的业务逻辑类，不包含横切关注点（如事务、日志）
 * Spring AOP 会为这个类生成代理对象
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class OrderServiceImpl implements OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    /**
     * 创建订单（核心业务逻辑）
     * 
     * @param orderId 订单 ID
     * @param amount 订单金额
     * @return 创建结果
     */
    @Override
    public String createOrder(String orderId, double amount) {
        log.info("[核心业务] 创建订单：orderId={}, amount={}", orderId, amount);
        
        // 模拟业务逻辑：验证参数
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("订单 ID 不能为空");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("订单金额必须大于 0");
        }
        
        // 模拟数据库操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "订单创建成功：" + orderId;
    }
    
    /**
     * 查询订单
     * 
     * @param orderId 订单 ID
     * @return 订单信息
     */
    @Override
    public String getOrder(String orderId) {
        log.info("[核心业务] 查询订单：orderId={}", orderId);
        return "订单详情-" + orderId + ": 商品 A*2, 总计￥199.00";
    }
    
    /**
     * 删除订单
     * 
     * @param orderId 订单 ID
     */
    @Override
    public void deleteOrder(String orderId) {
        log.info("[核心业务] 删除订单：orderId={}", orderId);
        System.out.println("订单已删除：" + orderId);
    }
}
