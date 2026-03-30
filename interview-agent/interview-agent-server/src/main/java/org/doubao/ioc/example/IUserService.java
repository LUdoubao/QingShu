package org.doubao.ioc.example;

import org.doubao.ioc.annotation.Component;

/**
 * 用户服务接口
 * <p>
 * 注意：接口本身不需要加@Component 注解
 * IOC 容器会自动扫描实现类并注入
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
public interface IUserService {
    
    /**
     * 根据 ID 获取用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    String getUserById(Long id);
    
    /**
     * 创建用户
     *
     * @param name 用户名
     * @return 创建结果
     */
    String createUser(String name);
}
