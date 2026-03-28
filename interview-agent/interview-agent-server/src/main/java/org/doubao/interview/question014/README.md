# AQS (AbstractQueuedSynchronizer) 面试题代码示例

## 目录结构

```
question014/
├── aqs/                          # 核心实现类包
│   ├── SimpleAQS.java           # AQS 简化实现（含详细注释）
│   ├── SimpleReentrantLock.java # 基于 AQS 实现的独占锁
│   └── LockSupport.java         # 线程阻塞/唤醒工具类
└── demo/                         # 使用示例包
    ├── AQSDemo.java             # AQS 使用示例
    └── LockSupportDemo.java     # LockSupport 使用示例
```

## 文件说明

### 核心实现类

#### 1. SimpleAQS.java
- **功能**：AQS 框架的简化实现
- **核心内容**：
  - state 状态维护（volatile int）
  - FIFO 同步队列（双向链表）
  - 独占式资源获取/释放
  - 节点管理（Node）
  - CAS 操作
- **重点注释**：
  - state 的物理和逻辑含义
  - 队列节点的入队出队流程
  - park/unpark 机制
  - 模板方法模式的应用

#### 2. SimpleReentrantLock.java
- **功能**：基于 AQS 实现的简单可重入锁
- **核心内容**：
  - Sync 内部类（继承 AQS）
  - tryAcquire/tryRelease 实现
  - 可重入机制（state 累加）
  - Condition 条件变量支持
- **重点注释**：
  - 非公平锁的实现
  - 可重入计数原理
  - Condition 的 await/signal 机制

#### 3. LockSupport.java
- **功能**：线程阻塞/唤醒工具类
- **核心内容**：
  - park() / unpark() 方法
  - permit 许可机制
  - 超时阻塞方法
  - blocker 对象支持
- **重点注释**：
  - permit 的工作原理
  - 先 unpark 后 park 的效果
  - 中断对 park 的影响

### 使用示例类

#### 1. AQSDemo.java
- **演示场景**：
  1. basicDemo()：基本加锁解锁
  2. reentrantDemo()：可重入特性
  3. producerConsumerDemo()：生产者消费者模式（使用 Condition）
- **运行方式**：直接运行 main 方法

#### 2. LockSupportDemo.java
- **演示场景**：
  1. basicParkUnpark()：基本阻塞唤醒
  2. permitDemo()：permit 许可机制
  3. orderMattersDemo()：调用顺序的影响
  4. parkWithBlocker()：带 blocker 的 park
  5. timedParkDemo()：超时 park
  6. interruptParkDemo()：中断 park
- **运行方式**：直接运行 main 方法

## 运行方法

### 方式一：IDE 中运行
1. 打开 `AQSDemo.java`，运行 `main()` 方法
2. 打开 `LockSupportDemo.java`，运行 `main()` 方法

### 方式二：命令行编译运行

```bash
# 进入项目根目录
cd D:\workspace\doubao\QingShu

# 编译所有 Java 文件
javac -d target/classes interview-agent/interview-agent-server/src/main/java/org/doubao/interview/question014/**/*.java

# 运行 AQS 示例
java -cp target/classes org.doubao.interview.question014.demo.AQSDemo

# 运行 LockSupport 示例
java -cp target/classes org.doubao.interview.question014.demo.LockSupportDemo
```

## 知识点总结

### AQS 核心要点

1. **state 状态**
   - volatile int 类型
   - 不同场景有不同含义（锁计数、信号量、倒数计数等）
   - 通过 CAS 保证原子性修改

2. **FIFO 队列**
   - 双向链表结构
   - 头节点是虚拟节点
   - 失败线程入队并阻塞

3. **模板方法模式**
   - tryAcquire(int arg)：尝试获取资源
   - tryRelease(int arg)：尝试释放资源
   - tryAcquireShared(int arg)：共享式获取
   - tryReleaseShared(int arg)：共享式释放

4. **阻塞/唤醒机制**
   - LockSupport.park()：阻塞线程
   - LockSupport.unpark(Thread)：唤醒线程
   - 基于 permit 许可（0 或 1）

### 可重入锁原理

```java
// 首次获取锁
if (state == 0 && compareAndSetState(0, 1)) {
    setExclusiveOwnerThread(current);
    return true;
}
// 重复获取（可重入）
else if (current == getExclusiveOwnerThread()) {
    setState(state + 1);
    return true;
}
```

### Condition 工作机制

```java
// await() 流程
1. 将当前线程加入条件队列
2. 完全释放锁（state=0）
3. park() 阻塞
4. 被 signal() 唤醒
5. 重新竞争锁

// signal() 流程
1. 从条件队列找到第一个节点
2. 转移到同步队列
3. unpark() 唤醒
```

## 面试高频问题

### Q1: AQS 是什么？
**答**：AQS（AbstractQueuedSynchronizer）是并发编程的基础框架，用于构建锁和同步器。它维护一个 state 状态和一个 FIFO 等待队列。

### Q2: AQS 的核心组件？
**答**：
1. **state**：volatile int 类型的同步状态
2. **Node 队列**：双向链表，存储等待线程
3. **CLH 变体**：Craig-Landin-Hagersten 队列的变种
4. **模板方法**：tryAcquire/tryRelease 等

### Q3: 哪些类使用了 AQS？
**答**：
- ReentrantLock（独占锁）
- Semaphore（信号量）
- CountDownLatch（倒数计数器）
- CyclicBarrier（循环屏障）
- ReentrantReadWriteLock（读写锁）

### Q4: 公平锁和非公平锁的区别？
**答**：
- **公平锁**：新请求的线程先检查队列，有等待则入队
- **非公平锁**：新请求直接尝试获取，可能插队

### Q5: AQS 如何保证线程安全？
**答**：
1. volatile 保证 state 可见性
2. CAS 操作保证原子性
3. LockSupport.park() 阻塞失败线程
4. synchronized 保证某些操作的原子性

## 相关资源

- [Java 并发编程实战](https://book.douban.com/subject/10484692/)
- [AQS 源码分析](https://www.cnblogs.com/waterystone/p/4920797.html)
- [JUC 并发包官方文档](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)
