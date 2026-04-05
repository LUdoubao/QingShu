package org.doubao.interview.agent.server.collection;

import java.util.Arrays;

/**
 * SimpleArrayList测试类
 * 
 * 验证我们实现的SimpleArrayList是否符合预期
 */
public class SimpleArrayListTest {

    public static void main(String[] args) {
        System.out.println("=== SimpleArrayList测试 ===");
        
        testBasicOperations();
        testExpansion();
        testEdgeCases();
    }

    /**
     * 测试基本操作
     */
    public static void testBasicOperations() {
        System.out.println("1. 测试基本操作:");
        
        SimpleArrayList<String> list = new SimpleArrayList<>();
        
        // 添加元素
        list.add("A");
        list.add("B");
        list.add("C");
        System.out.println("添加ABC后: " + Arrays.toString(toArray(list)));
        
        // 获取元素
        System.out.println("索引1的元素: " + list.get(1));
        
        // 设置元素
        String oldVal = list.set(1, "X");
        System.out.println("设置索引1为X，原值: " + oldVal);
        System.out.println("设置后: " + Arrays.toString(toArray(list)));
        
        // 插入元素
        list.add(1, "Y");
        System.out.println("在索引1插入Y: " + Arrays.toString(toArray(list)));
        
        // 删除元素
        String removed = list.remove(1);
        System.out.println("删除索引1的元素: " + removed);
        System.out.println("删除后: " + Arrays.toString(toArray(list)));
        
        // 检查大小
        System.out.println("当前大小: " + list.size());
        
        System.out.println();
    }

    /**
     * 测试扩容机制
     */
    public static void testExpansion() {
        System.out.println("2. 测试扩容机制:");
        
        // 使用小容量初始化，更容易观察扩容
        SimpleArrayList<Integer> list = new SimpleArrayList<>(2);
        System.out.println("初始容量: " + getCapacity(list));
        
        // 添加元素直到触发扩容
        for (int i = 0; i < 5; i++) {
            list.add(i);
            System.out.println("添加 " + i + "，当前大小: " + list.size() + "，容量: " + getCapacity(list));
        }
        
        System.out.println("扩容后内容: " + Arrays.toString(toArray(list)));
        System.out.println();
    }

    /**
     * 测试边界情况
     */
    public static void testEdgeCases() {
        System.out.println("3. 测试边界情况:");
        
        SimpleArrayList<String> list = new SimpleArrayList<>();
        
        // 测试空列表
        System.out.println("空列表大小: " + list.size());
        System.out.println("空列表是否为空: " + list.isEmpty());
        
        // 测试构造函数
        SimpleArrayList<String> list2 = new SimpleArrayList<>(5);
        System.out.println("指定容量5的列表容量: " + getCapacity(list2));
        
        // 测试包含操作
        list.add("Hello");
        list.add("World");
        System.out.println("包含Hello: " + list.contains("Hello"));
        System.out.println("包含Java: " + list.contains("Java"));
        
        System.out.println("索引Hello: " + list.indexOf("Hello"));
        System.out.println("索引Java: " + list.indexOf("Java"));
        
        System.out.println();
    }

    /**
     * 辅助方法：获取SimpleArrayList的容量
     */
    private static int getCapacity(SimpleArrayList<?> list) {
        try {
            java.lang.reflect.Field field = SimpleArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] array = (Object[]) field.get(list);
            return array.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 辅助方法：将SimpleArrayList转换为普通数组
     */
    private static Object[] toArray(SimpleArrayList<?> list) {
        Object[] result = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}