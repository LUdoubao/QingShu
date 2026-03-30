package org.doubao.ioc.example;

import org.doubao.ioc.annotation.Autowired;
import org.doubao.ioc.annotation.Component;

/**
 * 订单服务 - 演示依赖注入
 * <p>
 * 使用@Autowired 自动注入 UserService
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Component("orderService")
public class OrderService {
    
    /**
     * 使用@Autowired 注解自动注入依赖
     * <p>
     * IOC 容器会通过反射将这个字段设置为 userService Bean
     * </p>
     */
    @Autowired
    private IUserService userService;
    
    /**
     * 测试方法：验证依赖注入是否成功
     */
    public OrderService() {
        System.out.println("[OrderService] 构造器被调用");
    }
    
    /**
     * 创建订单
     * <p>
     * 调用注入的 userService
     * </p>
     *
     * @param userId 用户 ID
     * @return 订单信息
     */
    public String createOrder(Long userId) {
        // 使用注入的 userService
        String userInfo = userService.getUserById(userId);
        return "为" + userInfo + "创建订单成功";
    }
    
    /**
     * 获取用户信息（通过注入的服务）
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    public String getUserInfo(Long id) {
        return userService.getUserById(id);
    }
}
