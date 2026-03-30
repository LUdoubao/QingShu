package org.doubao.interview.agent.server.entity.method;

/**
 * 狗类 - 继承自动物类并重写方法
 * <p>
 * 演示方法重写：重写父类的 makeSound() 和 move() 方法
 * 提供狗特有的实现
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public class Dog extends Animal {

    /**
     * 狗的品种
     */
    private String breed;

    /**
     * 无参构造函数
     */
    public Dog() {
        super();
    }

    /**
     * 带参构造函数
     *
     * @param name 狗的名称
     * @param breed 狗的品种
     */
    public Dog(String name, String breed) {
        super(name);
        this.breed = breed;
    }

    /**
     * 获取狗的品种
     *
     * @return 狗的品种
     */
    public String getBreed() {
        return breed;
    }

    /**
     * 设置狗的品种
     *
     * @param breed 狗的品种
     */
    public void setBreed(String breed) {
        this.breed = breed;
    }

    /**
     * 重写父类的 makeSound 方法
     * <p>
     * 体现方法重写：提供狗的特定叫声实现
     * 注解 @Override 确保编译器检查是否正确重写
     * </p>
     *
     * @return 狗的叫声
     */
    @Override
    public String makeSound() {
        return name + "汪汪汪！";
    }

    /**
     * 重写父类的 move 方法
     * <p>
     * 体现方法重写：提供狗的特定移动方式
     * </p>
     *
     * @return 狗的移动方式描述
     */
    @Override
    public String move() {
        return name + "用四条腿奔跑";
    }

    /**
     * 狗特有的行为：看家护院
     * <p>
     * 这是子类新增的方法，不是重写
     * </p>
     *
     * @return 行为描述
     */
    public String guardHome() {
        return name + "正在看家护院";
    }

    @Override
    public String getInfo() {
        return "狗{name='" + name + "', breed='" + breed + "'}";
    }
}
