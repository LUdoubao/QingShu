package org.doubao.interview.agent.server.collection;

import java.util.*;

/**
 * ArrayList底层实现原理演示类
 * 
 * 演示ArrayList基于动态数组的核心机制，包括初始化、扩容、增删改查等操作
 */
public class ArrayListPrincipleDemo {

    public static void main(String[] args) {
        demonstrateInitialization();
        demonstrateExpansion();
        demonstrateCoreOperations();
        demonstratePerformanceCharacteristics();
    }

    /**
     * 演示ArrayList的初始化机制
     */
    public static void demonstrateInitialization() {
        System.out.println("=== 演示ArrayList初始化机制 ===");

        // 无参构造：懒加载机制（JDK 1.8）
        ArrayList<String> list1 = new ArrayList<>();
        System.out.println("无参构造后容量（通过反射获取）: " + getArrayLength(list1));
        System.out.println("size: " + list1.size());

        // 第一次添加元素时初始化容量为10
        list1.add("element1");
        System.out.println("第一次add后容量: " + getArrayLength(list1));
        System.out.println("size: " + list1.size());

        // 指定初始容量构造
        ArrayList<String> list2 = new ArrayList<>(20);
        System.out.println("指定容量20构造后容量: " + getArrayLength(list2));
        System.out.println("size: " + list2.size());

        // 传入集合构造
        List<String> sourceList = Arrays.asList("a", "b", "c");
        ArrayList<String> list3 = new ArrayList<>(sourceList);
        System.out.println("传入集合构造后容量: " + getArrayLength(list3));
        System.out.println("size: " + list3.size());

        System.out.println();
    }

    /**
     * 演示ArrayList的自动扩容机制
     */
    public static void demonstrateExpansion() {
        System.out.println("=== 演示ArrayList自动扩容机制 ===");

        ArrayList<Integer> list = new ArrayList<>(5); // 初始容量5
        System.out.println("初始容量: " + getArrayLength(list));

        // 添加元素直到触发扩容
        for (int i = 0; i < 10; i++) {
            list.add(i);
            System.out.println("添加元素 " + i + ", 当前size: " + list.size() + ", 容量: " + getArrayLength(list));
            
            // 检查是否发生扩容
            if (i == 4) {
                System.out.println("  >>> 容量满，下次add将触发扩容 <<<");
            } else if (i == 5) {
                System.out.println("  >>> 发生扩容！容量从5变为10 <<<");
            } else if (i == 9) {
                System.out.println("  >>> 容量再次满，下次add将触发扩容 <<<");
            }
        }

        // 继续添加更多元素，观察1.5倍扩容规律
        list.add(10);
        System.out.println("添加第11个元素后，容量变为: " + getArrayLength(list) + " (10->15)");
        
        list.add(11);
        list.add(12);
        list.add(13);
        list.add(14);
        System.out.println("添加第15个元素后，容量变为: " + getArrayLength(list) + " (15->22)");

        System.out.println();
    }

    /**
     * 演示核心操作的性能特征
     */
    public static void demonstrateCoreOperations() {
        System.out.println("=== 演示ArrayList核心操作 ===");

        ArrayList<String> list = new ArrayList<>();
        
        // 尾部添加
        list.add("A");
        list.add("B");
        list.add("C");
        System.out.println("尾部添加: " + list);

        // 指定位置添加（中间插入）
        list.add(1, "X"); // 在索引1处插入X
        System.out.println("在索引1处插入X: " + list);

        // 查询操作
        System.out.println("索引1处的元素: " + list.get(1));

        // 删除操作
        String removed = list.remove(1); // 删除索引1处的元素
        System.out.println("删除索引1处的元素 '" + removed + "': " + list);

        System.out.println();
    }

    /**
     * 演示性能特征
     */
    public static void demonstratePerformanceCharacteristics() {
        System.out.println("=== 演示性能特征 ===");

        // 准备大数据量测试
        int size = 100000;
        ArrayList<Integer> list = new ArrayList<>(size);
        
        // 预填充数据
        for (int i = 0; i < size; i++) {
            list.add(i);
        }

        // 测试随机访问性能
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            int idx = i % size;
            int value = list.get(idx); // O(1) 操作
        }
        long getRandomTime = System.currentTimeMillis() - start;
        System.out.println("随机访问10000次耗时: " + getRandomTime + " ms");

        // 测试尾部添加性能
        start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            list.add(size + i); // O(1) 操作（假设容量足够）
        }
        long addTailTime = System.currentTimeMillis() - start;
        System.out.println("尾部添加10000次耗时: " + addTailTime + " ms");

        // 测试中间插入性能
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            list.add(size/2, -1); // O(n) 操作，需要移动元素
        }
        long addMiddleTime = System.currentTimeMillis() - start;
        System.out.println("中间插入1000次耗时: " + addMiddleTime + " ms");

        System.out.println();
    }

    /**
     * 通过反射获取ArrayList内部数组长度
     */
    private static int getArrayLength(ArrayList<?> list) {
        try {
            java.lang.reflect.Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] array = (Object[]) field.get(list);
            return array.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}