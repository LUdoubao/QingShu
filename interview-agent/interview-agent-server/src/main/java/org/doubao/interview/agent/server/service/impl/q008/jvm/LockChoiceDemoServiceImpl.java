package org.doubao.interview.agent.server.service.impl.q008.jvm;

import org.doubao.interview.agent.api.dto.q008.jvm.LockChoiceDemoRequest;
import org.doubao.interview.agent.api.dto.q008.jvm.LockChoiceDemoResponse;
import org.doubao.interview.agent.api.service.q008.jvm.LockChoiceDemoService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 问题 008(JVM)：synchronized 与 ReentrantLock 选型对比演示服务类。
 * <p>
 * 【核心选型结论】
 * <p>
 * 1. 简单互斥场景优先选择 synchronized：
 *    - 语法简洁：直接在方法或代码块上使用，无需显式创建锁对象
 *    - 自动释放：JVM 会在方法退出或异常时自动释放锁，降低死锁风险
 *    - 代码可维护性高：不易遗漏 unlock 操作，减少 bug
 *    - JDK 1.6+ 优化后性能大幅提升：偏向锁、轻量级锁、锁消除等优化
 * <p>
 * 2. 需要高级并发控制能力时选择 ReentrantLock：
 *    - 可中断响应（interruptible）：等待锁的过程中可以响应线程中断
 *    - 可超时获取（tryLock with timeout）：避免无限期等待，防止死锁
 *    - 公平锁支持（fair lock）：可选择按 FIFO 顺序分配锁，避免线程饥饿
 *    - 多条件队列（multiple condition queues）：支持多个 Condition 对象，实现精细的线程通信
 *    - 非阻塞尝试（tryLock）：立即返回获取锁的结果，适合特定业务场景
 * <p>
 * 3. 性能对比需基于具体场景压测：
 *    - 低竞争场景：synchronized 经过优化后性能与 ReentrantLock 相当
 *    - 激烈竞争场景：ReentrantLock 可能表现更好，但差异通常不大
 *    - 不建议仅凭"谁更快"做绝对结论，应结合实际业务场景测试
 * <p>
 * 【关键区别对比】
 * <p>
 * | 特性             | synchronized              | ReentrantLock                    |
 * |------------------|---------------------------|----------------------------------|
 * | 实现层面         | JVM 层面（字节码指令）     | API 层面（Java 类库）            |
 * | 锁释放           | 自动释放                   | 必须手动调用 unlock()            |
 * | 可中断性         | 不支持                     | 支持（lockInterruptibly）        |
 * | 超时机制         | 不支持                     | 支持（tryLock with timeout）     |
 * | 公平锁           | 仅非公平锁                 | 可选公平/非公平                  |
 * | 条件队列         | 单个（wait/notify）        | 多个（Condition 对象）           |
 * | 性能优化         | JVM 自动优化               | 需要开发者手动优化               |
 * | 代码复杂度       | 低                         | 高（需 finally 保证解锁）        |
 * <p>
 * 【本示例演示内容】
 * 1. synchronized 模式：演示简单互斥场景下的计数器递增
 * 2. ReentrantLock 模式：演示带超时机制和公平锁选项的计数器递增
 * 3. 对比两种模式的最终结果、tryLock 失败次数等指标
 */
@Service
public class LockChoiceDemoServiceImpl implements LockChoiceDemoService {

    /**
     * 执行锁选型对比演示的核心方法。
     * <p>
     * 【处理流程】
     * 1. 提取并校验请求参数：mode、threadCount、incrementPerThread 等
     * 2. 根据 mode 参数选择执行 synchronized 或 ReentrantLock 演示
     * 3. 返回对应的演示结果和统计信息
     * <p>
     * 【参数说明】
     * - mode: 锁模式，可选值为 "synchronized" 或 "reentrant/reentrantlock"
     * - threadCount: 并发线程数（必须大于 0）
     * - incrementPerThread: 每个线程的递增次数（必须大于 0）
     * - fairLock: 是否使用公平锁（仅 ReentrantLock 有效，默认 false）
     * - tryLockTimeoutMillis: tryLock 超时时间（仅 ReentrantLock 有效，默认 10ms）
     * 
     * @param request 请求对象，包含上述配置参数
     * @return 响应对象，包含演示结果、计数器统计、锁使用情况等
     */
    @Override
    public LockChoiceDemoResponse run(LockChoiceDemoRequest request) {
        // ========== 步骤 1: 提取请求参数 ==========
        // 空值保护：防止请求对象或字段为 null 导致 NPE
        String mode = request == null ? null : request.getMode();
        int threadCount = request == null ? 0 : request.getThreadCount();
        int incrementPerThread = request == null ? 0 : request.getIncrementPerThread();
        boolean fair = request != null && request.isFairLock();  // 公平锁选项
        long timeout = request == null ? 0 : request.getTryLockTimeoutMillis();  // tryLock 超时
        
        // ========== 步骤 2: 严格参数校验 ==========
        // mode 不能为空，threadCount 和 incrementPerThread 必须为正整数
        if (threadCount <= 0 || incrementPerThread <= 0 || mode == null || mode.trim().isEmpty()) {
            return fail("PARAM_VALIDATION", "mode 不能为空，threadCount/incrementPerThread 需>0");
        }

        // ========== 步骤 3: 根据 mode 分发到不同的演示逻辑 ==========
        if ("synchronized".equalsIgnoreCase(mode)) {
            // synchronized 模式：演示简单互斥场景
            return runSynchronized(threadCount, incrementPerThread);
        }
        if ("reentrant".equalsIgnoreCase(mode) || "reentrantlock".equalsIgnoreCase(mode)) {
            // ReentrantLock 模式：演示高级锁功能（公平锁、超时 tryLock）
            // 如果 timeout<=0，使用默认值 10ms
            return runReentrant(threadCount, incrementPerThread, fair, timeout <= 0 ? 10L : timeout);
        }

        // mode 参数不匹配任何支持的选项
        return fail("PARAM_VALIDATION", "mode 仅支持 synchronized/reentrant");
    }

    /**
     * 【synchronized 模式演示】执行基于 synchronized 的计数器递增测试。
     * 
     * 【实验设计】
     * 1. 创建 SyncCounter 对象，其 increment() 和 get() 方法都是 synchronized 的
     * 2. 启动 threadCount 个线程，每个线程执行 incrementPerThread 次 counter.increment()
     * 3. 等待所有线程完成后，统计最终值
     * 4. 验证实际值是否等于理论值（证明 synchronized 保证了原子性）
     * 
     * 【synchronized 的优势】
     * - 语法简洁：直接在方法上添加修饰符即可
     * - 自动管理：JVM 自动处理锁的获取和释放，即使在异常情况下
     * - 代码安全：不会出现忘记解锁导致的死锁问题
     * - JVM 优化：JIT 编译器可以进行锁消除、锁粗化等优化
     * 
     * @param threadCount 并发线程数
     * @param incrementPerThread 每个线程的递增次数
     * @return 响应对象，包含 synchronized 模式的演示结果
     */
    private LockChoiceDemoResponse runSynchronized(int threadCount, int incrementPerThread) {
        // 创建同步计数器对象，内部使用 synchronized 方法保护 count++ 操作
        final SyncCounter counter = new SyncCounter();
        
        // 创建 CountDownLatch 用于等待所有线程完成
        // latch 初始值为 threadCount，每有一个线程完成就减 1，直到为 0
        CountDownLatch latch = new CountDownLatch(threadCount);

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                // 每个线程执行 incrementPerThread 次同步递增操作
                for (int j = 0; j < incrementPerThread; j++) {
                    // 【关键点】调用 synchronized 方法 increment()
                    // 同一时刻只有一个线程能执行该方法，其他线程必须等待获取锁
                    counter.increment();
                }
                // 当前线程完成所有递增操作，latch 计数减 1
                latch.countDown();
            }, "sync-choice-" + i);  // 线程命名便于调试
            t.start();
        }

        // ========== 等待所有线程完成 ==========
        // 调用封装的 await 方法，处理 InterruptedException
        await(latch);
        
        // 计算理论值：线程数 × 每线程递增次数
        int expected = threadCount * incrementPerThread;
        // 获取实际值：synchronized 计数器的最终值（应该等于理论值）
        int actual = counter.get();

        // ========== 构建响应对象 ==========
        LockChoiceDemoResponse r = new LockChoiceDemoResponse();
        r.setSuccess(true);
        r.setStage("SYNCHRONIZED_DONE");
        // 核心结论：synchronized 适合简单互斥，自动释放锁，代码更稳妥
        r.setMessage("synchronized 适合简单互斥，自动释放锁，代码更稳妥");
        r.setMode("synchronized");          // 设置锁模式
        r.setExpectedCount(expected);        // 理论值
        r.setActualCount(actual);            // 实际值
        r.setFairLock(false);                // synchronized 不支持公平锁
        r.setTryLockFailedCount(0);          // synchronized 没有 tryLock 机制
        return r;
    }

    /**
     * 【ReentrantLock 模式演示】执行基于 ReentrantLock 的计数器递增测试。
     * 
     * 【实验设计】
     * 1. 创建 ReentrantLock 对象，可选择公平/非公平模式
     * 2. 使用 AtomicInteger 作为计数器（本身线程安全）
     * 3. 启动 threadCount 个线程，每个线程尝试用 tryLock 获取锁
     * 4. 如果在超时时间内成功获取锁，则执行递增操作
     * 5. 如果超时未获取到锁，记录失败次数并跳过本次操作
     * 6. 统计最终值和 tryLock 失败次数
     * 
     * 【ReentrantLock 的高级特性】
     * - tryLock(timeout, unit): 在指定时间内尝试获取锁，超时返回 false
     *   优势：避免无限期等待，可以设置重试策略或降级处理
     * - 公平锁（fair=true）：按 FIFO 顺序分配锁，避免线程饥饿
     *   劣势：吞吐量通常低于非公平锁（因为需要维护队列顺序）
     * - 可中断响应：lockInterruptibly() 方法可以响应线程中断
     * - 多条件队列：通过 newCondition() 创建多个 Condition 对象
     * 
     * 【关键注意事项】
     * - 必须在 finally 块中调用 unlock()，确保锁被释放
     * - 使用 tryLock 时，只有在成功获取锁后才能操作共享资源
     * - 公平锁的性能通常低于非公平锁，需谨慎选择
     * 
     * @param threadCount 并发线程数
     * @param incrementPerThread 每个线程的递增次数
     * @param fair 是否使用公平锁（true=公平锁，false=非公平锁）
     * @param timeoutMillis tryLock 的超时时间（毫秒）
     * @return 响应对象，包含 ReentrantLock 模式的演示结果
     */
    private LockChoiceDemoResponse runReentrant(int threadCount, int incrementPerThread, boolean fair, long timeoutMillis) {
        // 创建 ReentrantLock 对象
        // fair 参数决定是公平锁还是非公平锁：
        // - true: 公平锁，按等待顺序分配锁（FIFO），避免饥饿但吞吐量较低
        // - false: 非公平锁，允许插队，吞吐量较高但可能导致某些线程长期等待
        final ReentrantLock lock = new ReentrantLock(fair);
        
        // 使用 AtomicInteger 作为计数器（本身是线程安全的）
        // 注意：这里 AtomicInteger 的原子性与 ReentrantLock 无关，仅用于统计
        final AtomicInteger counter = new AtomicInteger(0);
        
        // 统计 tryLock 失败的次数（超时未获取到锁）
        final AtomicInteger tryFail = new AtomicInteger(0);
        
        // 创建 CountDownLatch 用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(threadCount);

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                // 每个线程执行 incrementPerThread 次带锁保护的递增操作
                for (int j = 0; j < incrementPerThread; j++) {
                    boolean locked = false;  // 标记是否成功获取锁
                    try {
                        // 【核心特性】tryLock with timeout：在指定时间内尝试获取锁
                        // 如果在 timeoutMillis 毫秒内成功获取锁，返回 true
                        // 如果超时仍未获取到锁，返回 false（不会抛出异常）
                        // 如果等待过程中被中断，抛出 InterruptedException
                        locked = lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
                        
                        if (!locked) {
                            // tryLock 失败：超时未获取到锁
                            // 记录失败次数，跳过本次递增操作
                            tryFail.incrementAndGet();
                            continue;
                        }
                        
                        // 成功获取锁，执行递增操作
                        // 注意：这里使用 AtomicInteger 是为了演示，实际上在锁保护下普通 int 也可以
                        counter.incrementAndGet();
                        
                    } catch (InterruptedException ex) {
                        // 等待锁的过程中被中断
                        // 恢复中断状态，退出循环
                        Thread.currentThread().interrupt();
                    } finally {
                        // 【关键实践】必须在 finally 块中释放锁
                        // 确保即使在 try 块中抛出异常，锁也能被正确释放
                        // 如果不释放锁，其他线程将永远无法获取该锁，导致死锁
                        if (locked) {
                            lock.unlock();
                        }
                    }
                }
                // 当前线程完成所有递增操作，latch 计数减 1
                latch.countDown();
            }, "reentrant-choice-" + i);  // 线程命名便于调试
            t.start();
        }

        // ========== 等待所有线程完成 ==========
        // 调用封装的 await 方法，处理 InterruptedException
        await(latch);
        
        // 计算理论值：假设所有操作都成功获取锁
        int expected = threadCount * incrementPerThread;

        // ========== 构建响应对象 ==========
        LockChoiceDemoResponse r = new LockChoiceDemoResponse();
        r.setSuccess(true);
        r.setStage("REENTRANTLOCK_DONE");
        // 核心结论：ReentrantLock 功能丰富但需要手动管理锁
        r.setMessage("ReentrantLock 支持公平锁/超时 tryLock，功能更丰富但需手动 unlock");
        r.setMode("reentrant");            // 设置锁模式
        r.setExpectedCount(expected);       // 理论值
        r.setActualCount(counter.get());    // 实际成功递增的次数
        r.setFairLock(fair);                // 是否使用了公平锁
        r.setTryLockFailedCount(tryFail.get());  // tryLock 超时失败的次数
        return r;
    }

    /**
     * 封装 CountDownLatch.await() 的辅助方法，统一处理中断异常。
     * 
     * 【设计目的】
     * 避免在每个需要等待的地方都重复编写相同的异常处理代码，
     * 提高代码复用性和可维护性。
     * 
     * 【中断处理原则】
     * - 捕获 InterruptedException 后，不能简单地吞掉异常
     * - 应该恢复中断状态：Thread.currentThread().interrupt()
     * - 这样上层调用者才能检测到中断事件并做出相应处理
     * 
     * @param latch 需要等待的 CountDownLatch 对象
     */
    private void await(CountDownLatch latch) {
        try {
            // 阻塞等待，直到 latch.count 变为 0
            latch.await();
        } catch (InterruptedException ex) {
            // 恢复中断状态：良好的中断处理实践
            // 不要简单地吞掉 InterruptedException，应该向上传递中断信号
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 构建失败响应的辅助方法。
     * 
     * @param stage 错误阶段标识
     * @param message 错误描述信息
     * @return 失败响应对象
     */
    private LockChoiceDemoResponse fail(String stage, String message) {
        LockChoiceDemoResponse r = new LockChoiceDemoResponse();
        r.setSuccess(false);      // 标记为失败
        r.setStage(stage);        // 设置错误阶段
        r.setMessage(message);    // 设置错误信息
        return r;
    }

    /**
     * 【同步计数器实体类】用于 synchronized 模式演示。
     * 
     * 【设计意图】
     * 封装 synchronized 方法，演示简单互斥场景下的线程安全计数。
     * 
     * 【技术要点】
     * - increment() 和 get() 都使用 synchronized 修饰
     * - synchronized 修饰实例方法时，锁定的是 this 对象（当前实例）
     * - 所有对 count 的访问都在锁保护下，确保原子性和可见性
     * 
     * 【与 ReentrantLock 的对比】
     * - 优点：代码简洁，JVM 自动管理锁，不易出错
     * - 缺点：功能单一，无法实现超时、公平锁等高级特性
     */
    private static class SyncCounter {
        private int count;
        
        /**
         * 同步递增方法：synchronized 修饰实例方法。
         * 
         * 【线程安全保障】
         * - 同一时刻只有一个线程能执行此方法
         * - JVM 自动在方法入口插入 monitorenter，出口插入 monitorexit
         * - 即使在方法内抛出异常，monitorexit 也会被执行，锁会被释放
         */
        private synchronized void increment() { 
            count++;  // 在锁保护下执行复合操作
        }
        
        /**
         * 同步获取方法：synchronized 修饰实例方法。
         * 
         * 【为什么 get() 也需要同步】
         * 1. 保证可见性：确保读取到的是最新值
         * 2. 遵循最佳实践：对于 synchronized 保护的变量，所有访问都应该在锁内进行
         */
        private synchronized int get() { 
            return count;  // 在锁保护下读取
        }
    }
}
