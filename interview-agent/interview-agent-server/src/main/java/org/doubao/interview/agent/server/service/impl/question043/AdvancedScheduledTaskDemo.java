package org.doubao.interview.agent.server.service.impl.question043;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高级定时任务演示 - 异步和超时控制
 * 
 * 【类注释】
 * 职责：演示@Scheduled 的异步执行、超时控制和并发场景处理
 * 边界：仅用于学习和演示，实际业务中需要结合具体业务需求调整
 * 线程安全：使用原子变量和锁机制保证线程安全
 * 是否幂等：通过分布式锁保证幂等性
 * 
 * 【面试知识点 - 问题 043 扩展】
 * 这个类展示了生产环境中的关键考虑：
 * 1. 异步执行：避免长任务阻塞其他定时任务
 * 2. 超时控制：防止任务执行时间过长
 * 3. 并发控制：同一任务不允许多个实例同时执行
 * 4. 分布式锁：多实例部署时只在一个节点执行
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class AdvancedScheduledTaskDemo {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    // 任务计数器
    private AtomicLong asyncTaskCount = new AtomicLong(0);
    private AtomicLong timeoutTaskCount = new AtomicLong(0);
    
    // 任务执行标志（用于演示并发控制）
    private AtomicBoolean isTaskRunning = new AtomicBoolean(false);
    
    /**
     * 异步定时任务
     * 
     * 【关键代码段注释】
     * @Async 的作用：
     * 1. 将任务放到独立的线程池中执行
     * 2. 不会阻塞调度器的其他任务
     * 3. 需要配置线程池（否则使用默认的 SimpleAsyncTaskExecutor，不推荐）
     * 
     * 【为什么要异步？】
     * - 默认情况下，@Scheduled 任务是串行执行的
     * - 如果一个任务执行很慢，会影响后续所有任务
     * - 使用@Async 可以让多个任务并行执行
     * 
     * 【配置线程池】
     * 在配置类中添加：
     * @Bean(name = "taskScheduler")
     * public ThreadPoolTaskScheduler taskScheduler() {
     *     ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
     *     scheduler.setPoolSize(10);  // 线程池大小
     *     scheduler.setThreadNamePrefix("scheduled-task-");
     *     return scheduler;
     * }
     */
    @Async
    @Scheduled(fixedRate = 3000)
    public void executeAsyncTask() {
        long startTime = System.currentTimeMillis();
        asyncTaskCount.incrementAndGet();
        
        log.info("[AsyncTask] [线程：{}] 开始执行第 {} 次", 
                Thread.currentThread().getName(), asyncTaskCount.get());
        log.info("[AsyncTask] 当前时间：{}", sdf.format(new Date()));
        
        // 模拟耗时业务：5 秒（虽然固定频率是 3 秒，但异步执行不会阻塞）
        try {
            Thread.sleep(5000);
            log.info("[AsyncTask] 业务处理完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[AsyncTask] 任务被中断", e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("[AsyncTask] 执行完成，总耗时={}ms", duration);
    }
    
    /**
     * 带超时控制的任务
     * 
     * 【关键代码段注释】
     * 超时控制的实现方式：
     * 1. 使用 CompletableFuture 包装任务
     * 2. 设置超时时间（get(timeout, unit)）
     * 3. 超时时抛出 TimeoutException
     * 
     * 【生产环境的最佳实践】
     * - 所有定时任务都应该有超时控制
     * - 超时时间应该略小于任务间隔时间
     * - 超时时要记录日志并告警
     * - 考虑是否需要重试机制
     */
    @Scheduled(fixedRate = 10000)
    public void executeWithTimeout() {
        long startTime = System.currentTimeMillis();
        timeoutTaskCount.incrementAndGet();
        
        log.info("[TimeoutTask] 开始执行第 {} 次 (超时限制：8 秒)", timeoutTaskCount.get());
        
        try {
            // 使用 CompletableFuture 实现超时控制
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 模拟业务处理：可能很快，也可能很慢
                    int randomSleep = (int) (Math.random() * 12000); // 0-12 秒
                    log.info("[TimeoutTask] 模拟业务处理，预计耗时={}ms", randomSleep);
                    Thread.sleep(randomSleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // 设置超时时间：8 秒
            future.get(8, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[TimeoutTask] ✓ 执行成功，耗时={}ms (在 8 秒内完成)", duration);
            
        } catch (java.util.concurrent.TimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[TimeoutTask] ✗ 任务超时！已执行{}ms，超过 8 秒限制", duration, e);
            // 这里可以添加超时后的处理逻辑，如发送告警、记录数据库等
            
        } catch (Exception e) {
            log.error("[TimeoutTask] ✗ 执行异常", e);
        }
    }
    
    /**
     * 带并发控制的任务（单机版）
     * 
     * 【关键代码段注释】
     * 并发控制的必要性：
     * 1. 防止同一个任务有多个实例同时执行
     * 2. 特别是当任务执行时间 > 调度间隔时
     * 3. 使用 AtomicBoolean 实现轻量级锁
     * 
     * 【工作原理】
     * - 执行前尝试获取锁（compareAndSet）
     * - 如果已经有任务在运行，跳过本次执行
     * - 执行完成后释放锁
     * 
     * 【局限性】
     * - 只在单实例场景下有效
     * - 多实例部署时需要分布式锁（见下一个方法）
     */
    @Scheduled(fixedRate = 5000)
    public void executeWithConcurrencyControl() {
        log.info("[ConcurrencyControl] 尝试获取执行权限...");
        
        // 尝试获取锁：如果当前没有任务在运行，则设置为 true 并执行
        if (!isTaskRunning.compareAndSet(false, true)) {
            log.warn("[ConcurrencyControl] ⚠ 前一个任务还在执行，跳过本次调度");
            return; // 直接返回，不执行
        }
        
        try {
            log.info("[ConcurrencyControl] ✓ 获得执行权限，开始执行...");
            log.info("[ConcurrencyControl] 当前时间：{}", sdf.format(new Date()));
            
            // 模拟业务处理：3 秒
            Thread.sleep(3000);
            
            log.info("[ConcurrencyControl] 执行完成");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ConcurrencyControl] 任务被中断", e);
        } finally {
            // 无论成功失败，都要释放锁
            isTaskRunning.set(false);
            log.info("[ConcurrencyControl] 释放执行权限");
        }
    }
    
    /**
     * 带分布式锁的任务（多实例场景）
     * 
     * 【关键代码段注释】
     * 分布式锁的使用场景：
     * 1. 应用部署了多个实例（集群环境）
     * 2. 希望同一个任务在同一时间只有一个实例在执行
     * 3. 使用 Redis 实现分布式锁
     * 
     * 【实现方案】
     * - 基于 Redis SETNX 命令
     * - 设置过期时间防止死锁
     * - 使用 Lua 脚本保证原子性
     * 
     * 【生产环境建议】
     * - 使用 Redisson 等成熟框架
     * - 考虑锁的续期机制（WatchDog）
     * - 考虑主从切换时的锁安全性
     */
    @Scheduled(cron = "0/15 * * * * *")
    public void executeWithDistributedLock() throws UnknownHostException {
        String lockKey = "scheduled-task:question043:distributed-lock";
        String lockValue = getInstanceId(); // 当前实例的唯一标识
        
        log.info("[DistributedLock] 尝试获取分布式锁，lockKey={}, instanceId={}", 
                lockKey, lockValue);
        
        // 模拟获取分布式锁（实际项目中会使用 Redis）
        boolean lockAcquired = tryAcquireDistributedLock(lockKey, lockValue);
        
        if (!lockAcquired) {
            log.warn("[DistributedLock] ⚠ 未获取到分布式锁，可能是其他实例在执行，跳过本次");
            return;
        }
        
        try {
            log.info("[DistributedLock] ✓ 成功获取分布式锁，开始执行任务...");
            log.info("[DistributedLock] 当前时间：{}", sdf.format(new Date()));
            
            // 模拟业务处理
            Thread.sleep(2000);
            
            log.info("[DistributedLock] 任务执行完成");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DistributedLock] 任务被中断", e);
        } finally {
            // 释放分布式锁
            releaseDistributedLock(lockKey, lockValue);
            log.info("[DistributedLock] 已释放分布式锁");
        }
    }
    
    /**
     * 模拟获取分布式锁
     * 实际项目中应该使用 Redis 实现
     */
    private boolean tryAcquireDistributedLock(String lockKey, String lockValue) {
        // TODO: 实际项目中这里应该调用 Redis
        // 伪代码示例：
        // String script = 
        //   "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
        //   "  redis.call('expire', KEYS[1], 30); return 1; " +
        //   "else return 0; end";
        // Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
        // return result != null && result == 1;
        
        // 演示用：随机成功或失败
        boolean acquired = Math.random() > 0.3; // 70% 概率获取成功
        if (acquired) {
            log.info("[DistributedLock] (模拟) 成功获取锁");
        } else {
            log.info("[DistributedLock] (模拟) 获取锁失败");
        }
        return acquired;
    }
    
    /**
     * 模拟释放分布式锁
     */
    private void releaseDistributedLock(String lockKey, String lockValue) {
        // TODO: 实际项目中这里应该调用 Redis
        // 伪代码示例：
        // String script = 
        //   "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        //   "  return redis.call('del', KEYS[1]); else return 0; end";
        // redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
        
        log.info("[DistributedLock] (模拟) 释放锁");
    }
    
    /**
     * 获取当前实例的唯一标识
     */
    private String getInstanceId() throws UnknownHostException {
        // 实际项目中可以使用：IP+ 端口、UUID、机器名等
        return "instance-" + java.net.InetAddress.getLocalHost().getHostName() + 
               "-" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    }
}
