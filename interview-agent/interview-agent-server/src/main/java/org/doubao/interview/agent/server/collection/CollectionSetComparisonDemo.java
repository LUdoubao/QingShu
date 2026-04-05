package org.doubao.interview.agent.server.collection;

import java.util.*;

/**
 * HashSet、LinkedHashSet、TreeSet 区别演示类
 * 
 * 三者均实现 Set 接口，核心特性是元素不可重复，
 * 但底层数据结构不同，导致有序性、去重原理、性能表现、适用场景存在本质差异
 */
public class CollectionSetComparisonDemo {

    public static void main(String[] args) {
        demonstrateBasicDifferences();
        demonstrateOrderingDifferences();
        demonstrateNullHandling();
        demonstratePerformanceComparison();
        demonstrateTreeSetSpecialFeatures();
        demonstrateCustomObjectHandling();
    }

    /**
     * 演示基本区别
     */
    public static void demonstrateBasicDifferences() {
        System.out.println("=== 演示 HashSet、LinkedHashSet、TreeSet 基本区别 ===");

        // HashSet - 无序
        Set<String> hashSet = new HashSet<>();
        hashSet.add("B");
        hashSet.add("A");
        hashSet.add("C");
        hashSet.add("A"); // 重复元素，会被忽略
        System.out.println("HashSet (无序): " + hashSet);

        // LinkedHashSet - 插入有序
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("B");
        linkedHashSet.add("A");
        linkedHashSet.add("C");
        linkedHashSet.add("A"); // 重复元素，会被忽略
        System.out.println("LinkedHashSet (插入有序): " + linkedHashSet);

        // TreeSet - 排序有序
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("B");
        treeSet.add("A");
        treeSet.add("C");
        treeSet.add("A"); // 重复元素，会被忽略
        System.out.println("TreeSet (排序有序): " + treeSet);

        System.out.println();
    }

    /**
     * 演示有序性差异
     */
    public static void demonstrateOrderingDifferences() {
        System.out.println("=== 演示三种Set的有序性差异 ===");

        String[] elements = {"banana", "apple", "cherry", "date", "elderberry"};

        // HashSet - 无序
        Set<String> hashSet = new HashSet<>();
        for (String element : elements) {
            hashSet.add(element);
        }
        System.out.println("HashSet 遍历顺序（通常不是插入顺序）: " + hashSet);

        // LinkedHashSet - 插入有序
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (String element : elements) {
            linkedHashSet.add(element);
        }
        System.out.println("LinkedHashSet 遍历顺序（与插入顺序一致）: " + linkedHashSet);

        // TreeSet - 排序有序
        Set<String> treeSet = new TreeSet<>();
        for (String element : elements) {
            treeSet.add(element);
        }
        System.out.println("TreeSet 遍历顺序（按字典序排列）: " + treeSet);

        System.out.println();
    }

    /**
     * 演示null值处理差异
     */
    public static void demonstrateNullHandling() {
        System.out.println("=== 演示三种Set对null值的处理差异 ===");

        // HashSet - 允许null
        Set<String> hashSet = new HashSet<>();
        System.out.println("HashSet 添加null: " + hashSet.add(null));
        System.out.println("HashSet 再次添加null: " + hashSet.add(null));
        System.out.println("HashSet 内容: " + hashSet);

        // LinkedHashSet - 允许null
        Set<String> linkedHashSet = new LinkedHashSet<>();
        System.out.println("LinkedHashSet 添加null: " + linkedHashSet.add(null));
        System.out.println("LinkedHashSet 再次添加null: " + linkedHashSet.add(null));
        System.out.println("LinkedHashSet 内容: " + linkedHashSet);

        // TreeSet - 不允许null
        Set<String> treeSet = new TreeSet<>();
        try {
            System.out.println("TreeSet 添加null: " + treeSet.add(null));
        } catch (NullPointerException e) {
            System.out.println("TreeSet 添加null抛出异常: " + e.getClass().getSimpleName());
        }

        System.out.println();
    }

    /**
     * 演示性能比较
     */
    public static void demonstratePerformanceComparison() {
        System.out.println("=== 演示三种Set的性能比较 ===");

        int count = 100000;
        Random random = new Random();

        // HashSet 性能测试
        Set<Integer> hashSet = new HashSet<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            hashSet.add(random.nextInt(count * 2)); // 添加一些重复元素
        }
        long hashSetTime = System.currentTimeMillis() - start;
        System.out.println("HashSet 添加 " + count + " 个元素耗时: " + hashSetTime + " ms");

        // LinkedHashSet 性能测试
        Set<Integer> linkedHashSet = new LinkedHashSet<>();
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            linkedHashSet.add(random.nextInt(count * 2)); // 添加一些重复元素
        }
        long linkedHashSetTime = System.currentTimeMillis() - start;
        System.out.println("LinkedHashSet 添加 " + count + " 个元素耗时: " + linkedHashSetTime + " ms");

        // TreeSet 性能测试
        Set<Integer> treeSet = new TreeSet<>();
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            treeSet.add(random.nextInt(count * 2)); // 添加一些重复元素
        }
        long treeSetTime = System.currentTimeMillis() - start;
        System.out.println("TreeSet 添加 " + count + " 个元素耗时: " + treeSetTime + " ms");

        System.out.println("HashSet 大小: " + hashSet.size());
        System.out.println("LinkedHashSet 大小: " + linkedHashSet.size());
        System.out.println("TreeSet 大小: " + treeSet.size());

        System.out.println();
    }

    /**
     * 演示TreeSet的特殊功能
     */
    public static void demonstrateTreeSetSpecialFeatures() {
        System.out.println("=== 演示TreeSet的特殊功能 ===");

        TreeSet<Integer> treeSet = new TreeSet<>();
        Collections.addAll(treeSet, 1, 3, 5, 7, 9, 2, 4, 6, 8, 10);

        System.out.println("TreeSet内容: " + treeSet);
        System.out.println("ceiling(5): " + treeSet.ceiling(5)); // >= 5的最小元素
        System.out.println("floor(5): " + treeSet.floor(5)); // <= 5的最大元素
        System.out.println("higher(5): " + treeSet.higher(5)); // > 5的最小元素
        System.out.println("lower(5): " + treeSet.lower(5)); // < 5的最大元素
        System.out.println("first(): " + treeSet.first());
        System.out.println("last(): " + treeSet.last());
        System.out.println("subSet(3, 7): " + treeSet.subSet(3, 7)); // [3, 7)
        System.out.println("headSet(5): " + treeSet.headSet(5)); // < 5的所有元素
        System.out.println("tailSet(5): " + treeSet.tailSet(5)); // >= 5的所有元素

        System.out.println();
    }

    /**
     * 演示自定义对象的处理
     */
    public static void demonstrateCustomObjectHandling() {
        System.out.println("=== 演示自定义对象的处理 ===");

        // 演示HashSet需要重写hashCode和equals
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

        // HashSet - 使用hashCode和equals判断重复
        Set<Person> personHashSet = new HashSet<>();
        Person p1 = new Person("Alice", 25);
        Person p2 = new Person("Alice", 25); // 相同内容的不同对象
        System.out.println("HashSet 添加两个相同内容的对象:");
        System.out.println("第一次添加: " + personHashSet.add(p1));
        System.out.println("第二次添加: " + personHashSet.add(p2));
        System.out.println("HashSet 大小: " + personHashSet.size());

        // TreeSet - 需要实现Comparable或传入Comparator
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

        Set<Student> studentTreeSet = new TreeSet<>();
        Student s1 = new Student("Tom", 85);
        Student s2 = new Student("Jerry", 90);
        Student s3 = new Student("Alice", 85); // 分数相同视为重复

        System.out.println("\nTreeSet 添加学生（按分数排序）:");
        System.out.println("添加 " + s1 + ": " + studentTreeSet.add(s1));
        System.out.println("添加 " + s2 + ": " + studentTreeSet.add(s2));
        System.out.println("添加 " + s3 + ": " + studentTreeSet.add(s3)); // 分数相同，视为重复
        System.out.println("TreeSet 内容: " + studentTreeSet);

        System.out.println();
    }
}