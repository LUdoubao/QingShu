# 面试题 018：ReadWriteLock 适合什么场景？

## 📦 文件说明

### 核心实现类

1. **SimpleReadWriteLock.java**
   - 路径：`question018/concurrent/SimpleReadWriteLock.java`
   - ReadWriteLock（读写锁）的简化实现
   - 支持读锁共享、写锁独占
   - 使用 synchronized 和 wait/notify 机制

### 演示程序

2. **ReadWriteLockDemo.java**
   - 路径：`question018/demo/ReadWriteLockDemo.java`
   - 包含 3 个典型应用场景：
     - 缓存系统（读多写少）
     - 配置管理器
     - 共享数据字典

## 🎯 ReadWriteLock 核心概念

### 什么是 ReadWriteLock？

ReadWriteLock（读写锁）是一种特殊的锁机制，它区分读操作和写操作：

- **读锁（ReadLock）**：共享锁，多个线程可同时持有
- **写锁（WriteLock）**：独占锁，与读锁和写锁都互斥
- **适用场景**：读多写少的场景

### 锁规则

1. **读 - 读**：不冲突，可并发
2. **读 - 写**：冲突，互斥
3. **写 - 写**：冲突，互斥

## 📊 三大应用场景

### 场景 1：缓存系统

**需求**：缓存数据频繁被读取，偶尔更新，需要提高并发读性能

```java
class Cache<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
    
    public V get(K key) {
        try {
            lock.readLock().lock();  // 获取读锁
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void put(K key, V value) {
        try {
            lock.writeLock().lock();  // 获取写锁
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

**输出示例**：
```
[读] 线程-Read-1 读取 key=A, 当前并发读数：2
[读] 线程-Read-5 读取 key=A, 当前并发读数：5
[写] 线程-Write-1 写入 key=D, value=4
```

**关键点**：
- 5 个线程可同时读取（并发读数达到 5）
- 写操作时禁止读（互斥）
- 读性能大幅提升

### 场景 2：配置管理器

**需求**：系统配置频繁被读取，但很少更新

```java
class ConfigManager {
    private final Map<String, String> config = new HashMap<>();
    private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
    
    public String getConfig(String key) {
        try {
            lock.readLock().lock();
            return config.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void updateConfig(String key, String value) {
        try {
            lock.writeLock().lock();
            config.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

**输出示例**：
```
[线程 -1] 读取配置：timeout=3000, maxConnections=100
[线程 -2] 读取配置：timeout=3000, maxConnections=100
[配置更新] timeout = 5000
[配置更新] ✓ 已更新：timeout
```

**关键点**：
- 多个线程可同时读取配置
- 更新配置时需要等待所有读完成
- 保证配置的可见性和一致性

### 场景 3：共享数据字典

**需求**：字典数据频繁查询，偶尔添加/删除词条

```java
class DataDictionary {
    private final Map<String, String> dictionary = new HashMap<>();
    private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
    
    public String lookup(String word) {
        try {
            lock.readLock().lock();
            return dictionary.get(word);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void addWord(String word, String definition) {
        try {
            lock.writeLock().lock();
            dictionary.put(word, definition);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

**输出示例**：
```
[查询] "Java" -> 一种编程语言
[查询] "Lock" -> 锁机制
[添加] "ReadWriteLock" = "读写锁"
[添加] ✓ 已添加词条：ReadWriteLock
```

**关键点**：
- 查询操作可并发执行
- 添加/删除操作独占访问
- 适合字典、映射表等场景

## 🔍 对比其他同步工具

| 特性 | ReadWriteLock | Semaphore | CountDownLatch | CyclicBarrier |
|------|---------------|-----------|----------------|---------------|
| **用途** | 读多写少 | 限流、资源控制 | 主线程等子任务 | 子任务相互等待 |
| **读锁** | 共享 | - | - | - |
| **写锁** | 独占 | - | - | - |
| **计数** | - | permit 数量 | 倒计时 | 正计时 |
| **典型场景** | 缓存、配置 | 连接池、限流 | 启动初始化 | 并行计算 |

## 💡 面试高频问题

### Q1: ReadWriteLock 为什么适合读多写少场景？

**A**：因为读锁是共享的，允许多个线程同时读取：

```java
// 5 个线程可同时读
lock.readLock().lock();  // 不阻塞
// 并发读数：5
lock.readLock().unlock();

// 写操作必须等待所有读完成
lock.writeLock().lock();  // 阻塞，直到 readers=0
```

如果写操作频繁，会导致：
- 写锁等待时间变长
- 读锁也被阻塞
- 性能下降

### Q2: 读写锁的公平性如何保证？

**A**：简化的实现中使用了 waitWaiters 变量：

```java
// 读锁会检查是否有写线程在等待
while (writing || writeWaiters > 0) {
    wait();
}
readers++;

// 这样避免读线程一直占用，写线程饥饿
```

JDK 的 ReentrantReadWriteLock 提供更完善的公平策略。

### Q3: 什么时候不适合用 ReadWriteLock？

**A**：以下场景不适合：

1. **写多读少**：写锁独占，频繁写会导致性能下降
2. **读操作很短**：锁开销可能大于收益
3. **所有操作都是写**：退化为普通锁
4. **并发度要求极高**：可能需要更高级的并发结构（如 ConcurrentHashMap）

### Q4: ReadWriteLock 的实现原理是什么？

**A**：核心是维护状态变量：

```java
int readers = 0;       // 当前读线程数
boolean writing = false;  // 是否在写入
int writeWaiters = 0;  // 等待的写线程数

// 读锁获取条件：没有写线程且没有等待的写线程
while (writing || writeWaiters > 0) {
    wait();
}
readers++;

// 写锁获取条件：没有读线程且没有其他写线程
while (readers > 0 || writing) {
    wait();
}
writing = true;
```

### Q5: 如何保证锁的正确释放？

**A**：必须使用 try-finally 块：

```java
lock.readLock().lock();
try {
    // 执行业务逻辑
} finally {
    lock.readLock().unlock();  // 确保释放
}
```

避免异常导致锁未释放，造成死锁。

## 🚀 运行代码

### PowerShell 一键运行

```powershell
cd D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java
.\org\doubao\interview\question018\run-demo.ps1
```

### 手动编译运行

```bash
# 1. 编译
cd D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java
javac -encoding UTF-8 -d ..\..\..\..\target\classes ^
    org/doubao/interview/question018/concurrent/SimpleReadWriteLock.java ^
    org/doubao/interview/question018/demo/ReadWriteLockDemo.java

# 2. 运行
java -cp ..\..\..\..\target\classes ^
    org.doubao.interview.question018.demo.ReadWriteLockDemo
```

## 📝 核心要点总结

1. **ReadWriteLock 用于读多写少场景**
   - 读锁共享：多个线程可同时读
   - 写锁独占：与读、写都互斥

2. **三大应用场景**
   - 缓存系统（最经典）
   - 配置管理器（频繁读，偶尔写）
   - 数据字典（查询多，修改少）

3. **性能优势**
   - 读并发高时性能好
   - 写频繁时收益降低
   - 比 synchronized 更适合读多写少

4. **注意事项**
   - 必须用 try-finally 释放锁
   - 写操作可能饥饿（需公平策略）
   - 不适合写多读少场景

5. **与其他锁对比**
   - vs Semaphore：限流 vs 读写分离
   - vs ReentrantLock：独占 vs 共享 + 独占

## 🎓 技术深度

### 底层实现原理

```java
// 核心数据结构
int readers = 0;           // 读线程计数
boolean writing = false;   // 写标志
int writeWaiters = 0;      // 写等待计数
Object lockMutex = new Object();  // 监视器

// 读锁获取
synchronized (lockMutex) {
    while (writing || writeWaiters > 0) {
        lockMutex.wait();
    }
    readers++;
}

// 写锁获取
synchronized (lockMutex) {
    writeWaiters++;
    try {
        while (readers > 0 || writing) {
            lockMutex.wait();
        }
    } finally {
        writeWaiters--;
    }
    writing = true;
}
```

### JDK 中的真实实现

JDK 的 `ReentrantReadWriteLock` 基于 AQS 实现：

- **Sync**: 继承 AbstractQueuedSynchronizer
- **state 拆分**：高 16 位表示读锁，低 16 位表示写锁
- **FairSync**: 公平版本
- **NonfairSync**: 非公平版本
- 使用 CLH 队列管理等待线程
- 性能更优，功能更强

### 锁降级

ReadWriteLock 支持锁降级（写→读），但不支持锁升级（读→写）：

```java
// ✅ 锁降级：允许
lock.writeLock().lock();
try {
    // 修改数据
    lock.readLock().lock();  // 获取读锁
} finally {
    lock.writeLock().unlock();  // 释放写锁
    // 仍持有读锁
}

// ❌ 锁升级：不允许（会死锁）
lock.readLock().lock();
lock.writeLock().lock();  // 阻塞，等待读锁释放（但自己持有读锁）
```

## 📚 扩展阅读

- JUC 包中的 ReentrantReadWriteLock 源码
- 锁降级和锁升级
- StampedLock（更强大的读写锁）
- ConcurrentHashMap 的并发控制
- 乐观读锁

---

**✅ 编译运行成功**
- 最后运行时间：2026-03-28
- 输出验证：三个场景均正常工作
- 代码状态：可直接使用
