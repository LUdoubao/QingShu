package org.doubao.interview.agent.server.service.impl.mysql.q012;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question012.TransferRequest;
import org.doubao.interview.agent.api.dto.question012.TransferResponse;
import org.doubao.interview.agent.api.service.question012.ThreeLogsDemoService;
import org.doubao.interview.agent.server.entity.q012.Account;
import org.doubao.interview.agent.server.entity.q012.TransactionLog;
import org.doubao.interview.agent.server.mapper.question012.AccountMapper;
import org.doubao.interview.agent.server.mapper.question012.TransactionLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 三种日志演示服务实现类
 * 
 * 对应面试知识点：问题 012 - undo log、redo log、binlog 的区别
 * 
 * 【类注释】
 * 职责：实现转账业务逻辑，展示 MySQL 事务中三种日志的协同工作机制
 * 边界：仅用于演示，不包含真实金融系统的复杂校验（如风控、反洗钱等）
 * 线程安全：通过 Spring 事务管理和数据库锁保证并发安全
 * 幂等性：通过 transactionNo 唯一索引保证同一交易的幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class ThreeLogsDemoServiceImpl implements ThreeLogsDemoService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    /**
     * 执行转账操作 - 演示三种日志的完整流程
     * 
     * 【关键逻辑说明】
     * 1. 开启事务后，MySQL 会自动记录 undo log（用于回滚）
     * 2. 数据修改时，InnoDB 会先写 redo log（WAL 技术）
     * 3. 事务提交时，Server 层会写入 binlog（用于复制和恢复）
     * 4. 两阶段提交确保 redo log 和 binlog 的一致性
     * 
     * 【为什么需要两阶段提交】
     * 如果不使用两阶段提交，可能出现：
     * - redo log 已写入但 binlog 未写入：主从数据不一致
     * - binlog 已写入但 redo log 未写入：崩溃恢复时数据丢失
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request) {
        String transactionNo = request.getTransactionNo() != null 
                ? request.getTransactionNo() : UUID.randomUUID().toString();
        
        log.info("开始处理转账请求，transactionNo={}, fromAccountId={}, toAccountId={}, amount={}", 
                transactionNo, request.getFromAccountId(), 
                request.getToAccountId(), request.getAmount());
        
        // 1. 查询转出账户
        Account fromAccount = accountMapper.selectById(request.getFromAccountId());
        if (fromAccount == null) {
            log.error("转出账户不存在，accountId={}", request.getFromAccountId());
            throw new RuntimeException("转出账户不存在");
        }
        
        // 2. 查询转入账户
        Account toAccount = accountMapper.selectById(request.getToAccountId());
        if (toAccount == null) {
            log.error("转入账户不存在，accountId={}", request.getToAccountId());
            throw new RuntimeException("转入账户不存在");
        }
        
        // 3. 检查余额是否充足
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            log.error("余额不足，accountId={}, currentBalance={}, requiredAmount={}", 
                    request.getFromAccountId(), fromAccount.getBalance(), request.getAmount());
            throw new RuntimeException("余额不足");
        }
        
        // 保存交易前的余额 - 用于展示 undo log 的作用
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();
        
        // 4. 执行扣款操作 - 此时 InnoDB 引擎开始工作
        // 【关键点】这里会产生三种日志：
        // a) undo log: 记录反向操作 UPDATE account SET balance=fromBalanceBefore WHERE id=fromAccount.getId()
        // b) redo log: 记录数据页的物理修改（循环写入）
        // c) binlog: 记录 SQL 语句的逻辑变更（追加写入）
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        fromAccount.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(fromAccount);
        
        // 5. 执行入账操作
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        toAccount.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(toAccount);
        
        // 6. 模拟异常 - 用于演示 undo log 的回滚功能
        if (Boolean.TRUE.equals(request.getSimulateException())) {
            log.warn("模拟异常触发，将执行回滚操作，transactionNo={}", transactionNo);
            // 【重要】抛出异常后，Spring 会回滚事务
            // MySQL 会使用 undo log 将数据恢复到事务开始前的状态
            // 这就是 undo log 的核心作用：事务回滚
            throw new RuntimeException("模拟异常 - 演示 undo log 回滚");
        }
        
        // 7. 记录交易日志 - 模拟记录三种日志的信息
        TransactionLog logEntity = new TransactionLog();
        logEntity.setTransactionNo(transactionNo);
        logEntity.setFromAccountId(request.getFromAccountId());
        logEntity.setToAccountId(request.getToAccountId());
        logEntity.setAmount(request.getAmount());
        logEntity.setBalanceBefore(fromBalanceBefore);
        logEntity.setBalanceAfter(fromAccount.getBalance());
        logEntity.setStatus(1); // 成功
        logEntity.setUndoLogId("undo_" + System.currentTimeMillis());
        logEntity.setRedoLogId("redo_" + System.nanoTime());
        logEntity.setBinlogId("binlog_" + UUID.randomUUID().toString().substring(0, 8));
        logEntity.setRemark(request.getRemark());
        logEntity.setCreateTime(LocalDateTime.now());
        transactionLogMapper.insert(logEntity);
        
        // 8. 构建响应信息 - 展示三种日志的详细信息
        TransferResponse.UndoLogInfo undoLogInfo = TransferResponse.UndoLogInfo.builder()
                .undoLogId(logEntity.getUndoLogId())
                .operationType("UPDATE")
                .rollbackSql(String.format("UPDATE account_012 SET balance=%s WHERE id=%d", 
                        fromBalanceBefore, request.getFromAccountId()))
                .description("undo log 记录反向操作，用于事务回滚。当事务失败时，MySQL 执行回滚 SQL 将数据恢复到事务开始前的状态")
                .build();
        
        TransferResponse.RedoLogInfo redoLogInfo = TransferResponse.RedoLogInfo.builder()
                .redoLogId(logEntity.getRedoLogId())
                .pageId("page_" + request.getFromAccountId())
                .beforeImage("0x" + fromBalanceBefore.toString().hashCode())
                .afterImage("0x" + fromAccount.getBalance().toString().hashCode())
                .lsn(System.nanoTime())
                .description("redo log 记录数据页的物理修改，采用 WAL 技术循环写入。数据库崩溃后，通过重放 redo log 恢复已提交的数据")
                .build();
        
        TransferResponse.BinlogInfo binlogInfo = TransferResponse.BinlogInfo.builder()
                .binlogId(logEntity.getBinlogId())
                .binlogFileName("mysql-bin.000001")
                .position(System.currentTimeMillis())
                .executedSql(String.format("UPDATE account_012 SET balance=%s WHERE id=%d", 
                        fromAccount.getBalance(), request.getFromAccountId()))
                .eventType("Update")
                .description("binlog 记录 SQL 语句的逻辑变更，追加写入。用于主从复制、数据恢复和审计。通过两阶段提交与 redo log 保持一致")
                .build();
        
        TransferResponse response = TransferResponse.builder()
                .success(true)
                .transactionNo(transactionNo)
                .message("转账成功")
                .fromBalanceBefore(fromBalanceBefore)
                .fromBalanceAfter(fromAccount.getBalance())
                .toBalanceBefore(toBalanceBefore)
                .toBalanceAfter(toAccount.getBalance())
                .undoLogInfo(undoLogInfo)
                .redoLogInfo(redoLogInfo)
                .binlogInfo(binlogInfo)
                .build();
        
        log.info("转账处理成功，transactionNo={}, fromAccount新余额={}, toAccount新余额={}", 
                transactionNo, fromAccount.getBalance(), toAccount.getBalance());
        
        return response;
    }

    /**
     * 查询账户信息
     */
    @Override
    public String getAccountInfo(Long accountId) {
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            return String.format("{\"error\": \"账户%d不存在\"}", accountId);
        }
        
        return String.format(
                "{\"id\":%d,\"accountName\":\"%s\",\"balance\":%s,\"createTime\":\"%s\",\"updateTime\":\"%s\"}",
                account.getId(),
                account.getAccountName(),
                account.getBalance().toString(),
                account.getCreateTime(),
                account.getUpdateTime()
        );
    }

    /**
     * 初始化演示数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initDemoData() {
        log.info("开始初始化演示数据");
        
        // 清空已有数据
        accountMapper.delete(null);
        transactionLogMapper.delete(null);
        
        // 创建两个测试账户
        Account account1 = new Account();
        account1.setAccountName("张三账户");
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setVersion(0);
        account1.setCreateTime(LocalDateTime.now());
        account1.setUpdateTime(LocalDateTime.now());
        accountMapper.insert(account1);
        
        Account account2 = new Account();
        account2.setAccountName("李四账户");
        account2.setBalance(new BigDecimal("1000.00"));
        account2.setVersion(0);
        account2.setCreateTime(LocalDateTime.now());
        account2.setUpdateTime(LocalDateTime.now());
        accountMapper.insert(account2);
        
        log.info("演示数据初始化完成，创建账户：id1={}, id2={}", account1.getId(), account2.getId());
        
        return String.format(
                "演示数据初始化成功！\n" +
                "账户 1: ID=%d, 名称=%s, 余额=1000.00\n" +
                "账户 2: ID=%d, 名称=%s, 余额=1000.00\n" +
                "现在可以调用 /transfer 接口进行转账演示",
                account1.getId(), account1.getAccountName(),
                account2.getId(), account2.getAccountName()
        );
    }

    /**
     * 获取三种日志的详细对比说明
     */
    @Override
    public String getLogsComparison() {
        StringBuilder sb = new StringBuilder();
        sb.append("# MySQL 三种日志详解：undo log、redo log、binlog\n\n");
        
        sb.append("## 一、核心区别对比表\n\n");
        sb.append("| 特性 | undo log | redo log | binlog |\n");
        sb.append("|------|----------|----------|--------|\n");
        sb.append("| **所属层级** | InnoDB 引擎层 | InnoDB 引擎层 | MySQL Server 层 |\n");
        sb.append("| **日志类型** | 逻辑日志（记录反向操作） | 物理日志（记录数据页修改） | 逻辑日志（记录 SQL 语句） |\n");
        sb.append("| **写入方式** | 连续写入 | 循环写入（环形缓冲区） | 追加写入 |\n");
        sb.append("| **主要作用** | 事务回滚、MVCC | 崩溃恢复（crash-safe） | 主从复制、数据恢复、审计 |\n");
        sb.append("| **记录内容** | 数据的旧版本（反向操作） | 数据页的物理修改（What+Where） | SQL 语句的逻辑变更 |\n");
        sb.append("| **生命周期** | 事务提交后可删除 | 持久化到磁盘，无需删除 | 定期归档，可删除 |\n");
        sb.append("| **文件大小** | 相对较小 | 固定大小（默认 4GB） | 持续增长（需定期清理） |\n");
        sb.append("| **刷盘时机** | 随事务提交 | 后台线程定期刷盘 | 事务提交时 |\n\n");
        
        sb.append("## 二、详细作用说明\n\n");
        
        sb.append("### 2.1 undo log - 回滚日志\n\n");
        sb.append("**核心作用：**\n");
        sb.append("1. **事务回滚**：记录反向操作，事务失败时恢复数据\n");
        sb.append("   - INSERT -> 反向操作：DELETE\n");
        sb.append("   - UPDATE -> 反向操作：UPDATE(旧值)\n");
        sb.append("   - DELETE -> 反向操作：INSERT\n\n");
        sb.append("2. **MVCC（多版本并发控制）**：保存数据的历史版本，实现非阻塞读\n");
        sb.append("   - Read View + undo log 链 -> 获取历史版本数据\n");
        sb.append("   - 支持 READ COMMITTED 和 REPEATABLE READ 隔离级别\n\n");
        
        sb.append("**示例场景：**\n");
        sb.append("```sql\n");
        sb.append("-- 原始数据：balance = 200\n");
        sb.append("UPDATE account SET balance = 100 WHERE id = 1;\n");
        sb.append("-- undo log 记录：UPDATE account SET balance = 200 WHERE id = 1;\n");
        sb.append("-- 如果事务回滚，执行上述 undo log 即可恢复\n");
        sb.append("```\n\n");
        
        sb.append("### 2.2 redo log - 重做日志\n\n");
        sb.append("**核心作用：**\n");
        sb.append("1. **崩溃恢复（crash-safe）**：采用 WAL（Write-Ahead Logging）技术\n");
        sb.append("   - 先写日志，再写磁盘（避免随机 IO）\n");
        sb.append("   - 数据库宕机后，重放 redo log 恢复已提交数据\n\n");
        sb.append("2. **两阶段提交**：确保 redo log 和 binlog 一致性\n");
        sb.append("   - Prepare 阶段：写入 redo log\n");
        sb.append("   - Commit 阶段：写入 binlog，然后提交 redo log\n\n");
        
        sb.append("**写入流程：**\n");
        sb.append("```text\n");
        sb.append("1. 修改内存中的数据页（Dirty Page）\n");
        sb.append("2. 写入 redo log buffer（内存）\n");
        sb.append("3. 刷写到 redo log file（磁盘，顺序写入）\n");
        sb.append("4. 后台线程异步刷盘到数据文件（随机 IO）\n");
        sb.append("```\n\n");
        
        sb.append("### 2.3 binlog - 归档日志\n\n");
        sb.append("**核心作用：**\n");
        sb.append("1. **主从复制**：Master 将 binlog 发送给 Slave，Slave 重放 SQL\n");
        sb.append("2. **数据恢复**：通过 mysqlbinlog 工具恢复指定时间范围的数据\n");
        sb.append("3. **审计追踪**：记录所有 DDL 和 DML 操作，便于追溯\n\n");
        
        sb.append("**记录格式：**\n");
        sb.append("- STATEMENT：记录原始 SQL 语句（体积小，但可能不安全）\n");
        sb.append("- ROW：记录行的变更（精确，但体积大）\n");
        sb.append("- MIXED：混合模式（默认使用 STATEMENT，必要时切换 ROW）\n\n");
        
        sb.append("## 三、三者协同工作流程\n\n");
        sb.append("以转账操作为例：\n\n");
        sb.append("```text\n");
        sb.append("1. 开启事务 BEGIN\n");
        sb.append("   ↓\n");
        sb.append("2. 执行 UPDATE 扣款操作\n");
        sb.append("   - InnoDB 记录 undo log（反向操作）\n");
        sb.append("   - 修改内存中的数据页\n");
        sb.append("   - 写入 redo log buffer\n");
        sb.append("   ↓\n");
        sb.append("3. 执行 UPDATE 入账操作\n");
        sb.append("   - 同上流程\n");
        sb.append("   ↓\n");
        sb.append("4. 提交事务 COMMIT\n");
        sb.append("   - 【Prepare 阶段】写入 redo log（标记为 prepare）\n");
        sb.append("   - 【Commit 阶段】写入 binlog\n");
        sb.append("   - 【Commit 阶段】提交 redo log（标记为 commit）\n");
        sb.append("   ↓\n");
        sb.append("5. 后台线程异步刷盘\n");
        sb.append("   - redo log 刷到数据文件\n");
        sb.append("   - binlog 持续追加\n");
        sb.append("```\n\n");
        
        sb.append("## 四、面试高频考点\n\n");
        sb.append("1. **为什么需要两阶段提交？**\n");
        sb.append("   答：保证 redo log 和 binlog 的一致性，避免主从复制或崩溃恢复时数据不一致\n\n");
        
        sb.append("2. **undo log 和 redo log 的区别？**\n");
        sb.append("   答：undo 是逻辑日志用于回滚，redo 是物理日志用于恢复；undo 记录反向操作，redo 记录数据页修改\n\n");
        
        sb.append("3. **redo log 和 binlog 的区别？**\n");
        sb.append("   答：redo 是 InnoDB 引擎层的物理日志，循环写入，用于崩溃恢复；binlog 是 Server 层的逻辑日志，追加写入，用于主从复制\n\n");
        
        sb.append("4. **InnoDB 为什么比 MyISAM 更安全？**\n");
        sb.append("   答：InnoDB 有 redo log 实现 crash-safe，MyISAM 没有；InnoDB 支持事务和行级锁\n\n");
        
        return sb.toString();
    }
}
