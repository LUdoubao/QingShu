# 问题 014: InnoDB 锁的分类 - 总结文档

## 一、核心知识点回顾

### 1.1 锁的分类体系

InnoDB 锁可以从两个维度进行分类：

**按语义分类**：
- **共享锁 (S 锁)**：读锁，允许其他事务读，不允许写
- **排他锁 (X 锁)**：写锁，不允许其他事务读写
- **意向共享锁 (IS 锁)**：表级锁，事务准备给数据行加 S 锁
- **意向排他锁 (IX 锁)**：表级锁，事务准备给数据行加 X 锁

**按粒度分类**：
- **表级锁**：锁定整个表，开销小，易冲突
- **行级锁**：锁定特定行，开销大，并发高（InnoDB 主要使用）
- **页级锁**：锁定数据页，InnoDB 不支持

**行级锁的细分类型**：
- **记录锁 (Record Lock)**：锁住索引记录本身
- **间隙锁 (Gap Lock)**：锁住索引记录之间的间隙
- **临键锁 (Next-Key Lock)**：记录锁 + 间隙锁

### 1.2 形象比喻

- **共享锁 (S 锁)** 像"图书馆的书"：多人可以同时阅读，但不能涂改
- **排他锁 (X 锁)** 像"私人笔记本"：只有所有者能读写，他人不能碰
- **意向锁** 像"楼层指示牌"：快速知道某层楼是否有人，无需逐户检查
- **间隙锁** 像"占车位"：虽然车位是空的，但不允许别人停进来

## 二、详细锁类型说明

### 2.1 共享锁 (S 锁) 和排他锁 (X 锁)

#### 共享锁 (S 锁)
- **别名**：读锁
- **特点**：
  - 允许事务读取一行数据
  - 多个事务可以同时持有 S 锁（共享）
  - 持有 S 锁时，其他事务不能持有 X 锁
- **典型场景**：`SELECT ... LOCK IN SHARE MODE`
- **兼容性**：S 锁与 S 锁兼容，与 X 锁不兼容

#### 排他锁 (X 锁)
- **别名**：写锁
- **特点**：
  - 允许事务更新或删除一行数据
  - 一个事务持有 X 锁后，其他事务不能再持有任何锁
  - 独占性，互斥访问
- **典型场景**：UPDATE、DELETE、INSERT、`SELECT ... FOR UPDATE`
- **兼容性**：与任何锁都不兼容

#### 兼容性矩阵

```
         |  S 锁  |  X 锁  |
---------|-------|-------|
S 锁     |  √   |  ×   |
X 锁     |  ×   |  ×   |
```

### 2.2 意向锁 (Intention Lock)

#### 作用
- **表级锁**，用于快速判断表中是否有记录被加锁
- 避免表锁和行锁的冲突检查需要遍历所有行
- **InnoDB 自动添加**，无需人工干预

#### 规则
- 事务要给某行加 S 锁，必须先获得表的 IS 锁
- 事务要给某行加 X 锁，必须先获得表的 IX 锁
- IS 锁与 IX 锁兼容，但都与表锁不兼容

#### 示例
```sql
-- 事务 1
BEGIN;
SELECT * FROM product WHERE id = 1 LOCK IN SHARE MODE;
-- 此时自动添加：表的 IS 锁 + 记录的 S 锁

-- 事务 2
BEGIN;
UPDATE product SET stock = stock - 1 WHERE id = 2;
-- 需要获取：表的 IX 锁 + 记录的 X 锁
-- 由于 IS 与 IX 兼容，可以成功获取
```

### 2.3 记录锁 (Record Lock)

#### 特点
- 锁住**索引记录本身**
- **必须依赖索引**，否则退化为表锁
- 主键索引和唯一索引都会使用记录锁

#### 示例
```sql
-- 假设 id 是主键
SELECT * FROM product WHERE id = 1 FOR UPDATE;
-- 只会锁住 id=1 这一条记录（Record Lock）
-- 其他事务可以访问 id=2, id=3 等其他记录
```

#### 关键点
- 如果查询条件命中主键或唯一索引：使用 Record Lock
- 锁定的只是索引记录，不包括间隙

### 2.4 间隙锁 (Gap Lock)

#### 特点
- 锁住**索引记录之间的间隙**，不包含记录本身
- 用于防止幻读（Phantom Read）
- **只在 REPEATABLE READ 隔离级别下出现**

#### 示例
```sql
-- 假设 id 字段有索引，现有记录：1, 5, 10
SELECT * FROM product WHERE id > 3 AND id < 8 FOR UPDATE;
-- 会锁住以下间隙：
--   1. (1, 5) - id=1 和 id=5 之间的间隙
--   2. (5, 10) - id=5 和 id=10 之间的间隙
-- 其他事务无法在这些间隙中插入新记录
```

#### 作用
- 防止其他事务在间隙中插入记录
- 配合 Next-Key Lock 实现可重复读隔离级别

### 2.5 临键锁 (Next-Key Lock)

#### 特点
- **记录锁 + 间隙锁的组合**
- 锁住索引记录及其之前的间隙
- **InnoDB 在 REPEATABLE READ 下的默认锁算法**

#### 示例
```sql
-- 假设 id 字段有索引，现有记录：1, 5, 10
SELECT * FROM product WHERE id >= 5 FOR UPDATE;
-- 会锁住：
--   1. id=5 这条记录（Record Lock）
--   2. (1,5) 这个间隙（Gap Lock）
--   3. (5,10] 这个范围（Next-Key Lock）
```

#### 锁定范围
- 对于范围查询 `WHERE id >= 5`：
  - 锁住 (负无穷，5] 区间的所有记录和间隙
  - 以及 (5, 下一个记录] 的 Next-Key Lock

## 三、索引对锁的影响

### 3.1 使用索引（推荐）

```sql
-- id 是主键
UPDATE product SET stock = stock - 1 WHERE id = 1;
-- 执行计划：使用主键索引
-- 锁的范围：只锁住 id=1 这一条记录（Record Lock）
-- 并发影响：其他事务可以访问 id=2, id=3 等记录
```

**优点**：
- 锁的粒度小，并发度高
- 只影响目标记录，不影响其他数据

### 3.2 不使用索引（危险）

```sql
-- name 字段没有索引
UPDATE product SET stock = stock - 1 WHERE name = 'iPhone';
-- 执行计划：全表扫描
-- 锁的范围：锁住所有记录（可能升级为表锁）
-- 并发影响：其他事务无法访问任何记录
```

**缺点**：
- 锁的粒度大，并发度低
- 可能阻塞所有其他操作

### 3.3 面试丢分点

**错误说法**："行锁就是锁住这一行物理记录"

**正确理解**：
- 行锁是基于索引实现的
- 如果不走索引，会退化为全表扫描，锁住所有记录
- "行锁"的本质是"锁住索引记录"，而非物理行

## 四、隔离级别与锁的关系

### 4.1 READ UNCOMMITTED
- 最低隔离级别
- 不使用锁（或使用最少的锁）
- 允许脏读

### 4.2 READ COMMITTED
- 只使用 **Record Lock**
- **不使用 Gap Lock**
- 每次 SELECT 都是最新的已提交数据
- 可能出现幻读

### 4.3 REPEATABLE READ（MySQL 默认）
- 使用 **Record Lock + Gap Lock + Next-Key Lock**
- 防止幻读
- 同一事务内多次查询结果一致

### 4.4 SERIALIZABLE
- 最严格的隔离级别
- 所有 SELECT 都隐式加 S 锁
- 完全串行化执行

## 五、实战演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 初始化数据

```bash
curl -X POST http://localhost:9510/interview-agent/question014/init
```

### 5.3 演示行锁（使用索引）

```bash
curl -X POST "http://localhost:9510/interview-agent/question014/purchase" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":1,"lockMode":"X","useIndex":true}'
```

**观察点**：
- 响应中显示：`lockType="Record Lock (记录锁)"`
- `lockGranularity="行级锁"`
- `dependsOnIndex=true`

### 5.4 演示表锁（不走索引）

```bash
curl -X POST "http://localhost:9510/interview-agent/question014/purchase" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":1,"lockMode":"X","useIndex":false}'
```

**观察点**：
- 响应中显示：`lockType="Table Lock (表锁，因全表扫描导致)"`
- `lockGranularity="表级锁"`
- `dependsOnIndex=false`

### 5.5 查看锁分类说明

```bash
curl http://localhost:9510/interview-agent/question014/classification
```

## 六、实际观察 InnoDB 锁的方法

### 6.1 查看事务信息

```sql
-- 查看所有运行的事务
SELECT 
    trx_id,
    trx_state,
    trx_started,
    trx_query
FROM information_schema.INNODB_TRX;
```

### 6.2 查看锁信息（MySQL 8.0+）

```sql
-- 查看当前持有的锁
SELECT 
    engine_transaction_id,
    lock_type,
    lock_mode,
    lock_table,
    lock_index,
    lock_data
FROM performance_schema.data_locks;
```

### 6.3 查看锁等待

```sql
-- 查看锁等待情况
SELECT 
    requesting_trx_id,
    requested_lock_id,
    blocking_trx_id,
    blocking_lock_id
FROM performance_schema.data_lock_waits;
```

### 6.4 查看死锁信息

```sql
-- 查看最近的死锁信息
SHOW ENGINE INNODB STATUS\G
```

在输出中查找 `LATEST DETECTED DEADLOCK` 部分。

### 6.5 手动测试锁（两个会话）

**会话 1**：
```sql
BEGIN;
UPDATE product_014 SET stock = stock - 1 WHERE id = 1;
-- 此时 id=1 的记录被加上 X 锁
-- 不要提交，保持事务开启
```

**会话 2**：
```sql
BEGIN;
UPDATE product_014 SET stock = stock - 1 WHERE id = 1;
-- 会被阻塞，等待会话 1 提交或回滚
```

**查看阻塞**：
```sql
-- 在会话 3 中执行
SELECT * FROM performance_schema.data_lock_waits;
```

## 七、面试高频考点

### Q1: 行锁一定比表锁好吗？

**答**：不一定。

- **行锁优点**：并发度高，适合写多读少的 OLTP 系统
- **行锁缺点**：锁开销大，管理复杂
- **表锁优点**：开销小，适合批量操作
- **表锁缺点**：并发度低

**选择建议**：
- 高并发场景：优先行锁（使用索引）
- 批量操作场景：可以考虑表锁

### Q2: 为什么 InnoDB 的行锁要依赖索引？

**答**：InnoDB 是基于索引实现的行锁。

- InnoDB 的数据存储在聚簇索引中
- 锁是通过索引来定位和管理的
- 如果不走索引，只能全表扫描，锁住所有记录

### Q3: 什么是幻读？如何避免？

**答**：幻读是指同一个查询在不同时间得到不同的结果集。

**示例**：
```sql
-- 事务 1
SELECT * FROM product WHERE id > 5;
-- 结果：id=6, 7, 8

-- 事务 2 插入 id=9
INSERT INTO product VALUES (9, ...);

-- 事务 1 再次查询
SELECT * FROM product WHERE id > 5;
-- 结果：id=6, 7, 8, 9 （出现了"幻影"）
```

**避免方法**：
- InnoDB 通过 Next-Key Lock 避免幻读
- 使用 SERIALIZABLE 隔离级别

### Q4: 意向锁的作用是什么？

**答**：快速判断表中是否有记录被加锁。

**场景**：
```sql
-- 如果没有意向锁
-- 事务想给整个表加锁，需要检查每一行是否有锁
-- 效率极低

-- 有了意向锁
-- 只需检查表的 IS/IX 锁即可知道是否有行锁
-- 效率极高
```

### Q5: 什么情况下间隙锁会失效？

**答**：
1. **查询条件没有索引**：退化为表锁
2. **READ COMMITTED 隔离级别**：不使用 Gap Lock
3. **唯一索引的等值查询**：只使用 Record Lock

## 八、最佳实践建议

### 8.1 索引优化

1. **为查询条件添加索引**
   ```sql
   -- 好的设计
   ALTER TABLE product ADD INDEX idx_name (name);
   UPDATE product SET stock = stock - 1 WHERE name = 'iPhone';
   -- 使用索引，行锁
   ```

2. **避免全表扫描**
   ```sql
   -- 坏的设计
   UPDATE product SET stock = stock - 1 WHERE YEAR(create_time) = 2026;
   -- 函数导致索引失效，全表扫描
   ```

### 8.2 事务控制

1. **缩短事务持有时间**
   ```java
   // 推荐：短事务
   @Transactional
   public void purchase() {
       // 只包含必要的数据库操作
       updateStock();
       createOrder();
   }
   
   // 不推荐：长事务
   @Transactional
   public void purchase() {
       updateStock();
       callExternalAPI(); // 耗时操作
       createOrder();
       sendEmail(); // 耗时操作
   }
   ```

2. **合理设置锁等待超时**
   ```sql
   -- 默认 50 秒，可根据业务调整
   SET innodb_lock_wait_timeout = 30;
   ```

### 8.3 死锁预防

1. **固定访问顺序**
   ```java
   // 推荐：所有事务都按 id 升序访问
   updateProduct(id1);
   updateProduct(id2);
   
   // 不推荐：不同事务访问顺序不一致
   // 事务 1: id1 -> id2
   // 事务 2: id2 -> id1
   // 容易死锁
   ```

2. **批量操作分批进行**
   ```java
   // 推荐：分批处理
   for (List<Long> batch : batches) {
       updateStockBatch(batch);
   }
   
   // 不推荐：一次性处理大量数据
   updateStockBatch(allIds);
   ```

## 九、总结

### 9.1 一句话记忆

- **共享锁 (S)**： "大家都能看，但不能改"
- **排他锁 (X)**： "我只能自己用，别人都不能碰"
- **意向锁**： "提前打招呼，我要对某行动手了"
- **记录锁**： "就锁你这一条记录"
- **间隙锁**： "这块空地我占了，谁也别想插进来"
- **临键锁**： "记录 + 间隙一起锁"

### 9.2 核心要点

1. InnoDB 锁从两个维度分类：语义（S/X/IS/IX）和粒度（表/行）
2. 行级锁细分为：Record Lock、Gap Lock、Next-Key Lock
3. **行锁必须依赖索引**，否则退化为表锁（面试高频丢分点）
4. 意向锁是表级锁，由 InnoDB 自动添加，无需人工干预
5. 不同隔离级别使用的锁策略不同：RC 只用 Record Lock，RR 使用 Next-Key Lock

### 9.3 延伸学习

- MVCC 与锁的关系
- 死锁检测与处理机制
- 锁等待与超时控制
- 分布式锁的实现方案

---

**对应面试题**：问题 014 - InnoDB 锁的分类有哪些？

**关键词**：InnoDB 锁，S 锁，X 锁，意向锁，行锁，记录锁，间隙锁，临键锁

**难度等级**：⭐⭐⭐⭐⭐（超高频考点，必须熟练掌握）
