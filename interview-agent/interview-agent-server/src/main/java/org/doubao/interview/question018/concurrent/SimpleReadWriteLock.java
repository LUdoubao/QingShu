package org.doubao.interview.question018.concurrent;

/**
 * ReadWriteLock（读写锁）简化实现
 * 
 * 核心特性：
 * 1. 读锁共享：多个线程可同时持有读锁
 * 2. 写锁独占：写锁与读锁、写锁都互斥
 * 3. 适合读多写少场景
 * 
 * 应用场景：
 * - 缓存系统（读多写少）
 * - 配置管理（频繁读取，偶尔更新）
 * - 字典/映射表（查询多，修改少）
 */
public class SimpleReadWriteLock {
    
    /**
     * 读锁实现
     * 支持多个线程同时获取读锁
     */
    public class ReadLock {
        /**
         * 获取读锁
         * 如果有线程正在写入，则需要等待
         * @throws InterruptedException 如果线程被中断
         */
        public void lock() throws InterruptedException {
            synchronized (lockMutex) {
                // 等待条件：有线程在写入或有写线程在等待
                while (writing || writeWaiters > 0) {
                    lockMutex.wait();
                }
                readers++;
            }
        }
        
        /**
         * 释放读锁
         */
        public void unlock() {
            synchronized (lockMutex) {
                readers--;
                if (readers == 0) {
                    lockMutex.notifyAll();  // 通知所有等待的线程
                }
            }
        }
    }
    
    /**
     * 写锁实现
     * 独占访问，与读锁和写锁都互斥
     */
    public class WriteLock {
        /**
         * 获取写锁
         * 需要等待所有读线程完成且没有其他写线程
         * @throws InterruptedException 如果线程被中断
         */
        public void lock() throws InterruptedException {
            synchronized (lockMutex) {
                writeWaiters++;
                try {
                    // 等待条件：有线程在读或有其他线程在写
                    while (readers > 0 || writing) {
                        lockMutex.wait();
                    }
                } finally {
                    writeWaiters--;
                }
                writing = true;
            }
        }
        
        /**
         * 释放写锁
         */
        public void unlock() {
            synchronized (lockMutex) {
                writing = false;
                lockMutex.notifyAll();  // 通知所有等待的线程
            }
        }
    }
    
    /** 当前读线程数量 */
    private int readers = 0;
    
    /** 是否正在写入 */
    private boolean writing = false;
    
    /** 等待写入的线程数 */
    private int writeWaiters = 0;
    
    /** 锁对象的监视器 */
    private final Object lockMutex = new Object();
    
    /** 读锁实例 */
    private final ReadLock readLock = new ReadLock();
    
    /** 写锁实例 */
    private final WriteLock writeLock = new WriteLock();
    
    /**
     * 获取读锁
     * @return 读锁实例
     */
    public ReadLock readLock() {
        return readLock;
    }
    
    /**
     * 获取写锁
     * @return 写锁实例
     */
    public WriteLock writeLock() {
        return writeLock;
    }
    
    /**
     * 获取当前读线程数（用于测试）
     * @return 读线程数
     */
    public int getReaders() {
        synchronized (lockMutex) {
            return readers;
        }
    }
    
    /**
     * 检查是否正在写入（用于测试）
     * @return 是否在写入
     */
    public boolean isWriting() {
        synchronized (lockMutex) {
            return writing;
        }
    }
}
