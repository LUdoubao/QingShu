package org.doubao.interview.mysql.q003.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 聚簇索引和二级索引对比演示
 * 
 * 【核心概念】
 * 1. 聚簇索引（Clustered Index）：
 *    - InnoDB 的主键索引就是聚簇索引
 *    - 叶子节点直接存储完整的行数据
 *    - 一张表只能有一个聚簇索引
 *    - 数据按照主键顺序物理存储
 * 
 * 2. 二级索引（Secondary Index）：
 *    - 也叫非聚簇索引、辅助索引
 *    - 叶子节点存储"索引列的值 + 主键值"
 *    - 一张表可以有多个二级索引
 *    - 查询时通常需要"回表"
 * 
 * 【回表过程】
 * 1. 先在二级索引中找到对应的主键值
 * 2. 再到聚簇索引中根据主键查找完整数据
 * 3. 这个过程叫"回表"（Bookmark Lookup）
 * 
 * 【性能影响】
 * - 聚簇索引：查询快（直接拿到数据），但插入受主键顺序影响
 * - 二级索引：需要回表，多一次 B+Tree 查询
 * - 过多二级索引会影响写入性能（每个索引都要维护 B+Tree）
 * 
 * @author Interview Demo
 */
public class ClusteredVsSecondaryIndex {
    
    /**
     * 模拟数据行
     */
    static class Row {
        Long id;          // 主键
        String name;      // 姓名
        Integer age;      // 年龄
        String email;     // 邮箱
        
        public Row(Long id, String name, Integer age, String email) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.email = email;
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, name='%s', age=%d, email='%s'}", id, name, age, email);
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
        private int bPlusTreeNodeSize = 3;  // 模拟 B+Tree 节点大小
        private int splitCount = 0;  // 分裂次数
        
        /**
         * 插入数据（模拟 B+Tree 插入）
         * 
         * 【时间复杂度】O(log_N n)
         * 【空间复杂度】O(n)
         * 
         * @param row 要插入的行
         */
        public void insert(Row row) {
            System.out.println("[聚簇索引] 插入数据：" + row);
            
            if (data.containsKey(row.id)) {
                System.out.println("  ⚠️  主键冲突，更新数据");
            } else {
                System.out.println("  ✅ 插入成功，当前数据量：" + (data.size() + 1));
            }
            
            data.put(row.id, row);
            
            // 模拟 B+Tree 分裂（简化演示）
            if (data.size() % bPlusTreeNodeSize == 0) {
                splitCount++;
                System.out.println("  🔀 B+Tree 发生分裂（模拟），分裂次数：" + splitCount);
            }
        }
        
        /**
         * 根据主键查询整行数据
         * 
         * 【核心优势】直接获取完整数据，无需回表
         * 【时间复杂度】O(log_N n)
         * 
         * @param id 主键 ID
         * @return 行数据，不存在返回 null
         */
        public Row getById(Long id) {
            System.out.println("[聚簇索引] 查询 id=" + id);
            Row row = data.get(id);
            if (row != null) {
                System.out.println("  ✅ 找到：" + row + " （无需回表）");
            } else {
                System.out.println("  ❌ 未找到");
            }
            return row;
        }
        
        /**
         * 范围查询
         * 
         * 【核心优势】聚簇索引按主键顺序存储，范围查询高效
         * 
         * @param startId 起始 ID
         * @param endId 结束 ID
         * @return 范围内的所有行
         */
        public List<Row> rangeQuery(Long startId, Long endId) {
            System.out.println("[聚簇索引] 范围查询 [" + startId + ", " + endId + "]");
            
            List<Row> result = new ArrayList<>();
            for (Map.Entry<Long, Row> entry : data.entrySet()) {
                if (entry.getKey() >= startId && entry.getKey() <= endId) {
                    result.add(entry.getValue());
                    System.out.print(entry.getKey() + " ");
                }
            }
            System.out.println();
            
            System.out.println("  ✅ 找到 " + result.size() + " 条记录（连续扫描，高效）");
            return result;
        }
        
        /**
         * 打印聚簇索引结构
         */
        public void printStructure() {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║      聚簇索引结构（B+Tree）            ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            System.out.println("\n【叶子节点存储】完整行数据");
            System.out.println("┌─────────────────────────────────────┐");
            for (Map.Entry<Long, Row> entry : data.entrySet()) {
                System.out.println("│ " + String.format("%-3d", entry.getKey()) + " → " + entry.getValue().toString().substring(4));
            }
            System.out.println("└─────────────────────────────────────┘");
            
            System.out.println("\n特点：");
            System.out.println("  ✅ 叶子节点直接存整行数据");
            System.out.println("  ✅ 数据按主键有序存储");
            System.out.println("  ✅ 查询无需回表");
            System.out.println("  ℹ️  分裂次数：" + splitCount);
            System.out.println();
        }
    }
    
    /**
     * 二级索引模拟器
     * 
     * 【实现原理】
     * - 使用 TreeMap 模拟 B+Tree（有序）
     * - Key 是索引列的值，Value 是主键 ID 列表
     * - 叶子节点只存储"索引列 + 主键"，不存完整数据
     * - 需要回表才能获取完整数据
     */
    static class SecondaryIndex {
        private String indexName;  // 索引名称
        private Map<Object, List<Long>> indexData = new TreeMap<>();  // 索引列值 → 主键列表
        private ClusteredIndex clusteredIndex;  // 关联的聚簇索引（用于回表）
        private int splitCount = 0;
        
        public SecondaryIndex(String indexName, ClusteredIndex clusteredIndex) {
            this.indexName = indexName;
            this.clusteredIndex = clusteredIndex;
        }
        
        /**
         * 创建索引（批量插入数据）
         * 
         * @param rows 要创建索引的数据行
         * @param keyExtractor 提取索引列的函数
         */
        public void createIndex(List<Row> rows, java.util.function.Function<Row, Object> keyExtractor) {
            System.out.println("[二级索引] 创建索引 '" + indexName + "'...");
            
            for (Row row : rows) {
                Object keyValue = keyExtractor.apply(row);
                indexData.computeIfAbsent(keyValue, k -> new ArrayList<>()).add(row.id);
            }
            
            System.out.println("  ✅ 索引创建完成，唯一索引值：" + indexData.size());
        }
        
        /**
         * 根据索引列查询（需要回表）
         * 
         * 【回表过程】
         * 1. 在二级索引中找到主键 ID
         * 2. 到聚簇索引中根据 ID 获取完整数据
         * 
         * 【时间复杂度】O(log_N n + k*m)
         *   - log_N n: 查询二级索引
         *   - k*m: k 个匹配的主键，每个回表 m 次 I/O
         * 
         * @param keyValue 索引列的值
         * @return 匹配的行列表
         */
        public List<Row> query(Object keyValue) {
            System.out.println("[二级索引:" + indexName + "] 查询 " + keyValue);
            
            List<Long> primaryKeys = indexData.get(keyValue);
            
            if (primaryKeys == null || primaryKeys.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录");
                return new ArrayList<>();
            }
            
            System.out.println("  📖 二级索引找到主键：" + primaryKeys + " （需要回表）");
            
            // 回表查询
            List<Row> result = new ArrayList<>();
            for (Long pk : primaryKeys) {
                System.out.println("    ↩️  回表查询主键 id=" + pk);
                Row row = clusteredIndex.getById(pk);
                if (row != null) {
                    result.add(row);
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
            System.out.println("[二级索引:" + indexName + "] 范围查询 [" + startKey + ", " + endKey + "]");
            
            List<Row> result = new ArrayList<>();
            List<Long> primaryKeys = new ArrayList<>();
            
            // 在索引中找到范围内的主键
            for (Map.Entry<Object, List<Long>> entry : indexData.entrySet()) {
                Object key = entry.getKey();
                if (compare(key, startKey) >= 0 && compare(key, endKey) <= 0) {
                    primaryKeys.addAll(entry.getValue());
                    System.out.print(key + "(" + entry.getValue().size() + ") ");
                }
            }
            System.out.println();
            
            System.out.println("  📖 找到主键列表：" + primaryKeys + " （需要回表）");
            
            // 回表查询
            for (Long pk : primaryKeys) {
                Row row = clusteredIndex.getById(pk);
                if (row != null) {
                    result.add(row);
                }
            }
            
            System.out.println("  ✅ 回表完成，找到 " + result.size() + " 条记录");
            return result;
        }
        
        /**
         * 比较两个对象（简化实现）
         */
        @SuppressWarnings("unchecked")
        private int compare(Object o1, Object o2) {
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                return ((Comparable) o1).compareTo(o2);
            }
            return 0;
        }
        
        /**
         * 打印二级索引结构
         */
        public void printStructure() {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   二级索引结构（" + indexName + "）              ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            System.out.println("\n【叶子节点存储】索引列值 + 主键 ID");
            System.out.println("┌─────────────────────────────────────┐");
            for (Map.Entry<Object, List<Long>> entry : indexData.entrySet()) {
                System.out.println("│ " + String.format("%-10s", entry.getKey()) + " → " + entry.getValue());
            }
            System.out.println("└─────────────────────────────────────┘");
            
            System.out.println("\n特点：");
            System.out.println("  ⚠️  叶子节点只存索引列 + 主键");
            System.out.println("  ⚠️  查询需要回表获取完整数据");
            System.out.println("  ℹ️  可以有多个二级索引");
            System.out.println();
        }
    }
    
    // ========== 测试数据和方法 ==========
    
    private ClusteredIndex clusteredIndex;
    private SecondaryIndex nameIndex;
    private SecondaryIndex ageIndex;
    private List<Row> allRows = new ArrayList<>();
    
    /**
     * 初始化测试数据
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     聚簇索引 vs 二级索引演示            ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 创建聚簇索引
        clusteredIndex = new ClusteredIndex();
        
        // 准备测试数据
        allRows.add(new Row(1L, "张三", 25, "zhangsan@example.com"));
        allRows.add(new Row(2L, "李四", 30, "lisi@example.com"));
        allRows.add(new Row(3L, "王五", 28, "wangwu@example.com"));
        allRows.add(new Row(4L, "赵六", 35, "zhaoliu@example.com"));
        allRows.add(new Row(5L, "孙七", 22, "sunqi@example.com"));
        allRows.add(new Row(6L, "周八", 30, "zhouba@example.com"));
        allRows.add(new Row(7L, "吴九", 27, "wujiu@example.com"));
        allRows.add(new Row(8L, "郑十", 32, "zhengshi@example.com"));
        
        System.out.println("【步骤 1】向聚簇索引中插入数据\n");
        for (Row row : allRows) {
            clusteredIndex.insert(row);
        }
        
        System.out.println("\n【步骤 2】创建二级索引\n");
        
        // 创建 name 二级索引
        nameIndex = new SecondaryIndex("idx_name", clusteredIndex);
        nameIndex.createIndex(allRows, r -> r.name);
        
        // 创建 age 二级索引
        ageIndex = new SecondaryIndex("idx_age", clusteredIndex);
        ageIndex.createIndex(allRows, r -> r.age);
        
        System.out.println();
    }
    
    /**
     * 演示 1：聚簇索引查询（无需回表）
     */
    public void testClusteredIndexQuery() {
        System.out.println("【演示 1】聚簇索引查询（主键查询）");
        System.out.println("──────────────────────────────────────");
        
        // 查询 id=3 的数据
        Row row = clusteredIndex.getById(3L);
        
        System.out.println("\n结论：");
        System.out.println("  ✅ 聚簇索引直接存储完整数据");
        System.out.println("  ✅ 查询只需一次 B+Tree 查找");
        System.out.println("  ✅ 无需回表，性能最优\n");
    }
    
    /**
     * 演示 2：二级索引查询（需要回表）
     */
    public void testSecondaryIndexQuery() {
        System.out.println("【演示 2】二级索引查询（需要回表）");
        System.out.println("──────────────────────────────────────");
        
        // 通过 name 查询（name='李四'）
        System.out.println("场景：查询 name='李四' 的完整信息\n");
        List<Row> result = nameIndex.query("李四");
        
        System.out.println("\n回表过程分析：");
        System.out.println("  1️⃣  在 idx_name 索引中找到 '李四' → 得到主键 id=2");
        System.out.println("  2️⃣  到聚簇索引中查询 id=2 → 获取完整数据");
        System.out.println("  ⚠️  比聚簇索引多一次 B+Tree 查询\n");
    }
    
    /**
     * 演示 3：二级索引范围查询（多次回表）
     */
    public void testSecondaryIndexRangeQuery() {
        System.out.println("【演示 3】二级索引范围查询（多次回表）");
        System.out.println("──────────────────────────────────────");
        
        // 查询年龄在 25-30 之间的用户
        System.out.println("场景：查询 age BETWEEN 25 AND 30 的用户\n");
        List<Row> result = ageIndex.rangeQuery(25, 30);
        
        System.out.println("\n结果：");
        for (Row row : result) {
            System.out.println("  • " + row);
        }
        
        System.out.println("\n性能分析：");
        System.out.println("  • 二级索引找到 3 个不同的 age 值");
        System.out.println("  • 共 5 个主键需要回表");
        System.out.println("  • 需要 5 次回表查询");
        System.out.println("  ⚠️  回表成本高\n");
    }
    
    /**
     * 演示 4：覆盖索引优化（避免回表）
     */
    public void testCoveringIndex() {
        System.out.println("【演示 4】覆盖索引优化（避免回表）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("场景：只查询 name 和 age，不需要其他字段\n");
        
        // 直接在二级索引中获取数据（不回表）
        System.out.println("SQL: SELECT name, age FROM users WHERE name='李四'");
        System.out.println("优化：直接从 idx_name 获取，无需回表！");
        System.out.println("  ✅ 如果查询的列都在索引中，可以不用回表");
        System.out.println("  ✅ 这就是'覆盖索引'优化\n");
    }
    
    /**
     * 演示 5：打印索引结构对比
     */
    public void printIndexStructures() {
        System.out.println("【演示 5】索引结构对比");
        System.out.println("──────────────────────────────────────\n");
        
        // 打印聚簇索引结构
        clusteredIndex.printStructure();
        
        // 打印二级索引结构
        nameIndex.printStructure();
        ageIndex.printStructure();
    }
    
    /**
     * 演示 6：总结对比
     */
    public void printSummary() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   聚簇索引 vs 二级索引 对比总结         ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("┌─────────────┬──────────────┬──────────────┐");
        System.out.println("│   特性      │  聚簇索引     │   二级索引    │");
        System.out.println("├─────────────┼──────────────┼──────────────┤");
        System.out.println("│ 叶子节点    │ 完整行数据   │ 索引列 + 主键  │");
        System.out.println("│ 数量        │ 只有 1 个      │ 可以有多个   │");
        System.out.println("│ 回表        │ 不需要       │ 通常需要     │");
        System.out.println("│ 查询速度    │ 快           │ 较慢         │");
        System.out.println("│ 存储开销    │ 大           │ 小           │");
        System.out.println("│ 适用场景    │ 主键查询     │ 非主键查询   │");
        System.out.println("└─────────────┴──────────────┴──────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  InnoDB 只有 1 个聚簇索引（主键索引）");
        System.out.println("  2️⃣  二级索引查询通常需要回表");
        System.out.println("  3️⃣  覆盖索引可以避免回表");
        System.out.println("  4️⃣  过多二级索引会影响写入性能");
        System.out.println("  5️⃣  主键设计影响数据物理存储顺序\n");
        
        System.out.println("【最佳实践】");
        System.out.println("  ✅ 选择唯一、递增的列作为主键（如自增 ID）");
        System.out.println("  ✅ 避免过多二级索引（一般不超过 5 个）");
        System.out.println("  ✅ 尽量使用覆盖索引减少回表");
        System.out.println("  ✅ 范围查询优先使用聚簇索引\n");
    }
    
    public static void main(String[] args) {
        ClusteredVsSecondaryIndex demo = new ClusteredVsSecondaryIndex();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testClusteredIndexQuery();
        demo.testSecondaryIndexQuery();
        demo.testSecondaryIndexRangeQuery();
        demo.testCoveringIndex();
        demo.printIndexStructures();
        demo.printSummary();
    }
}
