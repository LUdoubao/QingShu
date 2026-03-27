package org.doubao.interview.agent.server.service.impl.q007.jvm;

import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoRequest;
import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoResponse;
import org.doubao.interview.agent.api.service.q007.jvm.SynchronizedDemoService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * 问题 007(JVM)：synchronized 底层原理演示服务类。
 * <p>
 * 【synchronized 的核心原理】
 * 1. 字节码层面：对应 monitorenter 和 monitorexit 指令
 *    - monitorenter: 线程尝试获取对象的监视器锁（Monitor），成功则进入临界区
 *    - monitorexit: 线程释放监视器锁，退出临界区
 *    - JVM 会自动插入 monitorexit，即使在同步块中抛出异常也会释放锁
 * <p>
 * 2. Monitor（对象监视器）机制：
 *    - 每个 Java 对象都可以关联一个 Monitor，用于实现线程间的互斥访问
 *    - Monitor 包含三个关键部分：
 *      ① Owner：当前持有锁的线程引用
 *      ② EntryList：等待获取锁的阻塞队列
 *      ③ WaitSet：调用 wait() 方法后进入的等待集合
 *    - 同一时刻只有一个线程能拥有 Monitor 的所有权（Owner），其他线程必须等待
 * <p>
 * 3. 锁升级优化（JDK 1.6+）：
 *    - 无锁 → 偏向锁 → 轻量级锁 → 重量级锁
 *    - 偏向锁：无竞争时，锁会"偏向"第一个获取它的线程，减少 CAS 开销
 *    - 轻量级锁：存在轻微竞争时，使用 CAS 自旋避免线程阻塞
 *    - 重量级锁：激烈竞争时，依赖操作系统的互斥量（Mutex）实现，线程阻塞
 * <p>
 * 4. Happens-Before 与可见性：
 *    - 解锁操作 happens-before 于后续对同一把锁的加锁操作
 *    - 这意味着：线程 A 在释放锁前对共享变量的所有写入，对线程 B 获取同一把锁后立即可见
 *    - synchronized 同时保证了原子性和可见性
 * <p>
 * 【本示例演示的两个核心特性】
 * 1. 互斥性（Mutual Exclusion）：
 *    - 多线程在同一把锁内执行 count++，结果严格等于理论值
 *    - 证明 synchronized 保证了"读 - 改 - 写"序列的原子性
 * <p>
 * 2. 可见性（Visibility）：
 *    - 写线程在 synchronized 块内设置 ready=true
 *    - 读线程在同一把锁内循环检查，直到看到 true
 *    - 证明锁的释放 - 获取语义保证了跨线程的内存可见性
 */
@Service
public class SynchronizedDemoServiceImpl implements SynchronizedDemoService {

    /**
     * 执行 synchronized 特性演示的核心方法。
     * <p>
     * 【演示流程】
     * 1. 参数校验：确保线程数和每线程递增次数有效
     * 2. 互斥性演示：验证 synchronized 保证 count++ 的原子性
     * 3. 可见性演示：验证 synchronized 的锁语义保证跨线程可见性
     * 4. 结果汇总：构建响应对象，展示两个演示的结果
     * <p>
     * 【预期结果】
     * - mutexWorked: true（实际值 = 理论值，互斥性生效）
     * - visibilityWorked: true（读线程成功看到写线程的修改，可见性生效）
     * 
     * @param request 请求对象，包含以下字段：
     *                - threadCount: 并发线程数（必须大于 0）
     *                - incrementPerThread: 每个线程的递增次数（必须大于 0）
     * @return 响应对象，包含演示结果和统计数据
     */
    @Override
    public SynchronizedDemoResponse run(SynchronizedDemoRequest request) {
        // ========== 步骤 1: 提取并校验请求参数 ==========
        // 空值保护：防止请求对象为 null 导致 NPE
        int threadCount = request == null ? 0 : request.getThreadCount();
        int incrementPerThread = request == null ? 0 : request.getIncrementPerThread();
        
        // 严格参数校验：threadCount 和 incrementPerThread 必须为正整数
        if (threadCount <= 0 || incrementPerThread <= 0) {
            return fail("PARAM_VALIDATION", "threadCount 和 incrementPerThread 必须大于 0");
        }

        // ========== 步骤 2: 执行互斥性演示 ==========
        // 验证 synchronized 保证 count++ 的原子性：实际值应该严格等于理论值
        CounterResult counterResult = runMutexCounterDemo(threadCount, incrementPerThread);
        
        // ========== 步骤 3: 执行可见性演示 ==========
        // 验证 synchronized 的锁语义保证跨线程可见性：读线程应能看到写线程的修改
        boolean visibilityWorked = runVisibilityDemo();

        // ========== 步骤 4: 构建响应对象 ==========
        SynchronizedDemoResponse response = new SynchronizedDemoResponse();
        response.setSuccess(true);
        response.setStage("SYNCHRONIZED_DEMO_DONE");
        // 核心结论：synchronized 基于 Monitor 提供互斥性与锁语义可见性
        response.setMessage("演示完成：synchronized 基于 Monitor 提供互斥与锁语义可见性");
        response.setExpectedCount(counterResult.expected);    // 理论应有的最终值
        response.setActualCount(counterResult.actual);        // 实际得到的最终值
        // 判断互斥性是否生效：实际值 == 理论值表示没有更新丢失
        response.setMutexWorked(counterResult.actual == counterResult.expected);
        response.setVisibilityWorked(visibilityWorked);       // 可见性是否生效
        return response;
    }

    /**
     * 【互斥性演示】验证 synchronized 保证复合操作的原子性。
     * 
     * 【实验设计】
     * 1. 创建 MonitorCounter 对象，其 increment() 方法是 synchronized 的
     * 2. 启动多个线程，每个线程执行 incrementPerThread 次 counter.increment()
     * 3. 等待所有线程完成后，统计最终值
     * 4. 比较理论值（threadCount * incrementPerThread）与实际值
     * 
     * 【关键原理】
     * - synchronized 方法等价于在方法体前后添加 monitorenter/monitorexit 指令
     * - 同一时刻只有一个线程能执行 increment() 方法，其他线程必须等待
     * - 这保证了 count++ 的"读 - 改 - 写"序列不会被其他线程打断
     * 
     * 【与 volatile 的对比】
     * - volatile: 只保证可见性，不保证原子性，count++ 会丢失更新
     * - synchronized: 既保证原子性（互斥访问），又保证可见性（happens-before）
     * 
     * @param threadCount 并发线程数
     * @param incrementPerThread 每个线程的递增次数
     * @return 包含理论值和实际值的結果对象
     */
    private CounterResult runMutexCounterDemo(int threadCount, int incrementPerThread) {
        // 创建同步计数器对象，内部使用 synchronized 方法保护 count++ 操作
        final MonitorCounter counter = new MonitorCounter();
        
        // 创建 CountDownLatch 用于等待所有线程完成
        // latch 初始值为 threadCount，每有一个线程完成就减 1，直到为 0
        final CountDownLatch latch = new CountDownLatch(threadCount);

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                // 每个线程执行 incrementPerThread 次同步递增操作
                for (int j = 0; j < incrementPerThread; j++) {
                    // 【关键点】调用 synchronized 方法 increment()
                    // 同一时刻只有一个线程能执行该方法，其他线程必须等待获取锁
                    // 这保证了 count++ 的原子性，不会丢失更新
                    counter.increment();
                }
                // 当前线程完成所有递增操作，latch 计数减 1
                latch.countDown();
            }, "sync-counter-" + i);
            t.start();
        }

        // ========== 等待所有线程完成 ==========
        // latch.await() 会阻塞当前线程，直到 latch.count 变为 0
        // 即所有线程都调用了 countDown()
        try {
            latch.await();
        } catch (InterruptedException ex) {
            // 如果被中断，恢复中断状态（良好的中断处理实践）
            Thread.currentThread().interrupt();
        }

        // ========== 构建结果对象 ==========
        CounterResult result = new CounterResult();
        // 理论值：线程数 × 每线程递增次数（假设没有并发冲突）
        result.expected = threadCount * incrementPerThread;
        // 实际值：同步计数器的最终值（应该严格等于理论值）
        result.actual = counter.get();
        return result;
    }

    /**
     * 【可见性演示】验证 synchronized 的锁语义保证跨线程内存可见性。
     * 
     * 【实验设计】
     * 1. 创建 VisibilityState 对象，包含 monitor 锁对象、ready 标志和 seen 标志
     * 2. 启动写线程：在 synchronized(state.monitor) 块内设置 ready=true
     * 3. 启动读线程：在 synchronized(state.monitor) 块内循环检查 ready 是否为 true
     * 4. 如果读线程最终看到了 ready=true，说明可见性生效
     * 
     * 【关键原理：Happens-Before 规则】
     * - JMM（Java 内存模型）规定：解锁操作 happens-before 于后续对同一把锁的加锁操作
     * - 具体到本例：
     *   ① 写线程获取 state.monitor 锁
     *   ② 写线程设置 ready=true
     *   ③ 写线程释放 state.monitor 锁
     *   ④ 读线程获取 state.monitor 锁（此时发生 happens-before 传递）
     *   ⑤ 读线程读取 ready 字段，必然看到 true
     * 
     * 【为什么必须在同一个 monitor 上同步】
     * - 如果写线程锁定的是 lockA，读线程锁定的是 lockB，两者互不相关
     * - 只有当两个线程在同一个 monitor 上建立"解锁→加锁"的先后关系时，
     *   happens-before 规则才会传递可见性保证
     * 
     * 【与 volatile 的对比】
     * - volatile: 单次读写可见，但无法建立 happens-before 链
     * - synchronized: 通过锁的释放 - 获取语义，建立了更强的 happens-before 关系
     * 
     * @return true 表示读线程成功看到写线程的修改（可见性生效），false 表示超时未看到
     */
    private boolean runVisibilityDemo() {
        // 创建可见性状态对象，包含 monitor 锁和两个布尔标志
        final VisibilityState state = new VisibilityState();
        
        // ========== 创建写线程 ==========
        Thread writer = new Thread(() -> {
            // 【关键动作】在 synchronized 块内写入 ready=true
            // synchronized(state.monitor) 确保：
            // 1. 互斥访问：同一时刻只有一个线程能进入此代码块
            // 2. 可见性：写入的值会在释放锁时刷新到主内存
            synchronized (state.monitor) {
                state.ready = true;
                // 注意：这里不需要显式唤醒读线程，因为读线程会主动轮询
            }
        }, "sync-writer");

        // ========== 创建读线程 ==========
        Thread reader = new Thread(() -> {
            // 设置超时时间：500ms 后停止检查，避免无限循环
            long end = System.currentTimeMillis() + 500L;
            
            // 【轮询检查】在同一把锁内反复读取 ready 字段
            while (System.currentTimeMillis() < end) {
                // 【关键点】必须在同一个 monitor (state.monitor) 上同步
                // 只有这样才能利用 happens-before 规则获得可见性保证
                synchronized (state.monitor) {
                    if (state.ready) {
                        // 成功看到 ready=true，标记 seen 并退出
                        state.seen = true;
                        return;
                    }
                }
                // Thread.yield()：提示调度器"我愿意让出 CPU"
                // 目的：减少忙等时的 CPU 消耗，同时给写线程执行的机会
                Thread.yield();
            }
            // 如果超时仍未看到 ready=true，循环自然结束，seen 保持为 false
        }, "sync-reader");

        // ========== 启动线程 ==========
        // 先启动读线程，再启动写线程
        // 这样可以确保读线程已经开始轮询，写线程的修改能被及时检测到
        reader.start();
        writer.start();

        // ========== 等待两个线程完成 ==========
        // 使用 join() 无限期等待，直到线程正常终止
        try {
            writer.join();  // 等待写线程完成
            reader.join();  // 等待读线程完成（最多 500ms）
        } catch (InterruptedException ex) {
            // 如果被中断，恢复中断状态
            Thread.currentThread().interrupt();
        }

        // ========== 返回可见性检测结果 ==========
        // state.seen 为 true 表示读线程在 synchronized 块内看到了 ready=true
        // 这证明了 synchronized 的锁语义保证了跨线程的内存可见性
        return state.seen;
    }

    /**
     * 构建失败响应的辅助方法。
     * 
     * @param stage 错误阶段标识
     * @param msg 错误描述信息
     * @return 失败响应对象
     */
    private SynchronizedDemoResponse fail(String stage, String msg) {
        SynchronizedDemoResponse response = new SynchronizedDemoResponse();
        response.setSuccess(false);      // 标记为失败
        response.setStage(stage);        // 设置错误阶段
        response.setMessage(msg);        // 设置错误信息
        return response;
    }

    /**
     * 【Monitor 计数器实体类】
     * 
     * 【设计意图】
     * 封装 synchronized 方法，演示互斥性和原子性保证。
     * 
     * 【synchronized 方法的字节码原理】
     * JDK 编译后，synchronized 方法会在方法入口和出口处生成 monitorenter/monitorexit 指令：
     * ```
     * public synchronized void increment();
     *   flags: ACC_PUBLIC, ACC_SYNCHRONIZED
     *   Code:
     *     0: aload_0
     *     1: dup
     *     2: getfield      #1 // Field count:I
     *     5: iconst_1
     *     6: iadd
     *     7: putfield      #1 // Field count:I
     *    10: return
     * ```
     * JVM 会自动在 monitorenter 之前获取 this 对象的 Monitor，在 monitorexit 时释放。
     * 即使在同步方法中抛出异常，JVM 也会确保 Monitor 被正确释放。
     * 
     * 【性能特点】
     * - JDK 1.6+ 引入了锁升级机制，synchronized 方法的性能已大幅优化
     * - 在低竞争场景下，偏向锁和轻量级锁的开销很小
     * - 在激烈竞争场景下，重量级锁会导致线程阻塞和上下文切换
     * 
     * 【适用场景】
     * - 需要保证原子性的复合操作（如 count++）
     * - 需要同时保证原子性和可见性的场景
     * - 临界区代码执行时间较短，锁持有时间不长
     */
    private static class MonitorCounter {
        /**
         * 计数器值：非 volatile，依赖 synchronized 保证可见性
         */
        private int count;

        /**
         * 同步递增方法：synchronized 修饰实例方法，锁定的是 this 对象（当前实例）。
         * 
         * 【线程安全分析】
         * - 当多个线程调用同一个 MonitorCounter 实例的 increment() 方法时，
         *   它们竞争的是同一个 this 对象的 Monitor 锁
         * - 同一时刻只有一个线程能执行 increment()，其他线程必须等待
         * - 这保证了 count++ 操作的原子性，不会丢失更新
         * 
         * 【注意事项】
         * - 如果创建多个 MonitorCounter 实例，它们之间的 increment() 调用是并发执行的
         *   因为每个实例有独立的 this 锁
         * - 若要全局互斥，需要使用 static synchronized 方法或共享同一个锁对象
         */
        private synchronized void increment() {
            // count++ 操作：在 synchronized 保护下，这是线程安全的
            // JVM 生成的字节码等价于：
            // monitorenter(this)
            //   count = this.count + 1
            // monitorexit(this)
            count++;
        }

        /**
         * 同步获取方法：synchronized 修饰实例方法。
         * 
         * 【为什么 get() 也需要同步】
         * 1. 保证可见性：如果不加 synchronized，get() 可能读取到过期的 count 值
         *    （因为 count 不是 volatile 字段）
         * 2. 保证有序性：防止 get() 被重排序到其他操作之前
         * 3. 遵循最佳实践：对于由 synchronized 保护的变量，所有访问都应该在锁内进行
         * 
         * 【Happens-Before 保证】
         * - 线程 A 调用 increment() 后释放锁
         * - 线程 B 调用 get() 时获取同一把锁
         * - 根据 happens-before 规则，线程 B 必然看到线程 A 的最新修改
         * 
         * @return 当前计数器值
         */
        private synchronized int get() {
            return count;   // 在锁保护下读取，确保看到的是最新值
        }
    }

    /**
     * 【可见性状态实体类】
     * 
     * 【设计意图】
     * 封装用于可见性演示的状态字段和 monitor 锁对象。
     * 
     * 【关键字段说明】
     * - monitor: 专用的锁对象，用于协调写线程和读线程的可见性
     *   为什么不直接使用 this 作为锁？
     *   - 使用独立的 monitor 可以更清晰地表达锁的用途
     *   - 避免与其他 synchronized 方法意外地竞争同一把锁
     *   - 更符合"锁分离"的设计原则
     * 
     * - ready: 写线程设置的标志位，初始为 false
     *   为什么不是 volatile？
     *   - 本例的目的是演示 synchronized 的可见性，而不是 volatile
     *   - 在 synchronized 块内读写非 volatile 字段，依靠锁的 happens-before 保证可见性
     * 
     * - seen: 读线程的检测标志，初始为 false
     *   如果最终 seen=true，说明读线程在 synchronized 块内看到了 ready=true
     * 
     * 【内存语义分析】
     * 写线程执行序列：
     *   ① 获取 monitor 锁
     *   ② 写入 ready=true（仅更新工作内存）
     *   ③ 释放 monitor 锁（将工作内存刷新到主内存）
     * 
     * 读线程执行序列：
     *   ① 获取 monitor 锁（从主内存重新加载变量）
     *   ② 读取 ready 字段（看到的是写线程刷新的新值）
     *   ③ 如果 ready==true，设置 seen=true
     *   ④ 释放 monitor 锁
     */
    private static class VisibilityState {
        /**
         * 专用锁对象：用于协调写线程和读线程的可见性。
         * 所有对该对象的 synchronized 引用都会竞争同一把 Monitor 锁。
         */
        private final Object monitor = new Object();
        
        /**
         * 就绪标志：由写线程设置为 true，由读线程检查。
         * 虽然本身不是 volatile，但在 synchronized 块内的读写受到锁的内存语义保护。
         */
        private boolean ready = false;
        
        /**
         * 检测标志：读线程在看到 ready=true 后设置此标志。
         * 最终用于判断可见性是否生效。
         */
        private boolean seen = false;
    }

    /**
     * 【结果数据类】封装互斥性演示的统计结果。
     * 
     * 【字段说明】
     * - expected: 理论上的最终值（假设没有并发冲突）
     * - actual: 实际的最终值（在 synchronized 保护下应该等于理论值）
     * 
     * 【判断标准】
     * 如果 actual == expected，说明 synchronized 成功保证了原子性
     * 如果 actual != expected，说明发生了严重的并发 bug（理论上不应该出现）
     */
    private static class CounterResult {
        private int expected;   // 理论值 = threadCount × incrementPerThread
        private int actual;     // 实际值 = synchronized count 的最终值（应该 == expected）
    }
}
