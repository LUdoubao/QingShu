package org.doubao.interview.mysql.q011.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MVCC（多版本并发控制）机制演示
 * 
 * 【核心概念】
 * 1. MVCC 定义：
 *    - Multi-Version Concurrency Control
 *    - 通过保存数据的历史版本，实现读写不阻塞
 *    - 提高数据库的并发性能
 * 
 * 2. 解决的问题：
 *    - 读操作不阻塞写操作
 *    - 写操作不阻塞读操作
 *    - 提高并发读性能
 * 
 * 3. 核心组件：
 *    - 隐藏列：DB_TRX_ID（事务 ID）、DB_ROLL_PTR（回滚指针）
 *    - Undo Log：记录历史版本
 *    - Read View：读视图，判断版本可见性
 * 
 * 4. 两种读取方式：
 *    - 快照读（Snapshot Read）：普通 SELECT，使用 MVCC
 *    - 当前读（Current Read）：SELECT ... FOR UPDATE，需要加锁
 * 
 * @author Interview Demo
 */
public class MVDemo {
    
    /**
     * 模拟数据行（支持多版本）
     */
    static class Row {
        Long id;
        Map<String, Object> data = new HashMap<>();
        
        // MVCC 隐藏列
        Long trxId;         // DB_TRX_ID：创建该版本的事务 ID
        Long rollPtr;       // DB_ROLL_PTR：指向上一个版本的指针（undo log 位置）
        boolean committed;  // 是否已提交
        
        public Row(Long id) {
            this.id = id;
            this.trxId = TransactionManager.getCurrentTransactionId();
            this.committed = false;
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, data=%s, trxId=%d, committed=%b}", 
                id, data, trxId, committed);
        }
    }
    
    /**
     * Undo Log（回滚日志）- 存储历史版本
     */
    static class UndoLog {
        static class Version {
            Long rowId;
            Map<String, Object> oldData;  // 旧值
            Long prevVersion;             // 上一个版本的指针
            
            public Version(Long rowId, Map<String, Object> oldData, Long prevVersion) {
                this.rowId = rowId;
                this.oldData = oldData;
                this.prevVersion = prevVersion;
            }
            
            @Override
            public String toString() {
                return String.format("Version{rowId=%d, data=%s, prev=%s}", 
                    rowId, oldData, prevVersion != null ? "v" + prevVersion : "null");
            }
        }
        
        private Map<Long, Version> versions = new ConcurrentHashMap<>();
        private AtomicLong versionCounter = new AtomicLong(1);
        
        /**
         * 记录更新操作（创建新版本）
         */
        public Long logUpdate(Long rowId, Map<String, Object> oldData, Long prevVersion) {
            Long versionId = versionCounter.getAndIncrement();
            versions.put(versionId, new Version(rowId, oldData, prevVersion));
            System.out.println("    [Undo Log] 记录版本 v" + versionId + ": " + oldData);
            return versionId;
        }
        
        /**
         * 获取指定版本的数据
         */
        public Map<String, Object> getVersion(Long versionId) {
            Version version = versions.get(versionId);
            if (version != null) {
                return version.oldData;
            }
            return null;
        }
        
        /**
         * 打印版本链
         */
        public void printVersionChain(Long rowId) {
            System.out.println("\n【Row " + rowId + " 的版本链】");
            System.out.println("┌─────────────────────────────────────┐");
            
            for (Map.Entry<Long, Version> entry : versions.entrySet()) {
                if (entry.getValue().rowId.equals(rowId)) {
                    System.out.printf("│ v%-6d → %s%n", entry.getKey(), entry.getValue().oldData);
                }
            }
            System.out.println("└─────────────────────────────────────┘\n");
        }
    }
    
    /**
     * Read View（读视图）- MVCC 核心
     */
    static class ReadView {
        long creatorTxId;              // 创建者事务 ID
        long minActiveTxId;            // 最小活跃事务 ID
        List<Long> activeTxIds = new ArrayList<>();  // 活跃事务列表（未提交）
        
        public ReadView(long creatorTxId, List<Long> activeTxIds) {
            this.creatorTxId = creatorTxId;
            this.minActiveTxId = activeTxIds.isEmpty() ? creatorTxId : activeTxIds.get(0);
            this.activeTxIds = new ArrayList<>(activeTxIds);
        }
        
        /**
         * 判断版本是否可见（核心逻辑）
         */
        public boolean isVisible(Row row) {
            // 规则 1：如果版本已提交，则可见
            if (row.committed) {
                return true;
            }
            
            // 规则 2：如果版本创建者是自己，则可见
            if (row.trxId.equals(creatorTxId)) {
                return true;
            }
            
            // 规则 3：如果版本未提交且不是自己，则不可见
            return false;
        }
        
        @Override
        public String toString() {
            return String.format("ReadView{creator=%d, minActive=%d, active=%s}", 
                creatorTxId, minActiveTxId, activeTxIds);
        }
    }
    
    /**
     * 事务管理器
     */
    static class TransactionManager {
        private static AtomicLong transactionIdGenerator = new AtomicLong(1);
        private Map<Long, Row> table = new ConcurrentHashMap<>();
        private UndoLog undoLog = new UndoLog();
        private Map<Long, ReadView> readViews = new HashMap<>();
        private List<Long> activeTransactions = new ArrayList<>();
        
        /**
         * 获取当前事务 ID
         */
        public static Long getCurrentTransactionId() {
            return transactionIdGenerator.get();
        }
        
        /**
         * 开始事务
         */
        public void beginTransaction(long txId) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   开始事务 TX-" + txId + "                     ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            activeTransactions.add(txId);
            
            // 创建 Read View（用于快照读）
            ReadView readView = new ReadView(txId, activeTransactions);
            readViews.put(txId, readView);
            System.out.println("  📖 创建 Read View: " + readView);
        }
        
        /**
         * 提交事务
         */
        public void commit(long txId) {
            System.out.println("\n【提交事务 TX-" + txId + "】");
            
            // 标记所有该事务修改的行为已提交
            for (Row row : table.values()) {
                if (row.trxId.equals(txId)) {
                    row.committed = true;
                }
            }
            
            // 从活跃事务列表中移除
            activeTransactions.remove(Long.valueOf(txId));
            
            // 清除 Read View
            readViews.remove(txId);
            
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
            newRow.trxId = currentTxId;
            
            table.put(id, newRow);
            
            System.out.println("  ✅ 插入成功：" + newRow);
        }
        
        /**
         * 更新数据（当前读，需要加锁）
         */
        public void updateForCurrent(Long id, Map<String, Object> newValues) {
            long currentTxId = getCurrentTransactionId();
            Row row = table.get(id);
            
            if (row == null) {
                System.out.println("[UPDATE] Row " + id + " 不存在");
                return;
            }
            
            System.out.println("\n[UPDATE FOR UPDATE] 更新 Row " + id + "（当前读，加锁）");
            System.out.println("  旧值：" + row.data);
            System.out.println("  新值：" + newValues);
            
            // 记录 undo log（保存旧版本）
            Long versionId = undoLog.logUpdate(id, new HashMap<>(row.data), row.rollPtr);
            row.rollPtr = versionId;
            
            // 更新数据和事务 ID
            row.data = new HashMap<>(row.data);
            row.data.putAll(newValues);
            row.trxId = currentTxId;
            
            System.out.println("  ✅ 更新成功，新版本：" + row);
        }
        
        /**
         * 快照读（使用 MVCC）
         */
        public Row selectForSnapshot(Long id) {
            long currentTxId = getCurrentTransactionId();
            Row row = table.get(id);
            
            if (row == null) {
                System.out.println("\n[SNAPSHOT READ] Row " + id + " 不存在");
                return null;
            }
            
            System.out.println("\n[SNAPSHOT READ] 查询 Row " + id);
            System.out.println("  当前事务 ID: " + currentTxId);
            
            ReadView readView = readViews.get(currentTxId);
            System.out.println("  Read View: " + readView);
            
            // 判断版本可见性
            if (readView.isVisible(row)) {
                System.out.println("  ✅ 返回可见版本：" + row);
                return row;
            } else {
                System.out.println("  ⚠️  当前版本不可见，尝试从 undo log 查找历史版本...");
                
                // 从 undo log 查找历史版本
                Row historicalVersion = findHistoricalVersion(id, readView);
                if (historicalVersion != null) {
                    System.out.println("  ✅ 找到历史版本：" + historicalVersion);
                    return historicalVersion;
                } else {
                    System.out.println("  ❌ 没有可见版本");
                    return null;
                }
            }
        }
        
        /**
         * 从 undo log 查找历史版本
         */
        private Row findHistoricalVersion(Long rowId, ReadView readView) {
            // 简化处理：遍历所有 undo log 版本
            for (UndoLog.Version version : undoLog.versions.values()) {
                if (version.rowId.equals(rowId)) {
                    // 创建一个临时的历史版本行
                    Row historicalRow = new Row(rowId);
                    historicalRow.data = new HashMap<>(version.oldData);
                    historicalRow.committed = true;  // 历史版本都是已提交的
                    
                    if (readView.isVisible(historicalRow)) {
                        return historicalRow;
                    }
                }
            }
            return null;
        }
        
        /**
         * 打印表数据（上帝视角）
         */
        public void printTable() {
            System.out.println("\n【当前表中的所有数据】（上帝视角）");
            System.out.println("┌─────────┬──────────────────────────┐");
            System.out.println("│   ID    │          数据             │");
            System.out.println("├─────────┼──────────────────────────┤");
            for (Map.Entry<Long, Row> entry : table.entrySet()) {
                System.out.printf("│ %7d │ %-24s │%n", entry.getKey(), entry.getValue());
            }
            System.out.println("└─────────┴──────────────────────────┘\n");
        }
        
        /**
         * 打印 undo log 版本链
         */
        public void printUndoLog() {
            undoLog.printVersionChain(1L);
        }
    }
    
    // ========== 测试场景 ==========
    
    private TransactionManager txManager;
    
    /**
     * 初始化
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     MVCC（多版本并发控制）机制演示      ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        txManager = new TransactionManager();
        
        // 初始化数据
        System.out.println("【初始化数据】");
        Map<String, Object> account = new HashMap<>();
        account.put("name", "张三");
        account.put("balance", 1000);
        txManager.insert(1L, account);
        
        txManager.printTable();
    }
    
    /**
     * 演示 1：MVCC 基本原理 - 读写不阻塞
     */
    public void testMVCCBasicPrinciple() {
        System.out.println("\n【演示 1】MVCC 基本原理 - 读写不阻塞");
        System.out.println("──────────────────────────────────────");
        
        // 事务 A：读取数据（快照读）
        System.out.println("\n【事务 A】开始事务并读取数据（快照读）");
        txManager.beginTransaction(1);
        txManager.selectForSnapshot(1L);
        
        // 事务 B：修改数据（不阻塞）
        System.out.println("\n【事务 B】开始事务并修改数据（当前读，加锁）");
        txManager.beginTransaction(2);
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 800);
        txManager.updateForCurrent(1L, update);
        System.out.println("  ℹ️  事务 B 的修改不会阻塞事务 A 的读取");
        
        // 事务 A：再次读取（仍看到旧版本）
        System.out.println("\n【事务 A】再次读取（MVCC 看到旧版本）");
        txManager.selectForSnapshot(1L);
        System.out.println("  ✅ 事务 A 看到的是自己事务开始时的版本（1000 元）");
        System.out.println("  ℹ️  这就是 MVCC：多版本并发控制");
        
        // 提交事务
        txManager.commit(2);
        txManager.commit(1);
        
        txManager.printTable();
        txManager.printUndoLog();
        
        System.out.println("\n【MVCC 解决的问题】");
        System.out.println("  ✅ 读操作不阻塞写操作");
        System.out.println("  ✅ 写操作不阻塞读操作");
        System.out.println("  ✅ 提高并发读性能");
        System.out.println("  ✅ 基于 undo log 保存历史版本");
    }
    
    /**
     * 演示 2：快照读 vs 当前读
     */
    public void testSnapshotReadVsCurrentRead() {
        System.out.println("\n【演示 2】快照读 vs 当前读");
        System.out.println("──────────────────────────────────────");
        
        // 重置数据
        txManager = new TransactionManager();
        Map<String, Object> account = new HashMap<>();
        account.put("balance", 1000);
        txManager.insert(1L, account);
        
        // 事务 A：快照读（普通 SELECT）
        System.out.println("\n【事务 A】快照读（普通 SELECT）");
        txManager.beginTransaction(1);
        txManager.selectForSnapshot(1L);
        
        // 事务 B：当前读（SELECT ... FOR UPDATE）
        System.out.println("\n【事务 B】当前读（SELECT ... FOR UPDATE，加锁）");
        txManager.beginTransaction(2);
        txManager.updateForCurrent(1L, new HashMap<String, Object>() {{ put("balance", 800); }});
        txManager.commit(2);
        
        // 事务 A：再次快照读
        System.out.println("\n【事务 A】再次快照读");
        txManager.selectForSnapshot(1L);
        System.out.println("  ℹ️  快照读使用 MVCC，看到旧版本");
        
        // 事务 C：当前读
        System.out.println("\n【事务 C】当前读（SELECT ... FOR UPDATE）");
        txManager.beginTransaction(3);
        txManager.updateForCurrent(1L, new HashMap<String, Object>() {{ put("balance", 600); }});
        txManager.commit(3);
        System.out.println("  ℹ️  当前读会看到最新版本（800 元）");
        
        txManager.commit(1);
        
        txManager.printTable();
        
        System.out.println("\n【快照读 vs 当前读对比】");
        System.out.println("  📖 快照读（普通 SELECT）：");
        System.out.println("    • 使用 MVCC，读取历史版本");
        System.out.println("    • 不加锁，读写不阻塞");
        System.out.println("    • 基于 Read View 判断可见性");
        System.out.println();
        System.out.println("  🔒 当前读（SELECT ... FOR UPDATE）：");
        System.out.println("    • 读取最新版本");
        System.out.println("    • 需要加锁，可能阻塞");
        System.out.println("    • 用于需要强一致性的场景");
    }
    
    /**
     * 总结 MVCC 机制
     */
    public void printSummary() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     MVCC 机制总结                       ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("【MVCC 核心组件】");
        System.out.println("┌─────────────────┬────────────────────────────┐");
        System.out.println("│     组件        │         作用               │");
        System.out.println("├─────────────────┼────────────────────────────┤");
        System.out.println("│ 隐藏列          │ 存储事务 ID 和回滚指针       │");
        System.out.println("│ Undo Log        │ 记录历史版本               │");
        System.out.println("│ Read View       │ 判断版本可见性             │");
        System.out.println("│ 版本链          │ 通过回滚指针连接多个版本   │");
        System.out.println("└─────────────────┴────────────────────────────┘\n");
        
        System.out.println("【可见性判断规则】");
        System.out.println("  1️⃣  如果版本已提交 → 可见");
        System.out.println("  2️⃣  如果版本创建者是自己 → 可见");
        System.out.println("  3️⃣  如果版本未提交且不是自己 → 不可见");
        System.out.println();
        
        System.out.println("【MVCC 解决的问题】");
        System.out.println("  ✅ 读操作不阻塞写操作");
        System.out.println("  ✅ 写操作不阻塞读操作");
        System.out.println("  ✅ 提高并发读性能");
        System.out.println("  ✅ 减少锁竞争");
        System.out.println();
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  MVCC 是多版本并发控制，不是完全替代锁");
        System.out.println("  2️⃣  快照读使用 MVCC，当前读需要加锁");
        System.out.println("  3️⃣  Undo Log 用于回溯历史版本");
        System.out.println("  4️⃣  Read View 决定哪些版本可见");
        System.out.println("  5️⃣  MVCC 主要提升并发读性能");
        System.out.println();
        
        System.out.println("【注意事项】");
        System.out.println("  • MVCC 不能解决所有并发问题（如写写冲突仍需锁）");
        System.out.println("  • 长事务会导致 undo log 膨胀");
        System.out.println("  • 需要定期清理旧版本（purge 线程）");
        System.out.println();
    }
    
    public static void main(String[] args) {
        MVDemo demo = new MVDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testMVCCBasicPrinciple();
        demo.testSnapshotReadVsCurrentRead();
        demo.printSummary();
    }
}
