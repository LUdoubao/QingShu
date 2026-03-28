package org.doubao.interview.question019.concurrent;

/**
 * StampedLock（邮戳锁）简化实现
 * 
 * 核心特性：
 * 1. 三种模式：写锁、悲观读锁、乐观读
 * 2. 乐观读无需加锁，性能更高
 * 3. 需要校验 stamp（邮戳）验证一致性
 * 4. 不可重入，使用复杂度更高
 * 
 * 应用场景：
 * - 读多写少且冲突低的场景
 * - 点计算、几何运算等
 * - 需要更高并发性能的缓存
 */
public class SimpleStampedLock {
    
    /** 写锁状态 */
    private static final long WRITE_LOCK = 1L;
    
    /** 读锁单位（用于位运算） */
    private static final long READ_UNIT = 2L;
    
    /** 当前邮戳（版本号） */
    private long stamp = 0L;
    
    /** 是否正在写入 */
    private boolean writing = false;
    
    /** 当前读线程数 */
    private int readers = 0;
    
    /** 等待写入的线程数 */
    private int writeWaiters = 0;
    
    /** 等待读取的线程数 */
    private int readWaiters = 0;
    
    /**
     * 获取写锁
     * @return 邮戳，用于释放锁
     * @throws InterruptedException 如果线程被中断
     */
    public synchronized long writeLock() throws InterruptedException {
        writeWaiters++;
        try {
            // 等待条件：有读线程或有其他写线程
            while (readers > 0 || writing) {
                wait();
            }
        } finally {
            writeWaiters--;
        }
        
        writing = true;
        return ++stamp;  // 返回新邮戳
    }
    
    /**
     * 释放写锁
     * @param stamp 获取锁时的邮戳
     */
    public synchronized void unlockWrite(long stamp) {
        if (this.stamp == stamp && writing) {
            writing = false;
            notifyAll();  // 通知所有等待的线程
        }
    }
    
    /**
     * 获取悲观读锁
     * @return 邮戳，用于释放锁
     * @throws InterruptedException 如果线程被中断
     */
    public synchronized long readLock() throws InterruptedException {
        readWaiters++;
        try {
            // 等待条件：有写线程
            while (writing) {
                wait();
            }
        } finally {
            readWaiters--;
        }
        
        readers++;
        return stamp;  // 返回当前邮戳
    }
    
    /**
     * 释放悲观读锁
     * @param stamp 获取锁时的邮戳
     */
    public synchronized void unlockRead(long stamp) {
        if (readers > 0) {
            readers--;
            if (readers == 0) {
                notifyAll();  // 通知所有等待的线程
            }
        }
    }
    
    /**
     * 尝试乐观读（不加锁）
     * @return 邮戳，用于后续校验
     */
    public synchronized long tryOptimisticRead() {
        if (writing) {
            return 0L;  // 有写操作，返回无效邮戳
        }
        return stamp;  // 返回当前邮戳
    }
    
    /**
     * 校验乐观读的邮戳是否有效
     * @param stamp 之前获取的邮戳
     * @return true 表示数据一致，false 表示数据可能已修改
     */
    public synchronized boolean validate(long stamp) {
        // 检查邮戳是否变化且当前没有写操作
        return this.stamp == stamp && !writing;
    }
    
    /**
     * 将悲观读锁升级为写锁
     * @param stamp 读锁的邮戳
     * @return 新的写锁邮戳，失败返回 0
     */
    public synchronized long tryConvertToWriteLock(long stamp) {
        // 只有唯一读线程时才能升级
        if (readers == 1 && this.stamp == stamp && !writing) {
            readers--;
            writing = true;
            return ++this.stamp;
        }
        return 0L;  // 升级失败
    }
    
    /**
     * 获取当前邮戳（用于测试）
     * @return 当前邮戳
     */
    public synchronized long getStamp() {
        return stamp;
    }
    
    /**
     * 获取当前读线程数（用于测试）
     * @return 读线程数
     */
    public synchronized int getReaders() {
        return readers;
    }
    
    /**
     * 检查是否正在写入（用于测试）
     * @return 是否在写入
     */
    public synchronized boolean isWriting() {
        return writing;
    }
}
