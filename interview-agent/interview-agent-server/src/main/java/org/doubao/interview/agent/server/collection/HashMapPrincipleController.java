package org.doubao.interview.agent.server.collection;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * HashMap JDK 1.8 底层实现原理演示控制器
 * 
 * 【面试核心知识点】
 * 1. 底层结构：数组 + 链表 + 红黑树
 * 2. 哈希算法：高16位 ^ 低16位
 * 3. 插入方式：尾插法（避免环形链表）
 * 4. 扩容机制：2倍扩容，元素迁移无需重算hash
 * 5. 树化规则：链表≥8且数组≥64转红黑树，节点≤6转回链表
 * 
 * 【API端点】
 * - GET /collection/hashmap-principle/structure : 演示底层数据结构
 * - GET /collection/hashmap-principle/hash-calculation : 演示哈希计算
 * - POST /collection/hashmap-principle/put-operation : 演示put操作
 * - GET /collection/hashmap-principle/get-operation : 演示get操作
 * - GET /collection/hashmap-principle/expansion : 演示扩容过程
 * - GET /collection/hashmap-principle/treeify : 演示树化过程
 * - GET /collection/hashmap-principle/jdk-comparison : JDK 1.7 vs 1.8 对比
 * 
 * @author Interview Agent
 * @date 2026-04-05
 */
@RestController
@RequestMapping("/collection/hashmap-principle")
public class HashMapPrincipleController {
    
    /**
     * 演示HashMap底层数据结构
     * 
     * 【返回内容】
     * 1. 默认参数说明
     * 2. 存储结构示意图
     * 3. 节点类型介绍
     * 
     * @return 数据结构说明
     */
    @GetMapping("/structure")
    public Map<String, Object> demonstrateStructure() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. 核心参数
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("默认初始容量", "16 (2的4次幂)");
        parameters.put("最大容量", "2^30");
        parameters.put("默认负载因子", "0.75");
        parameters.put("扩容阈值计算公式", "capacity × loadFactor");
        parameters.put("树化阈值", "8 (链表长度≥8)");
        parameters.put("反树化阈值", "6 (红黑树节点≤6)");
        parameters.put("最小树化容量", "64 (数组长度≥64才允许树化)");
        result.put("核心参数", parameters);
        
        // 2. 底层结构示意
        List<String> structure = new ArrayList<>();
        structure.add("哈希桶数组 (Node[] table)");
        structure.add("  ↓");
        structure.add("每个桶位置可能是：");
        structure.add("  - null: 空桶");
        structure.add("  - Node: 单个节点");
        structure.add("  - 链表: 多个Node通过next指针连接");
        structure.add("  - 红黑树: TreeNode组成的平衡二叉搜索树");
        structure.add("");
        structure.add("树化条件：链表长度 ≥ 8 且 数组长度 ≥ 64");
        structure.add("反树化条件：红黑树节点数 ≤ 6");
        result.put("底层结构", structure);
        
        // 3. 节点类型
        Map<String, String> nodeTypes = new LinkedHashMap<>();
        nodeTypes.put("Node<K,V>", "链表节点，包含hash、key、value、next四个字段");
        nodeTypes.put("TreeNode<K,V>", "红黑树节点，继承Node，增加parent、left、right、prev、red字段");
        result.put("节点类型", nodeTypes);
        
        return result;
    }
    
    /**
     * 演示哈希计算过程
     * 
     * 【JDK 1.8 优化】
     * hash(key) = key.hashCode() ^ (key.hashCode() >>> 16)
     * 
     * 【目的】
     * 让高位也参与运算，减少哈希冲突
     * 
     * @param testKey 测试key（可选，默认为"test"）
     * @return 哈希计算过程
     */
    @GetMapping("/hash-calculation")
    public Map<String, Object> demonstrateHashCalculation(
            @RequestParam(required = false, defaultValue = "test") String testKey) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 计算hashCode
        int hashCode = testKey.hashCode();
        int high16Bits = hashCode >>> 16;
        int hash = hashCode ^ high16Bits;
        
        result.put("测试key", testKey);
        result.put("原始hashCode", hashCode);
        result.put("hashCode二进制", Integer.toBinaryString(hashCode));
        result.put("高16位 (>>>16)", high16Bits);
        result.put("高16位二进制", Integer.toBinaryString(high16Bits));
        result.put("异或结果 (^)", hash);
        result.put("最终hash二进制", Integer.toBinaryString(hash));
        
        // 模拟不同容量下的索引计算
        int[] capacities = {16, 32, 64};
        Map<String, Object> indexCalculations = new LinkedHashMap<>();
        for (int cap : capacities) {
            int index = (cap - 1) & hash;
            indexCalculations.put("容量=" + cap + ", 索引=(n-1)&hash", index);
        }
        result.put("索引计算示例", indexCalculations);
        
        // 解释优势
        List<String> advantages = new ArrayList<>();
        advantages.add("1. 高16位参与运算，充分利用hash值的所有位");
        advantages.add("2. 减少低位相同导致的冲突");
        advantages.add("3. 配合 (n-1)&hash 使用，保证分布均匀");
        advantages.add("4. 位运算效率高，比取模运算快");
        result.put("优化优势", advantages);
        
        return result;
    }
    
    /**
     * 演示put操作流程
     * 
     * 【执行步骤】
     * 1. 计算hash值
     * 2. 初始化table（若为空）
     * 3. 定位数组下标
     * 4. 处理冲突（无冲突/覆盖/链表/红黑树）
     * 5. 判断是否需要扩容
     * 
     * @param operations put操作序列（JSON格式）
     * @return 操作过程和结果
     */
    @PostMapping("/put-operation")
    public Map<String, Object> demonstratePutOperation(
            @RequestBody(required = false) Map<String, Object> operations) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 创建简易HashMap实例
        SimpleHashMap<String, String> map = new SimpleHashMap<>(4); // 小容量便于观察扩容
        
        List<Map<String, Object>> steps = new ArrayList<>();
        
        // 默认操作序列
        if (operations == null || !operations.containsKey("keys")) {
            // 演示基本put流程
            String[] keys = {"apple", "banana", "cherry"};
            String[] values = {"苹果", "香蕉", "樱桃"};
            
            for (int i = 0; i < keys.length; i++) {
                Map<String, Object> step = new LinkedHashMap<>();
                step.put("步骤", i + 1);
                step.put("操作", "put(\"" + keys[i] + "\", \"" + values[i] + "\")");
                
                String oldValue = map.put(keys[i], values[i]);
                step.put("返回值", oldValue == null ? "null (新增)" : "\"" + oldValue + "\" (覆盖)");
                step.put("当前size", map.size());
                step.put("当前容量", map.capacity());
                step.put("扩容阈值", map.threshold());
                
                steps.add(step);
            }
        } else {
            // 自定义操作序列
            @SuppressWarnings("unchecked")
            List<String> keys = (List<String>) operations.get("keys");
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) operations.get("values");
            
            for (int i = 0; i < keys.size(); i++) {
                Map<String, Object> step = new LinkedHashMap<>();
                step.put("步骤", i + 1);
                step.put("操作", "put(\"" + keys.get(i) + "\", \"" + values.get(i) + "\")");
                
                String oldValue = map.put(keys.get(i), values.get(i));
                step.put("返回值", oldValue == null ? "null (新增)" : "\"" + oldValue + "\" (覆盖)");
                step.put("当前size", map.size());
                step.put("当前容量", map.capacity());
                
                steps.add(step);
            }
        }
        
        result.put("操作步骤", steps);
        result.put("最终状态", createMap(
            "size", map.size(),
            "capacity", map.capacity(),
            "threshold", map.threshold()
        ));
        
        return result;
    }
    
    /**
     * 演示get操作流程
     * 
     * 【查找策略】
     * 1. 计算hash和索引
     * 2. 检查头节点
     * 3. 若是红黑树，O(logn)查找
     * 4. 若是链表，O(n)遍历
     * 
     * @param key 要查找的key
     * @return 查找过程和结果
     */
    @GetMapping("/get-operation")
    public Map<String, Object> demonstrateGetOperation(
            @RequestParam(defaultValue = "apple") String key) {
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 准备测试数据
        SimpleHashMap<String, String> map = new SimpleHashMap<>();
        map.put("apple", "苹果");
        map.put("banana", "香蕉");
        map.put("cherry", "樱桃");
        map.put("date", "枣");
        
        // 执行get操作
        long startTime = System.nanoTime();
        String value = map.get(key);
        long endTime = System.nanoTime();
        
        result.put("查找key", key);
        result.put("查找结果", value);
        result.put("是否找到", value != null);
        result.put("耗时(纳秒)", endTime - startTime);
        
        // 解释查找过程
        List<String> process = new ArrayList<>();
        process.add("1. 计算key的hash值: hash(" + key + ")");
        process.add("2. 计算数组下标: index = (n-1) & hash");
        process.add("3. 定位到对应桶位置");
        process.add("4. 检查头节点是否匹配");
        process.add("5. 若不匹配：");
        process.add("   - 是红黑树: 调用getTreeNode() O(logn)");
        process.add("   - 是链表: 遍历链表 O(n)");
        process.add("6. 找到返回value，未找到返回null");
        result.put("查找过程", process);
        
        return result;
    }
    
    /**
     * 演示扩容过程
     * 
     * 【JDK 1.8 核心优化】
     * 1. 2倍扩容
     * 2. 元素迁移无需重新计算hash
     * 3. 根据 hash & oldCap 决定新位置：
     *    - == 0: 留在原索引
     *    - != 0: 移到 原索引 + oldCap
     * 
     * @return 扩容过程演示
     */
    @GetMapping("/expansion")
    public Map<String, Object> demonstrateExpansion() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 创建小容量HashMap便于观察扩容
        SimpleHashMap<Integer, String> map = new SimpleHashMap<>(2);
        
        List<Map<String, Object>> expansionSteps = new ArrayList<>();
        
        // 添加元素触发扩容
        for (int i = 1; i <= 10; i++) {
            int sizeBefore = map.size();
            int capacityBefore = map.capacity();
            int thresholdBefore = map.threshold();
            
            map.put(i, "value-" + i);
            
            int sizeAfter = map.size();
            int capacityAfter = map.capacity();
            int thresholdAfter = map.threshold();
            
            boolean expanded = capacityAfter > capacityBefore;
            
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("添加元素", i);
            step.put("扩容前容量", capacityBefore);
            step.put("扩容后容量", capacityAfter);
            step.put("是否扩容", expanded);
            
            if (expanded) {
                step.put("扩容说明", "size(" + sizeAfter + ") > threshold(" + thresholdBefore + ")，触发2倍扩容");
                step.put("新阈值", thresholdAfter);
                
                // 解释元素迁移规则
                List<String> migrationRules = new ArrayList<>();
                migrationRules.add("元素迁移规则：");
                migrationRules.add("- hash & oldCap == 0 → 留在原索引");
                migrationRules.add("- hash & oldCap != 0 → 移到 原索引 + oldCap");
                migrationRules.add("优势：无需重新计算hash，效率提升一倍");
                step.put("迁移规则", migrationRules);
            }
            
            expansionSteps.add(step);
        }
        
        result.put("扩容过程", expansionSteps);
        result.put("最终状态", createMap(
            "size", map.size(),
            "capacity", map.capacity(),
            "threshold", map.threshold()
        ));
        
        // 扩容总结
        List<String> summary = new ArrayList<>();
        summary.add("【扩容触发条件】");
        summary.add("1. 首次put时，table为空");
        summary.add("2. size > threshold (capacity × loadFactor)");
        summary.add("");
        summary.add("【扩容逻辑】");
        summary.add("1. 新容量 = 旧容量 × 2");
        summary.add("2. 新阈值 = 旧阈值 × 2");
        summary.add("3. 元素迁移：根据 hash & oldCap 决定位置");
        summary.add("");
        summary.add("【性能优势】");
        summary.add("- JDK 1.7: 重新计算hash，顺序颠倒");
        summary.add("- JDK 1.8: 无需重算hash，顺序不变，效率翻倍");
        result.put("扩容总结", summary);
        
        return result;
    }
    
    /**
     * 演示树化过程
     * 
     * 【树化条件】
     * 1. 链表长度 ≥ 8
     * 2. 数组长度 ≥ 64
     * 
     * 【反树化条件】
     * 红黑树节点数 ≤ 6
     * 
     * @return 树化过程说明
     */
    @GetMapping("/treeify")
    public Map<String, Object> demonstrateTreeify() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 树化规则说明
        Map<String, Object> treeifyRules = new LinkedHashMap<>();
        treeifyRules.put("链表转红黑树条件", Arrays.asList(
            "1. 链表长度 ≥ 8",
            "2. 数组长度 ≥ 64",
            "原因：避免过早使用复杂的红黑树，优先扩容"
        ));
        treeifyRules.put("红黑树转链表条件", Arrays.asList(
            "1. 红黑树节点数 ≤ 6",
            "原因：设置缓冲区间(6-8)，避免频繁转换"
        ));
        treeifyRules.put("性能对比", createMap(
            "链表查询", "O(n) - 最坏情况",
            "红黑树查询", "O(logn) - 稳定高效",
            "临界点", "当n=8时，log2(8)=3，红黑树优势明显"
        ));
        result.put("树化规则", treeifyRules);
        
        // 为什么选择8和6？
        List<String> thresholdExplanation = new ArrayList<>();
        thresholdExplanation.add("【为什么树化阈值是8？】");
        thresholdExplanation.add("- 泊松分布：在负载因子0.75下，链表长度达到8的概率约为千万分之一");
        thresholdExplanation.add("- 此时链表已经很长，查询效率严重下降");
        thresholdExplanation.add("- 红黑树的O(logn)优势开始体现");
        thresholdExplanation.add("");
        thresholdExplanation.add("【为什么反树化阈值是6？】");
        thresholdExplanation.add("- 避免频繁树化和反树化（设置缓冲区间）");
        thresholdExplanation.add("- 如果也是8，会在7-8之间反复转换，性能开销大");
        thresholdExplanation.add("- 6提供了一个合理的缓冲区");
        result.put("阈值选择原因", thresholdExplanation);
        
        // 红黑树性质
        List<String> rbTreeProperties = new ArrayList<>();
        rbTreeProperties.add("【红黑树5条性质】");
        rbTreeProperties.add("1. 节点是红色或黑色");
        rbTreeProperties.add("2. 根节点是黑色");
        rbTreeProperties.add("3. 叶子节点（NIL）是黑色");
        rbTreeProperties.add("4. 红色节点的子节点必须是黑色（不能连续红色）");
        rbTreeProperties.add("5. 从任一节点到其叶子的所有路径都包含相同数量的黑色节点");
        rbTreeProperties.add("");
        rbTreeProperties.add("【自平衡操作】");
        rbTreeProperties.add("- 左旋 (rotateLeft)");
        rbTreeProperties.add("- 右旋 (rotateRight)");
        rbTreeProperties.add("- 变色 (recolor)");
        result.put("红黑树性质", rbTreeProperties);
        
        return result;
    }
    
    /**
     * JDK 1.7 vs JDK 1.8 核心区别对比
     * 
     * @return 对比结果
     */
    @GetMapping("/jdk-comparison")
    public Map<String, Object> compareJdkVersions() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 对比表格
        List<Map<String, String>> comparisons = new ArrayList<>();
        
        comparisons.add(createComparison("底层结构", 
            "数组 + 链表", 
            "数组 + 链表 + 红黑树",
            "解决链表过长导致的查询性能退化"));
        
        comparisons.add(createComparison("插入方式", 
            "头插法", 
            "尾插法",
            "避免多线程扩容产生环形链表"));
        
        comparisons.add(createComparison("扩容迁移", 
            "重新计算hash，顺序颠倒", 
            "无需重算hash，顺序不变",
            "效率提升一倍"));
        
        comparisons.add(createComparison("哈希算法", 
            "4次位运算 + 5次异或", 
            "1次右移 + 1次异或",
            "简化计算，减少冲突"));
        
        comparisons.add(createComparison("并发问题", 
            "可能产生环形链表，导致死循环", 
            "无环形链表问题",
            "但仍非线程安全，推荐ConcurrentHashMap"));
        
        comparisons.add(createComparison("查询性能", 
            "链表长时退化为O(n)", 
            "红黑树稳定O(logn)",
            "极端情况下性能提升显著"));
        
        comparisons.add(createComparison("初始化时机", 
            "构造函数中立即初始化", 
            "首次put时懒加载",
            "避免无用初始化"));
        
        result.put("对比项", comparisons);
        
        // 总结
        List<String> summary = new ArrayList<>();
        summary.add("【JDK 1.8 核心改进】");
        summary.add("1. 引入红黑树：解决链表过长问题");
        summary.add("2. 尾插法：避免环形链表");
        summary.add("3. 优化扩容：无需重算hash");
        summary.add("4. 简化哈希：减少计算量");
        summary.add("5. 懒加载：延迟初始化");
        summary.add("");
        summary.add("【面试回答要点】");
        summary.add("- 底层结构：数组+链表+红黑树");
        summary.add("- 哈希计算：高16位^低16位");
        summary.add("- 插入逻辑：尾插法");
        summary.add("- 扩容机制：2倍扩容，高效迁移");
        summary.add("- 线程安全：非线程安全，高并发用ConcurrentHashMap");
        result.put("总结", summary);
        
        return result;
    }
    
    /**
     * 综合演示：完整操作流程
     * 
     * @return 完整流程演示
     */
    @GetMapping("/comprehensive-demo")
    public Map<String, Object> comprehensiveDemo() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        SimpleHashMap<String, Integer> map = new SimpleHashMap<>(4);
        
        List<String> demo = new ArrayList<>();
        demo.add("========== HashMap 完整操作流程演示 ==========");
        demo.add("");
        
        // 1. 初始化
        demo.add("【步骤1】创建HashMap，初始容量=4");
        demo.add("  容量: " + map.capacity());
        demo.add("  阈值: " + map.threshold());
        demo.add("  注意：此时table为null，采用懒加载");
        demo.add("");
        
        // 2. 首次put（触发初始化）
        demo.add("【步骤2】put(\"A\", 1) - 首次put，触发table初始化");
        map.put("A", 1);
        demo.add("  容量: " + map.capacity());
        demo.add("  size: " + map.size());
        demo.add("");
        
        // 3. 继续添加
        demo.add("【步骤3】连续添加多个元素");
        String[] keys = {"B", "C", "D", "E", "F"};
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], i + 2);
            demo.add("  put(\"" + keys[i] + "\", " + (i + 2) + ") - size=" + map.size() + ", capacity=" + map.capacity());
        }
        demo.add("");
        
        // 4. 查找
        demo.add("【步骤4】查找操作");
        demo.add("  get(\"A\") = " + map.get("A"));
        demo.add("  get(\"C\") = " + map.get("C"));
        demo.add("  get(\"Z\") = " + map.get("Z") + " (不存在)");
        demo.add("");
        
        // 5. 覆盖
        demo.add("【步骤5】覆盖已有key");
        Integer oldValue = map.put("A", 100);
        demo.add("  put(\"A\", 100) - 返回旧值: " + oldValue);
        demo.add("  get(\"A\") = " + map.get("A"));
        demo.add("");
        
        // 6. 删除
        demo.add("【步骤6】删除操作");
        Integer removedValue = map.remove("B");
        demo.add("  remove(\"B\") - 返回: " + removedValue);
        demo.add("  size: " + map.size());
        demo.add("");
        
        // 7. 其他方法
        demo.add("【步骤7】其他常用方法");
        demo.add("  containsKey(\"C\"): " + map.containsKey("C"));
        demo.add("  containsKey(\"Z\"): " + map.containsKey("Z"));
        demo.add("  containsValue(3): " + map.containsValue(3));
        demo.add("  isEmpty(): " + map.isEmpty());
        demo.add("");
        
        demo.add("==============================================");
        
        result.put("演示过程", demo);
        result.put("最终状态", createMap(
            "size", map.size(),
            "capacity", map.capacity(),
            "threshold", map.threshold()
        ));
        
        return result;
    }
    
    /**
     * 创建对比项
     */
    private Map<String, String> createComparison(String item, String jdk17, String jdk18, String reason) {
        Map<String, String> comparison = new LinkedHashMap<>();
        comparison.put("对比项", item);
        comparison.put("JDK 1.7", jdk17);
        comparison.put("JDK 1.8", jdk18);
        comparison.put("改进原因", reason);
        return comparison;
    }
    
    /**
     * 创建不可变Map (Java 8兼容)
     */
    @SafeVarargs
    private static <K, V> Map<K, V> createMap(Object... entries) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) entries[i];
            @SuppressWarnings("unchecked")
            V value = (V) entries[i + 1];
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }
}
