package org.doubao.interview.mysql.q002.demo;

import org.doubao.interview.mysql.q002.index.BPlusTree;
import org.doubao.interview.mysql.q002.index.RedBlackTree;
import org.doubao.interview.mysql.q002.index.HashIndex;

/**
 * InnoDB 索引结构对比演示
 * 
 * 【演示目的】
 * 通过可视化的方式，对比 B+Tree、红黑树、Hash 三种索引结构的特性，
 * 理解为什么 InnoDB 选择 B+Tree 作为主要索引结构。
 * 
 * 【核心对比维度】
 * 1. 插入性能和树高 - B+Tree 树高最低，磁盘 I/O 最少
 * 2. 等值查询性能 - Hash 最优（O(1)），B+Tree 和红黑树相当（O(log n)）
 * 3. 范围查询能力 - B+Tree 最强（链表扫描），红黑树和 Hash 不支持
 * 4. 有序遍历能力 - B+Tree 和红黑树支持，Hash 需要额外排序
 * 
 * 【技术要点】
 * - B+Tree：多叉树，非叶子节点只存索引，叶子节点存数据并相连
 * - 红黑树：二叉树，每个节点存数据，自平衡但树高较高
 * - Hash：哈希表，O(1) 查询但不支持范围查和有序遍历
 * 
 * @author Interview Demo
 */
public class IndexComparisonDemo {
    
    public static void main(String[] args) {
        // 【开场白】说明演示主题
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   InnoDB 索引结构对比演示              ║");
        System.out.println("║   B+Tree vs 红黑树 vs Hash             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 演示 1：插入性能和树高对比 ==========
        // 【目的】展示 B+Tree 的低树高特性，减少磁盘 I/O
        testInsertion();
        
        // ========== 演示 2：等值查询对比 ==========
        // 【目的】对比三种索引的点查询性能
        testPointQuery();
        
        // ========== 演示 3：范围查询对比 ==========
        // 【目的】突出 B+Tree 的范围查询优势（核心考点）
        testRangeQuery();
        
        // ========== 演示 4：有序遍历对比 ==========
        // 【目的】展示 B+Tree 和红黑树的有序性
        testOrderedTraversal();
        
        // ========== 总结 ==========
        // 【重点】归纳三种索引的优缺点，回答面试问题
        printSummary();
    }
    
    /**
     * 测试插入性能和树高
     */
    private static void testInsertion() {
        System.out.println("【演示 1】插入性能和树高对比");
        System.out.println("──────────────────────────────────────");
        
        BPlusTree bpt = new BPlusTree();
        RedBlackTree rbt = new RedBlackTree();
        HashIndex hash = new HashIndex();
        
        // 【简化数据集】避免边界情况导致的复杂分裂
        int[] keys = {50, 25, 75, 10, 30, 60, 90, 5, 15};
        
        System.out.println("\n-- 插入序列：" + arrayToString(keys) + "\n");
        
        System.out.println("=== B+Tree 插入过程 ===");
        for (int key : keys) {
            bpt.insert(key, "Value-" + key);
        }
        bpt.printTree();
        
        System.out.println("=== 红黑树插入过程 ===");
        for (int key : keys) {
            rbt.insert(key, "Value-" + key);
        }
        rbt.printTree();
        
        System.out.println("=== Hash 索引插入过程 ===");
        for (int key : keys) {
            hash.insert(key, "Value-" + key);
        }
        hash.printTable();
        
        System.out.println("\n【对比结果】");
        System.out.println("  B+Tree:   树高 = " + bpt.getHeight() + " 层");
        System.out.println("  红黑树：   树高 = " + rbt.getHeight() + " 层");
        System.out.println("  Hash:      O(1) 插入，无需考虑树高");
        System.out.println("\n结论：B+Tree 树高最低，磁盘 I/O 次数最少\n");
    }
    
    /**
     * 测试等值查询
     */
    private static void testPointQuery() {
        System.out.println("【演示 2】等值查询对比");
        System.out.println("──────────────────────────────────────");
        
        // 构建索引
        BPlusTree bpt = buildBPlusTree();
        RedBlackTree rbt = buildRedBlackTree();
        HashIndex hash = buildHashIndex();
        
        int[] queryKeys = {28, 60, 15, 99};
        
        System.out.println("\n-- 查询序列：" + arrayToString(queryKeys) + "\n");
        
        System.out.println("=== B+Tree 查询 ===");
        for (int key : queryKeys) {
            bpt.get(key);
        }
        
        System.out.println("\n=== 红黑树查询 ===");
        for (int key : queryKeys) {
            rbt.get(key);
        }
        
        System.out.println("\n=== Hash 索引查询 ===");
        for (int key : queryKeys) {
            hash.get(key);
        }
        
        System.out.println("\n【对比结果】");
        System.out.println("  B+Tree:    需要 " + bpt.getHeight() + " 次磁盘 I/O");
        System.out.println("  红黑树：    平均需要 log₂(n) 次 I/O");
        System.out.println("  Hash:      O(1) 时间复杂度，最快");
        System.out.println("\n结论：Hash 在等值查询上最优，但 B+Tree 表现稳定\n");
    }
    
    /**
     * 测试范围查询
     */
    private static void testRangeQuery() {
        System.out.println("【演示 3】范围查询对比 [20, 50]");
        System.out.println("──────────────────────────────────────");
        
        BPlusTree bpt = buildBPlusTree();
        RedBlackTree rbt = buildRedBlackTree();
        HashIndex hash = buildHashIndex();
        
        System.out.println("\n=== B+Tree 范围查询 ===");
        long start1 = System.nanoTime();
        java.util.List<String> result1 = bpt.rangeQuery(20, 50);
        long time1 = System.nanoTime() - start1;
        System.out.println("找到 " + result1.size() + " 个结果，耗时：" + time1 / 1000 + " μs");
        
        System.out.println("\n=== 红黑树范围查询 ===");
        System.out.println("⚠️ 红黑树不支持高效的范围查询");
        System.out.println("需要先中序遍历，然后过滤（O(n) 时间复杂度）");
        
        System.out.println("\n=== Hash 索引范围查询 ===");
        long start3 = System.nanoTime();
        java.util.List<String> result3 = hash.rangeQuery(20, 50);
        long time3 = System.nanoTime() - start3;
        System.out.println("找到 " + result3.size() + " 个结果，耗时：" + time3 / 1000 + " μs");
        
        System.out.println("\n【对比结果】");
        System.out.println("  B+Tree:    ✅ 天然支持，叶子节点链式遍历");
        System.out.println("  红黑树：    ❌ 需要全树遍历");
        System.out.println("  Hash:      ❌ 需要全表扫描");
        System.out.println("\n结论：B+Tree 在范围查询上优势明显\n");
    }
    
    /**
     * 测试有序遍历
     */
    private static void testOrderedTraversal() {
        System.out.println("【演示 4】有序遍历对比");
        System.out.println("──────────────────────────────────────");
        
        BPlusTree bpt = buildBPlusTree();
        RedBlackTree rbt = buildRedBlackTree();
        HashIndex hash = buildHashIndex();
        
        System.out.println("\n=== B+Tree 有序遍历 ===");
        System.out.println("✅ 直接遍历叶子节点链表（已排序）");
        bpt.printTree();
        
        System.out.println("\n=== 红黑树有序遍历 ===");
        System.out.println("✅ 中序遍历即可得到有序序列");
        rbt.inorderTraversal();
        
        System.out.println("\n=== Hash 索引有序遍历 ===");
        hash.orderedTraversal();
        System.out.println("⚠️ 需要先收集所有 key，然后排序");
        
        System.out.println("\n【对比结果】");
        System.out.println("  B+Tree:    ✅ 叶子节点天然有序，直接遍历");
        System.out.println("  红黑树：    ✅ 中序遍历可得有序序列");
        System.out.println("  Hash:      ❌ 无序存储，需要额外排序");
        System.out.println("\n结论：B+Tree 和红黑树支持有序遍历，Hash 不支持\n");
    }
    
    /**
     * 打印总结
     */
    private static void printSummary() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   三种索引结构综合对比                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("┌─────────────┬──────────┬──────────┬──────────┐");
        System.out.println("│   特性      │ B+Tree   │ 红黑树    │   Hash   │");
        System.out.println("├─────────────┼──────────┼──────────┼──────────┤");
        System.out.println("│ 树高/高度   │ 低 (3-4 层) │ 高 (log n) │ O(1)    │");
        System.out.println("│ 等值查询    │ O(log n)  │ O(log n)  │ O(1)     │");
        System.out.println("│ 范围查询    │ ✅ 优秀    │ ❌ 困难    │ ❌ 不支持  │");
        System.out.println("│ 有序遍历    │ ✅ 优秀    │ ✅ 支持    │ ❌ 不支持  │");
        System.out.println("│ 磁盘 I/O    │ ✅ 少      │ ❌ 多      │ ✅ 最少    │");
        System.out.println("│ 适用场景    │ OLTP     │ 内存索引  │ KV 存储   │");
        System.out.println("└─────────────┴──────────┴──────────┴──────────┘\n");
        
        System.out.println("【InnoDB 选择 B+Tree 的原因】");
        System.out.println("1. ✅ 树高低，减少磁盘 I/O 次数");
        System.out.println("2. ✅ 叶子节点有序相连，适合范围查询");
        System.out.println("3. ✅ 非叶子节点只存 key，单页容纳更多分支");
        System.out.println("4. ✅ 查询性能稳定，所有查询都要到叶子节点");
        System.out.println("5. ✅ 与页结构完美结合，适合 OLTP 场景\n");
        
        System.out.println("【为什么不用红黑树？】");
        System.out.println("❌ 树高较高，磁盘 I/O 次数多");
        System.out.println("❌ 每个节点存数据，占用空间大");
        System.out.println("❌ 范围查询效率低\n");
        
        System.out.println("【为什么不用 Hash？】");
        System.out.println("❌ 只支持等值查询，不支持范围查询");
        System.out.println("❌ 不支持有序遍历");
        System.out.println("❌ 存在哈希冲突问题");
        System.out.println("❌ 扩容成本高\n");
    }
    
    // ===== 辅助方法 =====
    
    private static BPlusTree buildBPlusTree() {
        BPlusTree bpt = new BPlusTree();
        // 【简化数据集】与 testInsertion 保持一致
        int[] keys = {50, 25, 75, 10, 30, 60, 90, 5, 15};
        for (int key : keys) {
            bpt.insert(key, "Value-" + key);
        }
        return bpt;
    }
    
    private static RedBlackTree buildRedBlackTree() {
        RedBlackTree rbt = new RedBlackTree();
        int[] keys = {50, 25, 75, 10, 30, 60, 90, 5, 15};
        for (int key : keys) {
            rbt.insert(key, "Value-" + key);
        }
        return rbt;
    }
    
    private static HashIndex buildHashIndex() {
        HashIndex hash = new HashIndex();
        int[] keys = {50, 25, 75, 10, 30, 60, 90, 5, 15};
        for (int key : keys) {
            hash.insert(key, "Value-" + key);
        }
        return hash;
    }
    
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
