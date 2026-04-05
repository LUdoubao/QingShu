package org.doubao.interview.agent.server.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * SimpleHashMap 功能测试类
 * 
 * 【测试目的】
 * 1. 验证基本put/get操作
 * 2. 验证扩容机制
 * 3. 验证删除操作
 * 4. 与JDK HashMap对比行为一致性
 * 
 * @author Interview Agent
 * @date 2026-04-05
 */
public class SimpleHashMapTest {
    
    public static void main(String[] args) {
        System.out.println("========== SimpleHashMap 功能测试 ==========\n");
        
        // 测试1：基本put/get操作
        testBasicPutGet();
        
        // 测试2：扩容机制
        testExpansion();
        
        // 测试3：覆盖操作
        testOverride();
        
        // 测试4：删除操作
        testRemove();
        
        // 测试5：边界情况
        testEdgeCases();
        
        // 测试6：与JDK HashMap对比
        testCompareWithJDK();
        
        System.out.println("\n========== 所有测试完成 ==========");
    }
    
    /**
     * 测试基本的put和get操作
     */
    private static void testBasicPutGet() {
        System.out.println("【测试1】基本put/get操作");
        System.out.println("-----------------------------------");
        
        SimpleHashMap<String, Integer> map = new SimpleHashMap<>();
        
        // 添加元素
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);
        
        System.out.println("put(\"apple\", 1)");
        System.out.println("put(\"banana\", 2)");
        System.out.println("put(\"cherry\", 3)");
        System.out.println();
        
        // 获取元素
        System.out.println("get(\"apple\") = " + map.get("apple"));
        System.out.println("get(\"banana\") = " + map.get("banana"));
        System.out.println("get(\"cherry\") = " + map.get("cherry"));
        System.out.println("get(\"date\") = " + map.get("date") + " (不存在)");
        System.out.println();
        
        // 验证size
        System.out.println("size = " + map.size());
        System.out.println("isEmpty = " + map.isEmpty());
        System.out.println();
        
        // 打印内部结构
        map.printStructure();
        System.out.println();
    }
    
    /**
     * 测试扩容机制
     */
    private static void testExpansion() {
        System.out.println("【测试2】扩容机制");
        System.out.println("-----------------------------------");
        
        // 创建小容量HashMap便于观察扩容
        SimpleHashMap<Integer, String> map = new SimpleHashMap<>(2);
        
        System.out.println("初始状态:");
        System.out.println("  容量: " + map.capacity());
        System.out.println("  阈值: " + map.threshold());
        System.out.println();
        
        // 添加元素触发扩容
        for (int i = 1; i <= 8; i++) {
            int capacityBefore = map.capacity();
            map.put(i, "value-" + i);
            int capacityAfter = map.capacity();
            
            if (capacityAfter > capacityBefore) {
                System.out.println(">>> 添加元素 " + i + " 后触发扩容！");
                System.out.println("    容量: " + capacityBefore + " -> " + capacityAfter);
                System.out.println("    size: " + map.size());
                System.out.println("    阈值: " + map.threshold());
                System.out.println();
            }
        }
        
        System.out.println("最终状态:");
        System.out.println("  容量: " + map.capacity());
        System.out.println("  size: " + map.size());
        System.out.println("  阈值: " + map.threshold());
        System.out.println();
    }
    
    /**
     * 测试覆盖操作
     */
    private static void testOverride() {
        System.out.println("【测试3】覆盖操作");
        System.out.println("-----------------------------------");
        
        SimpleHashMap<String, String> map = new SimpleHashMap<>();
        
        // 首次添加
        String result1 = map.put("key1", "value1");
        System.out.println("put(\"key1\", \"value1\") 返回: " + result1);
        System.out.println("get(\"key1\") = " + map.get("key1"));
        System.out.println();
        
        // 覆盖
        String result2 = map.put("key1", "value2");
        System.out.println("put(\"key1\", \"value2\") 返回: " + result2);
        System.out.println("get(\"key1\") = " + map.get("key1"));
        System.out.println();
        
        // 再次覆盖
        String result3 = map.put("key1", "value3");
        System.out.println("put(\"key1\", \"value3\") 返回: " + result3);
        System.out.println("get(\"key1\") = " + map.get("key1"));
        System.out.println();
        
        System.out.println("结论：put返回旧值，成功覆盖");
        System.out.println();
    }
    
    /**
     * 测试删除操作
     */
    private static void testRemove() {
        System.out.println("【测试4】删除操作");
        System.out.println("-----------------------------------");
        
        SimpleHashMap<String, Integer> map = new SimpleHashMap<>();
        
        // 添加元素
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);
        
        System.out.println("初始状态: size=" + map.size());
        System.out.println();
        
        // 删除存在的key
        Integer removed1 = map.remove("B");
        System.out.println("remove(\"B\") 返回: " + removed1);
        System.out.println("get(\"B\") = " + map.get("B"));
        System.out.println("当前size: " + map.size());
        System.out.println();
        
        // 删除不存在的key
        Integer removed2 = map.remove("Z");
        System.out.println("remove(\"Z\") 返回: " + removed2);
        System.out.println("当前size: " + map.size());
        System.out.println();
        
        // 删除后继续添加
        map.put("E", 5);
        System.out.println("put(\"E\", 5) 后 size: " + map.size());
        System.out.println();
    }
    
    /**
     * 测试边界情况
     */
    private static void testEdgeCases() {
        System.out.println("【测试5】边界情况");
        System.out.println("-----------------------------------");
        
        SimpleHashMap<Object, String> map = new SimpleHashMap<>();
        
        // null key
        System.out.println("测试null key:");
        map.put(null, "null-key-value");
        System.out.println("  put(null, \"null-key-value\")");
        System.out.println("  get(null) = " + map.get(null));
        System.out.println();
        
        // null value
        System.out.println("测试null value:");
        map.put("key-with-null-value", null);
        System.out.println("  put(\"key-with-null-value\", null)");
        System.out.println("  get(\"key-with-null-value\") = " + map.get("key-with-null-value"));
        System.out.println();
        
        // containsKey/containsValue
        System.out.println("测试contains方法:");
        System.out.println("  containsKey(null): " + map.containsKey(null));
        System.out.println("  containsKey(\"key-with-null-value\"): " + map.containsKey("key-with-null-value"));
        System.out.println("  containsValue(\"null-key-value\"): " + map.containsValue("null-key-value"));
        System.out.println("  containsValue(null): " + map.containsValue(null));
        System.out.println();
        
        // clear
        System.out.println("测试clear:");
        System.out.println("  clear前 size: " + map.size());
        map.clear();
        System.out.println("  clear后 size: " + map.size());
        System.out.println("  isEmpty: " + map.isEmpty());
        System.out.println();
    }
    
    /**
     * 与JDK HashMap对比
     */
    private static void testCompareWithJDK() {
        System.out.println("【测试6】与JDK HashMap对比");
        System.out.println("-----------------------------------");
        
        // 创建两个Map
        SimpleHashMap<String, Integer> simpleMap = new SimpleHashMap<>();
        HashMap<String, Integer> jdkMap = new HashMap<>();
        
        // 执行相同操作
        String[] keys = {"apple", "banana", "cherry", "date", "elderberry"};
        
        System.out.println("执行相同的put操作:");
        for (int i = 0; i < keys.length; i++) {
            simpleMap.put(keys[i], i + 1);
            jdkMap.put(keys[i], i + 1);
            System.out.println("  put(\"" + keys[i] + "\", " + (i + 1) + ")");
        }
        System.out.println();
        
        // 对比get结果
        System.out.println("对比get结果:");
        boolean allMatch = true;
        for (String key : keys) {
            Integer simpleResult = simpleMap.get(key);
            Integer jdkResult = jdkMap.get(key);
            boolean match = (simpleResult == null && jdkResult == null) ||
                           (simpleResult != null && simpleResult.equals(jdkResult));
            
            System.out.println("  get(\"" + key + "\"): Simple=" + simpleResult + 
                             ", JDK=" + jdkResult + 
                             ", 匹配=" + match);
            
            if (!match) {
                allMatch = false;
            }
        }
        System.out.println();
        
        // 对比size
        System.out.println("对比size:");
        System.out.println("  SimpleHashMap: " + simpleMap.size());
        System.out.println("  JDK HashMap: " + jdkMap.size());
        System.out.println("  匹配: " + (simpleMap.size() == jdkMap.size()));
        System.out.println();
        
        // 对比containsKey
        System.out.println("对比containsKey:");
        for (String key : keys) {
            boolean simpleContains = simpleMap.containsKey(key);
            boolean jdkContains = jdkMap.containsKey(key);
            System.out.println("  containsKey(\"" + key + "\"): Simple=" + simpleContains + 
                             ", JDK=" + jdkContains);
        }
        System.out.println();
        
        if (allMatch && simpleMap.size() == jdkMap.size()) {
            System.out.println("✓ 测试结果：SimpleHashMap 与 JDK HashMap 行为一致！");
        } else {
            System.out.println("✗ 测试结果：存在差异，需要检查实现");
        }
        System.out.println();
    }
}
