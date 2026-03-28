package org.doubao.interview.mysql.q005.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 联合索引最左前缀原则演示
 * 
 * 【核心概念】
 * 1. 联合索引（Composite Index）：
 *    - 在多个列上创建的索引，如 (a, b, c)
 *    - B+Tree 按最左列优先排序
 *    - 后续列在同值区间内有序
 * 
 * 2. 最左前缀原则（Leftmost Prefix Rule）：
 *    - 联合索引 (a,b,c) 可以被 a、a,b、a,b,c 这样的前缀匹配高效利用
 *    - 但不能直接高效利用 b、c 开头的条件
 *    - 本质是 B+Tree 的排序规则决定的
 * 
 * 3. 索引使用情况：
 *    ✅ WHERE a=1 AND b=2 AND c=3  → 完全使用
 *    ✅ WHERE a=1 AND b=2          → 完全使用
 *    ✅ WHERE a=1                  → 部分使用
 *    ❌ WHERE b=2 AND c=3          → 不使用（跳过最左列）
 *    ❌ WHERE c=3                  → 不使用（跳过最左两列）
 * 
 * 【设计原则】
 * 1. 过滤性强的列放前面
 * 2. 等值匹配的列优先于范围查询
 * 3. 高频查询条件优先考虑
 * 
 * @author Interview Demo
 */
public class LeftmostPrefixRuleDemo {
    
    /**
     * 模拟数据行
     */
    static class Row {
        Integer a;  // 第一列（如 department_id）
        Integer b;  // 第二列（如 age）
        Integer c;  // 第三列（如 salary）
        String data; // 其他数据
        
        public Row(Integer a, Integer b, Integer c, String data) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.data = data;
        }
        
        @Override
        public String toString() {
            return String.format("Row{a=%d, b=%d, c=%d, data='%s'}", a, b, c, data);
        }
    }
    
    /**
     * 联合索引模拟器
     * 
     * 【实现原理】
     * - 使用 TreeMap 模拟 B+Tree（有序）
     * - Key 是复合键 (a,b,c)，按字典序排序
     * - 叶子节点存储主键 ID 列表
     * 
     * 【排序规则】
     * 1. 先按 a 排序
     * 2. a 相同的情况下，按 b 排序
     * 3. a,b 都相同的情况下，按 c 排序
     */
    static class CompositeIndex {
        private String indexName;
        private Map<List<Integer>, List<Integer>> indexData = new TreeMap<>((key1, key2) -> {
            // 字典序比较：先比较 a，再比较 b，最后比较 c
            for (int i = 0; i < Math.min(key1.size(), key2.size()); i++) {
                int cmp = key1.get(i).compareTo(key2.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(key1.size(), key2.size());
        });
        private int queryCount = 0;
        private int scanCount = 0;
        
        public CompositeIndex(String indexName) {
            this.indexName = indexName;
        }
        
        /**
         * 创建联合索引
         * 
         * @param rows 数据行列表
         * @param extractor 提取索引键的函数
         */
        public void createIndex(List<Row> rows, java.util.function.Function<Row, List<Integer>> extractor) {
            System.out.println("[索引] 创建联合索引 '" + indexName + "'...");
            
            for (Row row : rows) {
                List<Integer> key = extractor.apply(row);
                // 简化处理：每个键对应一个主键
                int pk = row.hashCode();
                indexData.computeIfAbsent(key, k -> new ArrayList<>()).add(pk);
            }
            
            System.out.println("  ✅ 完成，索引项数：" + indexData.size());
        }
        
        /**
         * 精确匹配查询（使用最左前缀）
         * 
         * 【场景】WHERE a=? AND b=? AND c=?
         * 【性能】O(log_N n)，最优
         * 
         * @param a 第一列的值
         * @param b 第二列的值
         * @param c 第三列的值
         * @return 匹配的主键列表
         */
        public List<Integer> exactMatch(Integer a, Integer b, Integer c) {
            queryCount++;
            System.out.println("\n[查询] 精确匹配：a=" + a + ", b=" + b + ", c=" + c);
            
            List<Integer> key = java.util.Arrays.asList(a, b, c);
            List<Integer> result = indexData.get(key);
            
            if (result == null || result.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
                return new ArrayList<>();
            }
            
            System.out.println("  ✅ 找到 " + result.size() + " 条记录（使用完整索引）");
            return result;
        }
        
        /**
         * 前缀匹配查询（使用最左前缀）
         * 
         * 【场景】WHERE a=? AND b=?
         * 【性能】O(log_N n + k)，k 为匹配的记录数
         * 
         * @param a 第一列的值
         * @param b 第二列的值
         * @return 匹配的主键列表
         */
        public List<Integer> prefixMatch2(Integer a, Integer b) {
            queryCount++;
            System.out.println("\n[查询] 前缀匹配：a=" + a + ", b=" + b);
            
            List<Integer> result = new ArrayList<>();
            int matchCount = 0;
            
            // 查找所有 a=? AND b=? 的记录（c 可以是任意值）
            for (Map.Entry<List<Integer>, List<Integer>> entry : indexData.entrySet()) {
                List<Integer> key = entry.getKey();
                if (key.get(0).equals(a) && key.get(1).equals(b)) {
                    result.addAll(entry.getValue());
                    matchCount++;
                    scanCount++;
                } else if (key.get(0) > a || (key.get(0).equals(a) && key.get(1) > b)) {
                    // 已经超出范围，提前结束
                    break;
                }
            }
            
            if (result.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
            } else {
                System.out.println("  ✅ 找到 " + result.size() + " 条记录（使用前 2 列索引）");
            }
            
            return result;
        }
        
        /**
         * 单列前缀匹配查询（使用最左前缀）
         * 
         * 【场景】WHERE a=?
         * 【性能】O(log_N n + k)，k 为匹配的记录数
         * 
         * @param a 第一列的值
         * @return 匹配的主键列表
         */
        public List<Integer> prefixMatch1(Integer a) {
            queryCount++;
            System.out.println("\n[查询] 单列前缀匹配：a=" + a);
            
            List<Integer> result = new ArrayList<>();
            int matchCount = 0;
            
            // 查找所有 a=? 的记录（b,c 可以是任意值）
            for (Map.Entry<List<Integer>, List<Integer>> entry : indexData.entrySet()) {
                List<Integer> key = entry.getKey();
                if (key.get(0).equals(a)) {
                    result.addAll(entry.getValue());
                    matchCount++;
                    scanCount++;
                } else if (key.get(0) > a) {
                    // 已经超出范围，提前结束
                    break;
                }
            }
            
            if (result.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
            } else {
                System.out.println("  ✅ 找到 " + result.size() + " 条记录（使用第 1 列索引）");
            }
            
            return result;
        }
        
        /**
         * 跳过最左列的查询（无法高效利用索引）
         * 
         * 【场景】WHERE b=? AND c=?
         * 【性能】O(n)，全索引扫描
         * 
         * @param b 第二列的值
         * @param c 第三列的值
         * @return 匹配的主键列表
         */
        public List<Integer> skipLeftmost(Integer b, Integer c) {
            queryCount++;
            System.out.println("\n[查询] 跳过最左列：b=" + b + ", c=" + c);
            System.out.println("  ⚠️  无法使用最左前缀，需要全索引扫描");
            
            List<Integer> result = new ArrayList<>();
            int fullScanCount = 0;
            
            // 必须扫描整个索引
            for (Map.Entry<List<Integer>, List<Integer>> entry : indexData.entrySet()) {
                List<Integer> key = entry.getKey();
                fullScanCount++;
                
                if (key.get(1).equals(b) && key.get(2).equals(c)) {
                    result.addAll(entry.getValue());
                }
            }
            
            scanCount += fullScanCount;
            
            if (result.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录（全表扫描 " + fullScanCount + " 条）");
            } else {
                System.out.println("  ⚠️  找到 " + result.size() + " 条记录（全表扫描 " + fullScanCount + " 条）");
            }
            
            return result;
        }
        
        /**
         * 范围查询演示
         * 
         * 【场景】WHERE a=? AND b BETWEEN ? AND ?
         * 【性能】O(log_N n + k)
         * 
         * @param a 第一列的值
         * @param bStart b 的起始值
         * @param bEnd b 的结束值
         * @return 匹配的主键列表
         */
        public List<Integer> rangeQuery(Integer a, Integer bStart, Integer bEnd) {
            queryCount++;
            System.out.println("\n[查询] 范围查询：a=" + a + ", b BETWEEN [" + bStart + ", " + bEnd + "]");
            
            List<Integer> result = new ArrayList<>();
            int matchCount = 0;
            
            // 查找 a=? AND b 在 [bStart, bEnd] 范围内的记录
            for (Map.Entry<List<Integer>, List<Integer>> entry : indexData.entrySet()) {
                List<Integer> key = entry.getKey();
                if (key.get(0).equals(a)) {
                    if (key.get(1) >= bStart && key.get(1) <= bEnd) {
                        result.addAll(entry.getValue());
                        matchCount++;
                        scanCount++;
                    } else if (key.get(1) > bEnd) {
                        // 超出范围，提前结束
                        break;
                    }
                } else if (key.get(0) > a) {
                    // a 列已超出范围，提前结束
                    break;
                }
            }
            
            if (result.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
            } else {
                System.out.println("  ✅ 找到 " + result.size() + " 条记录（使用索引前 2 列 + 范围扫描）");
            }
            
            return result;
        }
        
        /**
         * 获取查询统计
         */
        public int getQueryCount() {
            return queryCount;
        }
        
        /**
         * 获取扫描统计
         */
        public int getScanCount() {
            return scanCount;
        }
        
        /**
         * 重置统计
         */
        public void resetStats() {
            queryCount = 0;
            scanCount = 0;
        }
        
        /**
         * 打印索引结构
         */
        public void printStructure() {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   联合索引结构：" + indexName + "           ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            System.out.println("\n【B+Tree 叶子节点】(a, b, c) → 主键列表");
            System.out.println("┌─────────────────────────────────────┐");
            
            int count = 0;
            for (Map.Entry<List<Integer>, List<Integer>> entry : indexData.entrySet()) {
                List<Integer> key = entry.getKey();
                System.out.println("│ (" + String.format("%2d", key.get(0)) + "," + 
                                   String.format("%2d", key.get(1)) + "," + 
                                   String.format("%2d", key.get(2)) + ") → " + 
                                   entry.getValue());
                count++;
                if (count >= 10) {
                    System.out.println("│ ... (共 " + indexData.size() + " 项)");
                    break;
                }
            }
            
            System.out.println("└─────────────────────────────────────┘");
            
            System.out.println("\n【排序规则】");
            System.out.println("  1. 先按 a 排序");
            System.out.println("  2. a 相同的情况下，按 b 排序");
            System.out.println("  3. a,b 都相同的情况下，按 c 排序");
            System.out.println("\n【可利用的前缀】");
            System.out.println("  ✅ (a) - 第 1 列");
            System.out.println("  ✅ (a,b) - 前 2 列");
            System.out.println("  ✅ (a,b,c) - 全部 3 列");
            System.out.println("  ❌ (b) - 跳过最左列，不可用");
            System.out.println("  ❌ (c) - 跳过最左两列，不可用");
            System.out.println();
        }
    }
    
    // ========== 测试场景 ==========
    
    private CompositeIndex idx_abc;
    private List<Row> testData = new ArrayList<>();
    
    /**
     * 初始化测试数据
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   联合索引最左前缀原则演示              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 创建联合索引 (a, b, c)
        idx_abc = new CompositeIndex("idx_a_b_c");
        
        // 准备测试数据
        // 模拟场景：department_id, age, salary
        testData.add(new Row(1, 25, 5000, "员工 1"));
        testData.add(new Row(1, 25, 6000, "员工 2"));
        testData.add(new Row(1, 28, 5500, "员工 3"));
        testData.add(new Row(1, 30, 7000, "员工 4"));
        testData.add(new Row(2, 25, 5000, "员工 5"));
        testData.add(new Row(2, 26, 5500, "员工 6"));
        testData.add(new Row(2, 30, 8000, "员工 7"));
        testData.add(new Row(3, 25, 6000, "员工 8"));
        testData.add(new Row(3, 28, 7000, "员工 9"));
        testData.add(new Row(3, 30, 9000, "员工 10"));
        
        System.out.println("【步骤 1】准备测试数据（10 条记录）\n");
        System.out.println("数据结构：Row(department_id=a, age=b, salary=c, data)");
        for (Row row : testData) {
            System.out.println("  • " + row);
        }
        
        System.out.println("\n【步骤 2】创建联合索引 (a, b, c)\n");
        idx_abc.createIndex(testData, r -> java.util.Arrays.asList(r.a, r.b, r.c));
        
        // 打印索引结构
        idx_abc.printStructure();
    }
    
    /**
     * 演示 1：完全匹配（使用完整索引）
     */
    public void testExactMatch() {
        System.out.println("【演示 1】完全匹配（使用完整索引）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询部门=1，年龄=25，薪资=5000 的员工");
        System.out.println("SQL: SELECT * FROM employees WHERE a=1 AND b=25 AND c=5000");
        
        List<Integer> result = idx_abc.exactMatch(1, 25, 5000);
        
        System.out.println("\n【性能分析】");
        System.out.println("  ✅ 使用了完整的 3 列索引");
        System.out.println("  ✅ 时间复杂度：O(log_N n)");
        System.out.println("  ✅ 最优查询性能");
    }
    
    /**
     * 演示 2：使用前 2 列前缀
     */
    public void testPrefixMatch2() {
        System.out.println("\n【演示 2】使用前 2 列前缀");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询部门=1，年龄=25 的所有员工（不管薪资）");
        System.out.println("SQL: SELECT * FROM employees WHERE a=1 AND b=25");
        
        List<Integer> result = idx_abc.prefixMatch2(1, 25);
        
        System.out.println("\n【性能分析】");
        System.out.println("  ✅ 使用了前 2 列索引 (a, b)");
        System.out.println("  ✅ 符合最左前缀原则");
        System.out.println("  ⚠️  需要扫描 c 列的所有值");
    }
    
    /**
     * 演示 3：使用第 1 列前缀
     */
    public void testPrefixMatch1() {
        System.out.println("\n【演示 3】使用第 1 列前缀");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询部门=2 的所有员工（不管年龄和薪资）");
        System.out.println("SQL: SELECT * FROM employees WHERE a=2");
        
        List<Integer> result = idx_abc.prefixMatch1(2);
        
        System.out.println("\n【性能分析】");
        System.out.println("  ✅ 使用了第 1 列索引 (a)");
        System.out.println("  ✅ 符合最左前缀原则");
        System.out.println("  ⚠️  需要扫描 b 和 c 列的所有组合");
    }
    
    /**
     * 演示 4：跳过最左列（无法使用索引）
     */
    public void testSkipLeftmost() {
        System.out.println("\n【演示 4】跳过最左列（无法使用索引）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询年龄=25，薪资=5000 的员工（没有部门条件）");
        System.out.println("SQL: SELECT * FROM employees WHERE b=25 AND c=5000");
        
        List<Integer> result = idx_abc.skipLeftmost(25, 5000);
        
        System.out.println("\n【性能分析】");
        System.out.println("  ❌ 跳过了最左列 a，无法利用索引的有序性");
        System.out.println("  ❌ 需要全索引扫描 O(n)");
        System.out.println("  ⚠️  性能最差，相当于没有索引");
    }
    
    /**
     * 演示 5：范围查询
     */
    public void testRangeQuery() {
        System.out.println("\n【演示 5】范围查询");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询部门=1，年龄在 25-28 之间的员工");
        System.out.println("SQL: SELECT * FROM employees WHERE a=1 AND b BETWEEN 25 AND 28");
        
        List<Integer> result = idx_abc.rangeQuery(1, 25, 28);
        
        System.out.println("\n【性能分析】");
        System.out.println("  ✅ 使用了前 2 列索引 (a, b)");
        System.out.println("  ✅ a 列等值匹配，b 列范围扫描");
        System.out.println("  ⚠️  c 列无法使用（被范围查询阻断）");
    }
    
    /**
     * 演示 6：对比不同查询方式的性能
     */
    public void comparePerformance() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        不同查询方式性能对比            ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("┌─────────────────┬──────────┬──────────┬──────────┐");
        System.out.println("│   查询类型       │ 使用列数  │ 扫描行数  │ 性能评估  │");
        System.out.println("├─────────────────┼──────────┼──────────┼──────────┤");
        System.out.println("│ a=1,b=25,c=5000 │ 3 列      │ 1 行      │ ⭐⭐⭐⭐⭐  │");
        System.out.println("│ a=1,b=25        │ 2 列      │ 2 行      │ ⭐⭐⭐⭐   │");
        System.out.println("│ a=1             │ 1 列      │ 4 行      │ ⭐⭐⭐    │");
        System.out.println("│ b=25,c=5000     │ 0 列      │ 10 行     │ ⭐ 最差  │");
        System.out.println("│ a=1,b 25-28     │ 2 列 + 范围 │ 3 行      │ ⭐⭐⭐⭐   │");
        System.out.println("└─────────────────┴──────────┴──────────┴──────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  最左前缀原则的本质是 B+Tree 的排序规则");
        System.out.println("  2️⃣  可以利用 (a)、(a,b)、(a,b,c) 作为前缀");
        System.out.println("  3️⃣  跳过最左列会导致索引失效（全扫描）");
        System.out.println("  4️⃣  范围查询会阻断后续列的使用");
        System.out.println("  5️⃣  设计索引时要把过滤性强的列放前面\n");
        
        System.out.println("【索引设计原则】");
        System.out.println("  ✅ 过滤性强、区分度高的列放前面");
        System.out.println("  ✅ 等值匹配的列优先于范围查询的列");
        System.out.println("  ✅ 高频查询条件优先考虑");
        System.out.println("  ✅ 考虑覆盖索引，减少回表\n");
    }
    
    public static void main(String[] args) {
        LeftmostPrefixRuleDemo demo = new LeftmostPrefixRuleDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testExactMatch();
        demo.testPrefixMatch2();
        demo.testPrefixMatch1();
        demo.testSkipLeftmost();
        demo.testRangeQuery();
        demo.comparePerformance();
    }
}
