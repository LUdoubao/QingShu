package org.doubao.interview.agent.server.collection;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Map比较API控制器
 * 
 * 提供API端点来演示HashMap、LinkedHashMap、TreeMap的区别
 */
@RestController
@RequestMapping("/collection/map-comparison")
public class MapComparisonController {

    /**
     * 比较三种Map的有序性
     */
    @PostMapping("/ordering")
    public Map<String, Object> compareOrdering(@RequestBody Map<String, String> inputMap) {
        Map<String, Object> result = new HashMap<>();

        // HashMap - 无序
        Map<String, String> hashMap = new HashMap<>(inputMap);
        result.put("hashmap_order", new ArrayList<>(hashMap.entrySet()));

        // LinkedHashMap - 插入有序
        Map<String, String> linkedHashMap = new LinkedHashMap<>(inputMap);
        result.put("linkedhashmap_order", new ArrayList<>(linkedHashMap.entrySet()));

        // TreeMap - 排序有序
        Map<String, String> treeMap = new TreeMap<>(inputMap);
        result.put("treemap_order", new ArrayList<>(treeMap.entrySet()));

        result.put("input_data", inputMap);
        result.put("explanation", "HashMap是无序的，LinkedHashMap保持插入顺序，TreeMap按键的自然排序");

        return result;
    }

    /**
     * 比较三种Map的键唯一性处理
     */
    @PostMapping("/key-uniqueness")
    public Map<String, Object> compareKeyUniqueness(@RequestBody List<Map.Entry<String, String>> entries) {
        Map<String, Object> result = new HashMap<>();

        // 使用HashMap处理重复键
        Map<String, String> hashMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String oldValue = hashMap.put(entry.getKey(), entry.getValue());
            if (oldValue != null) {
                result.put("hashmap_replaced_value_for_key_" + entry.getKey(), oldValue);
            }
        }
        result.put("hashmap_final", new ArrayList<>(hashMap.entrySet()));
        result.put("hashmap_size", hashMap.size());

        // 使用LinkedHashMap处理重复键
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String oldValue = linkedHashMap.put(entry.getKey(), entry.getValue());
            if (oldValue != null) {
                result.put("linkedhashmap_replaced_value_for_key_" + entry.getKey(), oldValue);
            }
        }
        result.put("linkedhashmap_final", new ArrayList<>(linkedHashMap.entrySet()));
        result.put("linkedhashmap_size", linkedHashMap.size());

        // 使用TreeMap处理重复键
        Map<String, String> treeMap = new TreeMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String oldValue = treeMap.put(entry.getKey(), entry.getValue());
            if (oldValue != null) {
                result.put("treemap_replaced_value_for_key_" + entry.getKey(), oldValue);
            }
        }
        result.put("treemap_final", new ArrayList<>(treeMap.entrySet()));
        result.put("treemap_size", treeMap.size());

        result.put("input_entries", entries);
        result.put("key_uniqueness_comparison", "三种Map都会处理重复键，用新值替换旧值");

        return result;
    }

    /**
     * 演示TreeMap的范围查询功能
     */
    @PostMapping("/treemap-range-operations")
    public Map<String, Object> treeMapRangeOperations(@RequestBody Map<Integer, String> inputMap) {
        Map<String, Object> result = new HashMap<>();

        TreeMap<Integer, String> treeMap = new TreeMap<>(inputMap);
        result.put("original_treemap", new ArrayList<>(treeMap.entrySet()));

        if (!treeMap.isEmpty()) {
            Integer firstKey = treeMap.firstKey();
            Integer lastKey = treeMap.lastKey();
            result.put("first_key", firstKey);
            result.put("last_key", lastKey);
            
            Integer middleKey = inputMap.keySet().stream()
                    .sorted()
                    .skip(inputMap.size() / 2)
                    .findFirst()
                    .orElse(firstKey);
            
            result.put("ceiling_of_middle", treeMap.ceilingKey(middleKey));
            result.put("floor_of_middle", treeMap.floorKey(middleKey));

            // 获取中间范围
            Integer min = treeMap.firstKey();
            Integer max = treeMap.lastKey();
            Integer mid = min + (max - min) / 2;
            result.put("submap_from_mid_to_max", treeMap.subMap(mid, max + 1));
            result.put("headmap_from_mid", treeMap.headMap(mid));
            result.put("tailmap_from_mid", treeMap.tailMap(mid));
        }

        result.put("input_map", inputMap);
        result.put("range_operations_explanation", "TreeMap支持范围查询操作，这是HashMap和LinkedHashMap不具备的功能");

        return result;
    }

    /**
     * 性能比较
     */
    @GetMapping("/performance-comparison")
    public Map<String, Object> performanceComparison(@RequestParam(defaultValue = "10000") int count) {
        Map<String, Object> result = new HashMap<>();

        Random random = new Random();
        List<Integer> randomKeys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            randomKeys.add(random.nextInt(count * 2)); // 添加一些重复键
        }

        // 测试HashMap性能
        Map<Integer, String> hashMap = new HashMap<>();
        long start = System.currentTimeMillis();
        for (Integer key : randomKeys) {
            hashMap.put(key, "value_" + key);
        }
        long hashMapTime = System.currentTimeMillis() - start;
        result.put("hashmap_time_ms", hashMapTime);
        result.put("hashmap_size", hashMap.size());

        // 测试LinkedHashMap性能
        Map<Integer, String> linkedHashMap = new LinkedHashMap<>();
        start = System.currentTimeMillis();
        for (Integer key : randomKeys) {
            linkedHashMap.put(key, "value_" + key);
        }
        long linkedHashMapTime = System.currentTimeMillis() - start;
        result.put("linkedhashmap_time_ms", linkedHashMapTime);
        result.put("linkedhashmap_size", linkedHashMap.size());

        // 测试TreeMap性能
        Map<Integer, String> treeMap = new TreeMap<>();
        start = System.currentTimeMillis();
        for (Integer key : randomKeys) {
            treeMap.put(key, "value_" + key);
        }
        long treeMapTime = System.currentTimeMillis() - start;
        result.put("treemap_time_ms", treeMapTime);
        result.put("treemap_size", treeMap.size());

        result.put("element_count", count);
        result.put("performance_note", "HashMap通常最快，LinkedHashMap次之，TreeMap最慢但支持排序和范围操作");

        return result;
    }

    /**
     * 比较null值处理
     */
    @GetMapping("/null-handling")
    public Map<String, Object> compareNullHandling() {
        Map<String, Object> result = new HashMap<>();

        // HashMap允许null键和null值
        Map<String, String> hashMap = new HashMap<>();
        String oldNullValue = hashMap.put(null, "value_for_null_key");
        result.put("hashmap_old_value_for_null_key", oldNullValue);
        String oldNullKeyValue = hashMap.put("key_for_null_value", null);
        result.put("hashmap_old_value_before_putting_null", oldNullKeyValue);
        result.put("hashmap_with_null", new ArrayList<>(hashMap.entrySet()));

        // LinkedHashMap允许null键和null值
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        String oldNullValueLinked = linkedHashMap.put(null, "value_for_null_key");
        result.put("linkedhashmap_old_value_for_null_key", oldNullValueLinked);
        String oldNullKeyValueLinked = linkedHashMap.put("key_for_null_value", null);
        result.put("linkedhashmap_old_value_before_putting_null", oldNullKeyValueLinked);
        result.put("linkedhashmap_with_null", new ArrayList<>(linkedHashMap.entrySet()));

        // TreeMap不允许null键
        Map<String, String> treeMap = new TreeMap<>();
        String treeMapError = null;
        try {
            treeMap.put(null, "value_for_null_key");
        } catch (Exception e) {
            treeMapError = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        result.put("treemap_error_for_null_key", treeMapError);
        
        String oldNullKeyValueTree = treeMap.put("key_for_null_value", null);
        result.put("treemap_old_value_before_putting_null", oldNullKeyValueTree);
        result.put("treemap_with_null_value_only", new ArrayList<>(treeMap.entrySet()));

        result.put("null_handling_note", "HashMap和LinkedHashMap允许null键和null值，TreeMap不允许null键");

        return result;
    }
    
    /**
     * 演示LinkedHashMap的访问顺序功能
     */
    @GetMapping("/linkedhashmap-access-order")
    public Map<String, Object> demonstrateAccessOrder() {
        Map<String, Object> result = new HashMap<>();

        // 插入顺序模式（默认）
        LinkedHashMap<String, Integer> insertionOrderMap = new LinkedHashMap<>();
        insertionOrderMap.put("first", 1);
        insertionOrderMap.put("second", 2);
        insertionOrderMap.put("third", 3);
        result.put("insertion_order_initial", new ArrayList<>(insertionOrderMap.entrySet()));

        // 访问某个元素
        insertionOrderMap.get("first");
        result.put("insertion_order_after_access", new ArrayList<>(insertionOrderMap.entrySet()));

        // 访问顺序模式
        LinkedHashMap<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true); // true表示访问顺序
        accessOrderMap.put("first", 1);
        accessOrderMap.put("second", 2);
        accessOrderMap.put("third", 3);
        result.put("access_order_initial", new ArrayList<>(accessOrderMap.entrySet()));

        // 访问某个元素
        accessOrderMap.get("first");
        result.put("access_order_after_access_first", new ArrayList<>(accessOrderMap.entrySet()));

        // 再次访问另一个元素
        accessOrderMap.get("second");
        result.put("access_order_after_access_second", new ArrayList<>(accessOrderMap.entrySet()));

        result.put("access_order_explanation", 
                  "在访问顺序模式下，访问过的元素会被移到链表末尾，可用于实现LRU缓存");

        return result;
    }
}