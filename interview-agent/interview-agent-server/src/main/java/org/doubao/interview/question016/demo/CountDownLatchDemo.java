package org.doubao.interview.question016.demo;

import org.doubao.interview.question016.concurrent.SimpleCountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch 使用示例 - 演示倒计时门闩的应用场景
 * 
 * 【典型场景】
 * 1. 主线程等待多个子任务完成
 * 2. 并行计算中等待所有分片完成
 * 3. 服务启动时等待依赖初始化
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class CountDownLatchDemo {
    
    /**
     * 场景一：主线程等待多个子任务完成
     * 
     * 【业务背景】
     * 主线程需要等待 3 个子任务（下载数据、解析文件、初始化数据库）全部完成后才能继续
     * 
     * 【实现方式】
     * 1. 创建 CountDownLatch(3)
     * 2. 每个子任务完成后调用 countDown()
     * 3. 主线程调用 await() 等待
     */
    public static void mainThreadWaitForSubTasks() throws InterruptedException {
        System.out.println("=== 场景一：主线程等待多个子任务 ===\n");
        
        // 创建倒计时门闩，需要等待 3 个子任务
        SimpleCountDownLatch latch = new SimpleCountDownLatch(3);
        
        // 子任务 1：下载数据
        Thread downloadTask = new Thread(() -> {
            System.out.println("[下载任务] 开始下载数据...");
            try {
                Thread.sleep(1500);  // 模拟下载耗时
                System.out.println("[下载任务] 数据下载完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();  // 完成任务，倒计时
                System.out.println("[下载任务] 已通知主任务（剩余：" + latch.getCount() + "）");
            }
        }, "Download-Thread");
        
        // 子任务 2：解析文件
        Thread parseTask = new Thread(() -> {
            System.out.println("[解析任务] 开始解析文件...");
            try {
                Thread.sleep(1000);  // 模拟解析耗时
                System.out.println("[解析任务] 文件解析完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
                System.out.println("[解析任务] 已通知主任务（剩余：" + latch.getCount() + "）");
            }
        }, "Parse-Thread");
        
        // 子任务 3：初始化数据库
        Thread dbInitTask = new Thread(() -> {
            System.out.println("[数据库任务] 开始初始化连接...");
            try {
                Thread.sleep(800);  // 模拟初始化耗时
                System.out.println("[数据库任务] 数据库初始化完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
                System.out.println("[数据库任务] 已通知主任务（剩余：" + latch.getCount() + "）");
            }
        }, "DB-Init-Thread");
        
        // 启动所有子任务
        downloadTask.start();
        parseTask.start();
        dbInitTask.start();
        
        // 主线程等待所有子任务完成
        System.out.println("[主线程] 等待所有子任务完成...");
        System.out.println("[主线程] 当前倒计时：" + latch.getCount());
        latch.await();  // 阻塞直到 count=0
        
        System.out.println("\n[主线程] ✓ 所有子任务已完成，主线程继续执行！");
        System.out.println("[主线程] 最终倒计时：" + latch.getCount());
    }
    
    /**
     * 场景二：并行计算中等待所有分片完成
     * 
     * 【业务背景】
     * 大数据处理中，将数据分成 5 个分片并行处理，全部处理完成后汇总结果
     * 
     * 【实现方式】
     * 1. 创建 CountDownLatch(5)
     * 2. 每个分片处理完成后 countDown()
     * 3. 主线程等待并汇总结果
     */
    public static void parallelComputation() throws InterruptedException {
        System.out.println("\n=== 场景二：并行计算等待所有分片 ===\n");
        
        final int[] results = new int[5];  // 存储各分片结果
        SimpleCountDownLatch latch = new SimpleCountDownLatch(5);
        
        // 创建 5 个分片处理任务
        for (int i = 0; i < 5; i++) {
            final int shardIndex = i;
            Thread shardTask = new Thread(() -> {
                System.out.println("[分片" + shardIndex + "] 开始处理数据...");
                try {
                    // 模拟不同耗时的计算
                    Thread.sleep((shardIndex + 1) * 200);
                    results[shardIndex] = (shardIndex + 1) * 100;
                    System.out.println("[分片" + shardIndex + "] 处理完成，结果：" + results[shardIndex]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                    System.out.println("[分片" + shardIndex + "] 已完工（剩余：" + latch.getCount() + "）");
                }
            }, "Shard-" + i);
            
            shardTask.start();
        }
        
        // 主线程等待所有分片完成
        System.out.println("[主线程] 等待所有分片处理完成...");
        latch.await();
        
        // 汇总结果
        int total = 0;
        for (int result : results) {
            total += result;
        }
        
        System.out.println("\n[主线程] ✓ 所有分片处理完成！");
        System.out.println("[主线程] 汇总结果：" + total);
    }
    
    /**
     * 场景三：带超时的等待
     * 
     * 【业务背景】
     * 防止某个任务卡死导致主线程无限等待，设置超时时间
     * 
     * 【实现方式】
     * 使用 await(timeout, unit) 方法
     */
    public static void timeoutWait() throws InterruptedException {
        System.out.println("\n=== 场景三：带超时的等待 ===\n");
        
        SimpleCountDownLatch latch = new SimpleCountDownLatch(3);
        
        // 正常任务
        Thread normalTask = new Thread(() -> {
            System.out.println("[正常任务] 执行中...");
            try {
                Thread.sleep(500);
                System.out.println("[正常任务] 完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }, "Normal-Task");
        
        // 慢任务（可能超时）
        Thread slowTask = new Thread(() -> {
            System.out.println("[慢任务] 开始执行（预计超时）...");
            try {
                Thread.sleep(3000);  // 超过超时时间
                System.out.println("[慢任务] 完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }, "Slow-Task");
        
        // 另一个正常任务
        Thread anotherTask = new Thread(() -> {
            System.out.println("[另一任务] 执行中...");
            try {
                Thread.sleep(600);
                System.out.println("[另一任务] 完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }, "Another-Task");
        
        normalTask.start();
        slowTask.start();
        anotherTask.start();
        
        // 等待 2 秒（超时）
        System.out.println("[主线程] 等待任务完成（最多 2 秒）...");
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        
        if (completed) {
            System.out.println("[主线程] ✓ 所有任务按时完成");
        } else {
            System.out.println("[主线程] ⚠ 等待超时！部分任务未完成（剩余：" + latch.getCount() + "）");
        }
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   CountDownLatch 使用示例演示          ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        try {
            // 运行三个示例场景
            mainThreadWaitForSubTasks();
            parallelComputation();
            timeoutWait();
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   所有示例演示完成！                   ║");
            System.out.println("╚════════════════════════════════════════╝");
        } catch (InterruptedException e) {
            System.err.println("程序被中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
