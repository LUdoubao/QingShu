package org.doubao.interview.agent.server.collection;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * HashSet和HashMap关系演示API控制器
 * 
 * 这个控制器演示HashSet基于HashMap实现的原理
 * 通过API调用展示HashSet和HashMap的行为一致性
 */
@RestController
@RequestMapping("/collection/relationship")
public class HashSetHashMapController {

    /**
     * 演示HashSet和HashMap的重复元素处理机制
     */
    @PostMapping("/duplicate-handling")
    public Map<String, Object> demonstrateDuplicateHandling(@RequestBody String[] elements) {
        Map<String, Object> result = new HashMap<>();
        
        HashSet<String> hashSet = new HashSet<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
        
        // 使用HashSet处理元素
        for (String element : elements) {
            boolean setAdded = hashSet.add(element);
            // 对应HashMap操作
            Integer mapPreviousValue = hashMap.put(element, 1);
            
            result.put("element_" + element + "_hashset_added", setAdded);
            result.put("element_" + element + "_hashmap_previous", mapPreviousValue);
        }
        
        result.put("hashset_size", hashSet.size());
        result.put("hashmap_size", hashMap.size());
        
        return result;
    }

    /**
     * 演示HashSet和HashMap对null的处理
     */
    @GetMapping("/null-handling")
    public Map<String, Object> demonstrateNullHandling() {
        Map<String, Object> result = new HashMap<>();
        
        HashSet<String> hashSet = new HashSet<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
        
        // HashSet添加null
        boolean setNullAdded = hashSet.add(null);
        result.put("hashset_null_first_add", setNullAdded);
        
        // HashMap添加null键
        Integer mapNullPrevious = hashMap.put(null, 1);
        result.put("hashmap_null_first_put", mapNullPrevious);
        
        // 再次尝试添加null
        boolean setNullAddedAgain = hashSet.add(null);
        result.put("hashset_null_second_add", setNullAddedAgain);
        
        Integer mapNullPreviousAgain = hashMap.put(null, 2);
        result.put("hashmap_null_second_put", mapNullPreviousAgain);
        
        result.put("final_hashset_size", hashSet.size());
        result.put("final_hashmap_size", hashMap.size());
        
        return result;
    }

    /**
     * 演示HashSet的底层实现原理
     */
    @GetMapping("/implementation-principle")
    public Map<String, Object> explainImplementationPrinciple() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("principle", "HashSet基于HashMap实现，元素作为HashMap的key，固定值作为HashMap的value");
        result.put("benefit", "利用HashMap的key唯一性实现Set的元素唯一性");
        result.put("memory_optimization", "HashSet使用固定的PRESENT对象作为所有key的value，节省内存");
        
        // 用代码模拟HashSet的add方法
        HashSet<String> demoSet = new HashSet<>();
        demoSet.add("test");
        demoSet.add("test"); // 重复添加
        
        result.put("demo_set_size", demoSet.size()); // 应该是1，不是2
        result.put("demo_duplicate_prevented", true);
        
        return result;
    }

    /**
     * 性能对比测试
     */
    @GetMapping("/performance-comparison")
    public Map<String, Object> performanceComparison() {
        Map<String, Object> result = new HashMap<>();
        
        int iterations = 100000;
        
        // 测试HashSet性能
        HashSet<Integer> hashSet = new HashSet<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            hashSet.add(i);
        }
        long hashSetTime = System.currentTimeMillis() - startTime;
        
        // 测试HashMap性能（模拟HashSet的底层操作）
        HashMap<Integer, Object> hashMap = new HashMap<>();
        Object PRESENT = new Object();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            hashMap.put(i, PRESENT);
        }
        long hashMapTime = System.currentTimeMillis() - startTime;
        
        result.put("hashset_time_ms", hashSetTime);
        result.put("hashmap_time_ms", hashMapTime);
        result.put("hashset_size", hashSet.size());
        result.put("hashmap_size", hashMap.size());
        result.put("note", "HashSet的性能接近HashMap，因为它是基于HashMap实现的");
        
        return result;
    }
}