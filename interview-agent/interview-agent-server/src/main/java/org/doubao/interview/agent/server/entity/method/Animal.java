package org.doubao.interview.agent.server.entity.method;

/**
 * 动物基类 - 用于演示方法重写
 * <p>
 * 定义了动物的基本方法，子类可以重写这些方法
 * 方法重写的核心：父子类中，方法签名完全一致，子类提供特定实现
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public abstract class Animal {

    /**
     * 动物名称
     */
    protected String name;

    /**
     * 无参构造函数
     */
    public Animal() {
    }

    /**
     * 带参构造函数
     *
     * @param name 动物名称
     */
    public Animal(String name) {
        this.name = name;
    }

    /**
     * 获取动物名称
     *
     * @return 动物名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置动物名称
     *
     * @param name 动物名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 抽象方法：发出声音
     * <p>
     * 由具体子类实现各自的声音行为，体现方法重写
     * </p>
     *
     * @return 动物发出的声音描述
     */
    public abstract String makeSound();

    /**
     * 普通方法：移动
     * <p>
     * 所有动物都可以移动的通用行为，子类可以选择重写
     * </p>
     *
     * @return 移动方式的描述
     */
    public String move() {
        return name + "正在移动";
    }

    /**
     * 获取动物信息
     *
     * @return 动物信息字符串
     */
    public String getInfo() {
        return "动物{name='" + name + "'}";
    }
}
