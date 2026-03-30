package org.doubao.ioc.example;

import org.doubao.ioc.annotation.Autowired;
import org.doubao.ioc.annotation.Component;

/**
 * 用户控制器 - 演示多层依赖注入
 * <p>
 * Controller 依赖于 Service，展示 IOC 的层级注入
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Component("userController")
public class UserController {
    
    /**
     * 自动注入 UserService
     */
    @Autowired
    private IUserService userService;
    
    /**
     * 自动注入 OrderService
     * <p>
     * 注意：OrderService 本身也注入了 UserService
     * 这展示了 IOC 容器处理复杂依赖关系的能力
     * </p>
     */
    @Autowired
    private OrderService orderService;
    
    /**
     * 获取用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    public String getUserInfo(Long id) {
        return "Controller: " + userService.getUserById(id);
    }
    
    /**
     * 创建订单
     *
     * @param userId 用户 ID
     * @return 订单信息
     */
    public String createOrder(Long userId) {
        // 通过 orderService 间接使用 userService
        return "Controller: " + orderService.createOrder(userId);
    }
    
    /**
     * 测试方法：显示所有注入的依赖
     */
    public void showDependencies() {
        System.out.println("===== UserController 的依赖关系 =====");
        System.out.println("userService = " + (userService != null ? "✓ 已注入" : "✗ 未注入"));
        System.out.println("orderService = " + (orderService != null ? "✓ 已注入" : "✗ 未注入"));
        
        if (orderService != null) {
            System.out.println("\n检查 OrderService 的依赖:");
            try {
                java.lang.reflect.Field field = OrderService.class.getDeclaredField("userService");
                field.setAccessible(true);
                Object injectedService = field.get(orderService);
                System.out.println("OrderService.userService = " + 
                                 (injectedService != null ? "✓ 已注入" : "✗ 未注入"));
            } catch (Exception e) {
                System.out.println("检查失败：" + e.getMessage());
            }
        }
        System.out.println("========================================\n");
    }
}
