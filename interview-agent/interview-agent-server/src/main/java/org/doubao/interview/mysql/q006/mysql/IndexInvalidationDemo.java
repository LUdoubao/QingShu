package org.doubao.interview.mysql.q006.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 索引失效常见场景演示
 * 
 * 【核心概念】
 * 1. 索引失效：
 *    - 查询无法利用索引的快速查找能力
 *    - 退化为全表扫描 O(n)
 *    - 性能严重下降
 * 
 * 2. 常见失效场景：
 *    - 索引列参与函数或表达式
 *    - 隐式类型转换
 *    - 前导 % 模糊查询
 *    - 联合索引不满足最左前缀
 *    - 使用 OR 导致优化器放弃索引
 * 
 * 3. 判断方法：
 *    - 使用 EXPLAIN 查看执行计划
 *    - 观察 key/type/rows 字段
 * 
 * @author Interview Demo
 */
public class IndexInvalidationDemo {
    
    /**
     * 模拟数据行
     */
    static class Row {
        Long id;              // 主键
        String phone;         // 手机号（varchar）
        String name;          // 姓名
        Integer age;          // 年龄
        String email;         // 邮箱
        java.util.Date createTime; // 创建时间
        
        public Row(Long id, String phone, String name, Integer age, String email, java.util.Date createTime) {
            this.id = id;
            this.phone = phone;
            this.name = name;
            this.age = age;
            this.email = email;
            this.createTime = createTime;
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, phone='%s', name='%s', age=%d}", id, phone, name, age);
        }
    }
    
    /**
     * 索引模拟器
     */
    static class Index {
        private String indexName;
        private Map<String, List<Long>> indexData = new TreeMap<>();
        private List<Row> fullTable;
        private int scanCount = 0;
        private boolean isFullScan = false;
        
        public Index(String indexName, List<Row> rows) {
            this.indexName = indexName;
            this.fullTable = rows;
        }
        
        /**
         * 创建索引
         */
        public void createIndex(java.util.function.Function<Row, String> keyExtractor) {
            System.out.println("[索引] 创建索引 '" + indexName + "'...");
            
            for (Row row : fullTable) {
                String key = keyExtractor.apply(row);
                indexData.computeIfAbsent(key, k -> new ArrayList<>()).add(row.id);
            }
            
            System.out.println("  ✅ 完成，索引项数：" + indexData.size());
        }
        
        /**
         * 精确匹配（使用索引）
         */
        public List<Row> exactMatch(String keyValue) {
            scanCount++;
            System.out.println("\n[查询] 精确匹配：" + keyValue);
            
            List<Long> ids = indexData.get(keyValue);
            if (ids == null || ids.isEmpty()) {
                System.out.println("  ❌ 未找到匹配记录（索引查找）");
                return new ArrayList<>();
            }
            
            System.out.println("  ✅ 找到 " + ids.size() + " 条记录（索引查找 O(log_N n)）");
            
            List<Row> result = new ArrayList<>();
            for (Long id : ids) {
                for (Row row : fullTable) {
                    if (row.id.equals(id)) {
                        result.add(row);
                        break;
                    }
                }
            }
            
            return result;
        }
        
        /**
         * 范围匹配（使用索引）
         */
        public List<Row> rangeMatch(String startKey, String endKey) {
            scanCount++;
            System.out.println("\n[查询] 范围匹配：[" + startKey + ", " + endKey + "]");
            
            List<Row> result = new ArrayList<>();
            int matched = 0;
            
            for (Map.Entry<String, List<Long>> entry : indexData.entrySet()) {
                String key = entry.getKey();
                if (key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                    for (Long id : entry.getValue()) {
                        for (Row row : fullTable) {
                            if (row.id.equals(id)) {
                                result.add(row);
                                matched++;
                                break;
                            }
                        }
                    }
                } else if (key.compareTo(endKey) > 0) {
                    break;
                }
            }
            
            System.out.println("  ✅ 找到 " + matched + " 条记录（索引范围扫描）");
            return result;
        }
        
        /**
         * 全表扫描（索引失效）
         */
        public List<Row> fullTableScan(java.util.function.Predicate<Row> condition) {
            scanCount++;
            isFullScan = true;
            System.out.println("\n[查询] 全表扫描（索引失效）");
            
            List<Row> result = new ArrayList<>();
            for (Row row : fullTable) {
                if (condition.test(row)) {
                    result.add(row);
                }
            }
            
            System.out.println("  ⚠️  扫描全部 " + fullTable.size() + " 条记录（O(n)）");
            return result;
        }
        
        /**
         * 打印执行计划（模拟 EXPLAIN）
         */
        public void printExplain(String sql, boolean useIndex) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        EXPLAIN 执行计划分析            ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            System.out.println("\nSQL: " + sql);
            System.out.println("\n┌─────────────┬──────────────────────────┐");
            System.out.println("│   属性      │          值              │");
            System.out.println("├─────────────┼──────────────────────────┤");
            System.out.println("│ id          │ 1                        │");
            System.out.println("│ type        │ " + (useIndex ? "ref / range" : "ALL（全表扫描）") + " │");
            System.out.println("│ key         │ " + (useIndex ? indexName : "NULL（无索引）") + " │");
            System.out.println("│ rows        │ " + (useIndex ? "少量" : fullTable.size()) + " │");
            System.out.println("│ Extra       │ " + (useIndex ? "Using index condition" : "Using where") + " │");
            System.out.println("└─────────────┴──────────────────────────┘");
            
            if (!useIndex) {
                System.out.println("\n⚠️  索引失效，建议优化！");
            }
        }
        
        /**
         * 获取扫描次数
         */
        public int getScanCount() {
            return scanCount;
        }
        
        /**
         * 是否全表扫描
         */
        public boolean isFullScan() {
            return isFullScan;
        }
        
        /**
         * 重置统计
         */
        public void resetStats() {
            scanCount = 0;
            isFullScan = false;
        }
    }
    
    // ========== 测试场景 ==========
    
    private Index idx_phone;
    private Index idx_name;
    private Index idx_create_time;
    private List<Row> testData = new ArrayList<>();
    
    /**
     * 初始化测试数据
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     索引失效常见场景演示                ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 准备测试数据
        testData.add(new Row(1L, "13800138000", "张三", 25, "zhangsan@example.com", new java.util.Date()));
        testData.add(new Row(2L, "13800138001", "李四", 30, "lisi@example.com", new java.util.Date()));
        testData.add(new Row(3L, "13800138002", "王五", 28, "wangwu@example.com", new java.util.Date()));
        testData.add(new Row(4L, "13900139000", "赵六", 35, "zhaoliu@example.com", new java.util.Date()));
        testData.add(new Row(5L, "13900139001", "孙七", 22, "sunqi@example.com", new java.util.Date()));
        testData.add(new Row(6L, "13900139002", "周八", 30, "zhouba@example.com", new java.util.Date()));
        testData.add(new Row(7L, "13900139003", "吴九", 27, "wujiu@example.com", new java.util.Date()));
        testData.add(new Row(8L, "13900139004", "郑十", 32, "zhengshi@example.com", new java.util.Date()));
        testData.add(new Row(9L, "13900139005", "张三丰", 45, "zhangsanfeng@example.com", new java.util.Date()));
        testData.add(new Row(10L, "13900139006", "李明", 30, "liming@example.com", new java.util.Date()));
        
        System.out.println("【步骤 1】准备测试数据（10 条记录）\n");
        for (Row row : testData) {
            System.out.println("  • " + row);
        }
        
        System.out.println("\n【步骤 2】创建索引\n");
        
        // 创建 phone 索引（varchar 类型）
        idx_phone = new Index("idx_phone", testData);
        idx_phone.createIndex(r -> r.phone);
        
        // 创建 name 索引
        idx_name = new Index("idx_name", testData);
        idx_name.createIndex(r -> r.name);
        
        // 创建 create_time 索引
        idx_create_time = new Index("idx_create_time", testData);
        idx_create_time.createIndex(r -> String.valueOf(r.createTime.getTime()));
        
        System.out.println();
    }
    
    /**
     * 场景 1：隐式类型转换导致索引失效
     */
    public void testImplicitTypeConversion() {
        System.out.println("【场景 1】隐式类型转换导致索引失效");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n❌ 错误写法：phone 是 varchar，却用数字比较");
        String sql1 = "SELECT * FROM users WHERE phone = 13800138000";
        idx_phone.printExplain(sql1, false);
        
        System.out.println("\n✅ 正确写法：使用字符串常量");
        String sql2 = "SELECT * FROM users WHERE phone = '13800138000'";
        idx_phone.printExplain(sql2, true);
        idx_phone.exactMatch("13800138000");
        
        System.out.println("\n【原理分析】");
        System.out.println("  • MySQL 会将 phone 列的值转换为数字进行比较");
        System.out.println("  • 相当于对索引列做了函数操作：CAST(phone AS SIGNED)");
        System.out.println("  • 索引失效，退化为全表扫描");
        System.out.println("\n【最佳实践】");
        System.out.println("  ✅ 字符串类型要加引号");
        System.out.println("  ✅ 避免在索引列上进行类型转换");
    }
    
    /**
     * 场景 2：函数包裹导致索引失效
     */
    public void testFunctionOnIndex() {
        System.out.println("\n【场景 2】函数包裹导致索引失效");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n❌ 错误写法：在索引列上使用函数");
        String sql1 = "SELECT * FROM users WHERE DATE(create_time) = '2024-01-01'";
        idx_create_time.printExplain(sql1, false);
        
        System.out.println("\n✅ 正确写法：直接比较，避免函数");
        String sql2 = "SELECT * FROM users WHERE create_time >= '2024-01-01 00:00:00' AND create_time < '2024-01-02 00:00:00'";
        idx_create_time.printExplain(sql2, true);
        
        System.out.println("\n【原理分析】");
        System.out.println("  • DATE(create_time) 会对每一行进行计算");
        System.out.println("  • 无法利用索引的有序性");
        System.out.println("  • 退化为全表扫描");
        System.out.println("\n【类似场景】");
        System.out.println("  ❌ WHERE YEAR(create_time) = 2024");
        System.out.println("  ❌ WHERE SUBSTR(phone, 1, 3) = '138'");
        System.out.println("  ❌ WHERE LOWER(email) = 'test@example.com'");
    }
    
    /**
     * 场景 3：前导 % 模糊查询导致索引失效
     */
    public void testLeadingWildcardLike() {
        System.out.println("\n【场景 3】前导 % 模糊查询导致索引失效");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n❌ 错误写法：前导 % 无法利用索引");
        String sql1 = "SELECT * FROM users WHERE name LIKE '%三%'";
        idx_name.printExplain(sql1, false);
        
        System.out.println("\n✅ 正确写法：后缀 % 可以利用索引");
        String sql2 = "SELECT * FROM users WHERE name LIKE '张%'";
        idx_name.printExplain(sql2, true);
        idx_name.rangeMatch("张", "张~");
        
        System.out.println("\n【原理分析】");
        System.out.println("  • 索引按前缀排序，'张' 开头的在一起");
        System.out.println("  • '%三%' 需要扫描所有记录");
        System.out.println("  • '张%' 可以直接定位到'张'的范围");
        System.out.println("\n【最佳实践】");
        System.out.println("  ✅ 尽量使用 'abc%' 形式");
        System.out.println("  ❌ 避免使用 '%abc%' 或 '%abc'");
        System.out.println("  ⚠️  如果必须用 '%abc%'，考虑全文索引");
    }
    
    /**
     * 场景 4：联合索引不满足最左前缀
     */
    public void testCompositeIndexViolation() {
        System.out.println("\n【场景 4】联合索引不满足最左前缀");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n假设有联合索引：idx_dept_age(dept_id, age)");
        
        System.out.println("\n✅ 符合最左前缀：WHERE dept_id=1");
        System.out.println("  → 可以使用索引");
        
        System.out.println("\n✅ 符合最左前缀：WHERE dept_id=1 AND age=25");
        System.out.println("  → 可以使用完整索引");
        
        System.out.println("\n❌ 跳过最左列：WHERE age=25");
        System.out.println("  → 索引失效，全表扫描");
        
        System.out.println("\n【原理分析】");
        System.out.println("  • 联合索引按 (dept_id, age) 排序");
        System.out.println("  • 先按 dept_id 排序，再按 age 排序");
        System.out.println("  • 跳过 dept_id 直接查 age，age 是无序的");
        System.out.println("\n【最佳实践】");
        System.out.println("  ✅ 遵循最左前缀原则");
        System.out.println("  ✅ 把过滤性强的列放前面");
    }
    
    /**
     * 场景 5：OR 条件导致索引失效
     */
    public void testOrCondition() {
        System.out.println("\n【场景 5】OR 条件导致索引失效");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n❌ 错误写法：OR 连接非索引列");
        String sql1 = "SELECT * FROM users WHERE phone='13800138000' OR email='test@example.com'";
        System.out.println("SQL: " + sql1);
        System.out.println("  • phone 有索引，但 email 没有索引");
        System.out.println("  • 优化器可能放弃索引，选择全表扫描");
        
        System.out.println("\n✅ 正确写法：两个列都有索引");
        String sql2 = "SELECT * FROM users WHERE phone='13800138000' OR name='张三'";
        System.out.println("SQL: " + sql2);
        System.out.println("  • phone 和 name 都有索引");
        System.out.println("  • 可能使用 index_merge 优化");
        
        System.out.println("\n✅ 更好的写法：使用 UNION");
        String sql3 = "SELECT * FROM users WHERE phone='13800138000' UNION SELECT * FROM users WHERE email='test@example.com'";
        System.out.println("SQL: " + sql3);
        System.out.println("  • 分别使用索引，然后合并结果");
        
        System.out.println("\n【最佳实践】");
        System.out.println("  ✅ OR 连接的条件列都要有索引");
        System.out.println("  ✅ 或者使用 UNION 代替 OR");
    }
    
    /**
     * 场景 6：!= 或 <> 导致索引失效
     */
    public void testNotEqual() {
        System.out.println("\n【场景 6】!= 或 <> 导致索引失效");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n❌ 错误写法：不等于条件");
        String sql1 = "SELECT * FROM users WHERE age != 30";
        System.out.println("SQL: " + sql1);
        System.out.println("  • 大部分记录都满足条件");
        System.out.println("  • 优化器可能选择全表扫描");
        
        System.out.println("\n✅ 正确写法：转换为 IN 或范围查询");
        String sql2 = "SELECT * FROM users WHERE age < 30 OR age > 30";
        System.out.println("SQL: " + sql2);
        System.out.println("  • 仍然可能全表扫描");
        System.out.println("  • 但如果数据分布不均，可能使用索引");
        
        System.out.println("\n【最佳实践】");
        System.out.println("  ⚠️  慎用 != 或 <>");
        System.out.println("  ✅ 考虑业务场景，看是否能转换为其他条件");
    }
    
    /**
     * 总结对比
     */
    public void printSummary() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     索引失效场景总结                   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("┌─────────────────────┬──────────┬──────────┐");
        System.out.println("│   场景               │ 索引状态  │ 性能影响  │");
        System.out.println("├─────────────────────┼──────────┼──────────┤");
        System.out.println("│ 隐式类型转换         │ ❌ 失效   │ ⭐ 最差   │");
        System.out.println("│ 函数包裹索引列       │ ❌ 失效   │ ⭐ 最差   │");
        System.out.println("│ 前导 % 模糊查询      │ ❌ 失效   │ ⭐ 最差   │");
        System.out.println("│ 跳过最左前缀         │ ❌ 失效   │ ⭐ 最差   │");
        System.out.println("│ OR 连接非索引列      │ ❌ 失效   │ ⭐ 最差   │");
        System.out.println("│ != 或 <>             │ ⚠️  可能  │ ⭐⭐ 较差  │");
        System.out.println("│ IS NULL             │ ⚠️  可能  │ ⭐⭐⭐ 一般  │");
        System.out.println("│ 正常等值匹配         │ ✅ 有效   │ ⭐⭐⭐⭐⭐ 最优 │");
        System.out.println("└─────────────────────┴──────────┴──────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  索引列不能参与函数或表达式计算");
        System.out.println("  2️⃣  避免隐式类型转换（字符串要加引号）");
        System.out.println("  3️⃣  like 查询避免前导 %");
        System.out.println("  4️⃣  联合索引遵循最左前缀原则");
        System.out.println("  5️⃣  OR 条件要谨慎使用");
        System.out.println("  6️⃣  用 EXPLAIN 判断是否失效\n");
        
        System.out.println("【最佳实践】");
        System.out.println("  ✅ 查询前先用 EXPLAIN 分析");
        System.out.println("  ✅ 观察 key 字段是否为 NULL");
        System.out.println("  ✅ 观察 type 字段是否为 ALL");
        System.out.println("  ✅ 定期分析慢查询日志");
        System.out.println("  ❌ 不要靠猜，要看执行计划\n");
    }
    
    public static void main(String[] args) {
        IndexInvalidationDemo demo = new IndexInvalidationDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testImplicitTypeConversion();
        demo.testFunctionOnIndex();
        demo.testLeadingWildcardLike();
        demo.testCompositeIndexViolation();
        demo.testOrCondition();
        demo.testNotEqual();
        demo.printSummary();
    }
}
