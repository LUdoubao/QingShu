package org.doubao.interview.agent.server.entity.equals;

/**
 * 用户类 - 用于演示 equals() 方法重写
 * <p>
 * 演示如何正确重写 equals() 和 hashCode() 方法
 * 重写的原则：如果两个对象通过 equals() 比较相等，则它们的 hashCode() 必须相同
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public class User {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户年龄
     */
    private Integer age;

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
     * @param age 用户年龄
     */
    public User(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
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
     * 获取用户年龄
     *
     * @return 用户年龄
     */
    public Integer getAge() {
        return age;
    }

    /**
     * 设置用户年龄
     *
     * @param age 用户年龄
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * 重写 equals() 方法
     * <p>
     * 比较规则：如果两个用户的 id 相同，则认为它们是同一个用户
     * 注意：只比较 id 字段，不比较 name 和 age
     * </p>
     *
     * @param obj 要比较的对象
     * @return 如果两个用户 id 相同则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object obj) {
        // 1. 检查是否为 null
        if (obj == null) {
            return false;
        }
        
        // 2. 检查是否是同一个对象（地址相同）
        if (this == obj) {
            return true;
        }
        
        // 3. 检查是否是同一类型
        if (!(obj instanceof User)) {
            return false;
        }
        
        // 4. 类型转换并比较关键字段
        User other = (User) obj;
        
        // 5. 比较 id 字段（都为 null 时认为相等）
        if (this.id == null) {
            return other.id == null;
        } else {
            return this.id.equals(other.id);
        }
    }

    /**
     * 重写 hashCode() 方法
     * <p>
     * 与 equals() 保持一致：如果两个用户 equals() 相等，则 hashCode() 也必须相等
     * 只使用 id 字段计算 hashCode
     * </p>
     *
     * @return 基于 id 字段的哈希码
     */
    @Override
    public int hashCode() {
        // 如果 id 为 null，返回 0；否则使用 id 的 hashCode
        return id != null ? id.hashCode() : 0;
    }

    /**
     * 重写 toString() 方法
     *
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', age=" + age + "}";
    }
}
