package org.doubao.interview.question016.demo;

import org.doubao.interview.question016.concurrent.SimpleCountDownLatch;
import org.doubao.interview.question016.concurrent.SimpleCyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

/**
 * CountDownLatch vs CyclicBarrier 对比演示
 * 
 * 【核心区别】
 * 1. 用途不同：
 *    - CountDownLatch：一个线程等待其他多个线程完成
 *    - CyclicBarrier：多个线程相互等待后一起继续
 * 
 * 2. 可复用性：
 *    - CountDownLatch：一次性，计数器归零后无法重置
 *    - CyclicBarrier：可重复使用，每代完成后自动重置
 * 
 * 3. 计数方向：
 *    - CountDownLatch：倒计时（递减到 0）
 *    - CyclicBarrier：正计时（递增到阈值）
 * 
 * 4. 触发时机：
 *    - CountDownLatch：最后一个 countDown() 触发
 *    - CyclicBarrier：最后一个 await() 触发
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class ComparisonDemo {
    
    /**
     * 对比场景：主线程等待子任务 vs 子任务相互等待
     * 
     * 【业务背景】
     * 同样的 3 个子任务，分别用两种方式实现：
     * 1. CountDownLatch：主线程等 3 个子任务
     * 2. CyclicBarrier：3 个子任务相互等待后一起通知主线程
     */
    public static void waitPatternComparison() throws InterruptedException, BrokenBarrierException {
        System.out.println("=== 对比一：等待模式比较 ===\n");
        
        // ========== 使用 CountDownLatch ==========
        System.out.println("[方式 A] CountDownLatch - 主线程等待子任务\n");
        
        SimpleCountDownLatch latch = new SimpleCountDownLatch(3);
        
        Runnable taskA = () -> {
            try {
                System.out.println("[任务 A] 执行中...");
                Thread.sleep(500);
                System.out.println("[任务 A] 完成 ✓");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        
        Runnable taskB = () -> {
            try {
                System.out.println("[任务 B] 执行中...");
                Thread.sleep(700);
                System.out.println("[任务 B] 完成 ✓");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        
        Runnable taskC = () -> {
            try {
                System.out.println("[任务 C] 执行中...");
                Thread.sleep(600);
                System.out.println("[任务 C] 完成 ✓");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        
        new Thread(taskA).start();
        new Thread(taskB).start();
        new Thread(taskC).start();
        
        System.out.println("[主线程] 等待所有任务完成...");
        latch.await();
        System.out.println("[主线程] ✓ 所有任务完成，继续执行\n");
        
        // ========== 使用 CyclicBarrier ==========
        System.out.println("[方式 B] CyclicBarrier - 任务相互等待后一起通知\n");
        
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(3, () -> {
            System.out.println("[汇总动作] ✓ 所有任务已就绪，统一处理！\n");
        });
        
        Runnable task1 = () -> {
            try {
                System.out.println("[任务 1] 执行中...");
                Thread.sleep(500);
                System.out.println("[任务 1] 完成，等待其他任务...");
                barrier.await();
                System.out.println("[任务 1] 继续执行后续工作");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        Runnable task2 = () -> {
            try {
                System.out.println("[任务 2] 执行中...");
                Thread.sleep(700);
                System.out.println("[任务 2] 完成，等待其他任务...");
                barrier.await();
                System.out.println("[任务 2] 继续执行后续工作");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        Runnable task3 = () -> {
            try {
                System.out.println("[任务 3] 执行中...");
                Thread.sleep(600);
                System.out.println("[任务 3] 完成，等待其他任务...");
                barrier.await();
                System.out.println("[任务 3] 继续执行后续工作");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);
        Thread t3 = new Thread(task3);
        
        t1.start();
        t2.start();
        t3.start();
        
        t1.join();
        t2.join();
        t3.join();
        
        System.out.println("\n[观察者] 所有任务已完成并继续\n");
    }
    
    /**
     * 对比场景：可复用性演示
     * 
     * 【测试目的】
     * 验证 CountDownLatch 的一次性和 CyclicBarrier 的可复用性
     */
    public static void reusabilityComparison() throws InterruptedException, BrokenBarrierException {
        System.out.println("=== 对比二：可复用性比较 ===\n");
        
        // ========== CountDownLatch（一次性） ==========
        System.out.println("[CountDownLatch] 演示一次性特性\n");
        
        SimpleCountDownLatch latch = new SimpleCountDownLatch(2);
        
        // 第一轮
        System.out.println("第一轮：");
        Thread l1 = new Thread(() -> {
            System.out.println("  [任务 L1] 执行");
            latch.countDown();
        });
        Thread l2 = new Thread(() -> {
            System.out.println("  [任务 L2] 执行");
            latch.countDown();
        });
        
        l1.start();
        l2.start();
        
        try {
            latch.await();
            System.out.println("  [主线程] ✓ 第一轮完成\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 尝试第二轮（需要重新创建实例）
        System.out.println("第二轮（需要新实例）：");
        SimpleCountDownLatch latch2 = new SimpleCountDownLatch(2);
        Thread l3 = new Thread(() -> {
            System.out.println("  [任务 L3] 执行");
            latch2.countDown();
        });
        Thread l4 = new Thread(() -> {
            System.out.println("  [任务 L4] 执行");
            latch2.countDown();
        });
        
        l3.start();
        l4.start();
        
        try {
            latch2.await();
            System.out.println("  [主线程] ✓ 第二轮完成（使用了新实例）\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // ========== CyclicBarrier（可复用） ==========
        System.out.println("[CyclicBarrier] 演示可复用特性\n");
        
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(2, () -> {
            System.out.println("  [汇总] 一代完成，准备下一代...\n");
        });
        
        // 第一代
        System.out.println("第一代：");
        Thread b1 = new Thread(() -> {
            try {
                System.out.println("  [线程 B1] 到达屏障");
                barrier.await();
                System.out.println("  [线程 B1] 继续执行");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Thread b2 = new Thread(() -> {
            try {
                System.out.println("  [线程 B2] 到达屏障");
                barrier.await();
                System.out.println("  [线程 B2] 继续执行");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        
        // 第二代（使用同一个 barrier）
        System.out.println("\n第二代（同一实例）：");
        Thread b3 = new Thread(() -> {
            try {
                System.out.println("  [线程 B3] 到达屏障");
                barrier.await();
                System.out.println("  [线程 B3] 继续执行");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Thread b4 = new Thread(() -> {
            try {
                System.out.println("  [线程 B4] 到达屏障");
                barrier.await();
                System.out.println("  [线程 B4] 继续执行");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        b3.start();
        b4.start();
        b3.join();
        b4.join();
        
        System.out.println("\n[观察者] ✓ CyclicBarrier 成功复用了！\n");
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   CountDownLatch vs CyclicBarrier      ║");
        System.out.println("║         对比演示                       ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        try {
            waitPatternComparison();
            reusabilityComparison();
            
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║   对比演示完成！                       ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            // 输出总结
            System.out.println("\n【核心区别总结】");
            System.out.println("┌─────────────────────────────────────────┐");
            System.out.println("│ CountDownLatch       │ CyclicBarrier    │");
            System.out.println("├──────────────────────┼──────────────────┤");
            System.out.println("│ 主线程等子任务       │ 子任务相互等待   │");
            System.out.println("│ 一次性使用           │ 可重复使用       │");
            System.out.println("│ 倒计时（递减到 0）    │ 正计时（到阈值）  │");
            System.out.println("│ 最后一个 countDown   │ 最后一个 await   │");
            System.out.println("│ 触发等待的线程       │ 触发所有线程     │");
            System.out.println("└─────────────────────────────────────────┘");
            
        } catch (InterruptedException e) {
            System.err.println("程序被中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            System.err.println("栅栏被破坏：" + e.getMessage());
        }
    }
}
