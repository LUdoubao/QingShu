package org.doubao.interview.agent.server.config.question043;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务配置类
 * 
 * 【类注释】
 * 职责：配置 Spring 定时任务的线程池和调度器
 * 边界：仅用于演示目的，实际生产环境需要根据业务需求调整参数
 * 线程安全：由 Spring 容器保证
 * 
 * 【面试知识点 - 问题 043】
 * 这个配置类展示了：
 * 1. 启用定时任务支持（@EnableScheduling）
 * 2. 启用异步任务支持（@EnableAsync）
 * 3. 配置专用的线程池（避免使用默认的 SimpleAsyncTaskExecutor）
 * 4. 合理的线程池大小设置
 * 
 * 【核心配置说明】
 * - poolSize: 线程池大小，决定了能同时执行多少个任务
 *   • 太小：任务会排队等待
 *   • 太大：浪费资源，增加上下文切换开销
 *   • 建议：CPU 密集型 = CPU 核数 + 1；IO 密集型 = CPU 核数 * 2
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Configuration
@EnableScheduling  // 启用 Spring 的定时任务功能
@EnableAsync       // 启用 Spring 的异步任务功能
public class ScheduledTaskConfig {
    
    /**
     * 配置定时任务线程池
     * 
     * 【关键代码段注释】
     * 为什么要配置 ThreadPoolTaskScheduler？
     * 1. 默认情况下，Spring 使用 TaskScheduler 接口
     * 2. 如果不显式配置，会使用默认的调度器（只有一个线程）
     * 3. 所有@Scheduled 任务都是串行执行的
     * 4. 如果一个任务很慢，会阻塞其他所有任务
     * 
     * 配置了线程池后：
     * - 多个任务可以并行执行
     * - 特别是使用了@Async 的任务
     * - 提高整体执行效率
     * 
     * 【参数详解】
     * - poolSize: 10
     *   • 表示线程池中有 10 个线程
     *   • 可以同时执行 10 个任务
     *   • 适合中等规模的应用
     * 
     * - threadNamePrefix: "scheduled-task-"
     *   • 给线程起名字，方便日志排查
     *   • 例如：scheduled-task-1, scheduled-task-2
     * 
     * - waitForTasksToCompleteOnShutdown: true
     *   • 应用关闭时，等待任务执行完成
     *   • 防止数据不一致或资源未释放
     * 
     * - awaitTerminationSeconds: 60
     *   • 最多等待 60 秒
     *   • 超过时间强制关闭
     * 
     * - rejectedExecutionHandler: CallerRunsPolicy
     *   • 当线程池满时的拒绝策略
     *   • CallerRunsPolicy: 由调用线程执行被拒绝的任务
     *   • 这是一种"反压"机制，降低提交速度
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        log.info("[ScheduledTaskConfig] 初始化定时任务线程池...");
        
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 线程池大小：10 个线程
        // 可以根据实际情况调整：
        // - CPU 密集型任务：Runtime.getRuntime().availableProcessors() + 1
        // - IO 密集型任务：Runtime.getRuntime().availableProcessors() * 2
        scheduler.setPoolSize(10);
        
        // 线程名前缀，方便日志追踪
        scheduler.setThreadNamePrefix("scheduled-task-");
        
        // 应用关闭时，等待任务执行完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 最多等待 60 秒
        scheduler.setAwaitTerminationSeconds(60);
        
        // 错误处理：任务抛出异常时的处理器
        scheduler.setErrorHandler(t -> {
            log.error("[ScheduledTaskConfig] 定时任务执行异常", t);
            // 这里可以添加额外的错误处理逻辑
            // 如：发送告警、记录数据库等
        });
        
        // 拒绝策略：当线程池满时的处理方式
        // CallerRunsPolicy: 由调用者线程执行，这是一种反压机制
        scheduler.setRejectedExecutionHandler((r, executor) -> {
            log.warn("[ScheduledTaskConfig] 线程池已满，使用 CallerRuns 策略");
            if (!executor.isShutdown()) {
                r.run();
            }
        });
        
        scheduler.initialize();
        
        log.info("[ScheduledTaskConfig] 定时任务线程池初始化完成，poolSize={}", 
                scheduler.getPoolSize());
        
        return scheduler;
    }
    
    /**
     * 配置异步任务执行器（可选）
     * 
     * 【说明】
     * 如果不配置这个 Bean，@Async 会使用默认的 SimpleAsyncTaskExecutor
     * SimpleAsyncTaskExecutor 每次都会创建新线程，不推荐在生产环境使用
     * 
     * 注意：上面已经配置了 taskScheduler，它也可以用于@Async
     * 如果想为@Async 单独配置不同的线程池，可以使用这个 Bean
     */
    /*
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");
        
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 等待任务全部完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        log.info("[ScheduledTaskConfig] 异步任务执行器初始化完成");
        
        return executor;
    }
    */
}
