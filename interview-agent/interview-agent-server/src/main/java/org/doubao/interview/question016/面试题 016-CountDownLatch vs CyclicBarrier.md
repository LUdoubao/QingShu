# 面试题 016: CountDownLatch vs CyclicBarrier

## 代码结构

```
question016/
├── concurrent/                    # 核心实现类包
│   ├── SimpleCountDownLatch.java # CountDownLatch 简化实现
│   └── SimpleCyclicBarrier.java  # CyclicBarrier 简化实现
└── demo/                          # 使用示例包
    ├── CountDownLatchDemo.java   # CountDownLatch 使用示例
    ├── CyclicBarrierDemo.java    # CyclicBarrier 使用示例
    └── ComparisonDemo.java       # 对比演示
```

## 文件说明

### 1. SimpleCountDownLatch.java - 倒计时门闩

**核心组件：**
- `volatile int count`: 当前计数值
- `Sync`: 内部同步器类

**主要方法：**
- `await()`: 等待直到 count=0
- `countDown()`: 倒计时（count--）
- `getCount()`: 获取当前计数值

**关键注释点：**
- 一次性使用特性
- 倒计时机制原理
- 阻塞等待实现
- 典型应用场景

### 2. SimpleCyclicBarrier.java - 循环栅栏

**核心组件：**
- `int parties`: 需要等待的线程总数
- `int count`: 当前已到达的线程数
- `long generation`: 代数（用于检测重用）
- `boolean broken`: 栅栏是否被破坏

**主要方法：**
- `await()`: 等待其他线程到达
- `reset()`: 重置栅栏
- `getNumberWaiting()`: 获取等待的线程数

**关键注释点：**
- 可复用性原理
- 代际管理机制
- 屏障动作执行
- BrokenBarrierException 处理

### 3. CountDownLatchDemo.java - 使用示例

**演示场景：**
1. **mainThreadWaitForSubTasks()**: 主线程等待多个子任务
   - 下载数据、解析文件、初始化数据库
   - 3 个子任务并行执行
   - 主线程等待所有完成后继续

2. **parallelComputation()**: 并行计算等待所有分片
   - 5 个分片并行处理
   - 汇总所有结果

3. **timeoutWait()**: 带超时的等待
   - 防止任务卡死
   - 超时处理逻辑

### 4. CyclicBarrierDemo.java - 使用示例

**演示场景：**
1. **multiPlayerGame()**: 多人游戏等待开始
   - 4 个玩家相互等待
   - 所有人都准备好后同时开始

2. **multiStageComputation()**: 多阶段并行计算
   - 3 个阶段，5 个线程
   - 每阶段完成后一起进入下一阶段

3. **runningRace()**: 田径赛跑模拟
   - 6 个选手等待发令
   - 同时起跑

### 5. ComparisonDemo.java - 对比演示

**对比内容：**
1. **waitPatternComparison()**: 等待模式比较
   - CountDownLatch：主线程等子任务
   - CyclicBarrier：子任务相互等待

2. **reusabilityComparison()**: 可复用性比较
   - CountDownLatch 需要新实例
   - CyclicBarrier 可重复使用

## 核心区别总结

### 设计目的不同

| 特性 | CountDownLatch | CyclicBarrier |
|------|----------------|---------------|
| 用途 | 一个线程等待其他多个线程完成 | 多个线程相互等待后一起继续 |
| 场景 | 主线程等子任务 | 多线程协作点 |
| 比喻 | 老板等员工完成工作 | 朋友约定集合时间 |

### 可复用性

| 特性 | CountDownLatch | CyclicBarrier |
|------|----------------|---------------|
| 复用性 | 一次性，不可重置 | 可重复使用 |
| 计数归零后 | 无法再次使用 | 自动重置，进入下一代 |
| 实例数量 | 每次需要新实例 | 同一实例可多次使用 |

### 计数方式

| 特性 | CountDownLatch | CyclicBarrier |
|------|----------------|---------------|
| 计数方向 | 递减（N → 0） | 递增（0 → N） |
| 触发条件 | 最后一个 countDown() | 最后一个 await() |
| 触发效果 | 唤醒等待的主线程 | 唤醒所有等待的线程 |

### API 对比

| 方法 | CountDownLatch | CyclicBarrier |
|------|----------------|---------------|
| 等待 | `await()` | `await()` |
| 通知 | `countDown()` | 无（通过 await 隐式通知） |
| 超时 | `await(timeout, unit)` | `await(timeout, unit)` |
| 查询 | `getCount()` | `getNumberWaiting()` |
| 重置 | 无 | `reset()` |

## 典型应用场景

### CountDownLatch 适用场景

1. **服务启动初始化**
   ```java
   // 主线程等待 N 个依赖服务初始化完成
   CountDownLatch latch = new CountDownLatch(N);
   // 每个依赖服务初始化完成后 latch.countDown()
   latch.await();  // 主线程等待
   // 所有依赖初始化完成，主服务启动
   ```

2. **并行任务汇总**
   ```java
   // 将大任务拆分为 N 个子任务并行执行
   CountDownLatch latch = new CountDownLatch(N);
   // 每个子任务完成后 countDown()
   latch.await();  // 等待所有子任务完成
   // 汇总结果
   ```

3. **性能测试**
   ```java
   // 让 N 个线程同时开始执行
   CountDownLatch startLatch = new CountDownLatch(1);
   // 所有线程准备完毕后 await()
   startLatch.countDown();  // 同时开始
   ```

### CyclicBarrier 适用场景

1. **多线程协作计算**
   ```java
   // 矩阵运算分为多个阶段
   CyclicBarrier barrier = new CyclicBarrier(N);
   // 每个线程完成当前阶段后 await()
   // 所有线程完成后一起进入下一阶段
   ```

2. **分布式模拟**
   ```java
   // 模拟比赛，所有选手同时起跑
   CyclicBarrier barrier = new CyclicBarrier(N);
   // 所有选手准备完毕后 await()
   // 最后一个到达时，同时开始
   ```

3. **批量数据处理**
   ```java
   // 多线程加载数据，全部加载完成后统一处理
   CyclicBarrier barrier = new CyclicBarrier(N, () -> {
       // 所有数据加载完成的汇总动作
   });
   ```

## 编译和运行

### 命令行编译

```bash
cd D:\workspace\doubao\QingShu

# 编译所有 Java 文件
javac -encoding UTF-8 -d target\classes ^
    interview-agent\interview-agent-server\src\main\java\org\doubao\interview\question016\concurrent\*.java ^
    interview-agent\interview-agent-server\src\main\java\org\doubao\interview\question016\demo\*.java

# 运行 CountDownLatch 示例
java -cp target\classes org.doubao.interview.question016.demo.CountDownLatchDemo

# 运行 CyclicBarrier 示例
java -cp target\classes org.doubao.interview.question016.demo.CyclicBarrierDemo

# 运行对比演示
java -cp target\classes org.doubao.interview.question016.demo.ComparisonDemo
```

## 面试高频问题

### Q1: CountDownLatch 和 CyclicBarrier 的区别？

**答：**

1. **用途不同**：
   - CountDownLatch：一个线程等待其他多个线程完成
   - CyclicBarrier：多个线程相互等待后一起继续

2. **可复用性**：
   - CountDownLatch：一次性，计数器归零后无法重置
   - CyclicBarrier：可重复使用，每代完成后自动重置

3. **计数方式**：
   - CountDownLatch：倒计时（递减到 0）
   - CyclicBarrier：正计时（递增到阈值）

4. **触发时机**：
   - CountDownLatch：最后一个 countDown() 触发
   - CyclicBarrier：最后一个 await() 触发

### Q2: 各自的应用场景？

**答：**

**CountDownLatch**：
- 主线程等待多个子任务完成
- 服务启动时等待依赖初始化
- 并行计算中等待所有分片完成

**CyclicBarrier**：
- 多线程相互等待后一起执行
- 并行计算的多阶段处理
- 分布式系统的同步起点

### Q3: 底层实现原理？

**答：**

**CountDownLatch**：
- 基于 AQS 的共享模式
- state 表示剩余计数
- countDown() 使 state--，为 0 时唤醒所有等待线程
- await() 在 state>0 时阻塞

**CyclicBarrier**：
- 使用 ReentrantLock 和 Condition
- 维护 count 计数和 generation 代数
- 最后一个到达的线程触发 nextGeneration()
- 重置 count 并唤醒所有线程

### Q4: 如何选择？

**答：**

根据业务需求选择：

- 如果是**一个线程等待多个线程完成** → CountDownLatch
- 如果是**多个线程相互等待后一起继续** → CyclicBarrier
- 如果需要**重复使用** → CyclicBarrier
- 如果只需要**一次性的等待** → CountDownLatch

## 测试输出示例

### CountDownLatch 输出
```
=== 场景一：主线程等待多个子任务 ===
[主线程] 等待所有子任务完成...
[下载任务] 开始下载数据...
[解析任务] 开始解析文件...
[数据库任务] 开始初始化连接...
[数据库任务] 数据库初始化完成 ✓
[解析任务] 文件解析完成 ✓
[下载任务] 数据下载完成 ✓
[主线程] ✓ 所有子任务已完成，主线程继续执行！
```

### CyclicBarrier 输出
```
=== 场景一：多人游戏等待开始 ===
[玩家 1] 进入房间，准备中...
[玩家 2] 进入房间，准备中...
[玩家 3] 进入房间，准备中...
[玩家 4] 进入房间，准备中...
[玩家 1] 准备完成，等待其他玩家...
[玩家 2] 准备完成，等待其他玩家...
[玩家 3] 准备完成，等待其他玩家...
[玩家 4] 准备完成，等待其他玩家...
[系统] ✓ 所有玩家已就绪，游戏开始！🎮
[玩家 1] （索引：0）游戏开始，出发！🏃
...
```

### 对比演示输出
```
【核心区别总结】
┌─────────────────────────────────────────┐
│ CountDownLatch       │ CyclicBarrier    │
├──────────────────────┼──────────────────┤
│ 主线程等子任务       │ 子任务相互等待   │
│ 一次性使用           │ 可重复使用       │
│ 倒计时（递减到 0）    │ 正计时（到阈值）  │
│ 最后一个 countDown   │ 最后一个 await   │
│ 触发等待的线程       │ 触发所有线程     │
└─────────────────────────────────────────┘
```

## 相关资源

- [Java 并发编程实战](https://book.douban.com/subject/10484692/)
- [CountDownLatch 源码分析](https://www.cnblogs.com/waterystone/p/5979058.html)
- [CyclicBarrier 源码分析](https://www.cnblogs.com/waterystone/p/5980973.html)
