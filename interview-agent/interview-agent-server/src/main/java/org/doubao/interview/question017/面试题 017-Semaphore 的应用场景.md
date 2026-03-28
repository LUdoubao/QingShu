# 面试题 017：Semaphore 的应用场景

## 📦 文件说明

### 核心实现类

1. **SimpleSemaphore.java**
   - 路径：`question017/concurrent/SimpleSemaphore.java`
   - Semaphore（信号量）的简化实现
   - 支持公平/非公平模式
   - 提供 acquire/release/tryAcquire 方法

### 演示程序

2. **SemaphoreDemo.java**
   - 路径：`question017/demo/SemaphoreDemo.java`
   - 包含 3 个典型应用场景：
     - 数据库连接池控制
     - API 限流器
     - 停车场管理系统

## 🎯 Semaphore 核心概念

### 什么是 Semaphore？

Semaphore（信号量）用于控制同时访问特定资源的线程数量，通过维护一个许可计数器实现：

- **permit（许可）**: 表示可同时执行的任务数量
- **acquire()**: 获取许可，无许可时阻塞等待
- **release()**: 释放许可，增加可用数量
- **tryAcquire()**: 尝试获取许可（可带超时）

### 两种模式

1. **公平模式 (Fair)**
   - 按照 FIFO 顺序分配许可
   - 避免线程饥饿
   - 性能略低

2. **非公平模式 (Non-fair)**
   - 不保证顺序
   - 性能更好
   - 可能导致饥饿

## 📊 三大应用场景

### 场景 1：数据库连接池控制

**需求**：限制同时访问数据库的连接数，避免资源耗尽

```java
// 创建最多 5 个数据库连接
SimpleSemaphore semaphore = new SimpleSemaphore(5);

// 获取连接
semaphore.acquire();
try {
    // 执行数据库操作
} finally {
    // 释放连接
    semaphore.release();
}
```

**输出示例**：
```
[线程：DB-Thread-1] 请求数据库连接，当前可用：5
[线程：DB-Thread-1] ✓ 获得数据库连接，剩余许可：4
  └─ [查询-Q1] 正在执行...
  └─ [查询-Q1] ✓ 执行完成
[线程：DB-Thread-1] ✓ 释放数据库连接，剩余许可：5
```

**关键点**：
- 防止数据库连接过多导致性能下降
- 使用 try-finally 确保连接释放
- 公平模式保证请求顺序

### 场景 2：API 限流器

**需求**：限制每秒/每分钟处理的请求数

```java
// 每秒最多处理 10 个请求
SimpleSemaphore rateLimiter = new SimpleSemaphore(10);

// 处理请求时获取许可
if (rateLimiter.tryAcquire(1, 1000)) {
    try {
        // 处理请求
    } finally {
        rateLimiter.release();
    }
} else {
    // 拒绝服务或降级处理
}
```

**输出示例**：
```
[请求-R1] 等待处理...
[请求-R1] ✓ 开始处理，剩余配额：9
[请求-R1] ✓ 处理完成
[请求-R6] ✗ 请求超时，拒绝服务
```

**关键点**：
- 使用 tryAcquire 带超时避免无限等待
- 需要定期重置配额（如定时任务补充 permit）
- 可结合滑动窗口实现更复杂的限流

### 场景 3：停车场管理系统

**需求**：控制停车场内的车辆数，满了需要等待

```java
// 停车场有 10 个车位
SimpleSemaphore parkingLot = new SimpleSemaphore(10);

// 车辆进入
if (parkingLot.tryAcquire(1, 3000)) {
    try {
        // 停车
    } finally {
        parkingLot.release();  // 离开
    }
} else {
    // 停车场已满，离开
}
```

**输出示例**：
```
[车辆 - 京 A-001] 尝试进入停车场，剩余车位：10
[车辆 - 京 A-001] ✓ 进入成功，剩余车位：9
[车辆 - 京 A-006] 尝试进入停车场，剩余车位：0
[车辆 - 京 A-006] ✗ 停车场已满，离开
```

**关键点**：
- 超时机制避免长时间等待
- 离开时必须释放车位
- 可用于任何有限资源管理

## 🔍 对比其他同步工具

| 特性 | Semaphore | CountDownLatch | CyclicBarrier |
|------|-----------|----------------|---------------|
| **用途** | 限流、资源控制 | 主线程等子任务 | 子任务相互等待 |
| **计数方向** | 可增可减 | 递减到 0 | 递增到阈值 |
| **可复用性** | ✅ 可复用 | ❌ 一次性 | ✅ 可复用 |
| **操作方式** | acquire/release | countDown/await | await |
| **典型场景** | 连接池、限流 | 启动初始化 | 并行计算 |

## 💡 面试高频问题

### Q1: Semaphore 如何保证线程安全？

**A**：通过 synchronized 和 wait/notify 机制：

```java
synchronized void acquire(int acquires) throws InterruptedException {
    while (permits < acquires) {
        wait();  // 释放锁并等待
    }
    permits -= acquires;
}

synchronized void release(int releases) {
    permits += releases;
    notifyAll();  // 通知等待线程
}
```

### Q2: 公平模式和非公平模式的区别？

**A**：
- **公平模式**：按等待顺序分配，先等待的先获取（FIFO）
- **非公平模式**：允许插队，新来的线程可能比等待的线程先获取
- **性能**：非公平模式吞吐量更高（减少上下文切换）
- **公平性**：公平模式避免线程饥饿

### Q3: Semaphore 如何实现限流？

**A**：通过固定数量的 permit 控制并发：

```java
// 每秒 100 个请求的限流器
SimpleSemaphore limiter = new SimpleSemaphore(100);

// 每个请求消耗 1 个 permit
limiter.acquire();
try {
    handleRequest();
} finally {
    limiter.release();
}
```

配合定时任务每秒重置 permit 数量。

### Q4: acquire() 和 tryAcquire() 的区别？

**A**：
- **acquire()**: 阻塞式，无 permit 时一直等待
- **tryAcquire()**: 非阻塞，立即返回 true/false
- **tryAcquire(timeout)**: 带超时的阻塞，超时后返回 false

### Q5: 为什么要用 try-finally 释放 permit？

**A**：确保异常情况下也能释放资源：

```java
semaphore.acquire();
try {
    // 执行业务逻辑（可能抛异常）
} finally {
    semaphore.release();  // 无论如何都会执行
}
```

避免忘记 release 导致资源泄漏。

## 🚀 运行代码

### PowerShell 一键运行

```powershell
cd D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java
.\org\doubao\interview\question017\run-demo.ps1
```

### 手动编译运行

```bash
# 1. 编译
cd D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java
javac -encoding UTF-8 -d ..\..\..\..\target\classes ^
    org/doubao/interview/question017/concurrent/SimpleSemaphore.java ^
    org/doubao/interview/question017/demo/SemaphoreDemo.java

# 2. 运行
java -cp ..\..\..\..\target\classes ^
    org.doubao.interview.question017.demo.SemaphoreDemo
```

## 📝 核心要点总结

1. **Semaphore 用于控制并发访问量**
   - 许可计数器管理资源
   - acquire 获取许可
   - release 释放许可

2. **三大应用场景**
   - 数据库连接池（最经典）
   - API 限流器（最常用）
   - 停车场管理（资源池）

3. **公平 vs 非公平**
   - 公平模式：FIFO，防饥饿
   - 非公平模式：性能好，可能插队

4. **与 CountDownLatch/CyclicBarrier 区别**
   - Semaphore：限流、资源控制
   - CountDownLatch：主线程等子任务
   - CyclicBarrier：子任务相互等待

5. **注意事项**
   - 必须用 try-finally 释放 permit
   - 合理设置 permit 数量
   - 超时机制避免死等

## 🎓 技术深度

### 底层实现原理

```java
// 简化的内部结构
class Sync {
    private int permits;          // 许可数量
    private final boolean fair;   // 是否公平
    private int waiters = 0;      // 等待线程数
    
    synchronized void acquire() {
        while (permits < 1) {
            waiters++;
            try {
                wait();
            } finally {
                waiters--;
            }
        }
        permits--;
    }
    
    synchronized void release() {
        permits++;
        if (fair && waiters > 0) {
            notify();    // 公平：通知第一个
        } else {
            notifyAll(); // 非公平：通知所有
        }
    }
}
```

### JDK 中的真实实现

JDK 的 Semaphore 基于 AQS（AbstractQueuedSynchronizer）实现：

- **Sync**: 继承 AQS
- **FairSync**: 公平同步器
- **NonfairSync**: 非公平同步器
- 使用 CLH 队列管理等待线程
- 性能更优，功能更强

## 📚 扩展阅读

- JUC 包中的 Semaphore 源码
- AQS 框架原理
- 令牌桶算法（更复杂的限流）
- 漏桶算法
- Redis 分布式限流

---

**✅ 编译运行成功**
- 最后运行时间：2026-03-28
- 输出验证：三个场景均正常工作
- 代码状态：可直接使用
