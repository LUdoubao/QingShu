package org.doubao.interview.agent.server.collection;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Set比较API控制器
 * 
 * 提供API端点来演示HashSet、LinkedHashSet、TreeSet的区别
 */
@RestController
@RequestMapping("/collection/set-comparison")
public class SetComparisonController {

    /**
     * 比较三种Set的有序性
     */
    @PostMapping("/ordering")
    public Map<String, Object> compareOrdering(@RequestBody List<String> elements) {
        Map<String, Object> result = new HashMap<>();

        // HashSet - 无序
        Set<String> hashSet = new HashSet<>(elements);
        result.put("hashset_order", new ArrayList<>(hashSet));

        // LinkedHashSet - 插入有序
        Set<String> linkedHashSet = new LinkedHashSet<>(elements);
        result.put("linkedhashset_order", new ArrayList<>(linkedHashSet));

        // TreeSet - 排序有序
        Set<String> treeSet = new TreeSet<>(elements);
        result.put("treeset_order", new ArrayList<>(treeSet));

        result.put("input_elements", elements);
        result.put("explanation", "HashSet是无序的，LinkedHashSet保持插入顺序，TreeSet按自然排序");

        return result;
    }

    /**
     * 比较三种Set的去重机制
     */
    @PostMapping("/deduplication")
    public Map<String, Object> compareDeduplication(@RequestBody List<String> elements) {
        Map<String, Object> result = new HashMap<>();

        // 使用HashSet去重
        Set<String> hashSet = new HashSet<>(elements);
        result.put("hashset_deduplicated", new ArrayList<>(hashSet));
        result.put("hashset_original_size", elements.size());
        result.put("hashset_after_deduplication_size", hashSet.size());

        // 使用LinkedHashSet去重
        Set<String> linkedHashSet = new LinkedHashSet<>(elements);
        result.put("linkedhashset_deduplicated", new ArrayList<>(linkedHashSet));
        result.put("linkedhashset_after_deduplication_size", linkedHashSet.size());

        // 使用TreeSet去重
        Set<String> treeSet = new TreeSet<>(elements);
        result.put("treeset_deduplicated", new ArrayList<>(treeSet));
        result.put("treeset_after_deduplication_size", treeSet.size());

        result.put("input_elements", elements);
        result.put("deduplication_comparison", "三种Set都会去重，但TreeSet还会排序");

        return result;
    }

    /**
     * 演示TreeSet的范围查询功能
     */
    @PostMapping("/treeset-range-operations")
    public Map<String, Object> treeSetRangeOperations(@RequestBody List<Integer> numbers) {
        Map<String, Object> result = new HashMap<>();

        TreeSet<Integer> treeSet = new TreeSet<>(numbers);
        result.put("original_treeset", new ArrayList<>(treeSet));

        if (!treeSet.isEmpty()) {
            result.put("first", treeSet.first());
            result.put("last", treeSet.last());
            result.put("ceiling_of_middle", treeSet.ceiling(numbers.get(numbers.size() / 2)));
            result.put("floor_of_middle", treeSet.floor(numbers.get(numbers.size() / 2)));

            // 获取中间范围
            int min = treeSet.first();
            int max = treeSet.last();
            int mid = (min + max) / 2;
            result.put("subset_from_mid_to_max", treeSet.subSet(mid, max + 1));
            result.put("headset_from_mid", treeSet.headSet(mid));
            result.put("tailset_from_mid", treeSet.tailSet(mid));
        }

        result.put("input_numbers", numbers);
        result.put("range_operations_explanation", "TreeSet支持范围查询操作，这是HashSet和LinkedHashSet不具备的功能");

        return result;
    }

    /**
     * 性能比较
     */
    @GetMapping("/performance-comparison")
    public Map<String, Object> performanceComparison(@RequestParam(defaultValue = "10000") int count) {
        Map<String, Object> result = new HashMap<>();

        Random random = new Random();
        List<Integer> randomNumbers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            randomNumbers.add(random.nextInt(count * 2)); // 添加一些重复元素
        }

        // 测试HashSet性能
        Set<Integer> hashSet = new HashSet<>();
        long start = System.currentTimeMillis();
        for (Integer num : randomNumbers) {
            hashSet.add(num);
        }
        long hashSetTime = System.currentTimeMillis() - start;
        result.put("hashset_time_ms", hashSetTime);
        result.put("hashset_size", hashSet.size());

        // 测试LinkedHashSet性能
        Set<Integer> linkedHashSet = new LinkedHashSet<>();
        start = System.currentTimeMillis();
        for (Integer num : randomNumbers) {
            linkedHashSet.add(num);
        }
        long linkedHashSetTime = System.currentTimeMillis() - start;
        result.put("linkedhashset_time_ms", linkedHashSetTime);
        result.put("linkedhashset_size", linkedHashSet.size());

        // 测试TreeSet性能
        Set<Integer> treeSet = new TreeSet<>();
        start = System.currentTimeMillis();
        for (Integer num : randomNumbers) {
            treeSet.add(num);
        }
        long treeSetTime = System.currentTimeMillis() - start;
        result.put("treeset_time_ms", treeSetTime);
        result.put("treeset_size", treeSet.size());

        result.put("element_count", count);
        result.put("performance_note", "HashSet通常最快，LinkedHashSet次之，TreeSet最慢但支持排序和范围操作");

        return result;
    }

    /**
     * 比较null值处理
     */
    @GetMapping("/null-handling")
    public Map<String, Object> compareNullHandling() {
        Map<String, Object> result = new HashMap<>();

        // HashSet允许null
        Set<String> hashSet = new HashSet<>();
        boolean hashSetNullAdded = hashSet.add(null);
        result.put("hashset_accepts_null", hashSetNullAdded);
        result.put("hashset_with_null", hashSet);

        // LinkedHashSet允许null
        Set<String> linkedHashSet = new LinkedHashSet<>();
        boolean linkedHashSetNullAdded = linkedHashSet.add(null);
        result.put("linkedhashset_accepts_null", linkedHashSetNullAdded);
        result.put("linkedhashset_with_null", linkedHashSet);

        // TreeSet不允许null
        Set<String> treeSet = new TreeSet<>();
        boolean treeSetNullAdded = false;
        String treeSetError = null;
        try {
            treeSetNullAdded = treeSet.add(null);
        } catch (Exception e) {
            treeSetError = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        result.put("treeset_accepts_null", treeSetNullAdded);
        result.put("treeset_error", treeSetError);

        result.put("null_handling_note", "HashSet和LinkedHashSet允许null值，TreeSet不允许");

        return result;
    }
}