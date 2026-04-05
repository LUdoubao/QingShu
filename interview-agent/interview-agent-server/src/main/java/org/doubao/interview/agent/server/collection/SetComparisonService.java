package org.doubao.interview.agent.server.collection;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Set比较服务类
 * 
 * 提供高级Set比较功能，包括自定义对象处理、线程安全Set对比等
 */
@Service
public class SetComparisonService {

    /**
     * 比较自定义对象在不同Set中的行为
     */
    public Map<String, Object> compareCustomObjectsHandling() {
        Map<String, Object> result = new HashMap<>();

        // 定义一个实现了Comparable接口的类
        class Product implements Comparable<Product> {
            private String name;
            private double price;

            public Product(String name, double price) {
                this.name = name;
                this.price = price;
            }

            @Override
            public int compareTo(Product other) {
                int nameCompare = this.name.compareTo(other.name);
                if (nameCompare != 0) return nameCompare;
                return Double.compare(this.price, other.price);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                Product product = (Product) obj;
                return Double.compare(product.price, price) == 0 &&
                        Objects.equals(name, product.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, price);
            }

            @Override
            public String toString() {
                return "Product{name='" + name + "', price=" + price + '}';
            }
        }

        // 创建测试数据
        Product p1 = new Product("iPhone", 999.99);
        Product p2 = new Product("Samsung Galaxy", 899.99);
        Product p3 = new Product("iPhone", 999.99); // 与p1内容相同
        Product p4 = new Product("Pixel", 799.99);

        List<Product> products = Arrays.asList(p1, p2, p3, p4);

        // HashSet - 使用hashCode和equals判断重复
        Set<Product> productHashSet = new HashSet<>(products);
        result.put("product_hashset", productHashSet.toString());
        result.put("product_hashset_size", productHashSet.size());

        // LinkedHashSet - 保持插入顺序
        Set<Product> productLinkedHashSet = new LinkedHashSet<>(products);
        result.put("product_linkedhashset", productLinkedHashSet.toString());
        result.put("product_linkedhashset_size", productLinkedHashSet.size());

        // TreeSet - 使用compareTo判断重复和排序
        Set<Product> productTreeSet = new TreeSet<>(products);
        result.put("product_treeset", productTreeSet.toString());
        result.put("product_treeset_size", productTreeSet.size());

        result.put("custom_object_note", "HashSet使用hashCode/equals判断重复，TreeSet使用compareTo判断重复");

        return result;
    }

    /**
     * 演示线程安全的Set实现
     */
    public Map<String, Object> demonstrateThreadSafeSets() {
        Map<String, Object> result = new HashMap<>();

        // 非线程安全Set
        Set<String> hashSet = new HashSet<>();
        Set<String> treeSet = new TreeSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();

        // 线程安全Set
        Set<String> synchronizedHashSet = Collections.synchronizedSet(new HashSet<>());
        Set<String> concurrentSkipListSet = new ConcurrentSkipListSet<>();

        result.put("non_thread_safe_sets", Arrays.asList("HashSet", "TreeSet", "LinkedHashSet"));
        result.put("thread_safe_sets", Arrays.asList(
            "Collections.synchronizedSet(new HashSet<>())",
            "ConcurrentSkipListSet"
        ));

        result.put("thread_safety_note", "HashSet、TreeSet、LinkedHashSet都不是线程安全的，需要额外处理");

        return result;
    }

    /**
     * 分析三种Set的适用场景
     */
    public Map<String, String> analyzeUseCases() {
        Map<String, String> useCases = new HashMap<>();

        useCases.put("HashSet_use_case", "适用于需要快速查找、插入、删除的场景，不要求元素有序");
        useCases.put("LinkedHashSet_use_case", "适用于需要去重但又需要保持元素插入顺序的场景");
        useCases.put("TreeSet_use_case", "适用于需要对元素进行排序，或者需要范围查询的场景");
        useCases.put("performance_note", "HashSet性能最高，LinkedHashSet次之，TreeSet最慢但功能最丰富");

        return useCases;
    }

    /**
     * 比较三种Set的空间复杂度
     */
    public Map<String, String> compareSpaceComplexity() {
        Map<String, String> spaceComplexity = new HashMap<>();

        spaceComplexity.put("HashSet_space", "O(n)，其中n是元素数量，需要额外的哈希桶空间");
        spaceComplexity.put("LinkedHashSet_space", "O(n)，除了哈希表空间外，还需要维护双向链表指针");
        spaceComplexity.put("TreeSet_space", "O(n)，每个节点需要额外的空间维护红黑树结构");

        return spaceComplexity;
    }

    /**
     * 展示TreeSet的自定义排序功能
     */
    public Map<String, Object> demonstrateCustomSorting() {
        Map<String, Object> result = new HashMap<>();

        List<String> words = Arrays.asList("banana", "apple", "cherry", "date", "elderberry");

        // 自然排序（字母顺序）
        Set<String> naturalOrder = new TreeSet<>(words);
        result.put("natural_order", new ArrayList<>(naturalOrder));

        // 逆序排序
        Set<String> reverseOrder = new TreeSet<>(Collections.reverseOrder());
        reverseOrder.addAll(words);
        result.put("reverse_order", new ArrayList<>(reverseOrder));

        // 按长度排序
        Set<String> lengthOrder = new TreeSet<>((s1, s2) -> {
            int lenCompare = Integer.compare(s1.length(), s2.length());
            if (lenCompare != 0) return lenCompare;
            return s1.compareTo(s2); // 长度相同时按字母顺序
        });
        lengthOrder.addAll(words);
        result.put("length_order", new ArrayList<>(lengthOrder));

        result.put("custom_sorting_note", "TreeSet支持多种排序方式，包括自然排序和自定义排序");

        return result;
    }

    /**
     * 比较fail-fast行为
     */
    public Map<String, Boolean> compareFailFastBehavior() {
        Map<String, Boolean> result = new HashMap<>();

        // HashSet fail-fast
        Set<String> hashSet = new HashSet<>();
        hashSet.addAll(Arrays.asList("a", "b", "c"));
        boolean hashSetThrows = testFailFastBehavior(hashSet);

        // LinkedHashSet fail-fast
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(Arrays.asList("a", "b", "c"));
        boolean linkedHashSetThrows = testFailFastBehavior(linkedHashSet);

        // TreeSet fail-fast
        Set<String> treeSet = new TreeSet<>();
        treeSet.addAll(Arrays.asList("a", "b", "c"));
        boolean treeSetThrows = testFailFastBehavior(treeSet);

        result.put("hashset_fail_fast", hashSetThrows);
        result.put("linkedhashset_fail_fast", linkedHashSetThrows);
        result.put("treeset_fail_fast", treeSetThrows);

        return result;
    }

    /**
     * 测试fail-fast行为的辅助方法
     */
    private boolean testFailFastBehavior(Set<String> set) {
        Iterator<String> iterator = set.iterator();
        try {
            iterator.next();
            set.add("new_element");            // 修改集合
            iterator.next();                   // 尝试继续迭代
            return false;                      // 如果没抛异常，则不是fail-fast
        } catch (ConcurrentModificationException e) {
            return true;                       // 抛出异常，则是fail-fast
        }
    }
}