package org.doubao.interview.agent.server.entity.oop;

/**
 * 猫类 - 继承自动物类
 * <p>
 * 演示继承特性：复用父类的属性和方法
 * 演示多态特性：重写父类的抽象方法
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
     * @param age 猫的年龄
     * @param color 猫的颜色
     */
    public Cat(String name, Integer age, String color) {
        super(name, age);
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
     * 体现多态性：猫和狗发出不同的声音
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
     * 体现多态性：猫的移动方式优雅
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
     *
     * @return 行为描述
     */
    public String catchMouse() {
        return name + "正在抓老鼠";
    }

    @Override
    public String getInfo() {
        return "猫{name='" + name + "', age=" + age + ", color='" + color + "'}";
    }
}
