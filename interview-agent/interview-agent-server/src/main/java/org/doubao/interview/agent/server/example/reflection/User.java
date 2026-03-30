package org.doubao.interview.agent.server.example.reflection;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户实体类 - 用于反射演示
 * <p>
 * 包含各种类型的属性和方法，用于展示反射的各种用法
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 私有属性 - 演示访问 private 字段
     */
    private Long id;
    
    /**
     * 私有属性 - 演示修改 final 字段
     */
    private String username;
    
    /**
     * 私有属性 - 演示类型转换
     */
    private Integer age;
    
    /**
     * 私有属性 - 演示对象类型
     */
    private String email;
    
    /**
     * 静态常量 - 演示访问静态属性
     */
    public static final String TYPE = "USER";
    
    /**
     * 静态变量 - 演示修改静态属性
     */
    public static int count = 0;
    
    // ==================== 构造器 ====================
    
    /**
     * 无参构造器 - 演示调用无参构造
     */
    public User() {
        count++;
    }
    
    /**
     * 带参构造器 - 演示调用带参构造
     */
    public User(String username, Integer age) {
        this.username = username;
        this.age = age;
        count++;
    }
    
    /**
     * 全参构造器 - 演示获取特定构造器
     */
    public User(Long id, String username, Integer age, String email) {
        this.id = id;
        this.username = username;
        this.age = age;
        this.email = email;
        count++;
    }
    
    // ==================== Public 方法 ====================
    
    /**
     * 无参无返回值方法 - 演示调用 void 方法
     */
    public void showInfo() {
        System.out.println("===== 用户信息 =====");
        System.out.println("ID: " + this.id);
        System.out.println("用户名：" + this.username);
        System.out.println("年龄：" + this.age);
        System.out.println("邮箱：" + this.email);
        System.out.println("===================");
    }
    
    /**
     * 有参有返回值方法 - 演示调用带参方法
     */
    public String greet(String greeting) {
        return greeting + ", 我是" + username + "!";
    }
    
    /**
     * 多参数方法 - 演示复杂参数调用
     */
    public String createProfile(String hobby, String city) {
        return String.format("%s, %d岁，住在%s，爱好%s", 
                username, age, city, hobby);
    }
    
    /**
     * 静态方法 - 演示调用静态方法
     */
    public static String getUserType() {
        return TYPE;
    }
    
    /**
     * 静态方法带参数 - 演示静态方法传参
     */
    public static int addCount(int increment) {
        count += increment;
        return count;
    }
    
    // ==================== Private 方法 ====================
    
    /**
     * 私有方法 - 演示访问私有方法
     */
    private void validateAge() {
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("年龄不合法：" + age);
        }
    }
    
    /**
     * 私有带返回值方法 - 演示调用私有方法并获取返回值
     */
    private String formatEmail() {
        if (email == null || !email.contains("@")) {
            return "邮箱格式错误";
        }
        return email.toLowerCase();
    }
    
    /**
     * 私有静态方法 - 演示调用私有静态方法
     */
    private static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}
