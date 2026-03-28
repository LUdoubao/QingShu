package org.doubao.interview.mysql.q007.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EXPLAIN 执行计划关键字段详解演示
 * 
 * 【核心概念】
 * 1. EXPLAIN 作用：
 *    - 查看 SQL 的执行计划
 *    - 分析索引使用情况
 *    - 发现性能瓶颈
 * 
 * 2. 关键字段：
 *    - type：访问类型（最重要）
 *    - key：实际使用的索引
 *    - rows：预估扫描行数
 *    - filtered：过滤比例
 *    - Extra：额外信息
 * 
 * 3. 性能评估：
 *    - type 从好到坏：system > const > eq_ref > ref > range > index > ALL
 *    - rows 越少越好
 *    - Extra 中 Using filesort/Using temporary 要避免
 * 
 * @author Interview Demo
 */
public class ExplainKeyFieldsDemo {
    
    /**
     * 模拟数据行
     */
    static class Row {
        Long id;              // 主键
        String name;          // 姓名
        Integer age;          // 年龄
        String department;    // 部门
        Integer salary;       // 薪资
        
        public Row(Long id, String name, Integer age, String department, Integer salary) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.department = department;
            this.salary = salary;
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, name='%s', age=%d, dept='%s', salary=%d}", 
                id, name, age, department, salary);
        }
    }
    
    /**
     * 执行计划模拟器
     */
    static class ExecutionPlan {
        private String sql;
        private String type;              // 访问类型
        private String possibleKeys;      // 可能使用的索引
        private String key;               // 实际使用的索引
        private Integer rows;             // 预估扫描行数
        private Double filtered;          // 过滤比例
        private String extra;             // 额外信息
        private List<Row> data;           // 测试数据
        
        public ExecutionPlan(String sql, List<Row> data) {
            this.sql = sql;
            this.data = data;
        }
        
        /**
         * 设置执行计划参数
         */
        public void setParams(String type, String possibleKeys, String key, 
                             int rows, double filtered, String extra) {
            this.type = type;
            this.possibleKeys = possibleKeys;
            this.key = key;
            this.rows = rows;
            this.filtered = filtered;
            this.extra = extra;
        }
        
        /**
         * 打印 EXPLAIN 结果
         */
        public void printExplain() {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        EXPLAIN 执行计划分析            ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            System.out.println("SQL: " + sql);
            System.out.println();
            
            System.out.println("┌─────────────┬──────────────────────────────────┐");
            System.out.println("│   字段      │           值                     │");
            System.out.println("├─────────────┼──────────────────────────────────┤");
            System.out.printf("│ %-11s │ %-32s │%n", "id", "1");
            System.out.printf("│ %-11s │ %-32s │%n", "select_type", "SIMPLE");
            System.out.printf("│ %-11s │ %-32s │%n", "table", "employees");
            System.out.printf("│ %-11s │ %-32s │%n", "partitions", "NULL");
            System.out.printf("│ %-11s │ %-32s │%n", "type", getTypeDisplay());
            System.out.printf("│ %-11s │ %-32s │%n", "possible_keys", getOrNull(possibleKeys));
            System.out.printf("│ %-11s │ %-32s │%n", "key", getOrNull(key));
            System.out.printf("│ %-11s │ %-32s │%n", "key_len", "NULL");
            System.out.printf("│ %-11s │ %-32s │%n", "ref", "const");
            System.out.printf("│ %-11s │ %-32s │%n", "rows", String.valueOf(rows));
            System.out.printf("│ %-11s │ %-32s │%n", "filtered", String.format("%.2f%%", filtered));
            System.out.printf("│ %-11s │ %-32s │%n", "Extra", getOrNull(extra));
            System.out.println("└─────────────┴──────────────────────────────────┘");
            
            // 性能评估
            printPerformanceRating();
        }
        
        /**
         * 获取 type 字段的显示（带图标）
         */
        private String getTypeDisplay() {
            Map<String, String> typeIcons = new HashMap<>();
            typeIcons.put("system", "⭐⭐⭐⭐⭐ 最优");
            typeIcons.put("const", "⭐⭐⭐⭐⭐ 最优");
            typeIcons.put("eq_ref", "⭐⭐⭐⭐⭐ 最优");
            typeIcons.put("ref", "⭐⭐⭐⭐ 较好");
            typeIcons.put("range", "⭐⭐⭐ 一般");
            typeIcons.put("index", "⭐⭐ 较差");
            typeIcons.put("ALL", "⭐ 最差");
            
            String icon = typeIcons.getOrDefault(type, "");
            return type + (icon.isEmpty() ? "" : " " + icon);
        }
        
        /**
         * 处理 NULL 值显示
         */
        private String getOrNull(String value) {
            return value == null || value.isEmpty() ? "NULL" : value;
        }
        
        /**
         * 打印性能评估
         */
        private void printPerformanceRating() {
            System.out.println("\n【性能分析】");
            
            // Type 评级
            System.out.print("  • type: ");
            if ("system".equals(type) || "const".equals(type) || "eq_ref".equals(type)) {
                System.out.println("✅ 优秀（常量级查询）");
            } else if ("ref".equals(type) || "range".equals(type)) {
                System.out.println("⚠️  良好（索引查找）");
            } else if ("index".equals(type)) {
                System.out.println("❌ 较差（全索引扫描）");
            } else {
                System.out.println("❌ 最差（全表扫描）");
            }
            
            // Rows 评级
            System.out.print("  • rows: " + rows + " 行 - ");
            if (rows <= 10) {
                System.out.println("✅ 很少");
            } else if (rows <= 100) {
                System.out.println("⚠️  适中");
            } else if (rows <= 1000) {
                System.out.println("❌ 较多");
            } else {
                System.out.println("❌ 很多");
            }
            
            // Extra 分析
            if (extra != null && !extra.isEmpty()) {
                System.out.print("  • Extra: ");
                if (extra.contains("Using filesort")) {
                    System.out.println("⚠️  文件排序（成本高）");
                }
                if (extra.contains("Using temporary")) {
                    System.out.println("⚠️  临时表（成本高）");
                }
                if (extra.contains("Using index")) {
                    System.out.println("✅ 覆盖索引（优秀）");
                }
                if (extra.contains("Using where")) {
                    System.out.println("ℹ️  WHERE 过滤");
                }
            }
            
            System.out.println();
        }
        
        /**
         * 获取访问类型
         */
        public String getType() {
            return type;
        }
        
        /**
         * 获取扫描行数
         */
        public Integer getRows() {
            return rows;
        }
    }
    
    // ========== 测试场景 ==========
    
    private List<Row> testData = new ArrayList<>();
    
    /**
     * 初始化测试数据
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     EXPLAIN 关键字段详解演示            ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 准备测试数据
        testData.add(new Row(1L, "张三", 25, "技术部", 8000));
        testData.add(new Row(2L, "李四", 30, "产品部", 10000));
        testData.add(new Row(3L, "王五", 28, "技术部", 9000));
        testData.add(new Row(4L, "赵六", 35, "销售部", 12000));
        testData.add(new Row(5L, "孙七", 22, "技术部", 7000));
        testData.add(new Row(6L, "周八", 30, "人事部", 8500));
        testData.add(new Row(7L, "吴九", 27, "技术部", 8800));
        testData.add(new Row(8L, "郑十", 32, "财务部", 11000));
        testData.add(new Row(9L, "张三丰", 45, "技术部", 15000));
        testData.add(new Row(10L, "李明", 30, "产品部", 10500));
        
        System.out.println("【测试数据】10 条员工记录\n");
        for (Row row : testData) {
            System.out.println("  • " + row);
        }
        System.out.println();
        
        // 创建索引说明
        System.out.println("【索引配置】");
        System.out.println("  • PRIMARY KEY (id) - 主键索引");
        System.out.println("  • idx_department (department) - 部门索引");
        System.out.println("  • idx_age (age) - 年龄索引");
        System.out.println("  • idx_name (name) - 姓名索引");
        System.out.println();
    }
    
    /**
     * 场景 1：const 类型（最优）
     */
    public void testConstType() {
        System.out.println("【场景 1】type=const（最优）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT * FROM employees WHERE id = 1");
        System.out.println("说明：主键查询，常量级别");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT * FROM employees WHERE id = 1", testData);
        plan.setParams(
            "const",           // type
            "PRIMARY",         // possible_keys
            "PRIMARY",         // key
            1,                // rows
            100.0,            // filtered
            "NULL"            // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ✅ type=const：常量级查询，性能最优");
        System.out.println("  ✅ key=PRIMARY：使用主键索引");
        System.out.println("  ✅ rows=1：只扫描 1 行");
        System.out.println("  ℹ️  适用于主键或唯一索引的等值查询");
    }
    
    /**
     * 场景 2：ref 类型（较好）
     */
    public void testRefType() {
        System.out.println("\n【场景 2】type=ref（较好）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT * FROM employees WHERE department = '技术部'");
        System.out.println("说明：普通索引等值查询");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT * FROM employees WHERE department = '技术部'", testData);
        plan.setParams(
            "ref",            // type
            "idx_department", // possible_keys
            "idx_department", // key
            4,               // rows
            100.0,           // filtered
            "Using where"    // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ✅ type=ref：非唯一索引查找");
        System.out.println("  ✅ key=idx_department：使用部门索引");
        System.out.println("  ⚠️  rows=4：需要扫描 4 行（技术部有 4 人）");
        System.out.println("  ℹ️  Using where：需要 WHERE 过滤");
    }
    
    /**
     * 场景 3：range 类型（一般）
     */
    public void testRangeType() {
        System.out.println("\n【场景 3】type=range（一般）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT * FROM employees WHERE age BETWEEN 25 AND 30");
        System.out.println("说明：范围查询");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT * FROM employees WHERE age BETWEEN 25 AND 30", testData);
        plan.setParams(
            "range",         // type
            "idx_age",       // possible_keys
            "idx_age",       // key
            6,              // rows
            100.0,          // filtered
            "Using where"   // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ⚠️  type=range：范围查询，性能一般");
        System.out.println("  ✅ key=idx_age：使用了年龄索引");
        System.out.println("  ⚠️  rows=6：需要扫描 6 行");
        System.out.println("  ℹ️  范围查询比等值查询慢");
    }
    
    /**
     * 场景 4：ALL 类型（最差）
     */
    public void testAllType() {
        System.out.println("\n【场景 4】type=ALL（最差）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT * FROM employees WHERE name LIKE '%三%'");
        System.out.println("说明：前导 % 模糊查询，索引失效");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT * FROM employees WHERE name LIKE '%三%'", testData);
        plan.setParams(
            "ALL",           // type
            "NULL",          // possible_keys
            "NULL",          // key
            10,             // rows
            10.0,           // filtered
            "Using where"   // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ❌ type=ALL：全表扫描，性能最差");
        System.out.println("  ❌ key=NULL：未使用任何索引");
        System.out.println("  ❌ rows=10：扫描全部 10 行");
        System.out.println("  ⚠️  前导 % 导致索引失效");
    }
    
    /**
     * 场景 5：Using filesort（文件排序）
     */
    public void testUsingFilesort() {
        System.out.println("\n【场景 5】Extra=Using filesort（文件排序）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT * FROM employees ORDER BY salary DESC");
        System.out.println("说明：无索引排序，需要文件排序");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT * FROM employees ORDER BY salary DESC", testData);
        plan.setParams(
            "ALL",           // type
            "NULL",          // possible_keys
            "NULL",          // key
            10,             // rows
            100.0,          // filtered
            "Using filesort" // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ❌ type=ALL：全表扫描");
        System.out.println("  ❌ Extra=Using filesort：需要外部排序");
        System.out.println("  ⚠️  文件排序成本很高（磁盘 I/O）");
        System.out.println("  💡 优化：在 salary 上创建索引");
    }
    
    /**
     * 场景 6：Using temporary（临时表）
     */
    public void testUsingTemporary() {
        System.out.println("\n【场景 6】Extra=Using temporary（临时表）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT department, COUNT(*) FROM employees GROUP BY department");
        System.out.println("说明：GROUP BY 需要临时表");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT department, COUNT(*) FROM employees GROUP BY department", testData);
        plan.setParams(
            "ALL",              // type
            "NULL",             // possible_keys
            "NULL",             // key
            10,                // rows
            100.0,             // filtered
            "Using temporary"   // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ❌ type=ALL：全表扫描");
        System.out.println("  ❌ Extra=Using temporary：需要临时表");
        System.out.println("  ⚠️  临时表成本高（内存/磁盘）");
        System.out.println("  💡 优化：考虑在 GROUP BY 列上创建索引");
    }
    
    /**
     * 场景 7：Using index（覆盖索引）
     */
    public void testUsingIndex() {
        System.out.println("\n【场景 7】Extra=Using index（覆盖索引）");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\nSQL: SELECT id, name FROM employees WHERE name = '张三'");
        System.out.println("说明：覆盖索引，无需回表");
        
        ExecutionPlan plan = new ExecutionPlan(
            "SELECT id, name FROM employees WHERE name = '张三'", testData);
        plan.setParams(
            "ref",            // type
            "idx_name",       // possible_keys
            "idx_name",       // key
            1,               // rows
            100.0,           // filtered
            "Using index"     // Extra
        );
        
        plan.printExplain();
        
        System.out.println("【解读要点】");
        System.out.println("  ✅ type=ref：索引查找");
        System.out.println("  ✅ Extra=Using index：覆盖索引");
        System.out.println("  ✅ 无需回表，性能优秀");
        System.out.println("  ℹ️  查询的列都在索引中");
    }
    
    /**
     * 总结对比
     */
    public void printSummary() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     EXPLAIN 字段重要性总结              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("【关键字段优先级】");
        System.out.println("  1️⃣  type - 访问类型（最重要）");
        System.out.println("  2️⃣  key - 实际使用的索引");
        System.out.println("  3️⃣  rows - 预估扫描行数");
        System.out.println("  4️⃣  Extra - 额外信息");
        System.out.println("  5️⃣  filtered - 过滤比例");
        System.out.println();
        
        System.out.println("【type 字段性能排名】");
        System.out.println("┌─────────────┬──────────────┐");
        System.out.println("│   type      │   性能评估   │");
        System.out.println("├─────────────┼──────────────┤");
        System.out.println("│ system      │ ⭐⭐⭐⭐⭐ 最优  │");
        System.out.println("│ const       │ ⭐⭐⭐⭐⭐ 最优  │");
        System.out.println("│ eq_ref      │ ⭐⭐⭐⭐⭐ 最优  │");
        System.out.println("│ ref         │ ⭐⭐⭐⭐ 较好   │");
        System.out.println("│ range       │ ⭐⭐⭐ 一般    │");
        System.out.println("│ index       │ ⭐⭐ 较差     │");
        System.out.println("│ ALL         │ ⭐ 最差       │");
        System.out.println("└─────────────┴──────────────┘\n");
        
        System.out.println("【Extra 字段关键信息】");
        System.out.println("┌─────────────────────┬──────────────┐");
        System.out.println("│   Extra 值          │   含义       │");
        System.out.println("├─────────────────────┼──────────────┤");
        System.out.println("│ Using index         │ ✅ 覆盖索引  │");
        System.out.println("│ Using where         │ ℹ️  WHERE 过滤│");
        System.out.println("│ Using filesort      │ ⚠️  文件排序  │");
        System.out.println("│ Using temporary     │ ⚠️  临时表    │");
        System.out.println("│ Using index condition│ ⚠️  索引下推  │");
        System.out.println("└─────────────────────┴──────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  type 决定查询效率，const/ref/range 较好");
        System.out.println("  2️⃣  key 看实际用的索引，不是 possible_keys");
        System.out.println("  3️⃣  rows 越小越好，反映扫描成本");
        System.out.println("  4️⃣  Extra 中 Using filesort/temporary 要避免");
        System.out.println("  5️⃣  Using index 表示覆盖索引，是好事");
        System.out.println("  6️⃣  filtered 越高越好，最高 100%");
        System.out.println();
        
        System.out.println("【最佳实践】");
        System.out.println("  ✅ 查询前先用 EXPLAIN 分析");
        System.out.println("  ✅ 优先关注 type 和 key");
        System.out.println("  ✅ rows 太大要考虑优化");
        System.out.println("  ✅ 避免 Using filesort 和 Using temporary");
        System.out.println("  ✅ 争取 Using index（覆盖索引）");
        System.out.println("  ❌ 不要只看 possible_keys");
        System.out.println();
    }
    
    public static void main(String[] args) {
        ExplainKeyFieldsDemo demo = new ExplainKeyFieldsDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testConstType();
        demo.testRefType();
        demo.testRangeType();
        demo.testAllType();
        demo.testUsingFilesort();
        demo.testUsingTemporary();
        demo.testUsingIndex();
        demo.printSummary();
    }
}
