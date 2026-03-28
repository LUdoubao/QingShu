package org.doubao.interview.question016.demo;

import org.doubao.interview.question016.concurrent.SimpleCyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

/**
 * CyclicBarrier 使用示例 - 演示循环栅栏的应用场景
 * 
 * 【典型场景】
 * 1. 多线程相互等待后再一起继续
 * 2. 并行计算的多阶段处理
 * 3. 分布式模拟的同步起点
 * 
 * @author interview-code
 * @date 2026-03-28
 */
public class CyclicBarrierDemo {
    
    /**
     * 场景一：多线程相互等待后一起继续
     * 
     * 【业务背景】
     * 4 个玩家玩 multiplayer 游戏，需要等所有人都准备好后才开始游戏
     * 
     * 【实现方式】
     * 1. 创建 CyclicBarrier(4)
     * 2. 每个玩家准备完成后调用 await()
     * 3. 最后一个玩家到达时，所有人同时开始
     */
    public static void multiPlayerGame() throws InterruptedException, BrokenBarrierException {
        System.out.println("=== 场景一：多人游戏等待开始 ===\n");
        
        // 创建栅栏，需要等待 4 个玩家
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(4, () -> {
            System.out.println("[系统] ✓ 所有玩家已就绪，游戏开始！🎮\n");
        });
        
        // 创建 4 个玩家线程
        for (int i = 1; i <= 4; i++) {
            final int playerId = i;
            Thread playerThread = new Thread(() -> {
                try {
                    System.out.println("[玩家" + playerId + "] 进入房间，准备中...");
                    
                    // 模拟不同的准备时间
                    Thread.sleep((long) (Math.random() * 1000 + 500));
                    System.out.println("[玩家" + playerId + "] 准备完成，等待其他玩家...");
                    
                    // 等待其他玩家
                    int index = barrier.await();
                    System.out.println("[玩家" + playerId + "] （索引：" + index + "）游戏开始，出发！🏃");
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.err.println("[玩家" + playerId + "] 异常：" + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, "Player-" + i);
            
            playerThread.start();
        }
        
        // 主线程等待所有玩家开始游戏
        Thread.sleep(3000);
        System.out.println("\n[观察者] 所有玩家已经开始游戏！");
    }
    
    /**
     * 场景二：并行计算的多阶段处理
     * 
     * 【业务背景】
     * 矩阵运算分为 3 个阶段，每个阶段 5 个线程并行计算
     * 必须等所有线程完成当前阶段后才能进入下一阶段
     * 
     * 【实现方式】
     * 1. 创建 CyclicBarrier(5)
     *      * 2. 每个线程完成一个阶段后 await()
     *      * 3. 所有线程到达后一起进入下一阶段
     */
    public static void multiStageComputation() throws InterruptedException, BrokenBarrierException {
        System.out.println("\n=== 场景二：多阶段并行计算 ===\n");
        
        final int THREADS = 5;
        final int STAGES = 3;
        
        // 创建栅栏，5 个线程相互等待
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(THREADS);
        
        // 用于记录当前阶段（实际应该用原子变量，这里简化）
        final int[] currentStage = {1};
        
        // 创建 5 个工作线程
        for (int i = 0; i < THREADS; i++) {
            final int workerId = i;
            Thread workerThread = new Thread(() -> {
                try {
                    for (int stage = 1; stage <= STAGES; stage++) {
                        System.out.println("[工人" + workerId + "] 阶段 " + stage + " - 开始计算...");
                        
                        // 模拟计算耗时
                        Thread.sleep((long) (Math.random() * 300 + 100));
                        System.out.println("[工人" + workerId + "] 阶段 " + stage + " - 计算完成");
                        
                        // 等待其他工人
                        int index = barrier.await();
                        System.out.println("[工人" + workerId + "] 阶段 " + stage + " - 已就绪（索引：" + index + "）");
                    }
                    
                    System.out.println("[工人" + workerId + "] ✓ 所有阶段完成！");
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.err.println("[工人" + workerId + "] 异常：" + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, "Worker-" + i);
            
            workerThread.start();
        }
        
        // 主线程等待所有计算完成
        Thread.sleep(4000);
        System.out.println("\n[主管] ✓ 所有工人的所有阶段计算完成！");
    }
    
    /**
     * 场景三：分布式模拟 - 赛跑比赛
     * 
     * 【业务背景】
     * 模拟田径比赛，所有选手必须在起跑线等待，发令枪响后同时起跑
     * 
     * 【实现方式】
     * 1. 创建 CyclicBarrier(6)
     * 2. 最后一个到达的线程执行发令动作
     * 3. 所有选手同时起跑
     */
    public static void runningRace() throws InterruptedException, BrokenBarrierException {
        System.out.println("\n=== 场景三：田径赛跑模拟 ===\n");
        
        // 创建栅栏，6 个选手，最后一个到达时发令
        SimpleCyclicBarrier barrier = new SimpleCyclicBarrier(6, () -> {
            System.out.println("\n🔫 [发令员] 各就各位，预备——跑！！！\n");
        });
        
        // 创建 6 个选手线程
        String[] runners = {"博尔特", "加特林", "布雷克", "鲍威尔", "盖伊", "苏炳添"};
        
        for (String runner : runners) {
            Thread runnerThread = new Thread(() -> {
                try {
                    System.out.println("[" + runner + "] 进入起跑器，准备起跑...");
                    
                    // 模拟不同的准备时间
                    Thread.sleep((long) (Math.random() * 800 + 200));
                    System.out.println("[" + runner + "] 准备完毕，等待发令...");
                    
                    // 等待发令（屏障动作会在所有线程到达后自动执行）
                    int index = barrier.await();
                    
                    // 起跑
                    System.out.println("[" + runner + "] （道次：" + index + "）冲出起跑线！💨");
                    
                    // 模拟跑步过程
                    Thread.sleep((long) (Math.random() * 500 + 100));
                    System.out.println("[" + runner + "] 冲过终点线！🏁");
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.err.println("[" + runner + "] 异常：" + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, "Runner-" + runner);
            
            runnerThread.start();
        }
        
        // 主线程等待比赛结束
        Thread.sleep(3000);
        System.out.println("\n[裁判长] 比赛结束，成绩有效！");
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   CyclicBarrier 使用示例演示           ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        try {
            // 运行三个示例场景
            multiPlayerGame();
            multiStageComputation();
            runningRace();
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   所有示例演示完成！                   ║");
            System.out.println("╚════════════════════════════════════════╝");
        } catch (InterruptedException e) {
            System.err.println("程序被中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            System.err.println("栅栏被破坏：" + e.getMessage());
        }
    }
}
