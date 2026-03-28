package org.doubao.interview.question017.concurrent;

/**
 * Semaphore（信号量）简化实现
 * 
 * 核心功能：
 * 1. 维护一个许可 (permit) 计数器
 * 2. acquire() 获取许可，无许可时阻塞
 * 3. release() 释放许可，唤醒等待线程
 * 4. 支持公平/非公平模式
 * 
 * 应用场景：
 * - 数据库连接池控制
 * - 限流器（控制并发请求数）
 * - 资源池管理（线程池、连接池）
 */
public class SimpleSemaphore {
    
    /**
     * 同步器内部类
     * 使用 synchronized 和 wait/notify 实现许可管理
     */
    private static final class Sync {
        /** 当前可用许可数量 */
        private int permits;
        
        /** 是否公平模式（FIFO） */
        private final boolean fair;
        
        /** 等待队列（简化版，实际应使用 AQS） */
        private int waiters = 0;
        
        Sync(int permits, boolean fair) {
            this.permits = permits;
            this.fair = fair;
        }
        
        /**
         * 获取许可
         * @param acquires 需要获取的许可数量
         * @throws InterruptedException 如果线程被中断
         */
        synchronized void acquire(int acquires) throws InterruptedException {
            if (acquires <= 0) {
                throw new IllegalArgumentException("许可数必须大于 0");
            }
            
            // 公平模式：按等待顺序分配许可
            if (fair) {
                while (permits < acquires) {
                    waiters++;
                    try {
                        wait();
                    } finally {
                        waiters--;
                    }
                }
            } else {
                // 非公平模式：直接尝试获取，失败再等待
                while (permits < acquires) {
                    wait();
                }
            }
            
            // 扣除许可
            permits -= acquires;
        }
        
        /**
         * 释放许可
         * @param releases 释放的许可数量
         */
        synchronized void release(int releases) {
            if (releases <= 0) {
                throw new IllegalArgumentException("释放数必须大于 0");
            }
            
            // 增加许可
            permits += releases;
            
            // 通知等待的线程
            // 公平模式下通知第一个，非公平模式下通知所有
            if (fair && waiters > 0) {
                notify();
            } else {
                notifyAll();
            }
        }
        
        /**
         * 获取当前可用许可数
         */
        synchronized int availablePermits() {
            return permits;
        }
        
        /**
         * 获取等待的线程数（近似值）
         */
        synchronized int getWaiters() {
            return waiters;
        }
    }
    
    /** 同步器实例 */
    private final Sync sync;
    
    /**
     * 构造方法
     * @param permits 初始许可数量
     * @param fair 是否公平模式
     */
    public SimpleSemaphore(int permits, boolean fair) {
        if (permits < 0) {
            throw new IllegalArgumentException("许可数不能为负数");
        }
        this.sync = new Sync(permits, fair);
    }
    
    /**
     * 构造方法（默认非公平模式）
     * @param permits 初始许可数量
     */
    public SimpleSemaphore(int permits) {
        this(permits, false);
    }
    
    /**
     * 获取一个许可
     * 如果无可用许可则阻塞等待
     * @throws InterruptedException 如果线程被中断
     */
    public void acquire() throws InterruptedException {
        sync.acquire(1);
    }
    
    /**
     * 获取多个许可
     * @param permits 需要的许可数量
     * @throws InterruptedException 如果线程被中断
     */
    public void acquire(int permits) throws InterruptedException {
        sync.acquire(permits);
    }
    
    /**
     * 释放一个许可
     */
    public void release() {
        sync.release(1);
    }
    
    /**
     * 释放多个许可
     * @param permits 释放的许可数量
     */
    public void release(int permits) {
        sync.release(permits);
    }
    
    /**
     * 获取当前可用许可数
     * @return 可用许可数
     */
    public int availablePermits() {
        return sync.availablePermits();
    }
    
    /**
     * 获取等待的线程数（近似值）
     * @return 等待线程数
     */
    public int getWaiters() {
        return sync.getWaiters();
    }
    
    /**
     * 尝试获取一个许可（不阻塞）
     * @return true 表示获取成功，false 表示无可用许可
     */
    public synchronized boolean tryAcquire() {
        if (sync.availablePermits() >= 1) {
            try {
                sync.acquire(1);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    /**
     * 尝试获取许可（带超时）
     * @param permits 需要的许可数量
     * @param timeoutMs 超时时间（毫秒）
     * @return true 表示获取成功，false 表示超时
     */
    public synchronized boolean tryAcquire(int permits, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        long deadline = startTime + timeoutMs;
        
        try {
            while (sync.availablePermits() < permits) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;  // 超时
                }
                wait(remaining);
            }
            
            sync.acquire(permits);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
