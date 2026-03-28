package org.doubao.interview.question016.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

/**
 * CountDownLatch 简化实现 - 倒计时门闩
 * 
 * 【核心概念】
 * CountDownLatch 是一个同步辅助类，允许一个或多个线程等待其他线程完成操作。
 * 
 * 【主要特点】
 * 1. 一次性使用：计数器归零后无法重置，需要重新创建实例
 * 2. 倒计时机制：从初始值递减到 0
 * 3. 阻塞等待：await() 方法会阻塞直到计数器为 0
 * 4. 线程安全：内部使用 AQS 的共享模式实现
 * 
 * 【典型场景】
 * - 主线程等待多个子任务完成后继续执行
 * - 并行计算中等待所有分片计算完成
 * - 服务启动时等待所有依赖初始化完成
 * 
 * 【与 CyclicBarrier 的区别】
 * 1. CountDownLatch：一次性，倒计时，一个线程等 others
 * 2. CyclicBarrier：可复用，计数递增，others 相互等待
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class SimpleCountDownLatch {
    
    /**
     * 同步器内部类（基于 AQS 共享模式）
     * 
     * 【设计思路】
     * 使用 AQS 的 state 表示剩余计数：
     * - 初始值：构造时指定
     * - countDown()：state--
     * - await()：等待 state==0
     */
    private static final class Sync {
        /** 当前计数值（使用 volatile 保证可见性） */
        private volatile int count;
        
        /**
         * 构造函数
         * 
         * @param initialCount 初始计数值
         */
        Sync(int initialCount) {
            this.count = initialCount;
        }
        
        /**
         * 获取当前计数值
         * 
         * @return 当前计数
         */
        int getCount() {
            return count;
        }
        
        /**
         * 倒计时操作（countDown）
         * 
         * 【执行逻辑】
         * 1. count--（原子操作）
         * 2. 如果 count==0，唤醒所有等待线程
         */
        synchronized void countDown() {
            if (count > 0) {
                count--;
                // 计数归零时通知所有等待线程
                if (count == 0) {
                    notifyAll();
                }
            }
        }
        
        /**
         * 等待操作（await）
         * 
         * 【执行逻辑】
         * 1. 如果 count==0，立即返回
         * 2. 否则进入等待状态
         * 3. 被唤醒后检查 count 是否为 0
         * 
         * @throws InterruptedException 如果等待过程中被中断
         */
        synchronized void await() throws InterruptedException {
            while (count > 0) {
                wait();  // 释放锁并等待
            }
        }
        
        /**
         * 带超时的等待
         * 
         * @param timeout 超时时间（毫秒）
         * @return true-成功等待到 count=0，false-超时
         * @throws InterruptedException 如果被中断
         */
        synchronized boolean await(long timeout) throws InterruptedException {
            long startTime = System.currentTimeMillis();
            long elapsed = 0;
            
            while (count > 0 && elapsed < timeout) {
                wait(timeout - elapsed);
                elapsed = System.currentTimeMillis() - startTime;
            }
            
            return count == 0;
        }
    }
    
    /** 同步器实例 */
    private final Sync sync;
    
    /**
     * 构造函数
     * 
     * 【参数说明】
     * @param count 需要等待的线程数量
     * 
     * 【注意事项】
     * - count 必须 >= 0
     * - count=0 时，await() 会立即返回
     */
    public SimpleCountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("计数值不能小于 0");
        }
        this.sync = new Sync(count);
    }
    
    /**
     * 等待方法
     * 
     * 【调用场景】
     * 通常由主线程或需要等待的线程调用
     * 
     * 【阻塞机制】
     * - 如果 count>0：阻塞当前线程
     * - 如果 count==0：立即返回
     * 
     * @throws InterruptedException 如果等待过程中被中断
     */
    public void await() throws InterruptedException {
        sync.await();
    }
    
    /**
     * 带超时的等待
     * 
     * 【使用场景】
     * 需要避免无限等待的情况
     * 
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true-成功等待，false-超时
     * @throws InterruptedException 如果被中断
     */
    public boolean await(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        long millis = nanos / 1_000_000;
        return sync.await(millis);
    }
    
    /**
     * 倒计时方法
     * 
     * 【调用场景】
     * 由子任务在完成工作时调用
     * 
     * 【执行效果】
     * - count--
     * - 如果 count==0，唤醒所有等待线程
     * 
     * 【注意事项】
     * - 多次调用会继续减少计数（不会小于 0）
     * - 不同线程可以并发调用
     */
    public void countDown() {
        sync.countDown();
    }
    
    /**
     * 获取当前计数值
     * 
     * 【用途】
     * - 调试和监控
     * - 判断是否已经完成
     * 
     * @return 当前计数值
     */
    public long getCount() {
        return sync.getCount();
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return "SimpleCountDownLatch[count=" + sync.getCount() + "]";
    }
}
