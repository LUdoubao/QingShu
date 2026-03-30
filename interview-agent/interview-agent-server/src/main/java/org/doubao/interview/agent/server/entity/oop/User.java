package org.doubao.interview.agent.server.entity.oop;

import lombok.Data;

/**
 * 用户实体类 - 用于演示封装特性
 * <p>
 * 通过将属性私有化并提供公共的 getter/setter 方法，
 * 实现数据的隐藏和保护，体现封装的核心思想。
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Data
public class User {

    /**
     * 私有属性 - 外部不能直接访问
     * 必须通过公共方法进行访问和修改
     */
    private Long id;

    /**
     * 私有属性 - 姓名
     */
    private String name;

    /**
     * 私有属性 - 邮箱（通过 setter 可以进行格式校验）
     */
    private String email;

    /**
     * 无参构造函数
     */
    public User() {
    }

    /**
     * 带参构造函数
     *
     * @param id 用户 ID
     * @param name 用户姓名
     * @param email 用户邮箱
     */
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * 获取用户 ID
     *
     * @return 用户 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户 ID
     *
     * @param id 用户 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户姓名
     *
     * @return 用户姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置用户姓名
     *
     * @param name 用户姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取用户邮箱
     *
     * @return 用户邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置用户邮箱 - 可以在这里进行数据验证
     * <p>
     * 体现了封装的优势：在设置数据时可以进行校验，保证数据的有效性
     * </p>
     *
     * @param email 用户邮箱
     * @throws IllegalArgumentException 当邮箱格式不正确时抛出
     */
    public void setEmail(String email) {
        // 简单的邮箱格式校验
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("邮箱格式不正确：" + email);
        }
        this.email = email;
    }
}
