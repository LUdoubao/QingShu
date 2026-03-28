package org.doubao.interview.question016.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

/**
 * CyclicBarrier 简化实现 - 循环栅栏
 * 
 * 【核心概念】
 * CyclicBarrier 是一个同步辅助类，它允许一组线程互相等待，直到所有线程都到达某个公共屏障点。
 * 
 * 【主要特点】
 * 1. 可复用性：屏障可以被重复使用（计数重置）
 * 2. 相互等待：所有线程彼此等待，而不是一个等 others
 * 3. 屏障动作：可选地在所有线程到达后执行一个汇总动作
 * 4. 循环使用：到达屏障后自动重置，可以再次使用
 * 
 * 【典型场景】
 * - 并行计算中，多个线程需要相互等待后再一起进入下一阶段
 * - 多线程数据加载，全部加载完成后再统一处理
 * - 分布式模拟中，多个参与者需要同步开始
 * 
 * 【与 CountDownLatch 的区别】
 * 1. CyclicBarrier：可复用，相互等待，计数递增到阈值
 * 2. CountDownLatch：一次性，单向等待，倒计时递减到 0
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class SimpleCyclicBarrier {
    
    /**
     * 内部栅栏类
     * 
     * 【工作原理】
     * 维护一个计数器，每到达一个线程就 count++
     * 当 count==parties 时，所有线程被唤醒并继续执行
     */
    private static final class Barrier {
        /** 需要等待的线程总数（ parties） */
        private final int parties;
        
        /** 当前已到达的线程数 */
        private int count;
        
        /** 代数（用于检测重用） */
        private long generation;
        
        /** 是否有线程在等待 */
        private boolean waiting;
        
        /** 栅栏是否被破坏（broken） */
        private boolean broken;
        
        /** 屏障动作 */
        private Runnable barrierAction;
        
        /**
         * 构造函数
         * 
         * @param parties 需要等待的线程数量
         */
        Barrier(int parties) {
            this.parties = parties;
            this.count = 0;
            this.generation = 0;
            this.waiting = false;
            this.broken = false;
            this.barrierAction = null;
        }
        
        /**
         * 设置屏障动作
         * 
         * @param action 屏障动作
         */
        void setBarrierAction(Runnable action) {
            this.barrierAction = action;
        }
        
        /**
         * 等待其他线程到达
         * 
         * 【执行流程】
         * 1. 获取当前代数和计数
         * 2. count++
         * 3. 如果 count < parties：进入等待
         * 4. 如果 count == parties：唤醒所有线程，重置计数
         * 
         * @throws BrokenBarrierException 如果栅栏被破坏
         * @throws InterruptedException 如果被中断
         */
        synchronized int await() throws BrokenBarrierException, InterruptedException {
            // 检查栅栏是否被破坏
            if (broken) {
                throw new BrokenBarrierException("CyclicBarrier is broken");
            }
            
            // 检查是否被中断
            if (Thread.interrupted()) {
                nextGeneration(null);
                throw new InterruptedException("Interrupted while waiting at barrier");
            }
            
            // 记录当前索引（用于返回值）
            int index = count;
            count++;
            
            // 如果是最后一个到达的线程
            if (count == parties) {
                // 执行屏障动作并唤醒所有线程
                nextGeneration(barrierAction);
                return index;
            }
            
            // 进入等待状态
            try {
                while (!broken && !waiting) {
                    wait();
                }
                
                // 检查是否因为栅栏破坏而醒来
                if (broken) {
                    throw new BrokenBarrierException("CyclicBarrier is broken");
                }
                
                // 检查是否被中断
                if (Thread.interrupted()) {
                    nextGeneration(null);
                    throw new InterruptedException("Interrupted while waiting at barrier");
                }
                
                return index;
            } catch (Throwable t) {
                setBroken();
                throw t;
            }
        }
        
        /**
         * 带超时的等待
         * 
         * @param timeout 超时时间（毫秒）
         * @return 线程索引
         * @throws BrokenBarrierException 如果栅栏被破坏
         * @throws InterruptedException 如果被中断
         * @throws TimeoutException 如果超时
         */
        synchronized int await(long timeout) throws BrokenBarrierException, InterruptedException, TimeoutException {
            if (broken) {
                throw new BrokenBarrierException("CyclicBarrier is broken");
            }
            
            if (Thread.interrupted()) {
                nextGeneration(null);
                throw new InterruptedException("Interrupted while waiting at barrier");
            }
            
            int index = count;
            count++;
            
            if (count == parties) {
                nextGeneration(barrierAction);
                return index;
            }
            
            try {
                long startTime = System.currentTimeMillis();
                long elapsed = 0;
                
                while (!broken && !waiting && elapsed < timeout) {
                    wait(timeout - elapsed);
                    elapsed = System.currentTimeMillis() - startTime;
                }
                
                if (elapsed >= timeout) {
                    setBroken();
                    throw new TimeoutException("Timeout waiting at barrier");
                }
                
                if (broken) {
                    throw new BrokenBarrierException("CyclicBarrier is broken");
                }
                
                return index;
            } catch (Throwable t) {
                setBroken();
                throw t;
            }
        }
        
        /**
         * 进入下一代
         * 
         * 【触发时机】
         * - 所有线程都到达屏障
         * - 有线程被中断
         * 
         * 【执行顺序】
         * 1. 重置计数和代数
         * 2. 如果有屏障动作，先执行
         * 3. 唤醒所有等待线程
         */
        private void nextGeneration(Runnable action) {
            // 先执行屏障动作（在所有线程唤醒之前）
            if (action != null) {
                try {
                    action.run();
                } catch (Exception e) {
                    // 屏障动作异常不影响后续流程
                    System.err.println("屏障动作执行异常：" + e.getMessage());
                }
            }
            count = 0;    // 重置计数
            generation++; // 代数 +1
            waiting = true;
            notifyAll();  // 最后唤醒所有线程
        }
        
        /**
         * 标记栅栏为破坏状态
         */
        private void setBroken() {
            broken = true;
            notifyAll();
        }
        
        /**
         * 获取等待的线程数
         * 
         * @return 等待中的线程数
         */
        synchronized int getNumberWaiting() {
            if (waiting && count > 0) {
                return count - 1;  // 减 1 是因为最后一个到达的线程不等待
            }
            return 0;
        }
    }
    
    /** 栅栏实例 */
    private final Barrier barrier;
    
    /** 可选的屏障动作（在所有线程到达后执行） */
    private final Runnable barrierAction;
    
    /**
     * 构造函数
     * 
     * 【参数说明】
     * @param parties 需要互相等待的线程数量
     * @param barrierAction 当所有线程到达时执行的动作（可为 null）
     * 
     * 【注意事项】
     * - parties 必须 > 0
     * - barrierAction 由最后一个到达的线程执行，但会在所有线程唤醒之前完成
     */
    public SimpleCyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) {
            throw new IllegalArgumentException("parties must be > 0");
        }
        this.barrier = new Barrier(parties);
        this.barrier.setBarrierAction(barrierAction);  // 将屏障动作传递给 Barrier
        this.barrierAction = barrierAction;
    }
    
    /**
     * 构造函数（无屏障动作）
     * 
     * @param parties 线程数量
     */
    public SimpleCyclicBarrier(int parties) {
        this(parties, null);
    }
    
    /**
     * 等待方法
     * 
     * 【调用时机】
     * 每个线程在到达屏障点时调用
     * 
     * 【执行效果】
     * - 阻塞直到所有线程都调用此方法
     * - 所有线程同时被唤醒继续执行
     * - 屏障动作（如果有）在所有线程唤醒前执行完毕
     * 
     * @return 当前线程的索引（0 到 parties-1）
     * @throws BrokenBarrierException 如果栅栏被破坏
     * @throws InterruptedException 如果被中断
     */
    public int await() throws BrokenBarrierException, InterruptedException {
        return barrier.await();
    }
    
    /**
     * 带超时的等待
     * 
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 线程索引
     * @throws BrokenBarrierException 如果栅栏被破坏
     * @throws InterruptedException 如果被中断
     * @throws TimeoutException 如果超时
     */
    public int await(long timeout, java.util.concurrent.TimeUnit unit) throws BrokenBarrierException, InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        long millis = nanos / 1_000_000;
        return barrier.await(millis);
    }
    
    /**
     * 重置栅栏
     * 
     * 【使用场景】
     * - 提前结束当前代的等待
     * - 手动重置栅栏状态
     * 
     * 【效果】
     * - 打断所有等待的线程（抛出 BrokenBarrierException）
     * - 重置计数器和代数
     */
    public void reset() {
        // 简化实现：实际应该破坏当前代并重置
        // 这里为了简化，不做详细实现
    }
    
    /**
     * 检查是否有线程在等待
     * 
     * @return true-有等待线程，false-无等待
     */
    public boolean isBroken() {
        // 简化实现
        return false;
    }
    
    /**
     * 获取等待的线程数
     * 
     * @return 等待中的线程数
     */
    public int getNumberWaiting() {
        return barrier.getNumberWaiting();
    }
}
