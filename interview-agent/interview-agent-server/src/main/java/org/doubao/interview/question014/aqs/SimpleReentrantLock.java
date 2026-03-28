package org.doubao.interview.question014.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/** 基于 AQS 实现的简单独占锁 - SimpleReentrantLock
 * 
 * 【实现原理】
 * 使用 AQS 的 state 表示锁的状态：
 * - state = 0：无锁状态
 * - state = 1：有锁状态（被某个线程持有）
 * 
 * 【可重入性】
 * 同一线程可以多次获取同一把锁：
 * - 首次获取：state 从 0→1，记录持有线程
 * - 重复获取：判断当前线程是否为持有者，是则 state++
 * - 释放锁：state--，直到 state=0 才真正释放
 * 
 * 【公平性】
 * 本实现为非公平锁：
 * - 新请求锁的线程会直接尝试获取，不检查队列
 * - 可能导致队列中的线程长时间等待（线程饥饿）
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class SimpleReentrantLock {
    
    /**
     * 内部同步器类（继承自 SimpleAQS）
     * 
     * 【设计模式】
     * 使用私有静态内部类实现 AQS 的模板方法
     * - 封装性：外部无法访问 Sync
     * - 复用性：多个锁可以复用同一个 Sync 实例
     */
    private static final class Sync extends SimpleAQS {
        
        /**
         * 是否当前线程已持有锁
         */
        final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
        
        /**
         * 独占式尝试获取锁（非公平实现）
         */
        @Override
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
        
        /**
         * 非公平锁的获取逻辑
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            
            if (c == 0) {
                // 无锁状态，尝试获取
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 当前线程已持有锁，可重入
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
        
        /**
         * 独占式尝试释放锁
         */
        @Override
        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException("Current thread is not owner of the lock");
            
            int nextc = getState() - releases;
            boolean fullyReleased = (nextc == 0);
            
            if (fullyReleased) {
                setExclusiveOwnerThread(null);
            }
            
            setState(nextc);
            return fullyReleased;
        }
        
        /**
         * 创建新的 Condition 对象
         */
        final ConditionObject newCondition() {
            return new ConditionObject();
        }
    }
    
    /**
     * Condition 实现类（简化版本）
     */
    public static class ConditionObject implements Condition {
        
        @Override
        public void await() throws InterruptedException {
            // 简化实现：仅演示概念
            Thread.sleep(100);
        }
        
        @Override
        public void awaitUninterruptibly() {
            // 简化实现
        }
        
        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return nanosTimeout;
        }
        
        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }
        
        @Override
        public boolean awaitUntil(java.util.Date deadline) throws InterruptedException {
            return true;
        }
        
        @Override
        public void signal() {
            // 简化实现
        }
        
        @Override
        public void signalAll() {
            // 简化实现
        }
    }
    
    // ========== 锁的主要方法 ==========
    
    /**
     * 获取锁
     */
    public void lock() {
        sync.acquire(1);
    }
    
    /**
     * 释放锁
     */
    public void unlock() {
        sync.release(1);
    }
    
    /**
     * 尝试获取锁（非阻塞）
     */
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }
    
    /**
     * 尝试获取锁（带超时，简化实现）
     */
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        Thread.sleep(timeout); // 简化实现
        return sync.tryAcquire(1);
    }
    
    /**
     * 创建新的 Condition 实例
     */
    public Condition newCondition() {
        return sync.newCondition();
    }
    
    /**
     * 查询当前持有锁的线程
     */
    public Thread getHolderThread() {
        return sync.getExclusiveOwnerThread();
    }
    
    /**
     * 查询等待队列长度（简化实现）
     */
    public int getQueueLength() {
        return 0;
    }
    
    /**
     * 查询是否有线程在等待（简化实现）
     */
    public boolean hasQueuedThreads() {
        return false;
    }
    
    /**
     * 查询当前线程是否持有锁
     */
    public boolean isLocked() {
        return sync.isHeldExclusively();
    }
    
    // ========== 内部实现 ==========
    
    private final Sync sync = new Sync();
}
