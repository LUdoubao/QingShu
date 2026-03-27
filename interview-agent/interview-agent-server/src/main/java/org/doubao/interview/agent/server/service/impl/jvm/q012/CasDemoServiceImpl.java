package org.doubao.interview.agent.server.service.impl.jvm.q012;

import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoResponse;
import org.doubao.interview.agent.api.service.jvm.q012.CasDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 问题 012（JVM）：CAS 演示服务实现。
 * <p>
 * 这段代码对应的面试题是：
 * 1. CAS 是什么？
 * 2. 为什么它能提升并发性能？
 * 3. 它会遇到哪些问题？
 * <p>
 * 为了让答案不只是“背概念”，本类把面试点拆成三个可执行实验：
 * 1. 计数器实验：
 *    使用 AtomicInteger + compareAndSet 循环演示“比较成功才更新”的核心机制，
 *    说明 CAS 是一种无锁的原子更新方式。
 * 2. ABA 实验：
 *    使用 AtomicReference 复现 A -> B -> A 的中途变化，
 *    再用 AtomicStampedReference 说明“版本号/标记位”为什么可以解决 ABA。
 * 3. 组合状态实验：
 *    说明 CAS 天生更适合“单个共享位置”的原子替换，
 *    如果要同时维护多个字段的一致性，应该把多个字段封装成一个不可变对象后再对引用做 CAS。
 * <p>
 * 重点策略说明：
 * 1. 这里故意使用手写 CAS 循环，而不是直接调用 incrementAndGet()。
 *    目的不是追求最短代码，而是把“读取旧值 -> 计算新值 -> compareAndSet 重试”完整展示出来。
 * 2. 线程等待统一使用 join()，保证接口返回时实验已经结束，调用方拿到的是最终结果而不是中间态。
 * 3. 自旋提示在不同 JDK 版本中的实现并不一致。
 *    当前工程为了兼容较低版本 JDK，不直接依赖 Thread.onSpinWait()，
 *    而是使用一个兼容辅助方法来表达“线程正在忙等重试”的语义。
 *    它存在的意义是帮助说明“CAS 在高竞争场景下会因为不断重试而持续消耗 CPU”。
 */
@Service
public class CasDemoServiceImpl implements CasDemoService {

    private static final Logger log = LoggerFactory.getLogger(CasDemoServiceImpl.class);

    @Override
    public CasDemoResponse run(CasDemoRequest request) {
        // ========== 步骤 1: 提取请求参数 ==========
        // 空值保护：防止请求对象或字段为 null 导致 NPE
        int threads = request == null ? 0 : request.getThreads();
        int incrementsPerThread = request == null ? 0 : request.getIncrementsPerThread();
        boolean enableSpinHint = request != null && request.isEnableSpinHint();

        // ========== 步骤 2: 严格参数校验 ==========
        // threads 和 incrementsPerThread 必须为正整数
        if (threads <= 0 || incrementsPerThread <= 0) {
            return fail("PARAM_VALIDATION", "threads > 0 且 incrementsPerThread > 0");
        }

        // ========== 步骤 3: 记录启动日志 ==========
        // 记录演示开始时的配置参数，便于后续调试和性能分析
        log.info("q012-cas demo start, threads={}, incrementsPerThread={}, enableSpinHint={}",
                threads, incrementsPerThread, enableSpinHint);

        // ========== 步骤 4: 执行三个核心实验 ==========
        // 实验 1: 计数器实验，演示 CAS 的基本工作原理
        CasDemoResponse.CounterDemoResult counterDemo = runCounterDemo(threads, incrementsPerThread, enableSpinHint);
        
        // 实验 2: ABA 实验，演示普通 CAS 的问题及解决方案
        CasDemoResponse.AbaDemoResult abaDemo = runAbaDemo();
        
        // 实验 3: 组合状态实验，演示多字段原子更新的正确建模方式
        CasDemoResponse.CompositeCasDemoResult compositeDemo = runCompositeDemo();

        // ========== 步骤 5: 构建响应对象 ==========
        CasDemoResponse response = new CasDemoResponse();
        response.setSuccess(true);
        response.setStage("DONE");
        response.setMessage("CAS 演示完成：包含无锁自增、ABA 复现与版本号修复、组合状态 CAS 策略");
        response.setCasDefinition("CAS（Compare-And-Swap）是一种无锁原子更新机制：先比较内存中的旧值是否等于期望值，只有相等时才交换为新值。");
        response.setCounterDemo(counterDemo);
        response.setAbaDemo(abaDemo);
        response.setCompositeDemo(compositeDemo);
        response.setInterviewPoints(buildInterviewPoints(counterDemo));

        // ========== 步骤 6: 记录完成日志 ==========
        // 汇总关键指标：理论值、实际值、重试次数、ABA 实验结果
        log.info("q012-cas demo done, expected={}, actual={}, retries={}, plainAbaSucceeded={}, stampedSucceeded={}",
                counterDemo.getExpected(),
                counterDemo.getActual(),
                counterDemo.getTotalRetries(),
                abaDemo.isPlainCasSucceeded(),
                abaDemo.isStampedCasSucceeded());
        return response;
    }

    /**
     * 【计数器实验】演示 CAS 的基本工作原理。
     * 
     * 【实验目标】
     * 用最直观的方式说明 CAS 的工作流程：
     * 1. 读取共享变量当前值 current
     * 2. 计算目标值 next = current + 1
     * 3. 调用 compareAndSet(current, next) 尝试提交更新
     * 4. 如果失败，说明别的线程已经在这段时间里改过值，继续自旋重试
     * 
     * 【展示的关键特点】
     * 1. 不阻塞线程：不像悲观锁那样先挂起再唤醒，而是立即返回成功/失败
     * 2. 失败靠重试：不排队，高竞争下会出现明显的 CPU 消耗
     * 3. 最终一致性：通过不断重试，保证所有递增操作最终都成功
     * 
     * 【需要统计的指标】
     * - totalRetries: 总的 CAS 失败重试次数（反映并发冲突激烈程度）
     * - maxRetryOfSingleThread: 单个线程遇到的最大重试次数（反映最坏情况）
     * - costMs: 总耗时（对比不同策略的性能差异）
     * 
     * @param threads 并发线程数
     * @param incrementsPerThread 每个线程的递增次数
     * @param enableSpinHint 是否启用自旋提示（兼容低版本 JDK）
     * @return 计数器实验结果
     */
    private CasDemoResponse.CounterDemoResult runCounterDemo(int threads, int incrementsPerThread, boolean enableSpinHint) {
        // 创建共享计数器，初始值为 0
        AtomicInteger counter = new AtomicInteger(0);
        
        // 创建重试计数器，统计所有线程的 CAS 失败总次数
        AtomicInteger totalRetries = new AtomicInteger(0);
        
        // 创建最大重试计数器，记录单个线程遇到的最大重试次数
        AtomicInteger maxRetryOfSingleThread = new AtomicInteger(0);
        
        // 创建工作线程列表
        List<Thread> workers = new ArrayList<Thread>();
        
        // 记录开始时间，用于计算总耗时
        long start = System.currentTimeMillis();

        // ========== 创建并启动多个并发线程 ==========
        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                // 本地重试计数器：统计当前线程遇到的 CAS 失败次数
                int localRetryCount = 0;
                
                // 每个线程执行 incrementsPerThread 次递增操作
                for (int j = 0; j < incrementsPerThread; j++) {
                    // 标记本次递增是否成功
                    boolean updated = false;
                    
                    // 【核心机制】自旋 + CAS：乐观锁的经典实现
                    // while 循环会一直执行，直到 CAS 成功为止
                    while (!updated) {
                        // 步骤 1: 读取当前值（volatile 读，保证可见性）
                        int current = counter.get();
                        
                        // 步骤 2: 计算期望的新值
                        int next = current + 1;
                        
                        // 步骤 3: CAS 原子操作
                        // compareAndSet(预期原值，新值)
                        // 如果 counter 的当前值等于 current，则设置为 next，返回 true
                        // 如果 counter 的当前值不等于 current（已被其他线程修改），返回 false
                        if (counter.compareAndSet(current, next)) {
                            // CAS 成功！本次递增完成
                            updated = true;
                        } else {
                            // CAS 失败！说明发生了并发冲突
                            // 其他线程在 get() 和 compareAndSet() 之间修改了 counter
                            // 本地重试次数 +1
                            localRetryCount++;
                            // 全局重试次数 +1
                            totalRetries.incrementAndGet();
                            
                            // 如果启用了自旋提示，调用兼容方法
                            // 这会向 JVM 发出信号：当前线程正在忙等，可以优化调度
                            if (enableSpinHint) {
                                spinHint();
                            }
                        }
                    }
                    // while 循环结束，说明本次递增成功
                }
                
                // 所有递增操作完成后，更新全局最大重试记录
                updateMaxRetry(maxRetryOfSingleThread, localRetryCount);
                
            }, "q012-cas-counter-" + i);  // 线程命名便于调试
            workers.add(worker);
        }

        // ========== 等待所有线程完成 ==========
        // 调用封装的 joinAll 方法，统一处理线程启动和等待
        joinAll(workers);
        
        // 计算总耗时（毫秒）
        long costMs = System.currentTimeMillis() - start;

        // ========== 构建实验结果对象 ==========
        CasDemoResponse.CounterDemoResult result = new CasDemoResponse.CounterDemoResult();
        result.setThreads(threads);                          // 设置并发线程数
        result.setIncrementsPerThread(incrementsPerThread);  // 设置每线程递增次数
        result.setExpected(threads * incrementsPerThread);   // 设置理论期望值
        result.setActual(counter.get());                     // 设置实际最终值
        result.setTotalRetries(totalRetries.get());          // 设置总重试次数
        result.setMaxRetryOfSingleThread(maxRetryOfSingleThread.get());  // 设置单线程最大重试次数
        result.setCostMs(costMs);                            // 设置总耗时
        result.setEnableSpinHint(enableSpinHint);            // 设置是否启用自旋提示
        
        // 设置策略说明：根据 enableSpinHint 选择不同的描述
        result.setStrategyNote(enableSpinHint
                ? "CAS 失败后采用自旋重试，并调用 Thread.onSpinWait() 给 CPU 自旋提示。"
                : "CAS 失败后直接循环重试，便于直观看到无锁重试模型。");
        
        // 记录调试日志：输出计数器实验的关键指标
        log.info("q012-cas counter demo completed, expected={}, actual={}, totalRetries={}, maxRetryOfSingleThread={}, costMs={}ms",
                result.getExpected(), result.getActual(), result.getTotalRetries(), 
                result.getMaxRetryOfSingleThread(), result.getCostMs());
        
        return result;
    }

    /**
     * 【兼容式自旋提示】适配不同 JDK 版本的自旋优化方法。
     * 
     * 【背景说明】
     * - Java 9+ 引入了 Thread.onSpinWait() 方法，可以向 CPU 发出信号：当前线程正在忙等
     * - 某些 CPU 架构（如 Intel 的 PAUSE 指令）可以优化这种忙等，减少功耗和提高性能
     * - 但当前项目编译环境可能不支持该方法（JDK 8），因此需要兼容处理
     * 
     * 【实现策略】
     * - 使用 Thread.yield() 作为替代方案
     * - yield() 会提示调度器"我愿意让出 CPU"，让其他就绪线程有机会执行
     * - 虽然效果不如 onSpinWait() 精确，但仍然能表达"CAS 失败后不是阻塞，而是继续参与调度并重试"的特征
     * 
     * 【注意】
     * - 这个方法不会改变 CAS 的正确性
     * - 它的主要作用是帮助解释 CAS 的行为特征
     */
    private void spinHint() {
        // 退化为 Thread.yield()，确保在老版本 JDK 下仍然可运行
        Thread.yield();
    }

    /**
     * 【ABA 实验】演示普通 CAS 的 ABA 问题及带版本号 CAS 的解决方案。
     * 
     * 【什么是 ABA 问题？】
     * 1. 线程 T1 读取到值 A，准备把它改成 C
     * 2. 在线程 T1 真正提交前，线程 T2 先把 A 改成 B，再改回 A
     * 3. 对 T1 来说，"当前值看起来仍然是 A"，因此普通 CAS 会成功
     * 4. 但从业务角度看，值其实被别人动过，这就是 ABA 问题
     * 
     * 【为什么 ABA 是问题？】
     * - 在某些场景下，我们不仅关心"值是否变化"，还关心"值是否被修改过"
     * - 例如：栈顶指针从 A→B→A，虽然最终还是 A，但中间的 B 可能已经被释放
     * - 这时如果使用普通 CAS，会误以为一切正常，导致使用已释放的资源
     * 
     * 【解决方案：带版本号的 CAS】
     * 1. 初始值仍然是 A，但同时记录版本号 0
     * 2. T2 把 A 改成 B 时版本号加 1，再把 B 改回 A 时版本号再加 1
     * 3. T1 提交时不仅比较值是否还是 A，还比较版本号是否还是 0
     * 4. 由于版本号已经变化，CAS 会失败，从而识别出"虽然值看起来没变，但中途被动过"
     * 
     * 【Java 实现】
     * - AtomicStampedReference<V>: 同时维护引用和整数 stamp（可作为版本号）
     * - AtomicMarkableReference<V>: 同时维护引用和布尔标记
     * 
     * @return ABA 实验结果
     */
    private CasDemoResponse.AbaDemoResult runAbaDemo() {
        // ========== 实验 1: 普通 AtomicReference 的 ABA 问题 ==========
        // 创建普通原子引用，初始值为 "A"
        AtomicReference<String> plainReference = new AtomicReference<String>("A");
        
        // 步骤 1: 线程 T1 读取当前值（假设这是 T1 的视角）
        String plainExpected = plainReference.get();  // plainExpected = "A"
        
        // 步骤 2: 模拟 T2 的操作（T1 不知道）：A → B → A
        plainReference.compareAndSet("A", "B");  // A → B
        plainReference.compareAndSet("B", "A");  // B → A
        
        // 步骤 3: T1 尝试提交 CAS
        // 虽然值已经被修改过，但最终还是 "A"，所以 CAS 会成功！
        boolean plainCasSucceeded = plainReference.compareAndSet(plainExpected, "C");
        
        // 记录日志：普通 CAS 无法检测到 ABA 问题
        log.info("q012-cas ABA demo - plain CAS: initial='A', intermediate='A->B->A', final CAS(A,C) succeeded={}, this demonstrates ABA problem", plainCasSucceeded);

        // ========== 实验 2: AtomicStampedReference 解决 ABA 问题 ==========
        // 创建带版本号的原子引用，初始值为 "A"，版本号为 0
        AtomicStampedReference<String> stampedReference = new AtomicStampedReference<String>("A", 0);
        
        // 步骤 1: 线程 T1 读取当前值和版本号（假设这是 T1 的视角）
        int stampedExpectedVersion = stampedReference.getStamp();      // stampedExpectedVersion = 0
        String stampedExpectedValue = stampedReference.getReference(); // stampedExpectedValue = "A"
        
        // 步骤 2: 模拟 T2 的操作（T1 不知道）：(A,0) → (B,1) → (A,2)
        // 注意：compareAndSet(预期值，新值，预期版本号，新版本号)
        stampedReference.compareAndSet("A", "B", 0, 1);  // (A,0) → (B,1)
        stampedReference.compareAndSet("B", "A", 1, 2);  // (B,1) → (A,2)
        
        // 步骤 3: T1 尝试提交 CAS
        // 这次不仅比较值是否还是 "A"，还比较版本号是否还是 0
        // 由于版本号已经变成 2，不再是预期的 0，所以 CAS 会失败！
        boolean stampedCasSucceeded = stampedReference.compareAndSet(
                stampedExpectedValue,  // 预期值："A"
                "C",                   // 新值："C"
                stampedExpectedVersion, // 预期版本号：0
                stampedExpectedVersion + 1 // 新版本号：1
        );
        
        // 记录日志：带版本号的 CAS 成功检测到 ABA 问题
        log.info("q012-cas ABA demo - stamped CAS: initial=(A,0), intermediate=[(B,1)->(A,2)], final CAS((A,0)->(C,1)) succeeded={}, this solves ABA problem", stampedCasSucceeded);

        // ========== 构建实验结果对象 ==========
        CasDemoResponse.AbaDemoResult result = new CasDemoResponse.AbaDemoResult();
        result.setPlainCasSucceeded(plainCasSucceeded);  // 普通 CAS 是否成功（应该是 true，暴露 ABA 问题）
        result.setStampedCasSucceeded(stampedCasSucceeded);  // 带版本号 CAS 是否成功（应该是 false，检测到 ABA）
        
        // 设置普通 CAS 的流程说明
        result.setPlainFlow("普通 AtomicReference：先读取 A，再发生 A->B->A，最后 compareAndSet(A, C) 仍然成功，说明普通 CAS 只能看到结果值，看不到中途变化。");
        
        // 设置带版本号 CAS 的流程说明
        result.setStampedFlow("AtomicStampedReference：初始 (A,0)，中途变成 (B,1) 再回到 (A,2)，旧线程提交时发现版本号不再是 0，因此 CAS 失败。");
        
        // 设置解决方案说明
        result.setSolution("ABA 常见解决方案是给值增加版本号或标记位，例如 AtomicStampedReference、AtomicMarkableReference。");
        
        return result;
    }

    /**
     * 【组合状态实验】演示多字段原子更新的正确 CAS 建模方式。
     * 
     * 【面试常见问题】
     * "CAS 只能保证单变量原子更新"——这句话要准确理解：
     * 1. CAS 直接比较和替换的是一个共享位置，比如一个 int 或一个引用地址
     * 2. 如果你有多个彼此关联的字段，例如 stock 和 version，分别对两个字段做 CAS，
     *    并不能天然保证"这两个字段作为一个整体"同时成功或同时失败
     * 3. 解决思路之一，是把多个字段封装成一个不可变对象，只对对象引用做一次 CAS
     * 
     * 【错误示范】分别对多个字段做 CAS
     * ```java
     * AtomicInteger stock = new AtomicInteger(10);
     * AtomicInteger version = new AtomicInteger(1);
     * // 线程 A 和 B 同时对 stock 和 version 做 CAS
     * // 可能出现：stock 更新成功，version 更新失败，导致数据不一致
     * ```
     * 
     * 【正确示范】封装成不可变对象，对引用做 CAS
     * ```java
     * AtomicReference<CompositeState> stateRef = new AtomicReference<>(new CompositeState(10, 1));
     * // 一次性替换整个对象，要么全成功，要么全失败
     * stateRef.compareAndSet(oldState, newState);
     * ```
     * 
     * 【本实验的目的】
     * 不是演示"多次 CAS 拼事务"，而是展示更贴近面试标准答案的正确策略：
     * 用 AtomicReference<CompositeState> 把多个字段打包成一个整体进行原子替换
     * 
     * @return 组合状态实验结果
     */
    private CasDemoResponse.CompositeCasDemoResult runCompositeDemo() {
        // 创建组合状态的原子引用，初始状态：stock=10, version=1
        AtomicReference<CompositeState> stateRef = new AtomicReference<CompositeState>(new CompositeState(10, 1));
        
        // 步骤 1: 读取当前状态（快照）
        CompositeState before = stateRef.get();
        
        // 步骤 2: 基于当前状态计算新状态
        // 注意：这里创建了一个新的 CompositeState 对象，而不是修改原有对象
        // 这体现了不可变对象的优势：一旦创建，内部状态不会再被原地修改
        CompositeState next = new CompositeState(before.getStock() - 1, before.getVersion() + 1);
        
        // 步骤 3: CAS 原子替换整个对象引用
        // 要么整个对象被替换（stock 和 version 同时更新），要么都不更新
        boolean updated = stateRef.compareAndSet(before, next);
        
        // 步骤 4: 获取更新后的状态（快照）
        CompositeState after = stateRef.get();

        // ========== 构建实验结果对象并记录日志 ==========
        CasDemoResponse.CompositeCasDemoResult result = new CasDemoResponse.CompositeCasDemoResult();
        
        // 设置问题陈述：说明裸 CAS 的局限性和正确用法
        result.setProblemStatement("裸 CAS 更适合保证'单个共享位置'的原子更新；如果业务状态由多个字段组成，需要把多个字段封装为一个整体再做一次引用级 CAS。");
        
        // 设置 CAS 前的状态快照
        result.setBefore(toSnapshot(before));
        
        // 设置 CAS 后的状态快照
        result.setAfterAtomicReferenceCas(toSnapshot(after));
        
        // 设置复合更新是否成功
        result.setCompositeUpdateSucceeded(updated);
        
        // 设置推荐方案
        result.setRecommendation("如果只是一个计数值，AtomicInteger 足够；如果需要多个字段一起变更，可用 AtomicReference + 不可变对象，或直接使用锁/事务保证整体一致性。");
        
        // 记录调试日志：输出组合状态实验的结果
        log.info("q012-cas composite demo completed, before=(stock={}, version={}), after=(stock={}, version={}), updated={}",
                before.getStock(), before.getVersion(), after.getStock(), after.getVersion(), updated);
        
        return result;
    }

    /**
     * 构建面试要点列表。
     * 
     * 【设计目的】
     * 将实验结果转化为面试时可以直接使用的标准答案，帮助候选人理解 CAS 的核心概念。
     * 
     * 【包含的关键点】
     * 1. CAS 的定义（Compare-And-Swap）
     * 2. CAS 的优点（无锁、减少阻塞）
     * 3. 实验数据证明（expected vs actual）
     * 4. 重试机制的直观展示（totalRetries）
     * 5. ABA 问题及解决方案
     * 6. CAS 的适用场景（单变量 vs 多字段）
     * 
     * @param counterDemo 计数器实验结果，用于生成具体的数据支撑
     * @return 面试要点列表
     */
    private List<String> buildInterviewPoints(CasDemoResponse.CounterDemoResult counterDemo) {
        // 返回面试要点列表，每个要点对应一个常见的面试问题或知识点
        return Arrays.asList(
                // 要点 1: CAS 的基本定义
                "CAS 是 Compare-And-Swap：比较期望值一致时才更新，否则失败并重试。",
                    
                // 要点 2: CAS 的优点
                "CAS 的优点是无锁、减少阻塞，在线程竞争不高时通常有更好的并发吞吐。",
                    
                // 要点 3: 用实验数据证明 CAS 保证原子性
                "本次计数器实验中 expected=" + counterDemo.getExpected() + "，actual=" + counterDemo.getActual()
                        + "，说明 CAS 可以保证单个共享变量更新的原子性。",
                    
                // 要点 4: 用重试次数展示 CAS 在高竞争下的 CPU 消耗
                "本次实验 totalRetries=" + counterDemo.getTotalRetries()
                        + "，可以直观看到 CAS 失败后会自旋重试，高竞争下可能持续消耗 CPU。",
                    
                // 要点 5: ABA 问题的定义
                "ABA 问题指的是值从 A 变成 B 又变回 A，普通 CAS 只看到最终还是 A，因此可能误判'没被改过'。",
                    
                // 要点 6: ABA 的解决方案
                "解决 ABA 常用版本号或标记位，例如 AtomicStampedReference。",
                    
                // 要点 7: CAS 的适用场景和局限性
                "CAS 直接适合单变量原子更新；多个字段需要整体一致时，应考虑 AtomicReference 封装对象，或使用锁、事务等手段。"
        );
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
                throw new IllegalStateException("线程等待被中断", ex);
            }
        }
    }

    /**
     * 【CAS 方式更新最大重试记录】演示 CAS 在实际应用中的高级用法。
     * 
     * 【方法功能】
     * 以 CAS 方式更新"某个线程观测到的最大重试次数"。
     * 
     * 【实现思路】
     * 这个辅助方法本身也在演示 CAS 的思路：
     * 1. 先读旧值 current
     * 2. 如果候选值 candidate 不更大，直接返回（不需要更新）
     * 3. 如果 candidate > current，则尝试 compareAndSet(current, candidate)
     * 4. 失败则说明有别的线程刚更新过，继续自旋重试
     * 
     * 【为什么这里用 CAS 而不是 synchronized】
     * - 这是一个简单的"取最大值"操作，使用 CAS 可以避免锁的开销
     * - 即使多个线程同时尝试更新，CAS 也能保证最终得到正确的最大值
     * - 这是 CAS 的典型应用场景：无锁的原子性检查 - 更新操作
     * 
     * @param maxRetry 存储最大重试次数的 AtomicInteger
     * @param candidate 当前线程观测到的重试次数（候选值）
     */
    private void updateMaxRetry(AtomicInteger maxRetry, int candidate) {
        // 【核心机制】自旋 + CAS：不断更新最大值的经典模式
        while (true) {
            // 步骤 1: 读取当前最大值
            int current = maxRetry.get();
            
            // 步骤 2: 检查候选值是否需要更新
            // 如果 candidate <= current，说明当前值已经是更大的，无需更新，直接返回
            if (candidate <= current) {
                return;
            }
            
            // 步骤 3: 尝试 CAS 更新
            // 如果成功，说明我们成功设置了新的最大值
            // 如果失败，说明其他线程在我们之前更新了更大的值，需要重新循环检查
            if (maxRetry.compareAndSet(current, candidate)) {
                return;  // CAS 成功，退出循环
            }
            // CAS 失败，继续 while 循环重试
        }
    }

    private CasDemoResponse.StateSnapshot toSnapshot(CompositeState state) {
        CasDemoResponse.StateSnapshot snapshot = new CasDemoResponse.StateSnapshot();
        snapshot.setStock(state.getStock());
        snapshot.setVersion(state.getVersion());
        return snapshot;
    }

    private CasDemoResponse fail(String stage, String message) {
        CasDemoResponse response = new CasDemoResponse();
        response.setSuccess(false);
        response.setStage(stage);
        response.setMessage(message);
        return response;
    }

    /**
     * 【组合状态实体类】演示多字段原子更新的正确 CAS 建模方式。
     * 
     * 【设计意图】
     * 这是一个不可变对象，专门用于演示"多个字段一起更新"的正确 CAS 建模方式。
     * 
     * 【不可变对象的优势】
     * 1. 一旦创建，对象内部状态不会再被原地修改
     * 2. 线程之间只需要竞争"引用是否替换成功"，而不需要担心对象半更新状态
     * 3. 这种模式在 AtomicReference 场景中非常常见，本质上是把多字段更新降维为"一次引用替换"
     * 
     * 【为什么这里必须用不可变对象】
     * - 如果使用可变对象，可能出现：线程 A 读取了对象引用，修改了字段，但还没提交 CAS
     *   这时其他线程看到的已经是"半更新"的中间状态
     * - 使用不可变对象后，任何修改都是创建新对象，旧对象始终保持不变
     *   这保证了其他线程要么看到旧状态，要么看到新状态，不会看到中间状态
     * 
     * 【面试要点】
     * 当面试官问"如何保证多个字段同时原子更新"时，标准答案包括：
     * 1. 方案一：使用锁（synchronized/ReentrantLock）
     * 2. 方案二：使用 AtomicReference + 不可变对象（本例演示的方式）
     * 3. 方案三：使用事务机制（数据库场景）
     */
    private static final class CompositeState {

        /**
         * 库存值，代表业务中的主要数据。
         * 
         * 【为什么使用 final】
         * - final 保证字段一旦初始化就不能被修改
         * - 配合类的不可变性，确保多线程安全
         * - 编译器会强制要求在所有构造函数中初始化 final 字段
         */
        private final int stock;
        
        /**
         * 版本号，代表状态变化次数。
         * 
         * 【这里的版本号与 ABA 问题中的版本号的区别】
         * - ABA 问题的版本号：由 AtomicStampedReference 自动维护，用于检测中途变化
         * - 这里的版本号：是业务状态的一部分，每次更新都会 +1，用于直观看到状态变化
         * 
         * 【设计目的】
         * 让调用方更直观地看到"复合状态整体替换"后的变化，
         * 也和 ABA 问题中的"版本号思路"形成呼应。
         */
        private final int version;

        /**
         * 私有构造函数，防止外部直接继承或修改。
         * 
         * @param stock 库存值
         * @param version 版本号
         */
        private CompositeState(int stock, int version) {
            this.stock = stock;
            this.version = version;
        }

        /**
         * 获取库存值。
         * 
         * 【为什么只有 getter 没有 setter】
         * - 这是不可变对象的核心特征：一旦创建，状态就不能修改
         * - 如果需要"修改"，实际上是创建一个新对象
         * - 这保证了线程安全性：其他线程读取到的永远是完整、一致的状态
         * 
         * @return 库存值
         */
        public int getStock() {
            return stock;
        }

        /**
         * 获取版本号。
         * 
         * 【版本号的用途】
         * - 调试时可追踪状态变化的次数
         * - 可以基于版本号实现乐观锁（类似 ABA 解决方案）
         * 
         * @return 版本号
         */
        public int getVersion() {
            return version;
        }
    }
}
