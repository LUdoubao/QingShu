package org.doubao.ioc.example;

import org.doubao.ioc.annotation.Service;

/**
 * 用户服务实现类
 * <p>
 * 使用@Service 注解标识为 Spring Bean
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Service("userService")
public class UserServiceImpl implements IUserService {
    
    /**
     * 测试方法：验证服务是否正常工作
     */
    public UserServiceImpl() {
        System.out.println("[UserServiceImpl] 构造器被调用");
        System.out.println("[UserServiceImpl] 实现的接口：");
        for (Class<?> iface : this.getClass().getInterfaces()) {
            System.out.println("  - " + iface.getName());
        }
    }
    
    @Override
    public String getUserById(Long id) {
        return "用户-" + id + "-张三";
    }
    
    @Override
    public String createUser(String name) {
        return "创建用户成功：" + name;
    }
}
