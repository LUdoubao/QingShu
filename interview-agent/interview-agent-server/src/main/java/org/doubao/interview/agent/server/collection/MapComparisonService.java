package org.doubao.interview.agent.server.collection;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Map比较服务类
 * 
 * 提供高级Map比较功能，包括自定义对象处理、线程安全Map对比等
 */
@Service
public class MapComparisonService {

    /**
     * 比较自定义对象在不同Map中的行为
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

        // HashMap - 使用hashCode和equals判断重复键
        Map<Product, String> productHashMap = new HashMap<>();
        productHashMap.put(p1, "High-end phone 1");
        productHashMap.put(p2, "High-end phone 2");
        productHashMap.put(p3, "High-end phone 3"); // 应该替换p1的值
        productHashMap.put(p4, "Mid-range phone");
        result.put("product_hashmap", productHashMap.toString());
        result.put("product_hashmap_size", productHashMap.size());

        // TreeMap - 使用compareTo判断重复键和排序
        Map<Product, String> productTreeMap = new TreeMap<>();
        productTreeMap.put(p1, "High-end phone 1");
        productTreeMap.put(p2, "High-end phone 2");
        productTreeMap.put(p3, "High-end phone 3"); // 应该替换p1的值
        productTreeMap.put(p4, "Mid-range phone");
        result.put("product_treemap", productTreeMap.toString());
        result.put("product_treemap_size", productTreeMap.size());

        result.put("custom_object_note", 
                "HashMap使用hashCode/equals判断重复键，TreeMap使用compareTo判断重复键");

        return result;
    }

    /**
     * 演示线程安全的Map实现
     */
    public Map<String, Object> demonstrateThreadSafeMaps() {
        Map<String, Object> result = new HashMap<>();

        // 非线程安全Map
        Map<String, String> hashMap = new HashMap<>();
        Map<String, String> treeMap = new TreeMap<>();
        Map<String, String> linkedHashMap = new LinkedHashMap<>();

        // 线程安全Map
        Map<String, String> synchronizedHashMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> concurrentSkipListMap = new ConcurrentSkipListMap<>();

        result.put("non_thread_safe_maps", Arrays.asList("HashMap", "TreeMap", "LinkedHashMap"));
        result.put("thread_safe_maps", Arrays.asList(
            "Collections.synchronizedMap(new HashMap<>())",
            "ConcurrentSkipListMap"
        ));

        result.put("thread_safety_note", 
                "HashMap、TreeMap、LinkedHashMap都不是线程安全的，需要额外处理");

        return result;
    }

    /**
     * 分析三种Map的适用场景
     */
    public Map<String, String> analyzeUseCases() {
        Map<String, String> useCases = new HashMap<>();

        useCases.put("HashMap_use_case", 
                "适用于需要快速查找、插入、删除的场景，不要求键值对有序，如缓存、临时存储等");
        useCases.put("LinkedHashMap_use_case", 
                "适用于需要键值对去重但又需要保持键的插入顺序或访问顺序的场景，如LRU缓存实现");
        useCases.put("TreeMap_use_case", 
                "适用于需要对键进行排序，或者需要范围查询的场景，如按时间排序的日志、范围统计等");
        useCases.put("performance_note", 
                "HashMap性能最高O(1)，LinkedHashMap次之O(1)，TreeMap最慢O(logn)但功能最丰富");

        return useCases;
    }

    /**
     * 比较三种Map的空间复杂度
     */
    public Map<String, String> compareSpaceComplexity() {
        Map<String, String> spaceComplexity = new HashMap<>();

        spaceComplexity.put("HashMap_space", 
                "O(n)，其中n是键值对数量，需要额外的哈希桶空间，负载因子影响实际占用空间");
        spaceComplexity.put("LinkedHashMap_space", 
                "O(n)，除了哈希表空间外，还需要维护双向链表指针，比HashMap稍大");
        spaceComplexity.put("TreeMap_space", 
                "O(n)，每个节点需要额外的空间维护红黑树结构，通常比HashMap占用更多空间");

        return spaceComplexity;
    }

    /**
     * 展示TreeMap的自定义排序功能
     */
    public Map<String, Object> demonstrateCustomSorting() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> inputMap = new HashMap<>();
        inputMap.put("banana", 3);
        inputMap.put("apple", 1);
        inputMap.put("cherry", 2);
        inputMap.put("date", 4);
        inputMap.put("elderberry", 5);

        // 自然排序（字母顺序）
        Map<String, Integer> naturalOrder = new TreeMap<>(inputMap);
        result.put("natural_order", new ArrayList<>(naturalOrder.entrySet()));

        // 逆序排序
        Map<String, Integer> reverseOrder = new TreeMap<>(Collections.reverseOrder());
        reverseOrder.putAll(inputMap);
        result.put("reverse_order", new ArrayList<>(reverseOrder.entrySet()));

        // 按值排序（需要特殊处理）
        List<Map.Entry<String, Integer>> sortedByValueList = new ArrayList<>(inputMap.entrySet());
        sortedByValueList.sort(Map.Entry.comparingByValue());
        Map<String, Integer> sortByValue = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sortedByValueList) {
            sortByValue.put(entry.getKey(), entry.getValue());
        }
        result.put("sort_by_value", new ArrayList<>(sortByValue.entrySet()));

        result.put("custom_sorting_note", 
                "TreeMap支持多种排序方式，包括自然排序和自定义排序，但仅针对键");

        return result;
    }

    /**
     * 比较fail-fast行为
     */
    public Map<String, Boolean> compareFailFastBehavior() {
        Map<String, Boolean> result = new HashMap<>();

        // HashMap fail-fast
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("a", "1");
        hashMap.put("b", "2");
        hashMap.put("c", "3");
        boolean hashMapThrows = testFailFastBehavior(hashMap);

        // LinkedHashMap fail-fast
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("a", "1");
        linkedHashMap.put("b", "2");
        linkedHashMap.put("c", "3");
        boolean linkedHashMapThrows = testFailFastBehavior(linkedHashMap);

        // TreeMap fail-fast
        Map<String, String> treeMap = new TreeMap<>();
        treeMap.put("a", "1");
        treeMap.put("b", "2");
        treeMap.put("c", "3");
        boolean treeMapThrows = testFailFastBehavior(treeMap);

        result.put("hashmap_fail_fast", hashMapThrows);
        result.put("linkedhashmap_fail_fast", linkedHashMapThrows);
        result.put("treemap_fail_fast", treeMapThrows);

        return result;
    }

    /**
     * 测试fail-fast行为的辅助方法
     */
    private boolean testFailFastBehavior(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        try {
            iterator.next();
            map.put("new_key", "new_value");    // 修改Map
            iterator.next();                     // 尝试继续迭代
            return false;                        // 如果没抛异常，则不是fail-fast
        } catch (ConcurrentModificationException e) {
            return true;                         // 抛出异常，则是fail-fast
        }
    }
}