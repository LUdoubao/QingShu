package org.doubao.interview.question014.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * AQS (AbstractQueuedSynchronizer) - 抽象队列同步器
 * 
 * 【核心概念】
 * AQS 是 Java 并发包 (JUC) 中的核心基础框架，用于构建锁和同步器的基础组件。
 * 
 * 【主要特性】
 * 1. state 状态维护：使用 volatile int state 表示同步状态
 *    - 在独占锁中：state=0 表示无锁，state>0 表示有线程持有锁
 *    - 在共享锁中：state 表示可用资源数量（如信号量）
 *    - 在 CountDownLatch 中：state 表示倒数计数值
 * 
 * 2. FIFO 等待队列：维护一个先进先出的双端队列
 *    - 锁获取失败的线程会被封装成 Node 节点加入队列尾部
 *    - 队列中的线程会被阻塞，等待前驱节点释放锁后唤醒
 * 
 * 3. 模板方法模式：AQS 定义了标准流程，子类只需实现特定方法
 *    - tryAcquire(int arg)：独占式尝试获取资源
 *    - tryRelease(int arg)：独占式尝试释放资源
 *    - tryAcquireShared(int arg)：共享式尝试获取资源
 *    - tryReleaseShared(int arg)：共享式尝试释放资源
 *    - isHeldExclusively()：判断是否被当前线程独占
 * 
 * 【应用场景】
 * - ReentrantLock：基于 AQS 实现的独占锁
 * - Semaphore：基于 AQS 实现的信号量（共享式）
 * - CountDownLatch：基于 AQS 实现的倒数计数器（共享式）
 * - ReentrantReadWriteLock：读写锁
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class SimpleAQS {
    
    /**
     * 同步状态 - 使用 volatile 保证可见性
     * 
     * 【物理含义】
     * - 独占锁场景：0 表示无锁，1 表示有锁
     * - 信号量场景：表示剩余可用资源数
     * - CountDownLatch：表示还需要等待的事件数
     * 
     * 【volatile 作用】
     * 1. 可见性：一个线程修改 state 后，其他线程立即可见
     * 2. 禁止指令重排序：保证 CAS 操作的正确性
     */
    private volatile int state;
    
    /**
     * 同步队列的头节点
     * 
     * 【队列结构】
     * - 双向链表：pred 指向前驱，next 指向后继
     * - 头节点是虚拟节点，不存储实际线程
     * - 新节点从队尾加入，获取锁的线程从头节点之后开始竞争
     */
    private transient Node head;
    
    /**
     * 同步队列的尾节点
     * 
     * 【入队操作】
     * 1. 将当前线程封装成 Node 节点
     * 2. CAS 设置尾节点，失败则自旋重试
     * 3. 前驱节点的 next 指向新节点
     * 4. 新节点的 prev 指向前驱节点
     */
    private transient Node tail;
    
    /**
     * 独占锁的持有者
     * 
     * 【作用】
     * - 记录当前哪个线程持有锁
     * - 可重入锁需要判断当前线程是否已持有锁
     */
    private transient Thread exclusiveOwnerThread;
    
    /**
     * 队列节点类
     * 
     * 【节点状态】
     * - CANCELLED(1)：节点被取消（超时或中断）
     * - SIGNAL(-1)：后继线程需要被唤醒
     * - CONDITION(-2)：节点在条件队列中
     * - PROPAGATE(-3)：共享模式下需要向后传播
     * 
     * 【等待策略】
     * - LockSupport.park()：阻塞当前线程
     * - LockSupport.unpark(Thread)：唤醒指定线程
     */
    static final class Node {
        // 节点状态常量
        static final int CANCELLED = 1;      // 已取消
        static final int SIGNAL = -1;        // 需要唤醒后继
        static final int CONDITION = -2;     // 条件队列
        static final int PROPAGATE = -3;     // 共享传播
        
        // 节点模式
        enum Mode {
            EXCLUSIVE,  // 独占模式
            SHARED      // 共享模式
        }
        
        /** 后继节点引用 */
        volatile Node next;
        
        /** 前驱节点引用 */
        volatile Node prev;
        
        /** 等待的线程 */
        volatile Thread thread;
        
        /** 
         * 节点状态
         * 
         * 【状态流转】
         * 初始：0 → 等待：SIGNAL(-1) → 取消：CANCELLED(1)
         */
        volatile int waitStatus;
        
        /** 下一个等待条件的节点（条件队列使用） */
        Node nextWaiter;
        
        /** 等待模式标记 */
        boolean isSharedMode;
        
        /**
         * 获取前驱节点
         * 
         * @return 前驱节点
         * @throws NullPointerException 如果前驱为空
         */
        Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
        
        Node(Thread thread) {
            this.thread = thread;
            this.isSharedMode = false;
        }
        
        Node(Thread thread, boolean shared) {
            this.thread = thread;
            this.isSharedMode = shared;
        }
    }
    
    /**
     * 独占式获取资源
     * 
     * 【执行流程】
     * 1. 调用 tryAcquire(arg) 尝试获取资源
     * 2. 成功则直接返回
     * 3. 失败则将线程封装为 Node 加入等待队列
     * 4. 调用 LockSupport.park() 阻塞线程
     * 5. 等待被前驱节点唤醒后再次尝试
     * 
     * @param arg 资源参数
     */
    public final void acquire(int arg) {
        // 尝试获取资源，失败则加入队列并阻塞
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(true), arg)) {
            // 如果获取过程中被中断，恢复中断状态
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 独占式释放资源
     * 
     * 【执行流程】
     * 1. 调用 tryRelease(arg) 尝试释放资源
     * 2. 成功后检查头节点的 waitStatus
     * 3. 如果需要唤醒（waitStatus != 0），则 unpark 后继节点
     * 
     * @param arg 资源参数
     * @return 释放是否成功
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            // 唤醒后继节点
            if (h != null && h.waitStatus != 0) {
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
    }
    
    /**
     * 将节点添加到等待队列尾部
     * 
     * 【CAS 自旋入队】
     * 1. 记录当前尾节点
     * 2. CAS 尝试将新节点设为尾节点
     * 3. 失败则自旋重试（避免使用锁）
     * 4. 成功后原尾节点的 next 指向新节点
     * 
     * @return 添加的节点
     */
    private Node addWaiter(boolean exclusive) {
        Node node = new Node(Thread.currentThread(), !exclusive);
        Node pred = tail;
        
        // 快速路径：队列非空时直接入队
        if (pred != null) {
            node.prev = pred;
            // CAS 设置尾节点
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        
        // 慢速路径：自旋直到成功
        for (;;) {
            Node t = tail;
            if (t == null) {
                // 队列为空，初始化头节点
                if (compareAndSetHead(new Node(null)))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return node;
                }
            }
        }
    }
    
    /**
     * 节点在队列中等待获取资源
     * 
     * 【自旋 + 阻塞策略】
     * 1. 检查前驱节点是否为头节点且获取成功
     * 2. 否则设置前驱节点状态为 SIGNAL
     * 3. 再次尝试获取，失败则 park 阻塞
     * 4. 被唤醒后检查是否需要继续等待
     * 
     * @param node 当前节点
     * @param arg 资源参数
     * @return 等待过程中是否被中断
     */
    private boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                // 前驱是头节点且获取成功
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // GC 帮助
                    failed = false;
                    return interrupted;
                }
                // 检查是否需要阻塞
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            }
        } catch (Throwable t) {
            cancelAcquire(node);
            throw t;
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    
    /**
     * 检查获取失败后是否需要阻塞
     * 
     * 【状态判断】
     * - SIGNAL(-1)：需要阻塞
     * - CANCELLED(1)：跳过已取消的节点
     * - 其他状态：设置为 SIGNAL 后重试
     * 
     * @param pred 前驱节点
     * @param node 当前节点
     * @return 是否需要阻塞
     */
    private boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            // 前驱已经设置 SIGNAL 标志，可以安全阻塞
            return true;
        if (ws > 0) {
            // 前驱已取消，向前查找未取消的节点
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // 设置前驱状态为 SIGNAL
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
    
    /**
     * 阻塞当前线程并检查是否被中断
     * 
     * @return 是否被中断
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
    
    /**
     * 唤醒后继节点
     * 
     * 【唤醒策略】
     * 1. 从头节点的 next 开始查找第一个有效节点
     * 2. 使用 LockSupport.unpark() 唤醒该节点
     * 3. 避免因节点取消导致后继无法唤醒
     * 
     * @param h 头节点
     */
    private void unparkSuccessor(Node h) {
        int ws = h.waitStatus;
        if (ws < 0)
            // 重置状态
            compareAndSetWaitStatus(h, ws, 0);
        
        Node s = h.next;
        // 从队尾开始查找（防止 next 被置为 null）
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != h; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }
    
    /**
     * 设置新的头节点
     * 
     * 【注意事项】
     * - 不能直接设 head，需要用 CAS 保证原子性
     * - 原头节点的 next 要置为 null，帮助 GC
     * 
     * @param node 新头节点
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }
    
    /**
     * CAS 设置头节点（简化实现）
     */
    private final boolean compareAndSetHead(Node update) {
        if (head == null) {
            head = update;
            return true;
        }
        return false;
    }
    
    /**
     * CAS 设置尾节点（简化实现）
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        if (tail == expect) {
            tail = update;
            return true;
        }
        return false;
    }
    
    /**
     * CAS 设置等待状态（简化实现）
     */
    private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
        if (node.waitStatus == expect) {
            node.waitStatus = update;
            return true;
        }
        return false;
    }
    
    /**
     * 取消获取
     */
    private void cancelAcquire(Node node) {
        if (node == null)
            return;
        node.thread = null;
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;
        Node predNext = pred.next;
        node.waitStatus = Node.CANCELLED;
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    LockSupport.unpark(next.thread);
            }
        }
        node.next = node; // GC
    }
    
    private final boolean compareAndSetNext(Node node, Node expect, Node update) {
        if (node.next == expect) {
            node.next = update;
            return true;
        }
        return false;
    }
    
    // ========== 子类需要实现的模板方法 ==========
    
    /**
     * 独占式尝试获取资源
     * 
     * 【实现要点】
     * - 由子类重写，定义具体的获取逻辑
     * - 返回 true 表示获取成功，false 表示失败
     * 
     * @param arg 资源参数
     * @return 是否获取成功
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException("子类必须实现此方法");
    }
    
    /**
     * 独占式尝试释放资源
     * 
     * @param arg 资源参数
     * @return 是否释放成功
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException("子类必须实现此方法");
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 获取当前同步状态
     * 
     * @return state 值
     */
    protected final int getState() {
        return state;
    }
    
    /**
     * 设置同步状态
     * 
     * @param newState 新的状态值
     */
    protected final void setState(int newState) {
        state = newState;
    }
    
    /**
     * CAS 设置同步状态
     * 
     * @param expect 期望值
     * @param update 更新值
     * @return 设置是否成功
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // 实际实现需要使用 Unsafe 类的 CAS 操作
        if (state == expect) {
            state = update;
            return true;
        }
        return false;
    }
    
    /**
     * 获取当前持有独占锁的线程
     * 
     * @return 独占线程
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
    
    /**
     * 设置独占线程
     * 
     * @param thread 独占线程
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }
}
