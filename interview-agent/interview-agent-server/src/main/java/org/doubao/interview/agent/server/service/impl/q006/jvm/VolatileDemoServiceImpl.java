package org.doubao.interview.agent.server.service.impl.q006.jvm;

import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoRequest;
import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoResponse;
import org.doubao.interview.agent.api.service.q006.jvm.VolatileDemoService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * 问题 006(JVM)：volatile 关键字作用演示服务类。
 * <p>
 * 【volatile 的三大核心特性】
 * 1. 可见性（Visibility）：
 *    - 当一个线程修改了 volatile 变量的值，新值会立即刷新到主内存中
 *    - 其他线程读取该变量时，会从主内存重新加载，而不是使用本地缓存的旧值
 *    - 解决了多线程环境下，线程间共享变量的可见性问题
 * <p>
 * 2. 有序性的一部分（Partial Ordering）：
 *    - volatile 通过"内存屏障"禁止特定类型的指令重排序
 *    - 防止编译器和处理器对 volatile 读写操作进行重排序优化
 *    - 遵循 happens-before 原则，保证 volatile 写操作前的普通写先于 volatile 读操作后的普通读
 * <p>
 * 3. 不保证复合操作的原子性（Non-Atomicity for Compound Operations）：
 *    - volatile 只能保证单次读/写的原子性
 *    - 对于"读 - 改 - 写"这类复合操作（如 count++），volatile 无法提供原子性保障
 *    - 并发环境下会出现更新丢失问题，需要使用 AtomicInteger 或 synchronized
 * <p>
 * 【适用场景】
 * - 状态标志位：如本示例中的 running 标志
 * - 单次写入、多次读取的配置信息
 * - 不适用于计数器、累加器等需要复合操作原子性的场景
 */
@Service
public class VolatileDemoServiceImpl implements VolatileDemoService {

    /**
     * 执行 volatile 特性演示的核心方法。
     * <p>
     * 【演示流程】
     * 1. 参数校验：确保线程数和每线程递增次数有效
     * 2. 可见性演示：验证 volatile 标志位能否被工作线程及时感知
     * 3. 原子性演示：验证 volatile 对 count++ 复合操作的保护不足
     * 4. 结果汇总：构建响应对象，展示两个演示的结果
     * <p>
     * 【预期结果】
     * - visibilityWorked: true（volatile 成功保证可见性）
     * - atomicityLost: true（volatile 未能保证原子性，实际值小于理论值）
     * 
     * @param request 请求对象，包含以下字段：
     *                - threadCount: 并发线程数（必须大于 0）
     *                - incrementPerThread: 每个线程的递增次数（必须大于 0）
     * @return 响应对象，包含演示结果和统计数据
     */
    @Override
    public VolatileDemoResponse run(VolatileDemoRequest request) {
        // ========== 步骤 1: 提取并校验请求参数 ==========
        // 空值保护：防止请求对象为 null 导致 NPE
        int threadCount = request == null ? 0 : request.getThreadCount();
        int incrementPerThread = request == null ? 0 : request.getIncrementPerThread();

        // 严格参数校验：threadCount 和 incrementPerThread 必须为正整数
        if (threadCount <= 0 || incrementPerThread <= 0) {
            return buildFail("PARAM_VALIDATION", "threadCount 和 incrementPerThread 必须大于 0");
        }

        // ========== 步骤 2: 执行可见性演示 ==========
        // 验证 volatile 标志位的可见性：工作线程应能及时感知主线程对 flag 的修改
        boolean visibilityWorked = runVisibilityDemo();
        
        // ========== 步骤 3: 执行原子性演示 ==========
        // 验证 volatile 对 count++ 复合操作的保护不足：实际值通常小于理论值
        CounterResult counterResult = runAtomicityDemo(threadCount, incrementPerThread);

        // ========== 步骤 4: 构建响应对象 ==========
        VolatileDemoResponse response = new VolatileDemoResponse();
        response.setSuccess(true);
        response.setStage("VOLATILE_DEMO_DONE");
        // 核心结论：volatile 能保证标志位的可见性，但对 count++ 不提供原子性保障
        response.setMessage("演示完成：volatile 对标志位可见，但对 count++ 不提供原子性保障");
        response.setVisibilityWorked(visibilityWorked);       // 可见性是否生效
        response.setExpectedCount(counterResult.expected);    // 理论应有的最终值
        response.setActualCount(counterResult.actual);        // 实际得到的最终值
        // 如果实际值小于理论值，说明发生了更新丢失，证明 volatile 不保证原子性
        response.setAtomicityLost(counterResult.actual < counterResult.expected);
        return response;
    }

    /**
     * 【可见性演示】验证 volatile 保证线程间可见性。
     * <p>
     * 【实验设计】
     * 1. 创建 volatile 标志位 running，初始值为 true
     * 2. 启动工作线程，执行 while(running) 自旋等待
     * 3. 主线程休眠 50ms 后，将 running 设置为 false
     * 4. 观察工作线程是否能及时感知到 running 的变化并退出循环
     * <p>
     * 【关键原理】
     * - 如果没有 volatile，工作线程可能将 running 缓存到 CPU 缓存或寄存器中
     *   即使主线程修改了主内存中的 running 值，工作线程也无法感知
     * - 使用 volatile 后，每次读取都会从主内存重新加载，确保可见性
     * <p>
     * 【注意事项】
     * - 使用 busy spin（自旋）而非 LockSupport.park()，是为了避免阻塞掩盖可见性问题
     * - 主线程等待工作线程退出的超时时间设为 500ms，足够工作线程响应变化
     * 
     * @return true 表示工作线程及时退出（可见性生效），false 表示工作线程仍在运行（可见性失效）
     */
    private boolean runVisibilityDemo() {
        // 创建可见性标志位对象，内部包含 volatile boolean running 字段
        final VisibilityFlag flag = new VisibilityFlag();
        
        // 创建工作线程，执行自旋等待逻辑
        Thread worker = new Thread(() -> {
            // 【关键点】busy spin 自旋：持续检查 running 标志
            // 如果 running 不是 volatile，此循环可能永远无法退出（因为线程可能缓存了旧值）
            // volatile 保证每次读取都从主内存获取最新值，因此能及时感知变化
            while (flag.running) {
                // busy spin - 空循环消耗 CPU，仅用于测试可见性
            }
        }, "volatile-visibility-worker");
        worker.start();

        // ========== 主线程休眠 50ms ==========
        // 目的：确保工作线程已经进入 while 循环并开始自旋
        // 这样主线程修改 running=false 时，工作线程正在检查条件
        try {
            Thread.sleep(50L);
        } catch (InterruptedException ex) {
            // 恢复中断状态：良好的中断处理实践
            Thread.currentThread().interrupt();
        }

        // ========== 修改标志位 ==========
        // 关键动作：将 running 设置为 false
        // volatile 保证这个写操作立即刷新到主内存，工作线程下次读取时会看到新值
        flag.running = false;
        
        // ========== 等待工作线程退出 ==========
        // 使用 join(timeout) 方式等待，避免无限期阻塞
        // 超时时间 500ms：足够工作线程感知变化并退出循环
        try {
            worker.join(500L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        // 检查工作线程是否仍然存活
        // !worker.isAlive() 为 true 表示工作线程已退出，可见性生效
        // !worker.isAlive() 为 false 表示工作线程仍在运行，可见性失效
        return !worker.isAlive();
    }

    /**
     * 【原子性演示】验证 volatile 不保证复合操作的原子性。
     * <p>
     * 【实验设计】
     * 1. 创建 volatile 计数器 counter.count，初始值为 0
     * 2. 启动多个线程，每个线程执行 incrementPerThread 次 count++ 操作
     * 3. 等待所有线程完成后，统计最终值
     * 4. 比较理论值（threadCount * incrementPerThread）与实际值
     * <p>
     * 【问题根源：count++ 的非原子性】
     * count++ 操作实际上包含三个步骤：
     *   ① 读取：从内存读取 count 的值到寄存器
     *   ② 修改：在寄存器中对值加 1
     *   ③ 写入：将新值写回内存
     * <p>
     * 当多个线程并发执行时，可能发生以下情况：
     *   线程 A 读取 count=100
     *   线程 B 也读取 count=100（此时 A 还未写入）
     *   线程 A 写入 count=101
     *   线程 B 也写入 count=101（覆盖了 A 的更新）
     * 结果：两次递增操作只增加了 1，丢失了一次更新
     * <p>
     * 【volatile 的局限性】
     * - volatile 只能保证单次读或写的原子性
     * - 无法保证"读 - 改 - 写"整个序列的原子性
     * - 解决方案：使用 AtomicInteger（CAS 乐观锁）或 synchronized（悲观锁）
     * 
     * @param threadCount 并发线程数
     * @param incrementPerThread 每个线程的递增次数
     * @return 包含理论值和实际值的結果对象
     */
    private CounterResult runAtomicityDemo(int threadCount, int incrementPerThread) {
        // 创建 volatile 计数器，初始值为 0
        final VolatileCounter counter = new VolatileCounter();
        
        // 创建 CountDownLatch 用于等待所有线程完成
        // latch 初始值为 threadCount，每有一个线程完成就减 1，直到为 0
        final CountDownLatch latch = new CountDownLatch(threadCount);

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                // 每个线程执行 incrementPerThread 次 count++ 操作
                for (int j = 0; j < incrementPerThread; j++) {
                    // 【关键问题】count++ 是非原子复合操作
                    // 即使 count 是 volatile，也无法避免并发下的更新丢失
                    // volatile 只保证写入立即可见，但不保证"读 - 改 - 写"序列的原子性
                    counter.count++; 
                }
                // 当前线程完成所有递增操作，latch 计数减 1
                latch.countDown();
            }, "volatile-counter-" + i);
            t.start();
        }

        // ========== 等待所有线程完成 ==========
        // latch.await() 会阻塞当前线程，直到 latch.count 变为 0
        // 即所有线程都调用了 countDown()
        try {
            latch.await();
        } catch (InterruptedException ex) {
            // 如果被中断，恢复中断状态
            Thread.currentThread().interrupt();
        }

        // ========== 构建结果对象 ==========
        CounterResult result = new CounterResult();
        // 理论值：线程数 × 每线程递增次数（假设没有并发冲突）
        result.expected = threadCount * incrementPerThread;
        // 实际值：volatile 计数器的最终值（通常会小于理论值）
        result.actual = counter.count;
        return result;
    }

    /**
     * 构建失败响应的辅助方法。
     * 
     * @param stage 错误阶段标识
     * @param msg 错误描述信息
     * @return 失败响应对象
     */
    private VolatileDemoResponse buildFail(String stage, String msg) {
        VolatileDemoResponse response = new VolatileDemoResponse();
        response.setSuccess(false);      // 标记为失败
        response.setStage(stage);        // 设置错误阶段
        response.setMessage(msg);        // 设置错误信息
        return response;
    }

    /**
     * 【可见性标志位实体类】
     * 
     * 【设计意图】
     * 封装 volatile boolean 字段，用于演示可见性场景。
     * 
     * 【为什么这里适合用 volatile】
     * - 这是一个典型的"状态通知"场景：一个线程写（设置 false），其他线程读（检查 running）
     * - 只需要保证单次读写的可见性，不涉及复合操作
     * - 写入操作只发生一次（从 true 到 false），属于"写少读多"的场景
     * 
     * 【对比：如果不使用 volatile】
     * 工作线程可能将 running 标志缓存到 CPU 缓存或寄存器中
     * 即使主线程修改了 running=false，工作线程的 while 循环可能永远看不到新值
     * 导致工作线程无法退出，形成"伪死循环"
     */
    private static class VisibilityFlag {
        /**
         * volatile 标志位：控制工作线程的运行状态。
         * true = 继续运行，false = 停止运行
         */
        private volatile boolean running = true;
    }

    /**
     * 【volatile 计数器实体类】
     * 
     * 【设计意图】
     * 封装 volatile int 字段，用于演示原子性缺失问题。
     * 
     * 【为什么这里不适合用 volatile】
     * - count++ 是复合操作（读 - 改 - 写），volatile 无法保证其原子性
     * - 多线程并发时会出现更新丢失
     * - 正确的做法是使用 AtomicInteger 或 synchronized
     * 
     * 【volatile 的误区】
     * 很多初学者误以为 volatile 可以保证线程安全，这是错误的认知
     * volatile 只能保证可见性和部分有序性，不能保证原子性
     * 对于计数器、累加器等场景，必须使用原子类或锁
     */
    private static class VolatileCounter {
        /**
         * volatile 计数器：用于演示原子性问题。
         * 注意：虽然使用了 volatile，但 count++ 操作仍然不是线程安全的！
         */
        private volatile int count = 0;
    }

    /**
     * 【结果数据类】封装原子性演示的统计结果。
     * <p>
     * 【字段说明】
     * - expected: 理论上的最终值（假设没有并发冲突）
     * - actual: 实际的最终值（通常会小于理论值）
     * <p>
     * 【判断标准】
     * 如果 actual < expected，说明发生了更新丢失，证明 volatile 不保证原子性
     * 差值越大，说明并发冲突越严重
     */
    private static class CounterResult {
        private int expected;   // 理论值 = threadCount × incrementPerThread
        private int actual;     // 实际值 = volatile count 的最终值（通常 < expected）
    }
}
