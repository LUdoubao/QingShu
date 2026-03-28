package org.doubao.interview.mysql.q008.mysql;

import java.util.ArrayList;
import java.util.List;

/**
 * 慢 SQL 排查与优化步骤演示
 * 
 * 【核心概念】
 * 1. 慢 SQL 定义：
 *    - 执行时间超过阈值的 SQL
 *    - 通常指超过 1s 或 2s 的查询
 *    - 影响系统性能的关键因素
 * 
 * 2. 优化五步法：
 *    - 定位：通过慢查询日志/监控发现 SQL
 *    - 分析：使用 EXPLAIN 等工具分析问题
 *    - 改造：针对性优化（索引、SQL、架构）
 *    - 验证：压测验证 p95/p99 延迟
 *    - 观察：上线后持续监控
 * 
 * 3. 关键指标：
 *    - p95/p99 延迟（不是平均值）
 *    - QPS（每秒查询数）
 *    - 扫描行数
 *    - 锁等待时间
 * 
 * @author Interview Demo
 */
public class SlowSQLOptimizationDemo {
    
    /**
     * 模拟 SQL 执行记录
     */
    static class SQLRecord {
        String sql;
        long executionTime;  // 执行时间（毫秒）
        long scanRows;       // 扫描行数
        long returnRows;     // 返回行数
        String type;         // 访问类型
        String extra;        // Extra 信息
        
        public SQLRecord(String sql, long executionTime, long scanRows, 
                        long returnRows, String type, String extra) {
            this.sql = sql;
            this.executionTime = executionTime;
            this.scanRows = scanRows;
            this.returnRows = returnRows;
            this.type = type;
            this.extra = extra;
        }
        
        @Override
        public String toString() {
            return String.format("SQL: %s\n  执行时间：%dms | 扫描：%d行 | 返回：%d行 | type=%s | %s",
                sql, executionTime, scanRows, returnRows, type, extra);
        }
    }
    
    /**
     * 慢查询日志模拟器
     */
    static class SlowQueryLog {
        private List<SQLRecord> logs = new ArrayList<>();
        private long thresholdMs;  // 慢查询阈值（毫秒）
        
        public SlowQueryLog(long thresholdMs) {
            this.thresholdMs = thresholdMs;
        }
        
        /**
         * 记录 SQL 执行
         */
        public void log(SQLRecord record) {
            if (record.executionTime >= thresholdMs) {
                logs.add(record);
            }
        }
        
        /**
         * 获取 TopN 慢 SQL
         */
        public List<SQLRecord> getTopN(int n) {
            logs.sort((a, b) -> Long.compare(b.executionTime, a.executionTime));
            return logs.subList(0, Math.min(n, logs.size()));
        }
        
        /**
         * 打印慢查询统计
         */
        public void printStatistics() {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     慢查询日志统计（>" + thresholdMs + "ms）          ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            System.out.println("总慢查询数：" + logs.size());
            if (logs.isEmpty()) {
                System.out.println("✅ 无慢查询\n");
                return;
            }
            
            // 按执行时间排序
            List<SQLRecord> sorted = new ArrayList<>(logs);
            sorted.sort((a, b) -> Long.compare(b.executionTime, a.executionTime));
            
            System.out.println("\n【Top 5 最慢 SQL】");
            for (int i = 0; i < Math.min(5, sorted.size()); i++) {
                SQLRecord record = sorted.get(i);
                System.out.printf("  %d. %dms - %s%n", i + 1, record.executionTime, 
                    truncate(record.sql, 50));
            }
            System.out.println();
        }
        
        /**
         * 截断字符串
         */
        private String truncate(String str, int maxLen) {
            return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
        }
    }
    
    /**
     * 性能分析器
     */
    static class PerformanceAnalyzer {
        
        /**
         * 分析 SQL 性能问题
         */
        public void analyze(SQLRecord record) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        SQL 性能分析                     ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            System.out.println("原始 SQL: " + record.sql);
            System.out.println();
            
            // 执行时间分析
            System.out.print("【执行时间】" + record.executionTime + "ms - ");
            if (record.executionTime < 100) {
                System.out.println("✅ 优秀");
            } else if (record.executionTime < 500) {
                System.out.println("⚠️  良好");
            } else if (record.executionTime < 1000) {
                System.out.println("❌ 较慢");
            } else {
                System.out.println("❌ 很慢");
            }
            
            // 扫描行数分析
            System.out.print("【扫描行数】" + record.scanRows + " 行 - ");
            double ratio = (double) record.scanRows / record.returnRows;
            if (ratio <= 10) {
                System.out.println("✅ 优秀（扫描/返回=" + String.format("%.1f", ratio) + ")");
            } else if (ratio <= 100) {
                System.out.println("⚠️  适中（扫描/返回=" + String.format("%.1f", ratio) + ")");
            } else {
                System.out.println("❌ 太多（扫描/返回=" + String.format("%.1f", ratio) + ")");
            }
            
            // type 分析
            System.out.print("【访问类型】" + record.type + " - ");
            if ("const".equals(record.type) || "ref".equals(record.type)) {
                System.out.println("✅ 好");
            } else if ("range".equals(record.type)) {
                System.out.println("⚠️  一般");
            } else {
                System.out.println("❌ 差");
            }
            
            // Extra 分析
            if (record.extra != null && !record.extra.isEmpty()) {
                System.out.print("【Extra】" + record.extra + " - ");
                if (record.extra.contains("Using index")) {
                    System.out.println("✅ 覆盖索引");
                } else if (record.extra.contains("filesort") || record.extra.contains("temporary")) {
                    System.out.println("⚠️  成本高");
                } else {
                    System.out.println("ℹ️  普通");
                }
            }
            
            System.out.println();
        }
        
        /**
         * 给出优化建议
         */
        public void suggestOptimization(SQLRecord record) {
            System.out.println("【优化建议】");
            
            boolean hasSuggestion = false;
            
            // 扫描行数太多
            if (record.scanRows > 1000) {
                System.out.println("  1️⃣  扫描行数过多，考虑添加索引");
                hasSuggestion = true;
            }
            
            // 全表扫描
            if ("ALL".equals(record.type)) {
                System.out.println("  2️⃣  全表扫描，必须在 WHERE 列上加索引");
                hasSuggestion = true;
            }
            
            // 文件排序
            if (record.extra != null && record.extra.contains("filesort")) {
                System.out.println("  3️⃣  文件排序，在 ORDER BY 列上建索引");
                hasSuggestion = true;
            }
            
            // 临时表
            if (record.extra != null && record.extra.contains("temporary")) {
                System.out.println("  4️⃣  临时表，在 GROUP BY 列上建索引");
                hasSuggestion = true;
            }
            
            // 回表
            if (record.scanRows > record.returnRows * 10 && !"ALL".equals(record.type)) {
                System.out.println("  5️⃣  回表次数多，考虑覆盖索引");
                hasSuggestion = true;
            }
            
            if (!hasSuggestion) {
                System.out.println("  ℹ️  暂无明显优化点");
            }
            
            System.out.println();
        }
    }
    
    // ========== 测试场景 ==========
    
    private SlowQueryLog slowQueryLog;
    private PerformanceAnalyzer analyzer;
    
    /**
     * 初始化
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     慢 SQL 排查与优化步骤演示            ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // 创建慢查询日志（阈值 100ms）
        slowQueryLog = new SlowQueryLog(100);
        analyzer = new PerformanceAnalyzer();
        
        System.out.println("【步骤 1】定位 - 通过慢查询日志发现 TopN SQL\n");
        
        // 模拟一些慢查询
        slowQueryLog.log(new SQLRecord(
            "SELECT * FROM users WHERE name LIKE '%张%'",
            2500, 100000, 500, "ALL", "Using where"));
        
        slowQueryLog.log(new SQLRecord(
            "SELECT * FROM orders ORDER BY create_time DESC LIMIT 100000, 20",
            1800, 100020, 20, "ALL", "Using filesort"));
        
        slowQueryLog.log(new SQLRecord(
            "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id WHERE o.status = 1",
            950, 50000, 2000, "ALL", "Using where; Using temporary"));
        
        slowQueryLog.log(new SQLRecord(
            "SELECT COUNT(*) FROM articles WHERE category_id IN (1,2,3,4,5)",
            650, 30000, 1, "range", "Using where"));
        
        slowQueryLog.log(new SQLRecord(
            "SELECT * FROM products WHERE price BETWEEN 100 AND 500 ORDER BY sales",
            450, 20000, 500, "range", "Using filesort"));
        
        slowQueryLog.log(new SQLRecord(
            "SELECT * FROM users WHERE phone = '13800138000'",
            150, 1, 1, "ref", "Using index"));
        
        // 打印统计
        slowQueryLog.printStatistics();
    }
    
    /**
     * 步骤 1：定位慢 SQL
     */
    public void step1Locate() {
        System.out.println("【步骤 1】定位 - 查看 TopN 慢 SQL");
        System.out.println("──────────────────────────────────────");
        
        List<SQLRecord> topN = slowQueryLog.getTopN(3);
        
        System.out.println("\n【Top 3 最慢 SQL】\n");
        for (int i = 0; i < topN.size(); i++) {
            SQLRecord record = topN.get(i);
            System.out.println((i + 1) + ". " + record.sql);
            System.out.println("   执行时间：" + record.executionTime + "ms");
            System.out.println("   扫描行数：" + record.scanRows + " 行");
            System.out.println();
        }
        
        System.out.println("【下一步】选择最慢的 SQL 进行详细分析\n");
    }
    
    /**
     * 步骤 2：分析性能问题
     */
    public void step2Analyze() {
        System.out.println("\n【步骤 2】分析 - 使用 EXPLAIN 等工具");
        System.out.println("──────────────────────────────────────");
        
        // 选择最慢的 SQL 进行分析
        SQLRecord slowSQL = new SQLRecord(
            "SELECT * FROM users WHERE name LIKE '%张%'",
            2500, 100000, 500, "ALL", "Using where");
        
        analyzer.analyze(slowSQL);
        analyzer.suggestOptimization(slowSQL);
    }
    
    /**
     * 步骤 3：针对性优化
     */
    public void step3Optimize() {
        System.out.println("\n【步骤 3】改造 - 针对性优化");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n【原 SQL】");
        System.out.println("SELECT * FROM users WHERE name LIKE '%张%'");
        System.out.println("问题：前导 % 导致全表扫描\n");
        
        System.out.println("【优化方案 1】使用全文索引");
        System.out.println("ALTER TABLE users ADD FULLTEXT INDEX ft_name (name);");
        System.out.println("SELECT * FROM users WHERE MATCH(name) AGAINST('张');");
        System.out.println("效果：从 2500ms → 50ms\n");
        
        System.out.println("【优化方案 2】避免前导 %】");
        System.out.println("-- 如果业务允许，改为后缀匹配");
        System.out.println("SELECT * FROM users WHERE name LIKE '张%';");
        System.out.println("效果：从 2500ms → 10ms\n");
        
        System.out.println("【优化方案 3】搜索引擎】");
        System.out.println("-- 复杂场景使用 Elasticsearch");
        System.out.println("效果：从 2500ms → 20ms\n");
    }
    
    /**
     * 步骤 4：压测验证
     */
    public void step4Verify() {
        System.out.println("\n【步骤 4】验证 - 压测看 p95/p99");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n【压测指标对比】");
        System.out.println("┌─────────────┬──────────┬──────────┐");
        System.out.println("│   指标      │ 优化前   │ 优化后   │");
        System.out.println("├─────────────┼──────────┼──────────┤");
        System.out.println("│ avg (平均)  │ 2500ms   │ 50ms     │");
        System.out.println("│ p95         │ 3200ms   │ 80ms     │");
        System.out.println("│ p99         │ 4500ms   │ 120ms    │");
        System.out.println("│ QPS         │ 100      │ 5000     │");
        System.out.println("└─────────────┴──────────┴──────────┘\n");
        
        System.out.println("【关键点】");
        System.out.println("  ✅ 不能只看平均值（avg）");
        System.out.println("  ✅ 要看 p95/p99（长尾延迟）");
        System.out.println("  ✅ QPS 提升 50 倍");
        System.out.println();
    }
    
    /**
     * 步骤 5：持续观察
     */
    public void step5Monitor() {
        System.out.println("\n【步骤 5】观察 - 上线后持续监控");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n【监控指标】");
        System.out.println("  1️⃣  慢查询数量变化趋势");
        System.out.println("  2️⃣  p95/p99 延迟是否稳定");
        System.out.println("  3️⃣  QPS 是否达到预期");
        System.out.println("  4️⃣  是否有新的性能问题");
        System.out.println("  5️⃣  数据库 CPU/内存使用率");
        System.out.println();
        
        System.out.println("【观察周期】");
        System.out.println("  • 上线后 1 小时：密切监控");
        System.out.println("  • 上线后 24 小时：持续观察");
        System.out.println("  • 上线后 7 天：稳定期");
        System.out.println();
        
        System.out.println("【回归验证】");
        System.out.println("  ✅ 确认优化没有引入新问题");
        System.out.println("  ✅ 其他 SQL 未受影响");
        System.out.println("  ✅ 系统整体性能提升");
        System.out.println();
    }
    
    /**
     * 总结优化流程
     */
    public void printSummary() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     慢 SQL 优化五步法总结               ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("【完整流程】");
        System.out.println("  1️⃣  定位 - 慢查询日志/监控，发现 TopN SQL");
        System.out.println("  2️⃣  分析 - EXPLAIN 分析执行计划");
        System.out.println("  3️⃣  改造 - 针对性优化（索引、SQL、架构）");
        System.out.println("  4️⃣  验证 - 压测看 p95/p99，不只看平均");
        System.out.println("  5️⃣  观察 - 上线后持续监控，防止回归");
        System.out.println();
        
        System.out.println("【常见优化手段】");
        System.out.println("┌─────────────────────┬──────────────┐");
        System.out.println("│   问题类型          │   优化方案   │");
        System.out.println("├─────────────────────┼──────────────┤");
        System.out.println("│ 全表扫描 ALL        │ 加索引       │");
        System.out.println("│ 扫描行数多          │ 联合索引     │");
        System.out.println("│ Using filesort      │ ORDER BY 索引 │");
        System.out.println("│ Using temporary     │ GROUP BY 索引 │");
        System.out.println("│ 回表多              │ 覆盖索引     │");
        System.out.println("│ 深分页              │ 延迟关联     │");
        System.out.println("│ 大事务              │ 拆分事务     │");
        System.out.println("│ 前导 %              │ 全文索引     │");
        System.out.println("└─────────────────────┴──────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  先定位再分析，不要盲目优化");
        System.out.println("  2️⃣  EXPLAIN 是核心工具");
        System.out.println("  3️⃣  优化要验证 p95/p99，不是平均值");
        System.out.println("  4️⃣  上线后要观察，防止回归");
        System.out.println("  5️⃣  形成闭环管理");
        System.out.println();
        
        System.out.println("【最佳实践】");
        System.out.println("  ✅ 建立慢查询监控告警");
        System.out.println("  ✅ 定期分析慢查询日志");
        System.out.println("  ✅ 优化前备份，可快速回滚");
        System.out.println("  ✅ 小步快跑，逐步优化");
        System.out.println("  ❌ 不要凭感觉优化");
        System.out.println("  ❌ 不要一次性改太多");
        System.out.println();
    }
    
    public static void main(String[] args) {
        SlowSQLOptimizationDemo demo = new SlowSQLOptimizationDemo();
        
        // 初始化
        demo.init();
        
        // 执行五步法
        demo.step1Locate();
        demo.step2Analyze();
        demo.step3Optimize();
        demo.step4Verify();
        demo.step5Monitor();
        demo.printSummary();
    }
}
