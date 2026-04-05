package org.doubao.interview.agent.server.collection;

import java.util.*;

/**
 * HashMap、LinkedHashMap、TreeMap 区别演示类
 * 
 * 三者均实现 Map 接口（键值对存储），核心差异源于底层数据结构，
 * 直接决定了有序性、键的排序规则、性能表现和适用场景
 */
public class CollectionMapComparisonDemo {

    public static void main(String[] args) {
        demonstrateBasicDifferences();
        demonstrateOrderingDifferences();
        demonstrateNullHandling();
        demonstratePerformanceComparison();
        demonstrateTreeMapSpecialFeatures();
        demonstrateLinkedHashMapAccessOrder();
        demonstrateCustomObjectHandling();
    }

    /**
     * 演示基本区别
     */
    public static void demonstrateBasicDifferences() {
        System.out.println("=== 演示 HashMap、LinkedHashMap、TreeMap 基本区别 ===");

        // HashMap - 无序
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("B", 2);
        hashMap.put("A", 1);
        hashMap.put("C", 3);
        hashMap.put("A", 4); // 重复键，值会被替换
        System.out.println("HashMap (无序): " + hashMap);

        // LinkedHashMap - 插入有序
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("B", 2);
        linkedHashMap.put("A", 1);
        linkedHashMap.put("C", 3);
        linkedHashMap.put("A", 4); // 重复键，值会被替换
        System.out.println("LinkedHashMap (插入有序): " + linkedHashMap);

        // TreeMap - 排序有序
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("B", 2);
        treeMap.put("A", 1);
        treeMap.put("C", 3);
        treeMap.put("A", 4); // 重复键，值会被替换
        System.out.println("TreeMap (键排序有序): " + treeMap);

        System.out.println();
    }

    /**
     * 演示有序性差异
     */
    public static void demonstrateOrderingDifferences() {
        System.out.println("=== 演示三种Map的有序性差异 ===");

        String[] keys = {"banana", "apple", "cherry", "date", "elderberry"};
        Integer[] values = {2, 1, 3, 4, 5};

        // HashMap - 无序
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], values[i]);
        }
        System.out.println("HashMap 遍历顺序（通常不是插入顺序）: " + hashMap);

        // LinkedHashMap - 插入有序
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            linkedHashMap.put(keys[i], values[i]);
        }
        System.out.println("LinkedHashMap 遍历顺序（与插入顺序一致）: " + linkedHashMap);

        // TreeMap - 排序有序
        Map<String, Integer> treeMap = new TreeMap<>();
        for (int i = 0; i < keys.length; i++) {
            treeMap.put(keys[i], values[i]);
        }
        System.out.println("TreeMap 遍历顺序（按键字典序排列）: " + treeMap);

        System.out.println();
    }

    /**
     * 演示null值处理差异
     */
    public static void demonstrateNullHandling() {
        System.out.println("=== 演示三种Map对null值的处理差异 ===");

        // HashMap - 允许null键和null值
        Map<String, String> hashMap = new HashMap<>();
        System.out.println("HashMap put null key: " + hashMap.put(null, "value1"));
        System.out.println("HashMap put null value: " + hashMap.put("key1", null));
        System.out.println("HashMap put another null value: " + hashMap.put("key2", null));
        System.out.println("HashMap 内容: " + hashMap);

        // LinkedHashMap - 允许null键和null值
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        System.out.println("LinkedHashMap put null key: " + linkedHashMap.put(null, "value1"));
        System.out.println("LinkedHashMap put null value: " + linkedHashMap.put("key1", null));
        System.out.println("LinkedHashMap 内容: " + linkedHashMap);

        // TreeMap - 不允许null键，但允许null值
        Map<String, String> treeMap = new TreeMap<>();
        try {
            System.out.println("TreeMap put null key: " + treeMap.put(null, "value1"));
        } catch (NullPointerException e) {
            System.out.println("TreeMap put null key抛出异常: " + e.getClass().getSimpleName());
        }
        System.out.println("TreeMap put null value: " + treeMap.put("key1", null));
        System.out.println("TreeMap put another null value: " + treeMap.put("key2", null));
        System.out.println("TreeMap 内容: " + treeMap);

        System.out.println();
    }

    /**
     * 演示性能比较
     */
    public static void demonstratePerformanceComparison() {
        System.out.println("=== 演示三种Map的性能比较 ===");

        int count = 100000;
        Random random = new Random();

        // HashMap 性能测试
        Map<Integer, String> hashMap = new HashMap<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            hashMap.put(random.nextInt(count * 2), "value" + i);
        }
        long hashMapTime = System.currentTimeMillis() - start;
        System.out.println("HashMap put " + count + " 个键值对耗时: " + hashMapTime + " ms");

        // LinkedHashMap 性能测试
        Map<Integer, String> linkedHashMap = new LinkedHashMap<>();
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            linkedHashMap.put(random.nextInt(count * 2), "value" + i);
        }
        long linkedHashMapTime = System.currentTimeMillis() - start;
        System.out.println("LinkedHashMap put " + count + " 个键值对耗时: " + linkedHashMapTime + " ms");

        // TreeMap 性能测试
        Map<Integer, String> treeMap = new TreeMap<>();
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            treeMap.put(random.nextInt(count * 2), "value" + i);
        }
        long treeMapTime = System.currentTimeMillis() - start;
        System.out.println("TreeMap put " + count + " 个键值对耗时: " + treeMapTime + " ms");

        System.out.println("HashMap 大小: " + hashMap.size());
        System.out.println("LinkedHashMap 大小: " + linkedHashMap.size());
        System.out.println("TreeMap 大小: " + treeMap.size());

        System.out.println();
    }

    /**
     * 演示TreeMap的特殊功能
     */
    public static void demonstrateTreeMapSpecialFeatures() {
        System.out.println("=== 演示TreeMap的特殊功能 ===");

        TreeMap<Integer, String> treeMap = new TreeMap<>();
        treeMap.put(1, "one");
        treeMap.put(3, "three");
        treeMap.put(5, "five");
        treeMap.put(7, "seven");
        treeMap.put(9, "nine");
        treeMap.put(2, "two");
        treeMap.put(4, "four");
        treeMap.put(6, "six");
        treeMap.put(8, "eight");
        treeMap.put(10, "ten");

        System.out.println("TreeMap内容: " + treeMap);
        System.out.println("ceilingKey(5): " + treeMap.ceilingKey(5)); // >= 5的最小键
        System.out.println("floorKey(5): " + treeMap.floorKey(5)); // <= 5的最大键
        System.out.println("higherKey(5): " + treeMap.higherKey(5)); // > 5的最小键
        System.out.println("lowerKey(5): " + treeMap.lowerKey(5)); // < 5的最大键
        System.out.println("firstKey(): " + treeMap.firstKey());
        System.out.println("lastKey(): " + treeMap.lastKey());
        System.out.println("subMap(3, 7): " + treeMap.subMap(3, 7)); // [3, 7)
        System.out.println("headMap(5): " + treeMap.headMap(5)); // < 5的所有键
        System.out.println("tailMap(5): " + treeMap.tailMap(5)); // >= 5的所有键

        System.out.println();
    }

    /**
     * 演示LinkedHashMap的访问顺序功能
     */
    public static void demonstrateLinkedHashMapAccessOrder() {
        System.out.println("=== 演示LinkedHashMap的访问顺序功能 ===");

        // 插入顺序模式（默认）
        LinkedHashMap<String, Integer> insertionOrderMap = new LinkedHashMap<>();
        insertionOrderMap.put("first", 1);
        insertionOrderMap.put("second", 2);
        insertionOrderMap.put("third", 3);
        System.out.println("插入顺序模式 - 初始: " + insertionOrderMap);

        // 访问某个元素
        insertionOrderMap.get("first");
        System.out.println("访问'first'后（插入顺序）: " + insertionOrderMap);

        // 访问顺序模式
        LinkedHashMap<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true); // true表示访问顺序
        accessOrderMap.put("first", 1);
        accessOrderMap.put("second", 2);
        accessOrderMap.put("third", 3);
        System.out.println("访问顺序模式 - 初始: " + accessOrderMap);

        // 访问某个元素
        accessOrderMap.get("first");
        System.out.println("访问'first'后（访问顺序）: " + accessOrderMap);

        // 再次访问另一个元素
        accessOrderMap.get("second");
        System.out.println("访问'second'后（访问顺序）: " + accessOrderMap);

        System.out.println();
    }

    /**
     * 演示自定义对象的处理
     */
    public static void demonstrateCustomObjectHandling() {
        System.out.println("=== 演示自定义对象的处理 ===");

        // 演示HashMap需要重写hashCode和equals
        class Person {
            private String name;
            private int age;

            public Person(String name, int age) {
                this.name = name;
                this.age = age;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                Person person = (Person) obj;
                return age == person.age && Objects.equals(name, person.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, age);
            }

            @Override
            public String toString() {
                return "Person{name='" + name + "', age=" + age + '}';
            }
        }

        // HashMap - 使用hashCode和equals判断重复键
        Map<Person, String> personHashMap = new HashMap<>();
        Person p1 = new Person("Alice", 25);
        Person p2 = new Person("Alice", 25); // 相同内容的不同对象
        System.out.println("HashMap put两个相同内容的键:");
        System.out.println("第一次put: " + personHashMap.put(p1, "Employee 1"));
        System.out.println("第二次put: " + personHashMap.put(p2, "Employee 2")); // 应该替换第一次的值
        System.out.println("HashMap 大小: " + personHashMap.size());
        System.out.println("HashMap 内容: " + personHashMap);

        // TreeMap - 需要实现Comparable或传入Comparator
        class Student implements Comparable<Student> {
            private String name;
            private int score;

            public Student(String name, int score) {
                this.name = name;
                this.score = score;
            }

            @Override
            public int compareTo(Student other) {
                return Integer.compare(this.score, other.score); // 按分数排序
            }

            @Override
            public String toString() {
                return "Student{name='" + name + "', score=" + score + '}';
            }
        }

        Map<Student, String> studentTreeMap = new TreeMap<>();
        Student s1 = new Student("Tom", 85);
        Student s2 = new Student("Jerry", 90);
        Student s3 = new Student("Alice", 85); // 分数相同视为重复键

        System.out.println("\nTreeMap put学生（按分数排序）:");
        System.out.println("put " + s1 + ": " + studentTreeMap.put(s1, "Grade A"));
        System.out.println("put " + s2 + ": " + studentTreeMap.put(s2, "Grade B"));
        System.out.println("put " + s3 + ": " + studentTreeMap.put(s3, "Grade C")); // 分数相同，键重复，值会被替换
        System.out.println("TreeMap 内容: " + studentTreeMap);

        System.out.println();
    }
}