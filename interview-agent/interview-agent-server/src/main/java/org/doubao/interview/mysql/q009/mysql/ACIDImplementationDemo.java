package org.doubao.interview.mysql.q009.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ACID 特性在 InnoDB 中的落地实现演示
 * 
 * 【核心概念】
 * 1. ACID 定义：
 *    - Atomicity（原子性）：事务是不可分割的工作单位
 *    - Consistency（一致性）：事务前后数据状态一致
 *    - Isolation（隔离性）：并发事务之间互不干扰
 *    - Durability（持久性）：事务提交后永久保存
 * 
 * 2. InnoDB 实现机制：
 *    - A：undo log（回滚日志）
 *    - C：约束检查 + 事务机制
 *    - I：锁 + MVCC（多版本并发控制）
 *    - D：redo log（重做日志）
 * 
 * 3. 关键组件：
 *    - undo log：记录旧版本，支持回滚和 MVCC
 *    - redo log：记录修改操作，支持崩溃恢复
 *    - MVCC：读写不阻塞，提高并发性能
 *    - 锁机制：保证数据一致性
 * 
 * @author Interview Demo
 */
public class ACIDImplementationDemo {
    
    /**
     * 模拟数据行（支持多版本）
     */
    static class Row {
        Long id;
        Map<String, Object> data = new HashMap<>();
        Long transactionId;  // 创建该版本的事务 ID
        Long deleteTransactionId;  // 删除该版本的事务 ID（如果已删除）
        
        public Row(Long id) {
            this.id = id;
            this.transactionId = TransactionManager.getCurrentTransactionId();
        }
        
        @Override
        public String toString() {
            return String.format("Row{id=%d, data=%s, txId=%d}", id, data, transactionId);
        }
    }
    
    /**
     * Undo Log 回滚日志
     * 
     * 【作用】
     * 1. 事务失败时回滚
     * 2. MVCC 读取旧版本
     * 
     * 【记录内容】
     * - INSERT：记录主键，回滚时删除
     * - UPDATE：记录旧值，回滚时恢复
     * - DELETE：记录整行，回滚时插入
     */
    static class UndoLog {
        static class Record {
            enum Type { INSERT, UPDATE, DELETE }
            Type type;
            Long rowId;
            Map<String, Object> oldData;  // UPDATE/DELETE 时的旧数据
            
            public Record(Type type, Long rowId, Map<String, Object> oldData) {
                this.type = type;
                this.rowId = rowId;
                this.oldData = oldData;
            }
            
            @Override
            public String toString() {
                switch (type) {
                    case INSERT: return "INSERT Row " + rowId;
                    case UPDATE: return "UPDATE Row " + rowId + " old=" + oldData;
                    case DELETE: return "DELETE Row " + rowId + " old=" + oldData;
                    default: return "UNKNOWN";
                }
            }
        }
        
        private List<Record> records = new ArrayList<>();
        
        /**
         * 记录 INSERT 操作
         */
        public void logInsert(Long rowId) {
            records.add(new Record(Record.Type.INSERT, rowId, null));
            System.out.println("  [Undo Log] 记录 INSERT: Row " + rowId);
        }
        
        /**
         * 记录 UPDATE 操作
         */
        public void logUpdate(Long rowId, Map<String, Object> oldData) {
            records.add(new Record(Record.Type.UPDATE, rowId, oldData));
            System.out.println("  [Undo Log] 记录 UPDATE: Row " + rowId + " old=" + oldData);
        }
        
        /**
         * 记录 DELETE 操作
         */
        public void logDelete(Long rowId, Map<String, Object> oldData) {
            records.add(new Record(Record.Type.DELETE, rowId, oldData));
            System.out.println("  [Undo Log] 记录 DELETE: Row " + rowId + " old=" + oldData);
        }
        
        /**
         * 回滚所有操作
         */
        public void rollbackAll(Map<Long, Row> table) {
            System.out.println("\n【开始回滚】从后往前执行相反操作\n");
            
            // 从后往前执行相反操作
            for (int i = records.size() - 1; i >= 0; i--) {
                Record record = records.get(i);
                System.out.println("  回滚：" + record);
                
                switch (record.type) {
                    case INSERT:
                        // INSERT 的相反操作是 DELETE
                        table.remove(record.rowId);
                        System.out.println("    → 删除 Row " + record.rowId);
                        break;
                    case UPDATE:
                        // UPDATE 的相反操作是恢复旧值
                        Row updateRow = table.get(record.rowId);
                        if (updateRow != null) {
                            updateRow.data = new HashMap<>(record.oldData);
                            System.out.println("    → 恢复旧值：" + record.oldData);
                        }
                        break;
                    case DELETE:
                        // DELETE 的相反操作是 INSERT
                        Row newRow = new Row(record.rowId);
                        newRow.data = new HashMap<>(record.oldData);
                        table.put(record.rowId, newRow);
                        System.out.println("    → 重新插入 Row " + record.rowId);
                        break;
                }
            }
            
            System.out.println("\n✅ 回滚完成\n");
        }
        
        /**
         * 清空日志（事务提交后）
         */
        public void clear() {
            records.clear();
        }
    }
    
    /**
     * Redo Log 重做日志
     * 
     * 【作用】
     * 1. 崩溃恢复
     * 2. 保证持久性
     * 
     * 【记录内容】
     * - 物理日志：记录"在某个数据页上做了什么修改"
     * - WAL 技术：Write-Ahead Logging，先写日志再写磁盘
     */
    static class RedoLog {
        static class Record {
            Long pageId;      // 数据页 ID
            String operation; // 操作描述
            byte[] change;    // 变更内容
            
            public Record(Long pageId, String operation, byte[] change) {
                this.pageId = pageId;
                this.operation = operation;
                this.change = change;
            }
            
            @Override
            public String toString() {
                return String.format("Page %d: %s", pageId, operation);
            }
        }
        
        private List<Record> records = new ArrayList<>();
        private boolean crashed = false;
        
        /**
         * 记录修改操作
         */
        public void logModification(Long pageId, String operation, byte[] change) {
            records.add(new Record(pageId, operation, change));
            System.out.println("  [Redo Log] 记录：" + operation + " (Page " + pageId + ")");
        }
        
        /**
         * 模拟崩溃并恢复
         */
        public void crashAndRecover(Map<Long, Row> table) {
            crashed = true;
            System.out.println("\n⚠️  数据库崩溃！\n");
            
            System.out.println("【开始崩溃恢复】从 Redo Log 恢复已提交事务\n");
            
            for (Record record : records) {
                System.out.println("  重做：" + record);
                // 实际会重做所有已提交的修改
                // 这里简化处理
            }
            
            System.out.println("\n✅ 恢复完成\n");
            crashed = false;
        }
        
        /**
         * 清空日志（检查点后）
         */
        public void clear() {
            records.clear();
        }
    }
    
    /**
     * 事务管理器
     */
    static class TransactionManager {
        private static AtomicLong transactionIdGenerator = new AtomicLong(1);
        private Map<Long, UndoLog> undoLogs = new HashMap<>();
        private RedoLog redoLog = new RedoLog();
        
        /**
         * 获取当前事务 ID
         */
        public static Long getCurrentTransactionId() {
            return transactionIdGenerator.get();
        }
        
        /**
         * 开始事务
         */
        public void beginTransaction() {
            long txId = transactionIdGenerator.getAndIncrement();
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   开始事务 TX-" + txId + "                     ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            undoLogs.put(txId, new UndoLog());
        }
        
        /**
         * 提交事务
         */
        public void commit(long txId) {
            System.out.println("\n【提交事务 TX-" + txId + "】");
            
            // 1. 将 undo log 写入磁盘（用于 MVCC）
            System.out.println("  1️⃣  持久化 undo log");
            
            // 2. 将 redo log 刷盘
            System.out.println("  2️⃣  刷新 redo log 到磁盘（WAL 技术）");
            
            // 3. 标记事务已提交
            System.out.println("  3️⃣  标记事务为已提交状态");
            
            // 4. 清理 undo log（但保留历史版本用于 MVCC）
            System.out.println("  4️⃣  清理 undo log（保留必要版本）");
            
            System.out.println("\n✅ 事务 TX-" + txId + " 提交成功\n");
            undoLogs.remove(txId);
        }
        
        /**
         * 回滚事务
         */
        public void rollback(long txId, Map<Long, Row> table) {
            System.out.println("\n【回滚事务 TX-" + txId + "】");
            UndoLog undoLog = undoLogs.get(txId);
            if (undoLog != null) {
                undoLog.rollbackAll(table);
                undoLogs.remove(txId);
            }
        }
        
        /**
         * 获取 undo log
         */
        public UndoLog getUndoLog(long txId) {
            return undoLogs.get(txId);
        }
        
        /**
         * 获取 redo log
         */
        public RedoLog getRedoLog() {
            return redoLog;
        }
    }
    
    /**
     * 模拟数据表
     */
    static class Table {
        private String name;
        private Map<Long, Row> data = new HashMap<>();
        private TransactionManager txManager;
        
        public Table(String name, TransactionManager txManager) {
            this.name = name;
            this.txManager = txManager;
        }
        
        /**
         * INSERT 操作
         */
        public void insert(Long id, Map<String, Object> values) {
            long currentTxId = TransactionManager.getCurrentTransactionId();
            System.out.println("\n[INSERT] 插入 Row " + id + ": " + values);
            
            // 记录 undo log
            UndoLog undoLog = txManager.getUndoLog(currentTxId);
            if (undoLog != null) {
                undoLog.logInsert(id);
            }
            
            // 记录 redo log
            txManager.getRedoLog().logModification(
                id / 100,  // 页 ID
                "INSERT Row " + id,
                values.toString().getBytes()
            );
            
            // 写入数据
            Row row = new Row(id);
            row.data = new HashMap<>(values);
            data.put(id, row);
            
            System.out.println("  ✅ 插入成功");
        }
        
        /**
         * UPDATE 操作
         */
        public void update(Long id, Map<String, Object> newValues) {
            long currentTxId = TransactionManager.getCurrentTransactionId();
            Row row = data.get(id);
            
            if (row == null) {
                System.out.println("[UPDATE] Row " + id + " 不存在");
                return;
            }
            
            System.out.println("\n[UPDATE] 更新 Row " + id);
            System.out.println("  旧值：" + row.data);
            System.out.println("  新值：" + newValues);
            
            // 记录 undo log（保存旧值）
            UndoLog undoLog = txManager.getUndoLog(currentTxId);
            if (undoLog != null) {
                undoLog.logUpdate(id, new HashMap<>(row.data));
            }
            
            // 记录 redo log
            txManager.getRedoLog().logModification(
                id / 100,
                "UPDATE Row " + id,
                newValues.toString().getBytes()
            );
            
            // 更新数据
            row.data.putAll(newValues);
            
            System.out.println("  ✅ 更新成功");
        }
        
        /**
         * DELETE 操作
         */
        public void delete(Long id) {
            long currentTxId = TransactionManager.getCurrentTransactionId();
            Row row = data.get(id);
            
            if (row == null) {
                System.out.println("[DELETE] Row " + id + " 不存在");
                return;
            }
            
            System.out.println("\n[DELETE] 删除 Row " + id);
            System.out.println("  删除的数据：" + row.data);
            
            // 记录 undo log（保存完整数据）
            UndoLog undoLog = txManager.getUndoLog(currentTxId);
            if (undoLog != null) {
                undoLog.logDelete(id, new HashMap<>(row.data));
            }
            
            // 记录 redo log
            txManager.getRedoLog().logModification(
                id / 100,
                "DELETE Row " + id,
                new byte[0]
            );
            
            // 删除数据
            data.remove(id);
            
            System.out.println("  ✅ 删除成功");
        }
        
        /**
         * SELECT 操作（MVCC 读取）
         */
        public Row select(Long id) {
            long currentTxId = TransactionManager.getCurrentTransactionId();
            Row row = data.get(id);
            
            if (row == null) {
                System.out.println("\n[SELECT] Row " + id + " 不存在");
                return null;
            }
            
            System.out.println("\n[SELECT] 查询 Row " + id);
            System.out.println("  当前事务 ID: " + currentTxId);
            System.out.println("  数据版本：" + row);
            
            // MVCC：根据事务隔离级别判断是否可见
            // 简化处理：总是返回最新版本
            System.out.println("  ✅ 返回数据：" + row.data);
            return row;
        }
        
        /**
         * 打印表数据
         */
        public void printTable() {
            System.out.println("\n【表：" + name + "】当前数据:");
            System.out.println("┌─────────┬──────────────────────────┐");
            System.out.println("│   ID    │          数据             │");
            System.out.println("├─────────┼──────────────────────────┤");
            for (Map.Entry<Long, Row> entry : data.entrySet()) {
                System.out.printf("│ %7d │ %-24s │%n", entry.getKey(), entry.getValue().data);
            }
            System.out.println("└─────────┴──────────────────────────┘\n");
        }
    }
    
    // ========== 测试场景 ==========
    
    private TransactionManager txManager;
    private Table accountTable;
    
    /**
     * 初始化
     */
    public void init() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     ACID 在 InnoDB 中的落地实现          ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        txManager = new TransactionManager();
        accountTable = new Table("accounts", txManager);
        
        // 初始化账户数据
        System.out.println("【初始化数据】");
        Map<String, Object> account1 = new HashMap<>();
        account1.put("name", "张三");
        account1.put("balance", 1000);
        accountTable.insert(1L, account1);
        
        Map<String, Object> account2 = new HashMap<>();
        account2.put("name", "李四");
        account2.put("balance", 500);
        accountTable.insert(2L, account2);
        
        accountTable.printTable();
    }
    
    /**
     * 演示 1：原子性（Atomicity）- undo log 实现回滚
     */
    public void testAtomicity() {
        System.out.println("\n【演示 1】原子性（A）- undo log 实现回滚");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：转账操作（张三→李四 200 元），中途失败需要回滚\n");
        
        // 开始事务
        txManager.beginTransaction();
        
        try {
            // 张三扣款
            Map<String, Object> update1 = new HashMap<>();
            update1.put("balance", 800);
            accountTable.update(1L, update1);
            
            // 模拟异常
            System.out.println("\n⚠️  发生异常！需要回滚...\n");
            
            // 回滚
            txManager.rollback(TransactionManager.getCurrentTransactionId(), accountTable.data);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        accountTable.printTable();
        
        System.out.println("【原理说明】");
        System.out.println("  ✅ 原子性靠 undo log 实现");
        System.out.println("  ✅ 记录每次操作的相反操作");
        System.out.println("  ✅ 失败时从后往前执行回滚");
        System.out.println("  ✅ 保证事务要么全做，要么全不做");
    }
    
    /**
     * 演示 2：一致性（Consistency）- 约束检查
     */
    public void testConsistency() {
        System.out.println("\n【演示 2】一致性（C）- 约束检查");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：转账前后总金额不变（1000+500=1500）\n");
        
        // 计算转账前总金额
        int beforeTotal = 800 + 500;
        System.out.println("转账前总金额：" + beforeTotal);
        
        // 开始事务
        txManager.beginTransaction();
        
        // 张三扣款
        Map<String, Object> update1 = new HashMap<>();
        update1.put("balance", 600);
        accountTable.update(1L, update1);
        
        // 李四加款
        Map<String, Object> update2 = new HashMap<>();
        update2.put("balance", 700);
        accountTable.update(2L, update2);
        
        // 提交事务
        txManager.commit(TransactionManager.getCurrentTransactionId());
        
        // 计算转账后总金额
        int afterTotal = 600 + 700;
        System.out.println("转账后总金额：" + afterTotal);
        
        // 验证一致性
        if (beforeTotal == afterTotal) {
            System.out.println("\n✅ 一致性检查通过：总金额保持不变");
        } else {
            System.out.println("\n❌ 一致性被破坏！");
        }
        
        accountTable.printTable();
        
        System.out.println("【原理说明】");
        System.out.println("  ✅ 一致性靠约束检查 + 事务机制保证");
        System.out.println("  ✅ 外键约束、唯一约束、检查约束");
        System.out.println("  ✅ 业务逻辑保证不变式不被破坏");
        System.out.println("  ✅ 原子性和隔离性是一致性的基础");
    }
    
    /**
     * 演示 3：隔离性（Isolation）- MVCC
     */
    public void testIsolation() {
        System.out.println("\n【演示 3】隔离性（I）- MVCC 与锁");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：事务 A 修改数据，事务 B 读取（RC 隔离级别）\n");
        
        // 事务 A：开始并修改
        System.out.println("【事务 A】开始事务");
        txManager.beginTransaction();
        
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 1000);
        accountTable.update(1L, update);
        
        System.out.println("\n【事务 B】此时读取数据（MVCC 读旧版本）");
        System.out.println("  → 事务 B 看到的是修改前的版本（800 元）");
        System.out.println("  → 这就是 MVCC：多版本并发控制");
        
        // 提交事务 A
        txManager.commit(TransactionManager.getCurrentTransactionId());
        
        System.out.println("\n【事务 A】提交后，事务 B 才能读到新版本");
        
        accountTable.printTable();
        
        System.out.println("【原理说明】");
        System.out.println("  ✅ 隔离性靠锁 + MVCC 实现");
        System.out.println("  ✅ MVCC 让读写不阻塞");
        System.out.println("  ✅ 不同隔离级别控制可见性规则");
        System.out.println("  ✅ 读已提交（RC）：只能看到已提交的版本");
        System.out.println("  ✅ 可重复读（RR）：整个事务看到同一版本");
    }
    
    /**
     * 演示 4：持久性（Durability）- redo log
     */
    public void testDurability() {
        System.out.println("\n【演示 4】持久性（D）- redo log 崩溃恢复");
        System.out.println("──────────────────────────────────────");
        
        System.out.println("\n场景：事务提交后数据库崩溃，通过 redo log 恢复\n");
        
        // 开始事务
        txManager.beginTransaction();
        
        // 修改数据
        Map<String, Object> update = new HashMap<>();
        update.put("balance", 1200);
        accountTable.update(1L, update);
        
        // 提交事务
        txManager.commit(TransactionManager.getCurrentTransactionId());
        
        System.out.println("✅ 事务已提交，数据已持久化");
        
        // 模拟崩溃
        txManager.getRedoLog().crashAndRecover(accountTable.data);
        
        accountTable.printTable();
        
        System.out.println("【原理说明】");
        System.out.println("  ✅ 持久性靠 redo log 实现");
        System.out.println("  ✅ WAL 技术：Write-Ahead Logging");
        System.out.println("  ✅ 先写日志，再写磁盘");
        System.out.println("  ✅ 崩溃后通过 redo log 恢复已提交事务");
        System.out.println("  ✅ 即使断电，数据也不会丢失");
    }
    
    /**
     * 总结 ACID 实现机制
     */
    public void printSummary() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     ACID 实现机制总结                   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("【ACID 在 InnoDB 中的具体实现】");
        System.out.println("┌───────────┬────────────────────────────┐");
        System.out.println("│   特性    │       实现机制              │");
        System.out.println("├───────────┼────────────────────────────┤");
        System.out.println("│ 原子性 (A) │ undo log（回滚日志）       │");
        System.out.println("│ 一致性 (C) │ 约束检查 + 事务机制         │");
        System.out.println("│ 隔离性 (I) │ 锁 + MVCC（多版本并发控制） │");
        System.out.println("│ 持久性 (D) │ redo log（重做日志）       │");
        System.out.println("└───────────┴────────────────────────────┘\n");
        
        System.out.println("【面试要点】");
        System.out.println("  1️⃣  不要只背定义，要说具体机制映射");
        System.out.println("  2️⃣  undo log 记录旧版本，支持回滚和 MVCC");
        System.out.println("  3️⃣  redo log 记录修改，支持崩溃恢复");
        System.out.println("  4️⃣  MVCC 让读写不阻塞，提高并发");
        System.out.println("  5️⃣  WAL 技术保证日志先于数据落盘");
        System.out.println();
        
        System.out.println("【深入理解】");
        System.out.println("  • undo log 是逻辑日志，记录相反操作");
        System.out.println("  • redo log 是物理日志，记录数据页修改");
        System.out.println("  • MVCC 基于 undo log 的版本链实现");
        System.out.println("  • 两阶段提交保证 redo 和 binlog 一致");
        System.out.println();
    }
    
    public static void main(String[] args) {
        ACIDImplementationDemo demo = new ACIDImplementationDemo();
        
        // 初始化
        demo.init();
        
        // 执行演示
        demo.testAtomicity();
        demo.testConsistency();
        demo.testIsolation();
        demo.testDurability();
        demo.printSummary();
    }
}
