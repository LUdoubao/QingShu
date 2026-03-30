package org.doubao.ioc;

import org.doubao.ioc.container.ApplicationContext;
import org.doubao.ioc.example.UserController;
import org.doubao.ioc.example.OrderService;
import org.doubao.ioc.example.IUserService;

/**
 * 简易版 Spring IOC 容器 - 演示程序
 * <p>
 * 展示反射机制在 IOC 容器中的核心应用
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
public class SpringIOCDemo {
    
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          简易版 Spring IOC 容器演示                   ║");
        System.out.println("║     重点展示反射机制在 IOC 中的核心应用               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // ==================== 1. 创建 IOC 容器 ====================
            System.out.println("【步骤 1】创建 IOC 容器，自动扫描并初始化 Bean...\n");
            
            ApplicationContext context = new ApplicationContext("org.doubao.ioc");
            
            // ==================== 2. 查看容器中的 Bean ====================
            System.out.println("\n【步骤 2】查看容器中所有的 Bean:");
            String[] beanNames = context.getBeanDefinitionNames();
            System.out.println("容器中共有 " + beanNames.length + " 个 Bean:");
            for (String beanName : beanNames) {
                Object bean = context.getBean(beanName);
                System.out.println("  • " + beanName + " → " + 
                                 bean.getClass().getSimpleName());
            }
            
            // ==================== 3. 获取 Bean 并调用方法 ====================
            System.out.println("\n【步骤 3】从容器中获取 Bean 并调用方法:\n");
            
            // 方式 1: 通过名称获取 Bean
            System.out.println("--- 方式 1: 通过名称获取 Bean ---");
            IUserService userService = context.getBean("userService", IUserService.class);
            if (userService != null) {
                System.out.println("获取到 userService: " + userService.getClass().getSimpleName());
                String result = userService.getUserById(1L);
                System.out.println("调用 getUserById(1): " + result);
                System.out.println();
            }
            
            // 方式 2: 通过类型获取 Bean
            System.out.println("--- 方式 2: 通过类型获取 Bean ---");
            OrderService orderService = context.getBean(OrderService.class);
            if (orderService != null) {
                System.out.println("获取到 orderService: " + orderService.getClass().getSimpleName());
                String orderResult = orderService.createOrder(100L);
                System.out.println("调用 createOrder(100): " + orderResult);
                System.out.println();
            }
            
            // ==================== 4. 验证依赖注入 ====================
            System.out.println("\n【步骤 4】验证依赖注入是否成功:\n");
            
            UserController userController = context.getBean(UserController.class);
            if (userController != null) {
                System.out.println("获取到 userController: " + userController.getClass().getSimpleName());
                
                // 显示依赖关系
                userController.showDependencies();
                
                // 调用 Controller 方法（会间接使用注入的 Service）
                System.out.println("--- 调用 Controller 的方法 ---");
                String userInfo = userController.getUserInfo(1L);
                System.out.println("getUserInfo(1): " + userInfo);
                
                String orderInfo = userController.createOrder(100L);
                System.out.println("createOrder(100): " + orderInfo);
            }
            
            // ==================== 5. 总结 ====================
            System.out.println("\n");
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║                    核心要点总结                      ║");
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║ 1. 反射获取类信息：Class.forName()、getAnnotations() ║");
            System.out.println("║ 2. 反射创建实例：Constructor.newInstance()           ║");
            System.out.println("║ 3. 反射访问字段：Field.setAccessible(true)           ║");
            System.out.println("║ 4. 反射注入依赖：Field.set(obj, value)               ║");
            System.out.println("║ 5. 处理继承层次：getDeclaredFields()递归父类         ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("\n❌ IOC 容器运行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
