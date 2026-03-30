package org.doubao.interview.agent.server.entity.method;

/**
 * 计算器类 - 用于演示方法重载
 * <p>
 * 通过多个同名但参数不同的 add 方法，展示方法重载的特性
 * 方法重载的核心：同一个类中，方法名相同，参数列表不同
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public class Calculator {

    /**
     * 无参构造函数
     */
    public Calculator() {
    }

    /**
     * 两个整数相加 - 重载方法 1
     * <p>
     * 参数列表：(int, int)
     * </p>
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两数之和
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 三个整数相加 - 重载方法 2
     * <p>
     * 参数列表：(int, int, int)
     * </p>
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @param c 第三个整数
     * @return 三数之和
     */
    public int add(int a, int b, int c) {
        return a + b + c;
    }

    /**
     * 两个浮点数相加 - 重载方法 3
     * <p>
     * 参数列表：(double, double)
     * </p>
     *
     * @param a 第一个浮点数
     * @param b 第二个浮点数
     * @return 两数之和
     */
    public double add(double a, double b) {
        return a + b;
    }

    /**
     * 字符串拼接 - 重载方法 4
     * <p>
     * 参数列表：(String, String)
     * </p>
     *
     * @param a 第一个字符串
     * @param b 第二个字符串
     * @return 拼接后的字符串
     */
    public String add(String a, String b) {
        return a + b;
    }

    /**
     * 可变参数相加 - 重载方法 5
     * <p>
     * 参数列表：(int...)
     * </p>
     *
     * @param numbers 可变整数数组
     * @return 所有数的和
     */
    public int add(int... numbers) {
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }
}
