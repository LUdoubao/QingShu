package org.doubao.interview.agent.server.entity.oop;

/**
 * 狗类 - 继承自动物类
 * <p>
 * 演示继承特性：复用父类的属性和方法
 * 演示多态特性：重写父类的抽象方法
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
     * @param age 狗的年龄
     * @param breed 狗的品种
     */
    public Dog(String name, Integer age, String breed) {
        super(name, age);
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
     * 体现多态性：不同的动物发出不同的声音
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
     * 体现多态性：狗的移动方式有自己特定的描述
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
     *
     * @return 行为描述
     */
    public String guardHome() {
        return name + "正在看家护院";
    }

    @Override
    public String getInfo() {
        return "狗{name='" + name + "', age=" + age + ", breed='" + breed + "'}";
    }
}
