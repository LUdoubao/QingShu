package org.doubao.interview.agent.server.service.impl.jvm.q009;

import org.doubao.interview.agent.api.dto.jvm.q009.ReentrantLockDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q009.ReentrantLockDemoResponse;
import org.doubao.interview.agent.api.service.jvm.q009.ReentrantLockDemoService;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 问题 009(JVM)：可重入锁（Reentrant Lock）原理演示服务类。
 * <p>
 * 【可重入锁的核心概念】
 * <p>
 * 1. 什么是可重入性（Reentrancy）？
 *    - 指同一个线程在持有某把锁的情况下，可以再次获取同一把锁而不会阻塞
 *    - 如果没有可重入性，线程在递归调用或嵌套方法中会自己阻塞自己，导致死锁
 *    - Java 中的 synchronized 和 ReentrantLock 都是可重入锁
 * <p>
 * 2. 可重入锁的内部实现原理：
 *    - 记录当前持有锁的线程引用（owner thread）
 *    - 维护一个重入计数器（hold count / reentrancy count）
 *    - 当线程尝试获取锁时：
 *      ① 如果锁空闲，将 owner 设置为当前线程，holdCount = 1
 *      ② 如果 owner 是当前线程，holdCount++，允许重入
 *      ③ 如果 owner 是其他线程，进入等待队列阻塞
 *    - 当线程释放锁时：
 *      ① holdCount--
 *      ② 如果 holdCount == 0，清空 owner，锁真正释放
 *      ③ 如果 holdCount > 0，锁仍然被当前线程持有
 * <p>
 * 3. 为什么需要可重入性？
 *    - 支持递归调用：如本示例中的递归深度测试
 *    - 支持嵌套方法调用：方法 A 持有锁调用方法 B，方法 B 也需要同样的锁
 *    - 简化编程模型：开发者无需担心在锁内重复获取同一把锁
 * <p>
 * 4. synchronized 与 ReentrantLock 的可重入实现对比：
 * <p>
 *    | 特性             | synchronized                    | ReentrantLock                      |
 *    |------------------|---------------------------------|------------------------------------|
 *    | 重入计数获取     | 无法直接查看（JVM 内部管理）     | getHoldCount() 可查看              |
 *    | 实现方式         | Monitor 对象头 + 字节码指令      | AQS (AbstractQueuedSynchronizer)   |
 *    | 公平性           | 仅非公平                        | 可选公平/非公平                    |
 *    | 可中断性         | 不支持                          | 支持 lockInterruptibly()           |
 *    | 超时机制         | 不支持                          | 支持 tryLock(timeout)              |
 * <p>
 * 【本示例演示内容】
 * 1. synchronized 模式：通过递归调用，演示同一线程重复进入同一 monitor 不会死锁
 * 2. ReentrantLock 模式：通过递归调用 + getHoldCount()，观测重入计数的变化
 * 3. 记录并返回最大递归深度和最大重入计数
 */
@Service
public class ReentrantLockDemoServiceImpl implements ReentrantLockDemoService {

    /**
     * synchronized 专用的监视器锁对象。
     * 
     * 【为什么使用独立的锁对象】
     * - 避免锁定 this 导致的意外干扰：如果其他 synchronized 方法也锁定 this，可能产生不必要的竞争
     * - 明确锁的作用域：syncMonitor 专门用于演示 synchronized 的可重入性
     * - 符合"锁分离"原则：不同的并发控制使用不同的锁对象
     */
    private final Object syncMonitor = new Object();
    
    /**
     * ReentrantLock 实例。
     * 
     * 【默认配置】
     * - 非公平锁：new ReentrantLock() 等价于 new ReentrantLock(false)
     * - 可重入：内部基于 AQS 实现，支持同一线程重复获取
     * 
     * 【为什么不使用公平锁】
     * - 公平锁的性能通常低于非公平锁（需要维护 FIFO 队列）
     * - 本示例的目的是演示可重入性，而非公平性
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * 记录 synchronized 递归达到的最大深度。
     * 每次 run() 调用前会重置为 0。
     */
    private int syncMaxDepth;
    
    /**
     * 记录 ReentrantLock 的最大重入计数（hold count）。
     * 每次 run() 调用前会重置为 0。
     */
    private int lockMaxHoldCount;

    /**
     * 执行可重入锁演示的核心方法。
     * 
     * 【处理流程】
     * 1. 提取并校验请求参数：mode（锁模式）和 depth（递归深度）
     * 2. 根据 mode 选择执行 synchronized 或 ReentrantLock 演示
     * 3. 通过递归调用，演示同一线程重复获取同一把锁不会死锁
     * 4. 记录并返回最大递归深度/重入计数
     * 
     * 【参数说明】
     * - mode: 锁模式，可选值为 "synchronized" 或 "reentrant/reentrantlock"
     * - depth: 递归深度（必须大于 0），表示要递归调用多少层
     * 
     * 【预期结果】
     * - synchronized 模式：
     *   - maxReentrantCount: 等于 depth（实际记录的是递归深度 value）
     *   - finalResult: 等于 depth
     *   - 证明同一线程可以重复进入同一 monitor
     * 
     * - ReentrantLock 模式：
     *   - maxReentrantCount: 等于 depth（getHoldCount 的最大值）
     *   - finalResult: 等于 depth
     *   - 证明 ReentrantLock 内部维护了持有线程和 holdCount
     * 
     * @param request 请求对象，包含 mode 和 depth 参数
     * @return 响应对象，包含演示结果、最大重入计数、最终结果等
     */
    @Override
    public ReentrantLockDemoResponse run(ReentrantLockDemoRequest request) {
        // ========== 步骤 1: 提取并校验请求参数 ==========
        // 空值保护：防止请求对象或字段为 null 导致 NPE
        String mode = request == null ? null : request.getMode();
        int depth = request == null ? 0 : request.getDepth();
        
        // 严格参数校验：mode 不能为空，depth 必须为正整数
        if (mode == null || mode.trim().isEmpty() || depth <= 0) {
            return fail("PARAM_VALIDATION", "mode 不能为空且 depth > 0");
        }

        // ========== 步骤 2: 根据 mode 分发到不同的演示逻辑 ==========
        if ("synchronized".equalsIgnoreCase(mode)) {
            // ========== synchronized 模式 ==========
            // 重置最大深度计数器（每次测试前清零）
            syncMaxDepth = 0;
            // 从深度 1 开始递归，value 初始为 1
            int result = runWithSynchronized(depth, 1);
            // 构建响应：记录递归深度和结果
            return build(mode, depth, syncMaxDepth, result,
                    "synchronized 可重入：同线程重复进入同一 monitor 不会死锁");
        }

        if ("reentrant".equalsIgnoreCase(mode) || "reentrantlock".equalsIgnoreCase(mode)) {
            // ========== ReentrantLock 模式 ==========
            // 重置最大重入计数（每次测试前清零）
            lockMaxHoldCount = 0;
            // 从深度 1 开始递归，value 初始为 1
            int result = runWithReentrantLock(depth, 1);
            // 构建响应：记录最大 holdCount 和结果
            return build(mode, depth, lockMaxHoldCount, result,
                    "ReentrantLock 可重入：内部维护持有线程与 holdCount");
        }

        // mode 参数不匹配任何支持的选项
        return fail("PARAM_VALIDATION", "mode 仅支持 synchronized / reentrant");
    }

    /**
     * 【synchronized 可重入演示】通过递归调用演示 synchronized 的可重入性。
     * 
     * 【实验设计】
     * 1. 使用递归方式，每层递归都尝试获取同一把 syncMonitor 锁
     * 2. 如果是不可重入锁，第二层递归会阻塞在第一层（自己阻塞自己），导致死锁
     * 3. 由于 synchronized 是可重入锁，递归可以顺利进行到底
     * 4. 记录递归达到的最大深度，证明没有发生死锁
     * 
     * 【递归过程分析】（假设 depth=3）
     * 
     * 第 1 层递归：
     *   - 线程获取 syncMonitor 锁（Monitor 的 owner=当前线程，holdCount=1）
     *   - currentDepth = 1，更新 syncMaxDepth = 1
     *   - remainDepth=3 > 1，调用第 2 层递归
     * 
     * 第 2 层递归：
     *   - 同一线程再次尝试获取 syncMonitor 锁
     *   - 由于 owner 是当前线程，允许重入（holdCount++ → 2）
     *   - currentDepth = 2，更新 syncMaxDepth = 2
     *   - remainDepth=2 > 1，调用第 3 层递归
     * 
     * 第 3 层递归：
     *   - 同一线程第三次尝试获取 syncMonitor 锁
     *   - 允许重入（holdCount++ → 3）
     *   - currentDepth = 3，更新 syncMaxDepth = 3
     *   - remainDepth=1，触发终止条件，返回 value=3
     * 
     * 回溯过程：
     *   - 第 3 层返回 → 第 2 层返回 → 第 1 层返回
     *   - 每层退出 synchronized 块时，JVM 自动执行 monitorexit
     *   - holdCount 递减：3→2→1→0
     *   - 当 holdCount=0 时，锁真正释放，其他线程可以获取
     * 
     * 【关键点】
     * - JVM 负责管理 Monitor 的重入计数，开发者无法直接查看
     * - 每次退出 synchronized 块（包括异常退出），锁都会被正确释放
     * - 递归深度越大，证明可重入性越可靠
     * 
     * @param remainDepth 剩余递归深度（每层递归减 1，直到 1 时终止）
     * @param value 当前递归深度值（也是返回值，用于验证递归确实执行了）
     * @return 最终的递归深度值（等于初始的 depth 参数）
     */
    private int runWithSynchronized(int remainDepth, int value) {
        // 进入 synchronized 同步块，尝试获取 syncMonitor 锁
        synchronized (syncMonitor) {
            // 记录当前递归深度（value 从 1 递增到 depth）
            int currentDepth = value;
            
            // 更新最大深度记录
            // 如果是不可重入锁，这里永远到不了第二层（会死锁）
            if (currentDepth > syncMaxDepth) {
                syncMaxDepth = currentDepth;
            }
            
            // 检查是否达到终止条件
            if (remainDepth <= 1) {
                // 递归到底，返回最终值
                return value;
            }
            
            // 【关键动作】递归调用自身
            // 同一线程再次尝试获取 syncMonitor 锁
            // 由于 synchronized 是可重入锁，这里不会阻塞，而是允许重入
            // remainDepth - 1: 剩余深度减 1
            // value + 1: 当前深度值加 1
            return runWithSynchronized(remainDepth - 1, value + 1);
        }
        // 退出 synchronized 块时，JVM 自动执行 monitorexit，释放锁
        // 如果是递归调用，这里只是 holdCount--，不会立即释放锁
    }

    /**
     * 【ReentrantLock 可重入演示】通过递归调用 + getHoldCount() 观测重入计数。
     * 
     * 【实验设计】
     * 1. 使用递归方式，每层递归都调用 lock() 获取同一把 reentrantLock
     * 2. 使用 getHoldCount() 方法获取当前线程的重入计数
     * 3. 记录最大的 holdCount 值，证明 ReentrantLock 的可重入性
     * 4. 必须在 finally 块中调用 unlock()，确保锁被正确释放
     * 
     * 【ReentrantLock 的重入实现原理】（基于 AQS）
     * 
     * ReentrantLock 内部维护两个关键字段：
     * - exclusiveOwnerThread: 当前持有锁的线程引用
     * - state: 同步状态（在 ReentrantLock 中等价于 holdCount）
     * 
     * lock() 获取锁的流程：
     * 1. 检查 state 是否为 0（锁空闲）
     *    - 如果是，CAS 设置 state=1，设置 exclusiveOwnerThread=当前线程
     *    - 如果否，检查 exclusiveOwnerThread 是否是当前线程
     *      - 如果是，state++（重入），返回成功
     *      - 如果否，进入等待队列阻塞
     * 
     * unlock() 释放锁的流程：
     * 1. 检查 exclusiveOwnerThread 是否是当前线程
     *    - 如果不是，抛出 IllegalMonitorStateException
     *    - 如果是，state--
     * 2. 如果 state == 0，清空 exclusiveOwnerThread，唤醒等待队列中的线程
     *    如果 state > 0，锁仍然被当前线程持有
     * 
     * 【递归过程分析】（假设 depth=3）
     * 
     * 第 1 层递归：
     *   - lock(): state 从 0→1，exclusiveOwnerThread=当前线程
     *   - getHoldCount(): 返回 1
     *   - 更新 lockMaxHoldCount = 1
     *   - 调用第 2 层递归
     *   - unlock(): state 从 1→0（但此时还没执行，因为要等递归返回）
     * 
     * 第 2 层递归：
     *   - lock(): 发现 owner 是当前线程，state 从 1→2
     *   - getHoldCount(): 返回 2
     *   - 更新 lockMaxHoldCount = 2
     *   - 调用第 3 层递归
     *   - unlock(): state 从 2→1
     * 
     * 第 3 层递归：
     *   - lock(): state 从 2→3
     *   - getHoldCount(): 返回 3
     *   - 更新 lockMaxHoldCount = 3
     *   - 触发终止条件，返回
     *   - unlock(): state 从 3→2
     * 
     * 回溯完成后：
     *   - 所有递归层都返回，每层的 finally 都执行了 unlock()
     *   - state: 3→2→1→0
     *   - 当 state=0 时，锁真正释放
     * 
     * 【与 synchronized 的对比】
     * - synchronized: JVM 自动管理 holdCount，开发者无法查看
     * - ReentrantLock: 提供 getHoldCount() API，可以观测重入计数
     * 
     * @param remainDepth 剩余递归深度
     * @param value 当前递归深度值
     * @return 最终的递归深度值
     */
    private int runWithReentrantLock(int remainDepth, int value) {
        // 【关键动作】显式获取锁
        // 这会触发 ReentrantLock 的重入逻辑：
        // 1. 如果锁空闲，设置 owner 为当前线程，state=1
        // 2. 如果 owner 是当前线程，state++（重入）
        // 3. 如果 owner 是其他线程，阻塞等待
        reentrantLock.lock();
        
        try {
            // 【核心观测点】获取当前线程的重入计数
            // 这是 ReentrantLock 相比 synchronized 的优势：可以查看内部状态
            // 第一次调用：getHoldCount() = 1
            // 第二次递归：getHoldCount() = 2
            // 第 N 次递归：getHoldCount() = N
            int holdCount = reentrantLock.getHoldCount();
            
            // 更新最大重入计数记录
            // 这个值证明了 ReentrantLock 确实允许同一线程重复获取锁
            if (holdCount > lockMaxHoldCount) {
                lockMaxHoldCount = holdCount;
            }
            
            // 检查是否达到终止条件
            if (remainDepth <= 1) {
                // 递归到底，返回最终值
                return value;
            }
            
            // 【关键动作】递归调用自身
            // 同一线程再次调用 lock()，触发重入逻辑
            // holdCount 会再 +1
            return runWithReentrantLock(remainDepth - 1, value + 1);
            
        } finally {
            // 【关键实践】必须在 finally 块中释放锁
            // 确保即使在递归调用中抛出异常，锁也能被正确释放
            // 
            // 【解锁逻辑】
            // - 每次 unlock() 会使 holdCount--
            // - 只有当 holdCount 归零时，锁才真正释放
            // - 如果忘记在 finally 中解锁，会导致后续线程永远无法获取锁（死锁）
            reentrantLock.unlock();
        }
    }

    /**
     * 构建成功响应的辅助方法。
     * 
     * @param mode 使用的锁模式（"synchronized" 或 "reentrant"）
     * @param depth 请求的递归深度
     * @param maxCount 实际达到的最大重入计数/深度
     * @param result 最终返回值（应该等于 depth）
     * @param msg 描述信息
     * @return 封装好的响应对象
     */
    private ReentrantLockDemoResponse build(String mode, int depth, int maxCount, int result, String msg) {
        ReentrantLockDemoResponse r = new ReentrantLockDemoResponse();
        r.setSuccess(true);           // 设置成功标志
        r.setStage("REENTRANT_DEMO_DONE");  // 设置阶段标识
        r.setMessage(msg);            // 设置描述信息
        r.setMode(mode);              // 设置锁模式
        r.setDepth(depth);            // 设置请求的递归深度
        r.setMaxReentrantCount(maxCount);  // 设置实际达到的最大重入计数
        r.setFinalResult(result);     // 设置最终返回值
        return r;
    }

    /**
     * 构建失败响应的辅助方法。
     * 
     * @param stage 错误阶段标识
     * @param msg 错误描述信息
     * @return 失败响应对象
     */
    private ReentrantLockDemoResponse fail(String stage, String msg) {
        ReentrantLockDemoResponse r = new ReentrantLockDemoResponse();
        r.setSuccess(false);      // 标记为失败
        r.setStage(stage);        // 设置错误阶段
        r.setMessage(msg);        // 设置错误信息
        return r;
    }
}
