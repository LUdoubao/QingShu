package org.doubao.interview.question014.demo;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport 使用示例 - 演示线程阻塞与唤醒
 * 
 * 【核心演示】
 * 1. park() 和 unpark() 的基本用法
 * 2. permit 许可机制
 * 3. 先 unpark 后 park 的效果
 * 4. 中断对 park() 的影响
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class LockSupportDemo {
    
    /**
     * 基本使用示例
     * 
     * 【执行流程】
     * 1. 主线程启动子线程
     * 2. 主线程休眠 1 秒，确保子线程已经执行到 park()
     * 3. 主线程调用 unpark() 唤醒子线程
     * 4. 子线程被唤醒后继续执行
     */
    public static void basicParkUnpark() {
        System.out.println("=== 基本 park/unpark 示例 ===");
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行");
            
            System.out.println(Thread.currentThread().getName() + " 准备 park 阻塞...");
            LockSupport.park();  // 阻塞当前线程
            
            System.out.println(Thread.currentThread().getName() + " 被唤醒了！");
            System.out.println(Thread.currentThread().getName() + " 继续执行后续逻辑");
        }, "子线程");
        
        thread.start();
        
        try {
            Thread.sleep(1000);  // 等待 1 秒，确保子线程已经阻塞
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程准备唤醒子线程...");
        LockSupport.unpark(thread);  // 唤醒指定线程
        
        try {
            thread.join();  // 等待子线程结束
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程：子线程已结束\n");
    }
    
    /**
     * 演示 permit 许可机制
     * 
     * 【permit 特点】
     * 1. 每个线程有一个 permit 计数器（0 或 1）
     * 2. unpark() 会增加 permit（最多为 1）
     * 3. park() 会消耗 permit，如果没有则阻塞
     * 4. permit 可以累积一次（多次 unpark 只计一次）
     */
    public static void permitDemo() {
        System.out.println("=== permit 许可机制演示 ===");
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 第一次 park");
            LockSupport.park();  // 消耗 permit，如果有则立即返回
            System.out.println(Thread.currentThread().getName() + " 第一次 park 返回");
            
            System.out.println(Thread.currentThread().getName() + " 第二次 park");
            LockSupport.park();  // 再次阻塞，因为 permit 已用完
            System.out.println(Thread.currentThread().getName() + " 第二次 park 返回");
        }, "Permit 测试线程");
        
        thread.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程：unpark 两次（但只累积一个 permit）");
        LockSupport.unpark(thread);  // 第一次 unpark
        LockSupport.unpark(thread);  // 第二次 unpark（不会累积）
        
        try {
            Thread.sleep(1000);
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程：演示完成\n");
    }
    
    /**
     * 先 unpark 后 park 的演示
     * 
     * 【重要特性】
     * - 先调用 unpark() 再调用 park()，park() 会立即返回
     * - 因为 permit 已经被设置
     */
    public static void orderMattersDemo() {
        System.out.println("=== 调用顺序演示：先 unpark 后 park ===");
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 准备执行");
            
            try {
                Thread.sleep(1000);  // 模拟业务处理
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println(Thread.currentThread().getName() + " 执行 park()");
            LockSupport.park();  // 会立即返回，因为之前已被 unpark
            System.out.println(Thread.currentThread().getName() + " park() 立即返回！");
        }, "延迟 park 线程");
        
        // 立即 unpark（在线程还没到 park() 时）
        System.out.println("主线程：立即 unpark");
        LockSupport.unpark(thread);
        
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程：演示完成\n");
    }
    
    /**
     * 带 blocker 对象的 park 演示
     * 
     * 【blocker 作用】
     * 1. 便于监控和诊断
     * 2. 通过 ThreadMXBean 可以看到哪个对象阻塞了线程
     */
    public static void parkWithBlocker() {
        System.out.println("=== 带 blocker 的 park 示例 ===");
        
        Object blocker = new Object();
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 使用 blocker 对象 park");
            LockSupport.park(blocker);
            System.out.println(Thread.currentThread().getName() + " 被唤醒");
        }, "Blocker 测试线程");
        
        thread.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        LockSupport.unpark(thread);
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("演示完成\n");
    }
    
    /**
     * 带超时的 park 演示
     */
    public static void timedParkDemo() {
        System.out.println("=== 带超时的 park 示例 ===");
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始超时 park（2 秒）");
            long startTime = System.currentTimeMillis();
            
            LockSupport.parkNanos(2_000_000_000L);  // 阻塞 2 秒
            
            long endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " 醒来，耗时：" + (endTime - startTime) + "ms");
        }, "超时 park 线程");
        
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("演示完成\n");
    }
    
    /**
     * 中断对 park 的影响
     * 
     * 【重要特性】
     * - park() 可以被中断唤醒
     * - 唤醒后可以通过 Thread.interrupted() 检查是否被中断
     */
    public static void interruptParkDemo() {
        System.out.println("=== 中断 park 示例 ===");
        
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始 park");
            
            LockSupport.park();
            
            // 检查是否被中断
            if (Thread.interrupted()) {
                System.out.println(Thread.currentThread().getName() + " 是被中断唤醒的！");
            } else {
                System.out.println(Thread.currentThread().getName() + " 是正常唤醒的");
            }
        }, "中断测试线程");
        
        thread.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("主线程：中断子线程");
        thread.interrupt();  // 中断线程
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("演示完成\n");
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        System.out.println("LockSupport 使用示例演示\n");
        System.out.println("========================================\n");
        
        // 运行所有示例
        basicParkUnpark();
        permitDemo();
        orderMattersDemo();
        parkWithBlocker();
        timedParkDemo();
        interruptParkDemo();
        
        System.out.println("========================================");
        System.out.println("所有演示结束！");
    }
}
