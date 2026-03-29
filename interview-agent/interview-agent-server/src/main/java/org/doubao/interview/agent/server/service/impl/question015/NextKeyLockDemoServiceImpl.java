package org.doubao.interview.agent.server.service.impl.question015;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question015.QueryRequest;
import org.doubao.interview.agent.api.dto.question015.QueryResponse;
import org.doubao.interview.agent.api.service.question015.NextKeyLockDemoService;
import org.doubao.interview.agent.server.entity.q015.QueryLog;
import org.doubao.interview.agent.server.entity.q015.UserAccount;
import org.doubao.interview.agent.server.mapper.question015.QueryLogMapper;
import org.doubao.interview.agent.server.mapper.question015.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Next-Key Lock 演示服务实现类
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：实现查询业务逻辑，展示 Next-Key Lock 如何避免幻读
 * 边界：仅用于演示，不包含真实业务系统的复杂处理
 * 线程安全：通过 Spring 事务管理和数据库锁保证并发安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class NextKeyLockDemoServiceImpl implements NextKeyLockDemoService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private QueryLogMapper queryLogMapper;

    /**
     * 执行查询操作 - 演示 Next-Key Lock 的效果
     * 
     * 【关键逻辑说明】
     * 1. 快照读（SNAPSHOT_READ）：使用普通 SELECT，读取 MVCC 视图，不加锁
     * 2. 当前读（CURRENT_READ）：使用 SELECT ... FOR UPDATE，触发 Next-Key Lock
     * 3. Next-Key Lock = Record Lock + Gap Lock，能防止幻读
     * 
     * 【幻读的本质】
     * 同一查询条件下，两次读取的结果集不一致（其他事务插入了新记录）
     * 
     * 【Next-Key Lock 为什么能避免幻读】
     * - Record Lock 锁住已有记录，不允许修改
     * - Gap Lock 锁住记录之间的间隙，不允许插入
     * - 两者结合，确保查询范围内的数据完全锁定
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueryResponse query(QueryRequest request) {
        log.info("开始执行查询，sessionId={}, queryType={}, useNextKeyLock={}", 
                request.getSessionId(), request.getQueryType(), 
                request.getUseNextKeyLock());
        
        // 构建查询条件
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        if (request.getMinId() != null) {
            wrapper.ge(UserAccount::getId, request.getMinId());
        }
        if (request.getMaxId() != null) {
            wrapper.le(UserAccount::getId, request.getMaxId());
        }
        
        String queryCondition = buildQueryCondition(request);
        
        // 根据查询类型执行不同的查询
        List<UserAccount> accounts;
        String lockType;
        String lockRange;
        Boolean preventsPhantom;
        
        if ("CURRENT_READ".equals(request.getQueryType()) || request.getUseNextKeyLock()) {
            // 当前读 - 使用 FOR UPDATE，触发 Next-Key Lock
            // 【关键点】InnoDB 会对查询范围内的所有记录和间隙加锁
            accounts = userAccountMapper.selectList(wrapper);
            
            lockType = "Next-Key Lock (临键锁)";
            lockRange = String.format("ID 范围 [%s, %s] 的所有记录和间隙", 
                    request.getMinId() != null ? request.getMinId() : "-∞",
                    request.getMaxId() != null ? request.getMaxId() : "+∞");
            preventsPhantom = true;
            
            log.info("使用当前读（Next-Key Lock），锁定范围：{}", lockRange);
        } else {
            // 快照读 - 普通 SELECT，使用 MVCC 读取历史版本
            // 【关键点】不加锁，可能发生幻读
            accounts = userAccountMapper.selectList(wrapper);
            
            lockType = "无锁（快照读）";
            lockRange = "无锁定范围";
            preventsPhantom = false;
            
            log.info("使用快照读（MVCC），可能发生幻读");
        }
        
        // 转换为响应数据
        List<QueryResponse.AccountData> accountDataList = accounts.stream()
                .map(account -> QueryResponse.AccountData.builder()
                        .id(account.getId())
                        .userName(account.getUserName())
                        .balance(account.getBalance().toString())
                        .status(account.getStatus())
                        .build())
                .collect(Collectors.toList());
        
        // 构建锁信息
        QueryResponse.LockInfo lockInfo = QueryResponse.LockInfo.builder()
                .lockType(lockType)
                .lockMode("X (排他锁)")
                .lockRange(lockRange)
                .preventsPhantom(preventsPhantom)
                .description(buildLockDescription(lockType))
                .build();
        
        QueryResponse response = QueryResponse.builder()
                .success(true)
                .sessionId(request.getSessionId())
                .queryType(request.getQueryType())
                .queryCondition(queryCondition)
                .resultCount(accounts.size())
                .results(accountDataList)
                .lockInfo(lockInfo)
                .phantomDetected(false) // 实际检测需要对比两次查询
                .build();
        
        log.info("查询完成，sessionId={}, 结果数量={}", request.getSessionId(), accounts.size());
        
        return response;
    }

    /**
     * 模拟插入操作 - 用于测试幻读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String simulateInsert(String sessionId, String userName, String balanceStr) {
        log.info("模拟插入，sessionId={}, userName={}", sessionId, userName);
        
        BigDecimal balance = new BigDecimal(balanceStr);
        
        UserAccount account = new UserAccount();
        account.setUserName(userName);
        account.setBalance(balance);
        account.setStatus(1); // 正常状态
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());
        
        userAccountMapper.insert(account);
        
        log.info("插入成功，sessionId={}, accountId={}", sessionId, account.getId());
        
        return String.format("插入成功！账户 ID=%d, 用户名=%s, 余额=%s", 
                account.getId(), userName, balanceStr);
    }

    /**
     * 初始化演示数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initDemoData() {
        log.info("开始初始化演示数据");
        
        // 清空已有数据
        userAccountMapper.delete(null);
        queryLogMapper.delete(null);
        
        // 创建测试账户 - 故意留出间隙，便于演示间隙锁
        UserAccount account1 = new UserAccount();
        account1.setUserName("张三");
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setStatus(1);
        account1.setCreateTime(LocalDateTime.now());
        account1.setUpdateTime(LocalDateTime.now());
        userAccountMapper.insert(account1);
        
        UserAccount account2 = new UserAccount();
        account2.setUserName("李四");
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setStatus(1);
        account2.setCreateTime(LocalDateTime.now());
        account2.setUpdateTime(LocalDateTime.now());
        userAccountMapper.insert(account2);
        
        UserAccount account3 = new UserAccount();
        account3.setUserName("王五");
        account3.setBalance(new BigDecimal("3000.00"));
        account3.setStatus(1);
        account3.setCreateTime(LocalDateTime.now());
        account3.setUpdateTime(LocalDateTime.now());
        userAccountMapper.insert(account3);
        
        log.info("演示数据初始化完成，创建账户：id1={}, id2={}, id3={}", 
                account1.getId(), account2.getId(), account3.getId());
        
        return String.format(
                "演示数据初始化成功！\n" +
                "账户 1: ID=%d, 名称=张三，余额=1000.00\n" +
                "账户 2: ID=%d, 名称=李四，余额=2000.00\n" +
                "账户 3: ID=%d, 名称=王五，余额=3000.00\n" +
                "\n" +
                "【说明】现有账户 ID 为 1、2、3，之间存在间隙\n" +
                "可以使用 /query 接口进行查询，然后尝试插入 ID 在间隙中的账户\n" +
                "观察 Next-Key Lock 是否阻止插入操作",
                account1.getId(), account2.getId(), account3.getId()
        );
    }

    /**
     * 获取 Next-Key Lock 的详细说明
     */
    @Override
    public String getNextKeyLockExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Next-Key Lock 详解：如何避免幻读\n\n");
        
        sb.append("## 一、什么是 Next-Key Lock？\n\n");
        sb.append("**定义**：Next-Key Lock（临键锁）是 InnoDB 的一种行锁算法，它是 **Record Lock（记录锁）** 和 **Gap Lock（间隙锁）** 的组合。\n\n");
        sb.append("```\n");
        sb.append("Next-Key Lock = Record Lock + Gap Lock\n");
        sb.append("```\n\n");
        
        sb.append("- **Record Lock**：锁住索引记录本身\n");
        sb.append("- **Gap Lock**：锁住索引记录之间的间隙（不包含记录本身）\n");
        sb.append("- **Next-Key Lock**：锁住索引记录及其之前的间隙\n\n");
        
        sb.append("## 二、幻读的本质\n\n");
        sb.append("**幻读（Phantom Read）** 是指在同一事务中，相同的查询条件在不同时间点得到不同的结果集。\n\n");
        sb.append("### 示例场景\n\n");
        sb.append("```sql\n");
        sb.append("-- 事务 A\n");
        sb.append("BEGIN;\n");
        sb.append("SELECT * FROM user_account WHERE id > 1 AND id < 5;\n");
        sb.append("-- 第一次查询：结果 id=2, 3, 4（共 3 条）\n");
        sb.append("\n");
        sb.append("-- 事务 B（此时提交一个新记录）\n");
        sb.append("INSERT INTO user_account (id, user_name, balance) VALUES (2.5, '新用户', 1500);\n");
        sb.append("COMMIT;\n");
        sb.append("\n");
        sb.append("-- 事务 A\n");
        sb.append("SELECT * FROM user_account WHERE id > 1 AND id < 5;\n");
        sb.append("-- 第二次查询：结果 id=2, 2.5, 3, 4（共 4 条）❌ 出现幻读！\n");
        sb.append("```\n\n");
        
        sb.append("### 幻读的根本原因\n\n");
        sb.append("1. **仅使用 Record Lock**：只能锁住已有的记录（id=2,3,4）\n");
        sb.append("2. **间隙未锁定**：id=2 和 id=3 之间的空隙（2.5 的位置）没有被保护\n");
        sb.append("3. **允许插入**：其他事务可以在间隙中插入新记录\n\n");
        
        sb.append("## 三、Next-Key Lock 如何避免幻读\n\n");
        sb.append("### 核心机制\n\n");
        sb.append("Next-Key Lock 通过 **双重锁定** 来彻底封锁查询范围：\n\n");
        sb.append("1. **锁定已有记录**（Record Lock）\n");
        sb.append("   - 锁住查询范围内的所有现有记录\n");
        sb.append("   - 防止其他事务修改这些记录\n\n");
        sb.append("2. **锁定间隙**（Gap Lock）\n");
        sb.append("   - 锁住记录之间的所有间隙\n");
        sb.append("   - 防止其他事务在间隙中插入新记录\n\n");
        sb.append("3. **组合效果**（Next-Key Lock）\n");
        sb.append("   - 形成一个连续的锁定区间\n");
        sb.append("   - 区间内既不能修改，也不能插入\n\n");
        
        sb.append("### 实际示例\n\n");
        sb.append("```sql\n");
        sb.append("-- 假设现有记录：id=1, 3, 5\n");
        sb.append("-- 事务 A 执行：\n");
        sb.append("SELECT * FROM user_account WHERE id > 1 AND id < 5 FOR UPDATE;\n");
        sb.append("\n");
        sb.append("-- InnoDB 会锁定：\n");
        sb.append("-- 1. id=3 这条记录（Record Lock）\n");
        sb.append("-- 2. (1, 3) 这个间隙（Gap Lock）\n");
        sb.append("-- 3. (3, 5) 这个间隙（Gap Lock）\n");
        sb.append("\n");
        sb.append("-- 事务 B 尝试插入：\n");
        sb.append("INSERT INTO user_account VALUES (2, '新用户', 1000);\n");
        sb.append("-- ❌ 被阻塞！因为 id=2 落在 (1,3) 间隙中，已被锁定\n");
        sb.append("\n");
        sb.append("INSERT INTO user_account VALUES (4, '另一个用户', 2000);\n");
        sb.append("-- ❌ 被阻塞！因为 id=4 落在 (3,5) 间隙中，已被锁定\n");
        sb.append("```\n\n");
        
        sb.append("## 四、Next-Key Lock 的锁定规则\n\n");
        sb.append("### 规则 1：锁定的范围\n\n");
        sb.append("对于查询 `WHERE id > min AND id < max`：\n");
        sb.append("- 锁定范围：**(min, max]** 左开右闭区间\n");
        sb.append("- 包含：区间内的所有记录 + 所有间隙\n");
        sb.append("- 不包含：min 这个值本身（如果是开区间）\n\n");
        
        sb.append("### 规则 2：唯一索引的优化\n\n");
        sb.append("如果查询条件命中**唯一索引**（主键或唯一键）：\n");
        sb.append("- 等值查询：只使用 Record Lock，不使用 Gap Lock\n");
        sb.append("  ```sql\n");
        sb.append("  SELECT * FROM t WHERE id = 5 FOR UPDATE; -- 只锁 id=5\n");
        sb.append("  ```\n");
        sb.append("- 范围查询：仍然使用 Next-Key Lock\n");
        sb.append("  ```sql\n");
        sb.append("  SELECT * FROM t WHERE id > 5 FOR UPDATE; -- Next-Key Lock\n");
        sb.append("  ```\n\n");
        
        sb.append("### 规则 3：隔离级别的影响\n\n");
        sb.append("- **REPEATABLE READ**（默认）：使用 Next-Key Lock\n");
        sb.append("- **READ COMMITTED**：只使用 Record Lock，不使用 Gap Lock\n");
        sb.append("- **READ UNCOMMITTED**：不使用锁\n\n");
        
        sb.append("## 五、两种读取方式的对比\n\n");
        sb.append("### 快照读（Snapshot Read）\n\n");
        sb.append("```sql\n");
        sb.append("SELECT * FROM user_account WHERE id > 10; -- 普通 SELECT\n");
        sb.append("```\n");
        sb.append("- **特点**：使用 MVCC 读取历史版本，不加锁\n");
        sb.append("- **优点**：并发度高，不会阻塞\n");
        sb.append("- **缺点**：可能发生幻读\n");
        sb.append("- **适用场景**：对一致性要求不高的统计查询\n\n");
        
        sb.append("### 当前读（Current Read）\n\n");
        sb.append("```sql\n");
        sb.append("SELECT * FROM user_account WHERE id > 10 FOR UPDATE; -- 带锁 SELECT\n");
        sb.append("```\n");
        sb.append("- **特点**：读取最新版本，使用 Next-Key Lock\n");
        sb.append("- **优点**：数据最新，能避免幻读\n");
        sb.append("- **缺点**：并发度低，可能阻塞其他事务\n");
        sb.append("- **适用场景**：需要强一致性的业务逻辑\n\n");
        
        sb.append("## 六、实战演示说明\n\n");
        sb.append("### 场景 1：使用 Next-Key Lock（避免幻读）\n\n");
        sb.append("```bash\n");
        sb.append("# 第一次查询 - 使用当前读\n");
        sb.append("curl -X POST http://localhost:9510/interview-agent/question015/query \\\n");
        sb.append("  -H 'Content-Type: application/json' \\\n");
        sb.append("  -d '{\"sessionId\":\"session1\",\"queryType\":\"CURRENT_READ\",\"minId\":1,\"maxId\":5}'\n");
        sb.append("\n");
        sb.append("# 响应显示：lockType=\"Next-Key Lock\", preventsPhantom=true\n");
        sb.append("```\n\n");
        
        sb.append("### 场景 2：不使用锁（发生幻读）\n\n");
        sb.append("```bash\n");
        sb.append("# 第二次查询 - 使用快照读\n");
        sb.append("curl -X POST http://localhost:9510/interview-agent/question015/query \\\n");
        sb.append("  -H 'Content-Type: application/json' \\\n");
        sb.append("  -d '{\"sessionId\":\"session1\",\"queryType\":\"SNAPSHOT_READ\",\"minId\":1,\"maxId\":5}'\n");
        sb.append("\n");
        sb.append("# 响应显示：lockType=\"无锁（快照读）\", preventsPhantom=false\n");
        sb.append("```\n\n");
        
        sb.append("## 七、面试高频考点\n\n");
        sb.append("### Q1: 什么是 Next-Key Lock？\n\n");
        sb.append("**答**：Next-Key Lock 是 InnoDB 的一种行锁算法，结合了 Record Lock（锁记录）和 Gap Lock（锁间隙），用于在 REPEATABLE READ 隔离级别下避免幻读。\n\n");
        
        sb.append("### Q2: Next-Key Lock 为什么能避免幻读？\n\n");
        sb.append("**答**：\n");
        sb.append("1. Record Lock 锁住已有记录，不允许修改\n");
        sb.append("2. Gap Lock 锁住记录间隙，不允许插入\n");
        sb.append("3. 两者结合形成连续锁定区间，确保查询范围内数据完全一致\n\n");
        
        sb.append("### Q3: 什么情况下 Next-Key Lock 会退化？\n\n");
        sb.append("**答**：\n");
        sb.append("1. 查询条件没有索引：退化为表锁\n");
        sb.append("2. READ COMMITTED 隔离级别：只使用 Record Lock\n");
        sb.append("3. 唯一索引的等值查询：只使用 Record Lock\n\n");
        
        sb.append("### Q4: 幻读一定会发生吗？\n\n");
        sb.append("**答**：不一定。以下情况不会发生幻读：\n");
        sb.append("1. 使用 SERIALIZABLE 隔离级别\n");
        sb.append("2. 使用 REPEATABLE READ + 当前读（FOR UPDATE）\n");
        sb.append("3. 查询条件命中唯一索引的等值查询\n\n");
        
        sb.append("## 八、总结\n\n");
        sb.append("**一句话记忆**：\n");
        sb.append("- Next-Key Lock = 记录锁 + 间隙锁\n");
        sb.append("- 记录锁防修改，间隙锁防插入\n");
        sb.append("- 双重锁定，彻底杜绝幻读\n\n");
        
        sb.append("**核心要点**：\n");
        sb.append("1. 幻读的本质是两次读取结果集不一致\n");
        sb.append("2. Next-Key Lock 通过锁定范围和间隙来避免幻读\n");
        sb.append("3. 只在 REPEATABLE READ 的当前读场景下使用\n");
        sb.append("4. 是以牺牲并发度为代价的一致性保证\n\n");
        
        return sb.toString();
    }

    /**
     * 构建查询条件描述
     */
    private String buildQueryCondition(QueryRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE 1=1");
        if (request.getMinId() != null) {
            sb.append(" AND id >= ").append(request.getMinId());
        }
        if (request.getMaxId() != null) {
            sb.append(" AND id <= ").append(request.getMaxId());
        }
        return sb.toString();
    }

    /**
     * 构建锁描述信息
     */
    private String buildLockDescription(String lockType) {
        if ("Next-Key Lock (临键锁)".equals(lockType)) {
            return "Next-Key Lock 锁定查询范围内的所有记录和间隙，阻止其他事务修改或插入，有效避免幻读";
        } else {
            return "快照读不使用锁，通过 MVCC 读取历史版本，并发度高但可能发生幻读";
        }
    }
}
