package org.doubao.interview.agent.server.entity.method;

/**
 * 猫类 - 继承自动物类并重写方法
 * <p>
 * 演示方法重写：重写父类的 makeSound() 和 move() 方法
 * 提供猫特有的实现
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public class Cat extends Animal {

    /**
     * 猫的颜色
     */
    private String color;

    /**
     * 无参构造函数
     */
    public Cat() {
        super();
    }

    /**
     * 带参构造函数
     *
     * @param name 猫的名称
     * @param color 猫的颜色
     */
    public Cat(String name, String color) {
        super(name);
        this.color = color;
    }

    /**
     * 获取猫的颜色
     *
     * @return 猫的颜色
     */
    public String getColor() {
        return color;
    }

    /**
     * 设置猫的颜色
     *
     * @param color 猫的颜色
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 重写父类的 makeSound 方法
     * <p>
     * 体现方法重写：提供猫的特定叫声实现
     * 注解 @Override 确保编译器检查是否正确重写
     * </p>
     *
     * @return 猫的叫声
     */
    @Override
    public String makeSound() {
        return name + "喵喵喵~";
    }

    /**
     * 重写父类的 move 方法
     * <p>
     * 体现方法重写：提供猫的特定移动方式
     * </p>
     *
     * @return 猫的移动方式描述
     */
    @Override
    public String move() {
        return name + "优雅地踱步";
    }

    /**
     * 猫特有的行为：抓老鼠
     * <p>
     * 这是子类新增的方法，不是重写
     * </p>
     *
     * @return 行为描述
     */
    public String catchMouse() {
        return name + "正在抓老鼠";
    }

    @Override
    public String getInfo() {
        return "猫{name='" + name + "', color='" + color + "'}";
    }
}
