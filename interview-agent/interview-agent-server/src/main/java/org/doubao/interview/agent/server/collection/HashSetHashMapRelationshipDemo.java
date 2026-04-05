package org.doubao.interview.agent.server.collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * HashSet和HashMap关系演示类
 * 
 * 核心结论：HashSet完全基于HashMap实现，本质是HashMap的包装类
 * 利用HashMap的键唯一性来实现Set接口的元素不可重复特性
 */
public class HashSetHashMapRelationshipDemo {

    public static void main(String[] args) {
        demonstrateHashSetBasedOnHashMap();
        demonstrateMethodMapping();
        demonstrateCharacteristicsConsistency();
    }

    /**
     * 演示HashSet基于HashMap的实现原理
     */
    public static void demonstrateHashSetBasedOnHashMap() {
        System.out.println("=== 演示HashSet基于HashMap的实现原理 ===");

        // HashSet底层实际上是一个HashMap
        HashSet<String> hashSet = new HashSet<>();
        
        // 添加元素到HashSet
        boolean added1 = hashSet.add("Java");
        boolean added2 = hashSet.add("Java"); // 重复元素
        
        System.out.println("第一次添加'Java': " + added1); // true
        System.out.println("第二次添加'Java': " + added2); // false
        
        // 这相当于直接操作HashMap
        HashMap<String, Object> map = new HashMap<>();
        Object PRESENT = new Object(); // HashSet内部使用的固定常量
        
        Object result1 = map.put("Java", PRESENT);
        Object result2 = map.put("Java", PRESENT);
        
        System.out.println("HashMap第一次put结果: " + (result1 == null)); // true (null表示之前没有这个key)
        System.out.println("HashMap第二次put结果: " + (result2 != null)); // true (非null表示之前有这个key)
        
        System.out.println("HashSet大小: " + hashSet.size());
        System.out.println("HashMap大小: " + map.size());
        System.out.println();
    }

    /**
     * 演示HashSet核心方法与HashMap的映射关系
     */
    public static void demonstrateMethodMapping() {
        System.out.println("=== 演示HashSet方法与HashMap方法的映射关系 ===");
        
        // HashSet的操作
        HashSet<String> set = new HashSet<>();
        
        // add(E e) -> map.put(e, PRESENT)
        boolean setResult = set.add("Test");
        System.out.println("HashSet.add结果: " + setResult);
        
        // contains(Object o) -> map.containsKey(o)
        boolean containsResult = set.contains("Test");
        System.out.println("HashSet.contains结果: " + containsResult);
        
        // remove(Object o) -> map.remove(o)
        boolean removeResult = set.remove("Test");
        System.out.println("HashSet.remove结果: " + removeResult);
        
        // size() -> map.size()
        System.out.println("HashSet大小: " + set.size());
        
        // isEmpty() -> map.isEmpty()
        System.out.println("HashSet是否为空: " + set.isEmpty());
        
        System.out.println();
    }

    /**
     * 演示HashSet和HashMap特性的完全一致性
     */
    public static void demonstrateCharacteristicsConsistency() {
        System.out.println("=== 演示HashSet和HashMap特性的完全一致性 ===");
        
        HashSet<String> hashSet = new HashSet<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
        
        // 测试null值支持
        System.out.println("HashSet是否可以添加null: " + hashSet.add(null));
        System.out.println("HashMap是否可以添加null键: " + (hashMap.put(null, 1) == null));
        
        // 再次尝试添加null，应该失败
        System.out.println("HashSet再次添加null: " + hashSet.add(null));
        System.out.println("HashMap再次添加null键: " + (hashMap.put(null, 2) != null));
        
        // 测试元素/键唯一性
        System.out.println("HashSet添加重复元素'hello': " + hashSet.add("hello"));
        System.out.println("HashSet添加重复元素'hello': " + hashSet.add("hello"));
        
        System.out.println("HashMap添加重复键'world': " + (hashMap.put("world", 1) == null));
        System.out.println("HashMap添加重复键'world': " + (hashMap.put("world", 2) != null));
        
        // 测试无序性
        HashSet<Integer> orderedSet = new HashSet<>();
        orderedSet.add(1);
        orderedSet.add(2);
        orderedSet.add(3);
        orderedSet.add(4);
        System.out.println("HashSet遍历顺序（通常不是添加顺序）:");
        for(Integer i : orderedSet) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
    }
}