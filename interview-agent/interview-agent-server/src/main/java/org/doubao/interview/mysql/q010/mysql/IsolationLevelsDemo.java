package org.doubao.interview.mysql.q010.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 四种事务隔离级别及其并发问题演示
 * 
 * 【核心概念】
 * 1. 四种隔离级别（从低到高）：
 *    - 读未提交（Read Uncommitted）
 *    - 读已提交（Read Committed, RC）
 *    - 可重复读（Repeatable Read, RR）- InnoDB 默认
 *    - 串行化（Serializable）
 * 
 * 2. 三种并发问题：
 *    - 脏读（Dirty Read）：读到未提交的数据
 *    - 不可重复读（Non-repeatable Read）：同一查询读到不同数据
 *    - 幻读（Phantom Read）：同一查询读到不同行数
 * 
 * 3. MySQL InnoDB 特性：
 *    - 默认隔离级别：RR（可重复读）
 *    - MVCC + Next-Key Lock 抑制幻读
 *    - RC 和 RR 都使用 MVCC，但可见性规则不同
 * 
 * @author Interview Demo
 */
public class IsolationLevelsDemo {
    
    /**
     * 模拟数据行（支持多版本）
     */
    static class Row {
        Long id;
        Map<String, Object> data = new HashMap<>();
        Long transactionId;  // 创建该版本的事务 ID
        boolean committed = false;  // 是否已提交
        
        public Row(Long id) {
            this.id = id;
            this.transactionId = TransactionManager.getCurrentTransactionId();
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, data=%s, txId=%d, committed=%b}", 
                id, data, transactionId, committed);
        }
    }
    
    /**
     * 事务管理器（支持不同隔离级别）
     */
    static class TransactionManager {
        public enum IsolationLevel {
            READ_UNCOMMITTED,   // 读未提交
            READ_COMMITTED,     // 读已提交
            REPEATABLE_READ,    // 可重复读
            SERIALIZABLE        // 串行化
        }
        
        private static AtomicLong transactionIdGenerator = new AtomicLong(1);
        private Map<Long, List<Row>> rowVersions = new ConcurrentHashMap<>(); // 版本链
        private IsolationLevel isolationLevel = IsolationLevel.REPEATABLE_READ;
        private Map<Long, ReadView> readViews = new HashMap<>(); // 每个事务的读视图
        
        /**
         * 获取当前事务 ID
         */
        public static Long getCurrentTransactionId() {
            return transactionIdGenerator.get();
        }
        
        /**
         * 设置隔离级别
         */
        public void setIsolationLevel(IsolationLevel level) {
            this.isolationLevel = level;
            System.out.println("\n【设置隔离级别】" + getLevelName(level));
        }
        
        /**
         * 获取隔离级别名称
         */
        private String getLevelName(IsolationLevel level) {
            switch (level) {
                case READ_UNCOMMITTED: return "读未提交";
                case READ_COMMITTED: return "读已提交 (RC)";
                case REPEATABLE_READ: return "可重复读 (RR)";
                case SERIALIZABLE: return "串行化";
                default: return "未知";
            }
        }
        
        /**
         * 开始事务
         */
        public void beginTransaction(long txId) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   开始事务 TX-" + txId + "（隔离级别：" + getLevelName(isolationLevel) + "） ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            // 创建 Read View（用于 MVCC）
            if (isolationLevel == IsolationLevel.READ_COMMITTED || 
                isolationLevel == IsolationLevel.REPEATABLE_READ) {
                ReadView readView = new ReadView(txId);
                readViews.put(txId, readView);
                System.out.println("  📖 创建 Read View: " + readView);
            }
        }
        
        /**
         * 提交事务
         */
        public void commit(long txId) {
            System.out.println("\n【提交事务 TX-" + txId + "】");
            
            // 标记所有该事务修改的行为已提交
            for (List<Row> versions : rowVersions.values()) {
                for (Row row : versions) {
                    if (row.transactionId.equals(txId)) {
                        row.committed = true;
                    }
                }
            }
            
            // RC 级别：每次读取都创建新的 Read View
            if (isolationLevel == IsolationLevel.READ_COMMITTED) {
                readViews.remove(txId);
                System.out.println("  ℹ️  RC 级别：清除 Read View（下次读取时重新创建）");
            } else if (isolationLevel == IsolationLevel.REPEATABLE_READ) {
                // RR 级别：保持 Read View 不变
                System.out.println("  ℹ️  RR 级别：保持 Read View（整个事务看到同一版本）");
            }
            
            System.out.println("  ✅ 事务 TX-" + txId + " 提交成功\n");
        }
        
        /**
         * 插入数据
         */
        public void insert(Long id, Map<String, Object> values) {
            long currentTxId = getCurrentTransactionId();
            System.out.println("\n[INSERT] 插入 Row " + id + ": " + values);
            
            Row newRow = new Row(id);
            newRow.data = new HashMap<>(values);
            newRow.transactionId = currentTxId;
            
            // 添加到版本链
            rowVersions.computeIfAbsent(id, k -> new ArrayList<>()).add(newRow);
            
            System.out.println("  ✅ 插入成功，版本：" + newRow);
        }
        
        /**
         * 更新数据
         */
        public void update(Long id, Map<String, Object> newValues) {
            long currentTxId = getCurrentTransactionId();
            List<Row> versions = rowVersions.get(id);
            
            if (versions == null || versions.isEmpty()) {
                System.out.println("[UPDATE] Row " + id + " 不存在");
                return;
            }
            
            Row latestVersion = versions.get(versions.size() - 1);
            
            System.out.println("\n[UPDATE] 更新 Row " + id);
            System.out.println("  旧值：" + latestVersion.data);
            System.out.println("  新值：" + newValues);
            
            // 创建新版本
            Row newRow = new Row(id);
            newRow.data = new HashMap<>(latestVersion.data);
            newRow.data.putAll(newValues);
            newRow.transactionId = currentTxId;
            
            versions.add(newRow);
            
            System.out.println("  ✅ 更新成功，新版本：" + newRow);
        }
        
        /**
         * 读取数据（MVCC）
         */
        public Row select(Long id) {
            long currentTxId = getCurrentTransactionId();
            List<Row> versions = rowVersions.get(id);
            
            if (versions == null || versions.isEmpty()) {
                System.out.println("\n[SELECT] Row " + id + " 不存在");
                return null;
            }
            
            System.out.println("\n[SELECT] 查询 Row " + id);
            System.out.println("  当前事务 ID: " + currentTxId);
            System.out.println("  隔离级别：" + getLevelName(isolationLevel));
            
            // 根据隔离级别选择可见版本
            Row visibleVersion = findVisibleVersion(versions, currentTxId);
            
            if (visibleVersion != null) {
                System.out.println("  ✅ 返回可见版本：" + visibleVersion);
                return visibleVersion;
            } else {
                System.out.println("  ❌ 没有可见版本");
                return null;
            }
        }
        
        /**
         * 查找可见版本（核心逻辑）
         */
        private Row findVisibleVersion(List<Row> versions, long currentTxId) {
            // 从最新版本开始找
            for (int i = versions.size() - 1; i >= 0; i--) {
                Row version = versions.get(i);
                
                if (isVisible(version, currentTxId)) {
                    return version;
                }
            }
            
            return null;
        }
        
        /**
         * 判断版本是否可见（MVCC 核心）
         */
        private boolean isVisible(Row row, long currentTxId) {
            ReadView readView = readViews.get(currentTxId);
            
            switch (isolationLevel) {
                case READ_UNCOMMITTED:
                    // 读未提交：所有版本都可见（包括未提交的）
                    return true;
                    
                case READ_COMMITTED:
                    // 读已提交：只能看到已提交的版本
                    // 每次读取都会创建新的 Read View
                    if (readView == null) {
                        readView = new ReadView(currentTxId);
                        readViews.put(currentTxId, readView);
                    }
                    return readView.isVisible(row);
                    
                case REPEATABLE_READ:
                    // 可重复读：整个事务看到同一版本
                    // Read View 在事务开始时创建，保持不变
                    if (readView == null) {
                        readView = new ReadView(currentTxId);
                        readViews.put(currentTxId, readView);
                    }
                    return readView.isVisible(row);
                    
                case SERIALIZABLE:
                    // 串行化：通过锁机制强制串行执行
                    // 这里简化处理，等同于 RR
                    if (readView == null) {
                        readView = new ReadView(currentTxId);
                        readViews.put(currentTxId, readView);
                    }
                    return readView.isVisible(row);
                    
                default:
                    return false;
            }
        }
        
        /**
         * 打印所有数据（上帝视角）
         */
        public void printAllData() {
            System.out.println("\n【当前数据库中的所有数据】（上帝视角）");
            System.out.println("┌─────────┬──────────────────────────┐");
            System.out.println("│   ID    │       所有版本           │");
            System.out.println("├─────────┼──────────────────────────┤");
            
            for (Map.Entry<Long, List<Row>> entry : rowVersions.entrySet()) {
                System.out.printf("│ %7d │ %-24s │%n", entry.getKey(), 
                    entry.getValue().size() + " 个版本");
                for (Row row : entry.getValue()) {
                    System.out.printf("│         │   → %s%n", row);
                }
            }
            System.out.println("└─────────┴──────────────────────────┘\n");
        }
    }
    
    /**
     * Read View（读视图）- MVCC 核心
     */
    static class ReadView {
        long creatorTxId;  // 创建者事务 ID
        long minActiveTxId;  // 最小活跃事务 ID
        List<Long> activeTxIds = new ArrayList<>();  // 活跃事务列表
        
        public ReadView(long creatorTxId) {
            this.creatorTxId = creatorTxId;
            this.minActiveTxId = creatorTxId;
            // 简化：假设只有自己是活跃事务
            this.activeTxIds.add(creatorTxId);
        }
        
        /**
         * 判断版本是否可见
         */
        public boolean isVisible(Row row) {
            // 如果版本是已提交的，则可见
            if (row.committed) {
                return true;
            }
            
            // 如果版本创建者是自己，则可见
            if (row.transactionId.equals(creatorTxId)) {
                return true;
            }
            
            // 其他情况：未提交且不是自己的版本，不可见
            return false;
        }
        
        @Override
        public String toString() {
            return String.format("ReadView{creator=%d, active=%s}", 
                creatorTxId, activeTxIds);
        }
    }
    
    // ========== 测试场景 ==========
    
    private TransactionManager txManager;
    
    /**
     * 初始化
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     四种隔离级别及其并发问题演示        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        txManager = new TransactionManager();
        
        // 初始化数据
        System.out.println("【初始化数据】");
        Map<String, Object> account1 = new HashMap<>();
        account1.put("name", "张三");
        account1.put("balance", 1000);
        txManager.insert(1L, account1);
        
        Map<String, Object> account2 = new HashMap<>();
        account2.put("name", "李四");
        account2.put("balance", 500);
        txManager.insert(2L, account2);
        
        txManager.printAllData();
    }
    
    /**
     * 演示 1：读未提交 - 脏读
     */
    public void testReadUncommittedDirtyRead() {
        System.out.println("\n【演示 1】读未提交 - 脏读（Dirty Read）");
        System.out.println("──────────────────────────────────────");
        
        txManager.setIsolationLevel(TransactionManager.IsolationLevel.READ_UNCOMMITTED);
        
        // 事务 A：开始并修改（未提交）
        System.out.println("\n【事务 A】开始并修改数据");
        txManager.beginTransaction(1);
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 800);
        txManager.update(1L, update);
        System.out.println("  ⚠️  事务 A 未提交！");
        
        // 事务 B：读取（能看到未提交的数据）
        System.out.println("\n【事务 B】读取数据");
        txManager.beginTransaction(2);
        txManager.select(1L);
        System.out.println("  ⚠️  事务 B 读到了事务 A 未提交的数据（脏读）！");
        
        // 事务 A：回滚
        System.out.println("\n【事务 A】回滚");
        System.out.println("  → 如果事务 A 回滚，事务 B 读到的就是脏数据");
        
        txManager.printAllData();
        
        System.out.println("\n【并发问题总结】");
        System.out.println("  ❌ 脏读：读到未提交的数据");
        System.out.println("  ❌ 不可重复读：同一查询可能读到不同数据");
        System.out.println("  ❌ 幻读：同一查询可能读到不同行数");
    }
    
    /**
     * 演示 2：读已提交 - 避免脏读，但不可重复读
     */
    public void testReadCommittedNonRepeatableRead() {
        System.out.println("\n【演示 2】读已提交 - 不可重复读（Non-repeatable Read）");
        System.out.println("──────────────────────────────────────");
        
        txManager.setIsolationLevel(TransactionManager.IsolationLevel.READ_COMMITTED);
        
        // 重置数据
        txManager = new TransactionManager();
        Map<String, Object> account = new HashMap<>();
        account.put("balance", 1000);
        txManager.insert(1L, account);
        
        // 事务 A：第一次读取
        System.out.println("\n【事务 A】第一次读取");
        txManager.beginTransaction(1);
        txManager.select(1L);
        
        // 事务 B：修改并提交
        System.out.println("\n【事务 B】修改并提交");
        txManager.beginTransaction(2);
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 800);
        txManager.update(1L, update);
        txManager.commit(2);
        
        // 事务 A：第二次读取（读到不同的值）
        System.out.println("\n【事务 A】第二次读取（同一事务内）");
        txManager.select(1L);
        System.out.println("  ⚠️  同一事务内，两次读取结果不同（不可重复读）！");
        
        txManager.printAllData();
        
        System.out.println("\n【并发问题总结】");
        System.out.println("  ✅ 避免脏读：只读已提交的数据");
        System.out.println("  ❌ 不可重复读：同一事务内可能读到不同数据");
        System.out.println("  ❌ 幻读：仍可能发生");
    }
    
    /**
     * 演示 3：可重复读 - 避免不可重复读
     */
    public void testRepeatableReadNoNonRepeatableRead() {
        System.out.println("\n【演示 3】可重复读 - 解决不可重复读");
        System.out.println("──────────────────────────────────────");
        
        txManager.setIsolationLevel(TransactionManager.IsolationLevel.REPEATABLE_READ);
        
        // 重置数据
        txManager = new TransactionManager();
        Map<String, Object> account = new HashMap<>();
        account.put("balance", 1000);
        txManager.insert(1L, account);
        
        // 事务 A：第一次读取
        System.out.println("\n【事务 A】第一次读取");
        txManager.beginTransaction(1);
        txManager.select(1L);
        
        // 事务 B：修改并提交
        System.out.println("\n【事务 B】修改并提交");
        txManager.beginTransaction(2);
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 800);
        txManager.update(1L, update);
        txManager.commit(2);
        
        // 事务 A：第二次读取（仍读到相同的值）
        System.out.println("\n【事务 A】第二次读取（同一事务内）");
        txManager.select(1L);
        System.out.println("  ✅ 同一事务内，两次读取结果相同（可重复读）！");
        System.out.println("  ℹ️  RR 级别：Read View 在事务开始时创建，保持不变");
        
        txManager.printAllData();
        
        System.out.println("\n【并发问题总结】");
        System.out.println("  ✅ 避免脏读：只读已提交的数据");
        System.out.println("  ✅ 避免不可重复读：同一事务内看到相同数据");
        System.out.println("  ⚠️  大部分避免幻读：InnoDB 使用 Next-Key Lock");
    }
    
    /**
     * 演示 4：对比总结
     */
    public void printSummary() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     四种隔离级别对比总结               ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("【隔离级别与并发问题对照表】");
        System.out.println("┌─────────────┬──────────┬──────────────┬──────────┐");
        System.out.println("│ 隔离级别     │ 脏读     │ 不可重复读   │ 幻读     │");
        System.out.println("├─────────────┼──────────┼──────────────┼──────────┤");
        System.out.println("│ 读未提交     │ ✅ 可能   │ ✅ 可能      │ ✅ 可能   │");
        System.out.println("│ 读已提交     │ ❌ 避免   │ ✅ 可能      │ ✅ 可能   │");
        System.out.println("│ 可重复读     │ ❌ 避免   │ ❌ 避免      │ ⚠️ 大部分  │");
        System.out.println("│ 串行化       │ ❌ 避免   │ ❌ 避免      │ ❌ 避免   │");
        System.out.println("└─────────────┴──────────┴──────────────┴──────────┘\n");
        
        System.out.println("【性能对比】");
        System.out.println("┌─────────────┬──────────────┬──────────────┐");
        System.out.println("│ 隔离级别     │ 并发性能     │ 适用场景     │");
        System.out.println("├─────────────┼──────────────┼──────────────┤");
        System.out.println("│ 读未提交     │ ⭐⭐⭐⭐⭐ 最高  │ 统计类不精确  │");
        System.out.println("│ 读已提交     │ ⭐⭐⭐⭐ 较高   │ Oracle 默认   │");
        System.out.println("│ 可重复读     │ ⭐⭐⭐ 中等    │ MySQL 默认    │");
        System.out.println("│ 串行化       │ ⭐ 最低      │ 强一致性要求  │");
        System.out.println("└─────────────┴──────────────┴──────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  MySQL InnoDB 默认隔离级别是 RR（可重复读）");
        System.out.println("  2️⃣  RC 和 RR 都使用 MVCC，但 Read View 保持策略不同");
        System.out.println("  3️⃣  RC：每次读取都创建新 Read View");
        System.out.println("  4️⃣  RR：事务开始时创建 Read View，保持不变");
        System.out.println("  5️⃣  InnoDB 在 RR 下使用 Next-Key Lock 抑制幻读");
        System.out.println("  6️⃣  隔离级别越高，并发能力通常越弱");
        System.out.println();
        
        System.out.println("【MySQL vs Oracle】");
        System.out.println("  • MySQL InnoDB 默认：RR（可重复读）");
        System.out.println("  • Oracle 默认：RC（读已提交）");
        System.out.println("  • MySQL 支持 RR 的原因：历史兼容性 + MVCC 实现优秀");
        System.out.println("  • RR 的优势：避免不可重复读，适合复杂业务");
        System.out.println();
    }
    
    public static void main(String[] args) {
        IsolationLevelsDemo demo = new IsolationLevelsDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testReadUncommittedDirtyRead();
        demo.testReadCommittedNonRepeatableRead();
        demo.testRepeatableReadNoNonRepeatableRead();
        demo.printSummary();
    }
}
