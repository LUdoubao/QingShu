package org.doubao.interview.mysql.q004.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 回表机制与优化演示
 * 
 * 【核心概念】
 * 1. 回表（Bookmark Lookup）：
 *    - 先通过二级索引找到主键值
 *    - 再到聚簇索引中根据主键获取完整数据
 *    - 这个过程叫"回表"
 * 
 * 2. 回表的成本：
 *    - 增加一次 B+Tree 查询（O(log_N n)）
 *    - 可能产生随机 I/O（磁盘访问）
 *    - CPU 需要比较多条记录
 * 
 * 3. 覆盖索引（Covering Index）：
 *    - 如果查询的列都在索引中
 *    - 可以直接从索引返回结果，无需回表
 *    - 这是减少回表的主要手段
 * 
 * 【优化策略】
 * 1. 使用覆盖索引 - 查询列都在索引中
 * 2. 合理设计联合索引 - 减少回表次数
 * 3. 避免 SELECT * - 按需查询
 * 4. 使用 EXPLAIN 分析 - 观察是否 Using index
 * 
 * @author Interview Demo
 */
public class BookmarkLookupOptimization {
    
    /**
     * 模拟数据行
     */
    static class Row {
        Long id;          // 主键
        String name;      // 姓名
        Integer age;      // 年龄
        String email;     // 邮箱
        String department;// 部门
        
        public Row(Long id, String name, Integer age, String email, String department) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.email = email;
            this.department = department;
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, name='%s', age=%d, email='%s', dept='%s'}", 
                id, name, age, email, department);
        }
    }
    
    /**
     * 聚簇索引模拟器
     * 
     * 【实现原理】
     * - 使用 TreeMap 模拟 B+Tree（有序）
     * - Key 是主键 ID，Value 是完整的行数据
     * - 叶子节点直接存储整行数据（无需回表）
     */
    static class ClusteredIndex {
        private Map<Long, Row> data = new TreeMap<>();  // 按主键有序
        private int queryCount = 0;  // 查询次数统计
        
        /**
         * 插入数据
         */
        public void insert(Row row) {
            data.put(row.id, row);
        }
        
        /**
         * 批量插入数据
         */
        public void batchInsert(List<Row> rows) {
            for (Row row : rows) {
                insert(row);
            }
        }
        
        /**
         * 根据主键查询（聚簇索引查询）
         * 
         * 【性能特点】
         * - 时间复杂度：O(log_N n)
         * - 磁盘 I/O：1 次
         * - 无需回表
         * 
         * @param id 主键 ID
         * @return 行数据
         */
        public Row getById(Long id) {
            queryCount++;
            System.out.println("    [聚簇索引] 查询 id=" + id + " (第" + queryCount + "次查询)");
            return data.get(id);
        }
        
        /**
         * 获取查询次数
         */
        public int getQueryCount() {
            return queryCount;
        }
        
        /**
         * 重置查询计数
         */
        public void resetQueryCount() {
            queryCount = 0;
        }
    }
    
    /**
     * 二级索引模拟器
     * 
     * 【实现原理】
     * - 使用 TreeMap 模拟 B+Tree（有序）
     * - Key 是索引列的值，Value 是主键 ID 列表
     * - 叶子节点只存储"索引列 + 主键"
     * - 查询时需要回表
     */
    static class SecondaryIndex {
        private String indexName;              // 索引名称
        private List<String> indexColumns;     // 索引列名
        private Map<Object, List<Long>> indexData = new TreeMap<>();  // 索引数据
        private ClusteredIndex clusteredIndex; // 关联的聚簇索引
        private int lookupCount = 0;           // 查询次数
        private int bookmarkLookupCount = 0;   // 回表次数
        
        public SecondaryIndex(String indexName, List<String> indexColumns, ClusteredIndex clusteredIndex) {
            this.indexName = indexName;
            this.indexColumns = indexColumns;
            this.clusteredIndex = clusteredIndex;
        }
        
        /**
         * 创建索引
         * 
         * @param rows 数据行列表
         * @param keyExtractor 提取索引键的函数
         */
        public void createIndex(List<Row> rows, java.util.function.Function<Row, Object> keyExtractor) {
            System.out.println("[索引] 创建索引 '" + indexName + "'...");
            
            for (Row row : rows) {
                Object keyValue = keyExtractor.apply(row);
                indexData.computeIfAbsent(keyValue, k -> new ArrayList<>()).add(row.id);
            }
            
            System.out.println("  ✅ 完成，唯一索引值：" + indexData.size());
        }
        
        /**
         * 查询（需要回表）
         * 
         * 【回表过程】
         * 1. 在二级索引中找到主键 ID 列表
         * 2. 逐个主键到聚簇索引中获取完整数据
         * 
         * 【性能成本】
         * - 二级索引查询：O(log_N n)
         * - 回表次数：k 次（k 为匹配的主键数）
         * - 总成本：O(log_N n + k * log_N n)
         * 
         * @param keyValue 索引列的值
         * @return 匹配的行列表
         */
        public List<Row> query(Object keyValue) {
            lookupCount++;
            System.out.println("\n[二级索引:" + indexName + "] 查询 '" + keyValue + "'");
            
            // 步骤 1：在二级索引中查找
            List<Long> primaryKeys = indexData.get(keyValue);
            
            if (primaryKeys == null || primaryKeys.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
                return new ArrayList<>();
            }
            
            System.out.println("  📖 二级索引找到主键：" + primaryKeys);
            System.out.println("  ⚠️  需要回表，回表次数：" + primaryKeys.size());
            
            // 步骤 2：回表查询
            List<Row> result = new ArrayList<>();
            for (Long pk : primaryKeys) {
                Row row = clusteredIndex.getById(pk);
                if (row != null) {
                    result.add(row);
                    bookmarkLookupCount++;
                }
            }
            
            System.out.println("  ✅ 回表完成，找到 " + result.size() + " 条记录");
            return result;
        }
        
        /**
         * 范围查询（需要多次回表）
         * 
         * @param startKey 起始值
         * @param endKey 结束值
         * @return 范围内的所有行
         */
        public List<Row> rangeQuery(Object startKey, Object endKey) {
            lookupCount++;
            System.out.println("\n[二级索引:" + indexName + "] 范围查询 [" + startKey + ", " + endKey + "]");
            
            List<Long> allPrimaryKeys = new ArrayList<>();
            
            // 步骤 1：在二级索引中找到范围内的所有主键
            for (Map.Entry<Object, List<Long>> entry : indexData.entrySet()) {
                Object key = entry.getKey();
                if (compare(key, startKey) >= 0 && compare(key, endKey) <= 0) {
                    allPrimaryKeys.addAll(entry.getValue());
                    System.out.print("  " + key + "(" + entry.getValue().size() + ") ");
                }
            }
            System.out.println();
            
            System.out.println("  📖 找到主键列表：" + allPrimaryKeys);
            System.out.println("  ⚠️  需要回表，回表次数：" + allPrimaryKeys.size());
            
            // 步骤 2：回表查询
            List<Row> result = new ArrayList<>();
            for (Long pk : allPrimaryKeys) {
                Row row = clusteredIndex.getById(pk);
                if (row != null) {
                    result.add(row);
                    bookmarkLookupCount++;
                }
            }
            
            System.out.println("  ✅ 回表完成，找到 " + result.size() + " 条记录");
            return result;
        }
        
        /**
         * 覆盖索引查询（无需回表）
         * 
         * 【核心优势】
         * - 直接从二级索引返回结果
         * - 无需访问聚簇索引
         * - 性能最优
         * 
         * @param keyValue 索引列的值
         * @return 包含索引列和主键的结果
         */
        public List<Map<String, Object>> coveringIndexQuery(Object keyValue) {
            lookupCount++;
            System.out.println("\n[二级索引:" + indexName + "] 覆盖索引查询 '" + keyValue + "'");
            
            List<Long> primaryKeys = indexData.get(keyValue);
            
            if (primaryKeys == null || primaryKeys.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
                return new ArrayList<>();
            }
            
            System.out.println("  ✅ 直接从索引返回，无需回表！");
            System.out.println("  📊 性能提升：避免了 " + primaryKeys.size() + " 次回表");
            
            // 构造结果（只包含索引列和主键）
            List<Map<String, Object>> result = new ArrayList<>();
            for (Long pk : primaryKeys) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("id", pk);
                // 这里简化处理，实际应该从索引中直接获取对应列
                result.add(rowMap);
            }
            
            return result;
        }
        
        /**
         * 比较两个对象
         */
        @SuppressWarnings("unchecked")
        private int compare(Object o1, Object o2) {
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                return ((Comparable) o1).compareTo(o2);
            }
            return 0;
        }
        
        /**
         * 获取回表次数
         */
        public int getBookmarkLookupCount() {
            return bookmarkLookupCount;
        }
        
        /**
         * 重置统计
         */
        public void resetStats() {
            lookupCount = 0;
            bookmarkLookupCount = 0;
        }
    }
    
    /**
     * EXPLAIN 工具模拟器
     * 
     * 【作用】
     * - 分析 SQL 执行计划
     * - 判断是否使用了覆盖索引
     * - 观察 Extra 字段是否有"Using index"
     */
    static class ExplainAnalyzer {
        
        /**
         * 分析查询类型
         * 
         * @param sql SQL 语句
         * @param hasCoveringIndex 是否有覆盖索引
         */
        public static void analyze(String sql, boolean hasCoveringIndex) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        EXPLAIN 执行计划分析            ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("\nSQL: " + sql);
            System.out.println("\n┌─────────────┬──────────────────────────┐");
            System.out.println("│   属性      │          值              │");
            System.out.println("├─────────────┼──────────────────────────┤");
            System.out.println("│ id          │ 1                        │");
            System.out.println("│ type        │ ref / range              │");
            System.out.println("│ key         │ 使用的索引               │");
            System.out.println("│ rows        │ 扫描行数                 │");
            System.out.println("│ Extra       │ " + (hasCoveringIndex ? "Using index (覆盖索引)" : "Using where; Using index condition") + " │");
            System.out.println("└─────────────┴──────────────────────────┘");
            
            if (hasCoveringIndex) {
                System.out.println("\n✅ 使用了覆盖索引，无需回表！");
            } else {
                System.out.println("\n⚠️  需要回表查询完整数据");
            }
        }
    }
    
    // ========== 测试场景 ==========
    
    private ClusteredIndex clusteredIndex;
    private SecondaryIndex idx_department;  // 部门索引
    private SecondaryIndex idx_name_age;    // 联合索引 (name, age)
    private List<Row> employees = new ArrayList<>();
    
    /**
     * 初始化测试数据
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     回表机制与优化演示                  ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 创建聚簇索引
        clusteredIndex = new ClusteredIndex();
        
        // 准备测试数据（员工表）
        employees.add(new Row(1L, "张三", 25, "zhangsan@example.com", "技术部"));
        employees.add(new Row(2L, "李四", 30, "lisi@example.com", "产品部"));
        employees.add(new Row(3L, "王五", 28, "wangwu@example.com", "技术部"));
        employees.add(new Row(4L, "赵六", 35, "zhaoliu@example.com", "销售部"));
        employees.add(new Row(5L, "孙七", 22, "sunqi@example.com", "技术部"));
        employees.add(new Row(6L, "周八", 30, "zhouba@example.com", "人事部"));
        employees.add(new Row(7L, "吴九", 27, "wujiu@example.com", "技术部"));
        employees.add(new Row(8L, "郑十", 32, "zhengshi@example.com", "财务部"));
        employees.add(new Row(9L, "张三丰", 45, "zhangsanfeng@example.com", "技术部"));
        employees.add(new Row(10L, "李明", 30, "liming@example.com", "产品部"));
        
        System.out.println("【步骤 1】准备员工数据（10 条记录）\n");
        clusteredIndex.batchInsert(employees);
        System.out.println("  ✅ 数据插入完成\n");
        
        System.out.println("【步骤 2】创建二级索引\n");
        
        // 创建 department 索引
        idx_department = new SecondaryIndex("idx_department", java.util.Arrays.asList("department"), clusteredIndex);
        idx_department.createIndex(employees, r -> r.department);
        
        // 创建 name+age 联合索引
        idx_name_age = new SecondaryIndex("idx_name_age", java.util.Arrays.asList("name", "age"), clusteredIndex);
        idx_name_age.createIndex(employees, r -> r.name);
        
        System.out.println();
    }
    
    /**
     * 演示 1：普通二级索引查询（需要回表）
     */
    public void testNormalSecondaryIndex() {
        System.out.println("【演示 1】普通二级索引查询（需要回表）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询技术部的所有员工");
        System.out.println("SQL: SELECT * FROM employees WHERE department='技术部'");
        
        List<Row> result = idx_department.query("技术部");
        
        System.out.println("\n结果：");
        for (Row row : result) {
            System.out.println("  • " + row);
        }
        
        System.out.println("\n【性能分析】");
        System.out.println("  • 二级索引找到 5 个主键");
        System.out.println("  • 需要 5 次回表查询");
        System.out.println("  • 每次回表都是一次 B+Tree 查询");
        System.out.println("  ⚠️  回表成本高！");
    }
    
    /**
     * 演示 2：覆盖索引优化（避免回表）
     */
    public void testCoveringIndex() {
        System.out.println("\n【演示 2】覆盖索引优化（避免回表）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：只查询技术部员工的 ID（不需要其他字段）");
        System.out.println("SQL: SELECT id FROM employees WHERE department='技术部'");
        
        List<Map<String, Object>> result = idx_department.coveringIndexQuery("技术部");
        
        System.out.println("\n【性能对比】");
        System.out.println("  普通查询：5 次回表");
        System.out.println("  覆盖索引：0 次回表 ✅");
        System.out.println("  性能提升：显著！");
    }
    
    /**
     * 演示 3：范围查询的回表成本
     */
    public void testRangeQueryCost() {
        System.out.println("\n【演示 3】范围查询的回表成本");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：查询年龄在 25-30 岁之间的员工");
        System.out.println("SQL: SELECT * FROM employees WHERE age BETWEEN 25 AND 30");
        
        // 假设有 age 索引
        SecondaryIndex idx_age = new SecondaryIndex("idx_age", java.util.Arrays.asList("age"), clusteredIndex);
        idx_age.createIndex(employees, r -> r.age);
        
        List<Row> result = idx_age.rangeQuery(25, 30);
        
        System.out.println("\n【性能分析】");
        System.out.println("  • 范围查询找到多个主键");
        System.out.println("  • 每个主键都需要回表");
        System.out.println("  • 回表次数 = 匹配的记录数");
        System.out.println("  ⚠️  范围查询回表成本更高！");
    }
    
    /**
     * 演示 4：EXPLAIN 分析
     */
    public void testExplainAnalysis() {
        System.out.println("\n【演示 4】使用 EXPLAIN 分析查询");
        System.out.println("──────────────────────────────────────");
        
        // 情况 1：没有覆盖索引
        ExplainAnalyzer.analyze(
            "SELECT * FROM employees WHERE department='技术部'",
            false
        );
        
        // 情况 2：有覆盖索引
        ExplainAnalyzer.analyze(
            "SELECT id, department FROM employees WHERE department='技术部'",
            true
        );
        
        System.out.println("\n【判断技巧】");
        System.out.println("  ✅ Extra='Using index' → 使用了覆盖索引，无需回表");
        System.out.println("  ⚠️  Extra='Using where' → 需要回表");
    }
    
    /**
     * 演示 5：减少回表的优化策略
     */
    public void testOptimizationStrategies() {
        System.out.println("\n【演示 5】减少回表的优化策略");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n策略 1：使用覆盖索引");
        System.out.println("  优化前：SELECT * FROM employees WHERE department='技术部'");
        System.out.println("  优化后：SELECT id, department FROM employees WHERE department='技术部'");
        System.out.println("  效果：避免回表 ✅");
        
        System.out.println("\n策略 2：避免 SELECT *");
        System.out.println("  优化前：SELECT * FROM employees WHERE name='张三'");
        System.out.println("  优化后：SELECT id, name, age FROM employees WHERE name='张三'");
        System.out.println("  效果：减少回表列数 ✅");
        
        System.out.println("\n策略 3：合理设计联合索引");
        System.out.println("  场景：WHERE department='技术部' AND age>25");
        System.out.println("  索引：(department, age)");
        System.out.println("  效果：减少回表次数 ✅");
        
        System.out.println("\n策略 4：高频 SQL 优先优化");
        System.out.println("  方法：通过慢查询日志定位高成本 SQL");
        System.out.println("  工具：EXPLAIN 分析执行计划");
        System.out.println("  目标：减少回表成本 ✅");
    }
    
    /**
     * 演示 6：性能对比总结
     */
    public void printPerformanceComparison() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        回表性能对比总结                ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("┌─────────────┬──────────────┬──────────────┐");
        System.out.println("│   查询类型   │  回表次数     │   性能评估   │");
        System.out.println("├─────────────┼──────────────┼──────────────┤");
        System.out.println("│ 聚簇索引     │ 0 次          │ ⭐⭐⭐⭐⭐ 最优  │");
        System.out.println("│ 覆盖索引     │ 0 次          │ ⭐⭐⭐⭐⭐ 最优  │");
        System.out.println("│ 单条回表     │ 1 次          │ ⭐⭐⭐⭐ 较好   │");
        System.out.println("│ 少量回表     │ 2-5 次        │ ⭐⭐⭐ 一般    │");
        System.out.println("│ 大量回表     │ 10+ 次        │ ⭐⭐ 较差    │");
        System.out.println("│ 范围回表     │ N 次          │ ⭐ 最差      │");
        System.out.println("└─────────────┴──────────────┴──────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  回表会增加 B+Tree 查询次数和磁盘 I/O");
        System.out.println("  2️⃣  覆盖索引可以避免回表");
        System.out.println("  3️⃣  SELECT * 会增加回表成本");
        System.out.println("  4️⃣  联合索引要减少回表次数");
        System.out.println("  5️⃣  用 EXPLAIN 判断是否 Using index\n");
        
        System.out.println("【最佳实践】");
        System.out.println("  ✅ 能使用覆盖索引的尽量使用");
        System.out.println("  ✅ 避免 SELECT *，按需查询");
        System.out.println("  ✅ 高频 SQL 优先优化回表成本");
        System.out.println("  ✅ 定期分析慢查询日志");
        System.out.println("  ❌ 不要盲目追求索引覆盖\n");
    }
    
    public static void main(String[] args) {
        BookmarkLookupOptimization demo = new BookmarkLookupOptimization();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testNormalSecondaryIndex();
        demo.testCoveringIndex();
        demo.testRangeQueryCost();
        demo.testExplainAnalysis();
        demo.testOptimizationStrategies();
        demo.printPerformanceComparison();
    }
}
