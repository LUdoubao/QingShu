package org.doubao.interview.agent.server.example.designpattern;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例 Bean - 用户服务（单例模式）
 * 
 * 用于演示 Spring 工厂模式和单例模式的示例类
 * 特点：
 * 1. 无状态服务，适合使用单例模式
 * 2. 通过工厂模式创建
 * 3. 线程安全，可被多个请求共享
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@Data
@NoArgsConstructor
public class UserService {
    
    /**
     * 服务名称
     */
    private String serviceName = "UserService";
    
    /**
     * 模拟业务方法：获取用户信息
     * 
     * @param userId 用户 ID
     * @return 用户信息描述
     */
    public String getUserInfo(String userId) {
        // 模拟无状态服务，不保存任何实例变量
        return "User-" + userId + " information retrieved by singleton service";
    }
    
    /**
     * 模拟初始化方法
     * 
     * 在 Bean 创建完成后调用，对应 Spring 的 init-method
     */
    public void init() {
        System.out.println("UserService initialized");
    }
    
    /**
     * 模拟销毁方法
     * 
     * 在容器关闭时调用，对应 Spring 的 destroy-method
     */
    public void destroy() {
        System.out.println("UserService destroyed");
    }
}
