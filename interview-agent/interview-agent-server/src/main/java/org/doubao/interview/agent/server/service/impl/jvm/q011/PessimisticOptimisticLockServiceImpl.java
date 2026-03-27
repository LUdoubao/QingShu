package org.doubao.interview.agent.server.service.impl.jvm.q011;

import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockRequest;
import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockResponse;
import org.doubao.interview.agent.api.service.jvm.q011.PessimisticOptimisticLockService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 问题 011(JVM)：悲观锁与乐观锁对比演示服务类。
 * <p>
 * 
 * 【核心概念】
 * <p>
 * 1. 悲观锁（Pessimistic Lock）：
 *    - 核心思想：总是假设最坏情况，认为并发冲突一定会发生
 *    - 工作方式：在修改共享数据前先加锁，确保同一时刻只有一个线程能访问
 *    - 特点：
 *      ① 先锁后操作：线程必须先获取锁，才能执行临界区代码
 *      ② 阻塞等待：未获取到锁的线程会被阻塞，直到锁被释放
 *      ③ 串行执行：临界区代码在同一时刻只能被一个线程执行
 *    - 适用场景：
 *      ① 写多读少：频繁修改共享数据，冲突概率高
 *      ② 临界区代码较长：持有锁的时间较长
 *      ③ 对一致性要求严格：不能容忍任何并发冲突
 *    - Java 中的实现：synchronized、ReentrantLock 等
 * <p>
 * 2. 乐观锁（Optimistic Lock）：
 *    - 核心思想：假设最好情况，认为并发冲突很少发生
 *    - 工作方式：在修改共享数据时不加锁，而是在提交更新时检查是否有其他线程修改过
 *    - 特点：
 *      ① 无锁编程：线程不需要阻塞等待，可以自由执行
 *      ② CAS 机制：使用 Compare-And-Swap 原子操作保证数据一致性
 *      ③ 冲突重试：如果检测到冲突，则自旋重试直到成功
 *    - 适用场景：
 *      ① 读多写少：大部分时间是读取操作，写操作较少
 *      ② 临界区代码较短：CAS 操作简单，执行时间短
 *      ③ 追求高吞吐：不希望线程阻塞，追求更高的并发性能
 *    - Java 中的实现：AtomicInteger、AtomicLong、AtomicReference 等
 * <p>
 * 3. CAS（Compare-And-Swap）原子操作：
 *    - 三要素：内存位置 V、预期原值 A、新值 B
 *    - 操作流程：
 *      ① 比较：检查 V 的值是否等于 A
 *      ② 交换：如果相等，将 V 的值设置为 B
 *      ③ 失败：如果不相等，说明已被其他线程修改，返回失败
 *    - 硬件支持：现代 CPU 提供的原子指令（如 x86 的 CMPXCHG）
 *    - 优点：
 *      ① 无锁：不需要阻塞线程，减少上下文切换开销
 *      ② 高性能：在低竞争场景下性能优于悲观锁
 *    - 缺点：
 *      ① ABA 问题：值从 A→B→A，CAS 无法检测到中间变化
 *      ② 自旋开销：高竞争场景下大量重试会浪费 CPU
 *      ③ 只能保证单次操作的原子性
 * <p>
 * 4. 性能对比：
 * <p>
 *    | 特性             | 悲观锁                          | 乐观锁                          |
 *    |------------------|---------------------------------|---------------------------------|
 *    | 并发策略         | 先锁后操作，阻塞等待            | 先操作后检查，冲突重试          |
 *    | 线程阻塞         | 是（未获取到锁时）              | 否（自旋等待）                  |
 *    | 冲突处理         | 避免冲突（通过锁互斥）          | 检测并重试（通过 CAS）          |
 *    | CPU 开销         | 低（阻塞时不消耗 CPU）           | 高（自旋时持续消耗 CPU）         |
 *    | 上下文切换       | 多（阻塞/唤醒需要系统调用）      | 少（纯用户态操作）               |
 *    | 低竞争场景       | 较慢（锁操作有固定开销）         | 较快（无锁，直接 CAS）           |
 *    | 高竞争场景       | 稳定（阻塞避免了 CPU 空转）       | 较差（大量重试浪费 CPU）         |
 *    | 适用场景         | 写多读少、临界区长               | 读多写少、临界区短              |
 * <p>
 * 【本示例演示内容】
 * 1. 悲观锁模式：使用 synchronized + AtomicInteger 演示先锁后递增
 * 2. 乐观锁模式：使用 AtomicInteger.compareAndSet() 演示 CAS + 重试机制
 * 3. 统计并对比两种模式的最终结果和重试次数（仅乐观锁有重试）
 */
@Service
public class PessimisticOptimisticLockServiceImpl implements PessimisticOptimisticLockService {

    /**
     * 悲观锁专用的监视器锁对象。
     * 
     * 【为什么使用独立的锁对象】
     * - 避免锁定 this：防止与其他 synchronized 方法产生意外的锁竞争
     * - 明确作用域：monitor 专门用于悲观锁演示
     * - 符合锁分离原则：不同的并发控制使用不同的锁对象
     * 
     * 【注意】
     * 乐观锁不需要锁对象，它通过 CAS 原子操作实现并发控制
     */
    private final Object monitor = new Object();

    /**
     * 执行悲观锁与乐观锁对比演示的核心方法。
     * 
     * 【处理流程】
     * 1. 提取并校验请求参数：lockType、threads、incrementsPerThread
     * 2. 计算理论期望值：expected = threads × incrementsPerThread
     * 3. 根据 lockType 选择执行悲观锁或乐观锁演示
     * 4. 返回演示结果，包括实际值和重试次数（仅乐观锁）
     * 
     * 【参数说明】
     * - lockType: 锁类型，可选值为 "pessimistic" 或 "optimistic"
     * - threads: 并发线程数（必须大于 0）
     * - incrementsPerThread: 每个线程的递增次数（必须大于 0）
     * 
     * 【预期结果】
     * - 悲观锁：
     *   - actual == expected（保证原子性，不会丢失更新）
     *   - retries = 0（没有重试机制）
     * 
     * - 乐观锁：
     *   - actual == expected（CAS 保证最终一致性）
     *   - retries > 0（存在并发冲突时会重试）
     * 
     * @param request 请求对象，包含上述配置参数
     * @return 响应对象，包含演示结果、统计信息等
     */
    @Override
    public PessimisticOptimisticLockResponse run(PessimisticOptimisticLockRequest request) {
        // ========== 步骤 1: 提取请求参数 ==========
        // 空值保护：防止请求对象或字段为 null 导致 NPE
        String lockType = request == null ? null : request.getLockType();
        int threads = request == null ? 0 : request.getThreads();
        int incrementsPerThread = request == null ? 0 : request.getIncrementsPerThread();

        // ========== 步骤 2: 严格参数校验 ==========
        // lockType 不能为空，threads 和 incrementsPerThread 必须为正整数
        if (lockType == null || lockType.trim().isEmpty() || threads <= 0 || incrementsPerThread <= 0) {
            return fail("PARAM_VALIDATION", "lockType required; threads > 0; incrementsPerThread > 0");
        }

        // ========== 步骤 3: 计算理论期望值 ==========
        // 假设没有并发冲突，所有递增操作都成功执行
        int expected = threads * incrementsPerThread;
        
        // ========== 步骤 4: 根据 lockType 分发到不同的演示逻辑 ==========
        if ("pessimistic".equalsIgnoreCase(lockType)) {
            // 悲观锁模式：使用 synchronized 实现先锁后操作
            return runPessimistic(threads, incrementsPerThread, expected);
        }
        if ("optimistic".equalsIgnoreCase(lockType)) {
            // 乐观锁模式：使用 CAS 实现冲突检测和重试
            return runOptimistic(threads, incrementsPerThread, expected);
        }
        
        // lockType 参数不匹配任何支持的选项
        return fail("PARAM_VALIDATION", "lockType only supports pessimistic | optimistic");
    }

    /**
     * 【悲观锁模式演示】执行基于 synchronized 的计数器递增测试。
     * <p>
     * 【实验设计】
     * 1. 创建 AtomicInteger 作为共享计数器（虽然使用了 volatile，但这里主要是演示锁机制）
     * 2. 创建 threadCount 个线程，每个线程执行 incrementsPerThread 次递增操作
     * 3. 每次递增前必须先获取 monitor 锁，确保互斥访问
     * 4. 等待所有线程完成后，统计最终值
     * <p>
     * 【悲观锁的工作流程】
     * 线程 A 的执行序列：
     *   ① 尝试获取 monitor 锁
     *   ② 如果成功，进入 synchronized 块
     *   ③ 执行 counter.incrementAndGet()
     *   ④ 退出 synchronized 块，自动释放锁
     * <p>
     * 线程 B 的执行序列（与 A 并发）：
     *   ① 尝试获取 monitor 锁
     *   ② 如果 A 正持有锁，B 被阻塞，进入等待队列
     *   ③ 等待 A 释放锁后，B 获取锁并进入临界区
     *   ④ 执行 counter.incrementAndGet()
     *   ⑤ 释放锁
     * <p>
     * 【关键特点】
     * - 互斥访问：同一时刻只有一个线程能执行 incrementAndGet()
     * - 阻塞等待：未获取到锁的线程会被挂起，不消耗 CPU
     * - 自动释放：JVM 会在退出 synchronized 块时自动解锁，即使在异常情况下
     * - 零重试：因为通过锁避免了冲突，不需要重试机制
     * 
     * @param threads 并发线程数
     * @param incrementsPerThread 每个线程的递增次数
     * @param expected 理论期望值
     * @return 响应对象，包含悲观锁模式的演示结果
     */
    private PessimisticOptimisticLockResponse runPessimistic(int threads, int incrementsPerThread, int expected) {
        // 创建原子计数器，初始值为 0
        // 注意：虽然 AtomicInteger 本身提供 CAS 原子操作，但在悲观锁模式下
        // 我们使用 synchronized 包裹 incrementAndGet()，演示的是锁机制
        AtomicInteger counter = new AtomicInteger(0);
        
        // 创建工作线程列表
        List<Thread> workers = new ArrayList<>();

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                // 每个线程执行 incrementsPerThread 次递增操作
                for (int j = 0; j < incrementsPerThread; j++) {
                    // 【核心机制】synchronized 同步块：实现悲观锁
                    synchronized (monitor) {
                        // 在锁保护下执行原子递增
                        // synchronized 保证了：
                        // 1. 互斥性：同一时刻只有一个线程能执行这行代码
                        // 2. 可见性：修改后的值立即对其他线程可见
                        // 3. 有序性：禁止指令重排序
                        counter.incrementAndGet();
                    }
                    // 退出 synchronized 块时，JVM 自动执行 monitorexit 释放锁
                }
            }, "pessimistic-worker-" + i);  // 线程命名便于调试
            workers.add(worker);
        }

        // ========== 等待所有线程完成 ==========
        // 调用封装的 joinAll 方法，统一处理线程启动和等待
        joinAll(workers);
        
        // 构建响应对象
        return build(true, "PESSIMISTIC_DONE",
                "pessimistic lock: lock first, then mutate shared data",
                "pessimistic", threads, incrementsPerThread, expected, counter.get(), 0);
    }

    /**
     * 【乐观锁模式演示】执行基于 CAS 的计数器递增测试。
     * 
     * 【实验设计】
     * 1. 创建 AtomicInteger 作为共享计数器
     * 2. 创建 AtomicInteger 记录重试次数
     * 3. 创建 threadCount 个线程，每个线程执行 incrementsPerThread 次 CAS 递增操作
     * 4. 每次操作采用"自旋 + CAS"的方式：
     *    - 读取当前值
     *    - 计算新值
     *    - 尝试 CAS 更新
     *    - 如果失败，重试次数 +1，继续自旋直到成功
     * 5. 统计最终值和总重试次数
     * 
     * 【乐观锁的工作流程】（以一次递增为例）
     * 
     * 线程 A 的执行序列：
     *   ① 读取当前值：current = value.get()  （假设 current=100）
     *   ② 计算新值：next = current + 1  （next=101）
     *   ③ CAS 尝试：compareAndSet(100, 101)
     *      - 情况 1：成功（value 仍然是 100），返回 true，更新完成
     *      - 情况 2：失败（value 已被其他线程改为 102），返回 false
     *   ④ 如果失败：
     *      - retries++
     *      - 回到步骤①，重新读取、计算、CAS
     *   ⑤ 如果成功：updated=true，退出 while 循环，执行下一次递增
     * 
     * 【并发冲突场景分析】
     * 假设 value=100，线程 A 和 B 同时尝试递增：
     * 
     * 时间线：
     *   T1: A 读取 value，current=100
     *   T2: B 读取 value，current=100（与 A 相同）
     *   T3: A 计算 next=101，执行 CAS(100, 101)，成功！value 变为 101
     *   T4: B 计算 next=101，执行 CAS(100, 101)，失败！
     *       因为 value 已经被 A 改为 101，不等于预期的 100
     *   T5: B 重试：retries++，重新读取 value=101
     *   T6: B 计算 next=102，执行 CAS(101, 102)，成功！value 变为 102
     * 
     * 结果：
     * - value 从 100 增加到 102（正确）
     * - retries=1（B 重试了 1 次）
     * 
     * 【关键特点】
     * - 无锁编程：线程永远不会被阻塞，可以自由执行
     * - 冲突检测：CAS 操作会自动发现并发修改
     * - 自旋重试：失败后不是阻塞，而是立即重试（while 循环）
     * - CPU 密集：在高竞争场景下，大量重试会浪费 CPU 周期
     * 
     * @param threads 并发线程数
     * @param incrementsPerThread 每个线程的递增次数
     * @param expected 理论期望值
     * @return 响应对象，包含乐观锁模式的演示结果
     */
    private PessimisticOptimisticLockResponse runOptimistic(int threads, int incrementsPerThread, int expected) {
        // 创建原子计数器，初始值为 0
        // AtomicInteger 内部使用 volatile + CAS 保证线程安全
        AtomicInteger value = new AtomicInteger(0);
        
        // 创建重试计数器，记录 CAS 失败的总次数
        // 这个值反映了并发冲突的激烈程度
        AtomicInteger retries = new AtomicInteger(0);
        
        // 创建工作线程列表
        List<Thread> workers = new ArrayList<>();

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                // 每个线程执行 incrementsPerThread 次 CAS 递增操作
                for (int j = 0; j < incrementsPerThread; j++) {
                    // 标记本次递增是否成功
                    boolean updated = false;
                    
                    // 【核心机制】自旋 + CAS：乐观锁的经典实现
                    // while 循环会一直执行，直到 CAS 成功为止
                    while (!updated) {
                        // 步骤 1: 读取当前值（volatile 读，保证可见性）
                        int current = value.get();
                        
                        // 步骤 2: 计算期望的新值
                        int next = current + 1;
                        
                        // 步骤 3: CAS 原子操作
                        // compareAndSet(预期原值，新值)
                        // 如果 value 的当前值等于 current，则设置为 next，返回 true
                        // 如果 value 的当前值不等于 current（已被其他线程修改），返回 false
                        if (value.compareAndSet(current, next)) {
                            // CAS 成功！本次递增完成
                            updated = true;
                        } else {
                            // CAS 失败！说明发生了并发冲突
                            // 其他线程在 get() 和 compareAndSet() 之间修改了 value
                            // 重试次数 +1，然后继续自旋重试
                            retries.incrementAndGet();
                        }
                    }
                    // while 循环结束，说明本次递增成功
                }
                // 所有递增操作完成
            }, "optimistic-worker-" + i);  // 线程命名便于调试
            workers.add(worker);
        }

        // ========== 等待所有线程完成 ==========
        // 调用封装的 joinAll 方法，统一处理线程启动和等待
        joinAll(workers);
        
        // 构建响应对象
        return build(true, "OPTIMISTIC_DONE",
                "optimistic lock: CAS with retry on conflict",
                "optimistic", threads, incrementsPerThread, expected, value.get(), retries.get());
    }

    /**
     * 封装线程启动和等待的辅助方法。
     * 
     * 【处理流程】
     * 1. 启动所有线程：调用每个 Thread 的 start() 方法
     * 2. 等待所有线程完成：依次调用每个 Thread 的 join() 方法
     * 
     * 【为什么要分两个阶段】
     * - 第一阶段（start）：让所有线程进入就绪状态，可以并发执行
     * - 第二阶段（join）：主线程阻塞等待，确保所有子线程完成后才继续
     * 
     * 【中断处理】
     * - 如果主线程在 join() 过程中被中断，不能简单地吞掉 InterruptedException
     * - 应该恢复中断状态：Thread.currentThread().interrupt()
     * - 然后抛出运行时异常，终止当前操作
     * 
     * @param workers 工作线程列表
     */
    private void joinAll(List<Thread> workers) {
        // ========== 第一阶段：启动所有线程 ==========
        // 让所有线程进入就绪状态，由 JVM 调度器决定何时执行
        for (Thread worker : workers) {
            worker.start();
        }
        
        // ========== 第二阶段：等待所有线程完成 ==========
        // 主线程依次等待每个子线程终止
        for (Thread worker : workers) {
            try {
                // join() 会阻塞当前线程，直到目标线程正常终止
                worker.join();
            } catch (InterruptedException ex) {
                // 【关键实践】中断处理
                // 1. 捕获 InterruptedException 后，线程的中断状态会被清除
                // 2. 为了向上传递中断信号，需要手动恢复中断状态
                Thread.currentThread().interrupt();
                // 3. 抛出运行时异常，终止当前操作
                throw new IllegalStateException("thread interrupted", ex);
            }
        }
    }

    /**
     * 构建失败响应的辅助方法。
     * 
     * @param stage 错误阶段标识
     * @param message 错误描述信息
     * @return 失败响应对象
     */
    private PessimisticOptimisticLockResponse fail(String stage, String message) {
        // 失败响应：lockType 等字段设为 null，计数相关字段设为 0
        return build(false, stage, message, null, 0, 0, 0, 0, 0);
    }

    /**
     * 构建通用响应的辅助方法。
     * 
     * 【设计目的】
     * 统一悲观锁和乐观锁的响应格式，封装所有返回字段。
     * 
     * 【字段说明】
     * - success: 操作是否成功
     * - stage: 当前阶段标识（如 PESSIMISTIC_DONE、OPTIMISTIC_DONE）
     * - message: 人类可读的描述信息
     * - lockType: 使用的锁类型（"pessimistic" 或 "optimistic"）
     * - threads: 并发线程数
     * - incrementsPerThread: 每个线程的递增次数
     * - expected: 理论期望值（threads × incrementsPerThread）
     * - actual: 实际得到的最终值（应该等于 expected）
     * - retries: 重试次数（仅乐观锁有意义，悲观锁始终为 0）
     * 
     * @param success 是否成功
     * @param stage 阶段标识
     * @param message 描述信息
     * @param lockType 锁类型
     * @param threads 线程数
     * @param incrementsPerThread 每线程递增次数
     * @param expected 期望值
     * @param actual 实际值
     * @param retries 重试次数
     * @return 封装好的响应对象
     */
    private PessimisticOptimisticLockResponse build(boolean success,
                                                    String stage,
                                                    String message,
                                                    String lockType,
                                                    int threads,
                                                    int incrementsPerThread,
                                                    int expected,
                                                    int actual,
                                                    int retries) {
        PessimisticOptimisticLockResponse response = new PessimisticOptimisticLockResponse();
        response.setSuccess(success);                      // 设置成功标志
        response.setStage(stage);                          // 设置阶段标识
        response.setMessage(message);                      // 设置描述信息
        response.setLockType(lockType);                    // 设置锁类型
        response.setThreads(threads);                      // 设置线程数
        response.setIncrementsPerThread(incrementsPerThread);  // 设置每线程递增次数
        response.setExpected(expected);                    // 设置期望值
        response.setActual(actual);                        // 设置实际值
        response.setRetries(retries);                      // 设置重试次数
        return response;
    }
}
