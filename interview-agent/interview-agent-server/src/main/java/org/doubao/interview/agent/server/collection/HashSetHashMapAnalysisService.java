package org.doubao.interview.agent.server.collection;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HashSet和HashMap关系分析服务
 * 
 * 提供关于HashSet和HashMap关系的深入分析功能
 * 包括性能分析、内存占用比较、行为模式验证等
 */
@Service
public class HashSetHashMapAnalysisService {

    /**
     * 分析HashSet和HashMap的内存占用差异
     */
    public Map<String, Long> analyzeMemoryUsage(int elementCount) {
        Map<String, Long> result = new HashMap<>();
        
        // 创建HashSet并添加元素
        HashSet<String> hashSet = new HashSet<>();
        for (int i = 0; i < elementCount; i++) {
            hashSet.add("Element" + i);
        }
        
        // 创建HashMap并添加相同数量的键值对（模拟HashSet的内部实现）
        HashMap<String, Object> hashMap = new HashMap<>();
        Object PRESENT = new Object(); // HashSet内部使用的常量
        for (int i = 0; i < elementCount; i++) {
            hashMap.put("Element" + i, PRESENT);
        }
        
        // 这里我们无法精确测量内存使用，但可以记录元素数量
        result.put("hashset_element_count", (long) hashSet.size());
        result.put("hashmap_key_count", (long) hashMap.size());
        result.put("element_count_requested", (long) elementCount);
        
        return result;
    }

    /**
     * 验证HashSet和HashMap的迭代器行为
     */
    public Map<String, Object> compareIteratorBehavior() {
        Map<String, Object> result = new HashMap<>();
        
        // 创建测试数据
        List<String> testData = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
        
        // HashSet迭代
        HashSet<String> hashSet = new HashSet<>(testData);
        List<String> setIterationOrder = new ArrayList<>();
        for (String s : hashSet) {
            setIterationOrder.add(s);
        }
        
        // HashMap迭代（获取keySet）
        HashMap<String, Object> hashMap = new HashMap<>();
        for (String item : testData) {
            hashMap.put(item, 1);
        }
        List<String> mapKeySetIterationOrder = new ArrayList<>();
        for (String s : hashMap.keySet()) {
            mapKeySetIterationOrder.add(s);
        }
        
        result.put("hashset_iteration_order", setIterationOrder);
        result.put("hashmap_keyset_iteration_order", mapKeySetIterationOrder);
        result.put("orders_are_same", setIterationOrder.equals(mapKeySetIterationOrder));
        result.put("note", "HashSet和HashMap的keySet迭代顺序应该相同");
        
        return result;
    }

    /**
     * 比较HashSet和HashMap的fail-fast行为
     */
    public Map<String, Object> compareFailFastBehavior() {
        Map<String, Object> result = new HashMap<>();
        
        // 测试HashSet的fail-fast行为
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("item1");
        hashSet.add("item2");
        hashSet.add("item3");
        
        Iterator<String> setIterator = hashSet.iterator();
        boolean setThrowException = false;
        try {
            setIterator.next(); // 移动到第一个元素
            hashSet.add("item4"); // 修改集合
            setIterator.next(); // 尝试继续迭代
        } catch (ConcurrentModificationException e) {
            setThrowException = true;
        }
        
        // 测试HashMap的fail-fast行为
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("key1", 1);
        hashMap.put("key2", 2);
        hashMap.put("key3", 3);
        
        Iterator<String> mapIterator = hashMap.keySet().iterator();
        boolean mapThrowException = false;
        try {
            mapIterator.next(); // 移动到第一个元素
            hashMap.put("key4", 4); // 修改集合
            mapIterator.next(); // 尝试继续迭代
        } catch (ConcurrentModificationException e) {
            mapThrowException = true;
        }
        
        result.put("hashset_fail_fast", setThrowException);
        result.put("hashmap_fail_fast", mapThrowException);
        result.put("both_have_fail_fast_behavior", setThrowException && mapThrowException);
        
        return result;
    }

    /**
     * 演示HashSet基于HashMap的设计优势
     */
    public Map<String, String> explainDesignAdvantages() {
        Map<String, String> advantages = new HashMap<>();
        
        advantages.put("reuse", "复用HashMap的高效实现，无需重新开发哈希算法和冲突解决");
        advantages.put("consistency", "与HashMap保持行为一致性，降低学习成本");
        advantages.put("simplicity", "提供简洁的Set接口，隐藏键值对的复杂性");
        advantages.put("memory_efficiency", "使用固定对象作为value，减少内存占用");
        advantages.put("maintainability", "只需要维护HashMap的逻辑，HashSet自动获得改进");
        
        return advantages;
    }

    /**
     * 展示HashSet和HashMap在处理自定义对象时的行为
     */
    public Map<String, Object> demonstrateCustomObjectHandling() {
        Map<String, Object> result = new HashMap<>();
        
        // 自定义类演示equals和hashCode的重要性
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
        
        // 测试HashSet
        HashSet<Person> personSet = new HashSet<>();
        Person p1 = new Person("Alice", 25);
        Person p2 = new Person("Alice", 25); // 相同内容的不同对象
        
        boolean firstAdded = personSet.add(p1);
        boolean secondAdded = personSet.add(p2); // 应该返回false，因为内容相同
        
        // 测试HashMap
        HashMap<Person, String> personMap = new HashMap<>();
        boolean mapFirstAdded = personMap.put(p1, "first") == null;
        String mapSecondResult = personMap.put(p2, "second"); // 应该返回"first"
        
        result.put("person_set_first_added", firstAdded);
        result.put("person_set_second_added", secondAdded);
        result.put("person_map_first_added", mapFirstAdded);
        result.put("person_map_second_result", mapSecondResult);
        result.put("set_final_size", personSet.size());
        result.put("map_final_size", personMap.size());
        result.put("explanation", "HashSet和HashMap都使用equals和hashCode判断重复");
        
        return result;
    }
}