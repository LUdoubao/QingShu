package org.doubao.interview.question014.demo;

import org.doubao.interview.question014.aqs.SimpleReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.ArrayList;
import java.util.List;

/**
 * AQS 使用示例 - 演示基于 AQS 实现的锁的使用方式
 * 
 * 【示例场景】
 * 1. 基本的加锁/解锁操作
 * 2. 可重入特性演示
 * 3. Condition 条件等待
 * 4. 多线程并发访问共享资源
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class AQSDemo {
    
    /**
     * 共享资源类
     * 
     * 【线程安全保证】
     * 使用 SimpleReentrantLock 保护临界区，确保同一时间只有一个线程访问
     */
    static class Counter {
        private int count = 0;
        private final SimpleReentrantLock lock = new SimpleReentrantLock();
        
        /**
         * 增加计数
         * 
         * 【执行流程】
         * 1. lock()：获取锁（如果锁被占用则阻塞等待）
         * 2. 执行临界区代码（count++）
         * 3. finally 块中 unlock()：释放锁
         */
        public void increment() {
            lock.lock();  // 获取锁
            try {
                count++;
                System.out.println(Thread.currentThread().getName() + " 增加计数，当前值：" + count);
            } finally {
                lock.unlock();  // 释放锁（放在 finally 中确保一定会执行）
            }
        }
        
        /**
         * 减少计数
         */
        public void decrement() {
            lock.lock();
            try {
                count--;
                System.out.println(Thread.currentThread().getName() + " 减少计数，当前值：" + count);
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * 获取当前计数值
         */
        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * 使用 Condition 实现生产者 - 消费者模式
     * 
     * 【Condition 作用】
     * - await()：当前线程释放锁并进入等待状态
     * - signal()：唤醒一个在等待的线程
     * - signalAll()：唤醒所有在等待的线程
     */
    static class Buffer {
        private final List<Integer> list = new ArrayList<>();
        private final int CAPACITY = 5;  // 缓冲区容量
        private final SimpleReentrantLock lock = new SimpleReentrantLock();
        private final Condition notFull = lock.newCondition();   // 未满条件
        private final Condition notEmpty = lock.newCondition();  // 非空条件
        
        /**
         * 生产（添加元素）
         * 
         * 【执行逻辑】
         * 1. 获取锁
         * 2. while 循环检查缓冲区是否已满
         * 3. 如果满了，调用 notFull.await() 等待（自动释放锁）
         * 4. 添加元素后，调用 notEmpty.signal() 通知消费者
         */
        public void produce(int value) throws InterruptedException {
            lock.lock();
            try {
                // 注意：必须用 while 而不是 if，防止虚假唤醒
                while (list.size() >= CAPACITY) {
                    System.out.println("缓冲区已满，生产者等待...");
                    notFull.await();  // 释放锁并等待
                }
                
                list.add(value);
                System.out.println("生产：" + value + "，当前大小：" + list.size());
                notEmpty.signal();  // 通知消费者
            } finally {
                lock.unlock();
            }
        }
        
        /**
         * 消费（取出元素）
         * 
         * 【执行逻辑】
         * 1. 获取锁
         * 2. while 循环检查缓冲区是否为空
         * 3. 如果空了，调用 notEmpty.await() 等待
         * 4. 取出元素后，调用 notFull.signal() 通知生产者
         */
        public int consume() throws InterruptedException {
            lock.lock();
            try {
                while (list.isEmpty()) {
                    System.out.println("缓冲区为空，消费者等待...");
                    notEmpty.await();  // 释放锁并等待
                }
                
                int value = list.remove(0);
                System.out.println("消费：" + value + "，当前大小：" + list.size());
                notFull.signal();  // 通知生产者
                return value;
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * 基本使用示例
     */
    public static void basicDemo() {
        System.out.println("=== 基本使用示例 ===");
        Counter counter = new Counter();
        
        // 创建多个线程同时修改计数器
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                counter.increment();
                try {
                    Thread.sleep(100);  // 模拟业务处理时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "线程-A");
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                counter.decrement();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "线程-B");
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("最终计数值：" + counter.getCount());
    }
    
    /**
     * 可重入性演示
     * 
     * 【可重入含义】
     * 同一个线程可以多次获取同一把锁，不会死锁
     */
    public static void reentrantDemo() {
        System.out.println("\n=== 可重入性演示 ===");
        SimpleReentrantLock lock = new SimpleReentrantLock();
        
        Runnable task = () -> {
            lock.lock();  // 第一次获取锁
            try {
                System.out.println(Thread.currentThread().getName() + " 第一次获取锁");
                System.out.println("持有锁的线程：" + lock.getHolderThread());
                
                // 嵌套调用，再次获取同一把锁
                lock.lock();  // 第二次获取锁（可重入）
                try {
                    System.out.println(Thread.currentThread().getName() + " 第二次获取锁（可重入）");
                    
                    // 第三次获取
                    lock.lock();
                    try {
                        System.out.println(Thread.currentThread().getName() + " 第三次获取锁（可重入）");
                    } finally {
                        lock.unlock();  // 释放一次
                    }
                    
                } finally {
                    lock.unlock();  // 再释放一次
                }
                
            } finally {
                lock.unlock();  // 最后释放
                System.out.println(Thread.currentThread().getName() + " 完全释放锁");
            }
        };
        
        new Thread(task, "可重入线程").start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 生产者 - 消费者示例
     */
    public static void producerConsumerDemo() {
        System.out.println("\n=== 生产者 - 消费者示例 ===");
        Buffer buffer = new Buffer();
        
        // 生产者线程
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.produce(i);
                    Thread.sleep((long) (Math.random() * 500));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "生产者");
        
        // 消费者线程
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.consume();
                    Thread.sleep((long) (Math.random() * 800));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "消费者");
        
        producer.start();
        consumer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("生产消费完成！");
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        System.out.println("AQS 使用示例演示\n");
        System.out.println("========================================");
        
        // 运行示例
        basicDemo();
        reentrantDemo();
        producerConsumerDemo();
        
        System.out.println("\n========================================");
        System.out.println("演示结束！");
    }
}
