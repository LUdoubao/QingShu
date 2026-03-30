package org.doubao.interview.agent.server.config.question044;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * 异步任务配置类
 * 
 * 【类注释】
 * 职责：配置@Async 使用的线程池和异常处理器
 * 边界：仅用于演示目的，实际生产环境需要根据业务需求调整参数
 * 线程安全：由 Spring 容器保证
 * 
 * 【面试知识点 - 问题 044】
 * 这个配置类展示了：
 * 1. 启用异步支持（@EnableAsync）
 * 2. 配置自定义线程池（避免使用默认的 SimpleAsyncTaskExecutor）
 * 3. 配置全局异常处理器（处理无返回值的异步方法异常）
 * 4. 实现 AsyncConfigurer 接口的高级用法
 * 
 * 【核心配置说明】
 * - corePoolSize: 核心线程数，决定了最小线程数量
 *   • 太小：任务会排队等待
 *   • 太大：浪费资源，增加上下文切换开销
 *   • 建议：CPU 密集型 = CPU 核数 + 1；IO 密集型 = CPU 核数 * 2
 * 
 * - maxPoolSize: 最大线程数，决定了线程池的上限
 *   • 当队列满时，会创建新线程直到达到最大值
 *   • 超过核心线程数的线程会被回收（keepAliveSeconds）
 * 
 * - queueCapacity: 队列容量，决定了能缓存多少任务
 *   • 太小：容易触发拒绝策略
 *   • 太大：内存占用高，任务响应慢
 *   • 建议：根据业务特点和内存情况设定
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Configuration
@EnableAsync  // 启用 Spring 的异步任务功能
public class AsyncConfig implements AsyncConfigurer {
    
    /**
     * 配置异步任务执行器（核心 Bean）
     * 
     * 【关键代码段注释】
     * 为什么要配置 ThreadPoolTaskExecutor？
     * 1. 默认情况下，@Async 使用 SimpleAsyncTaskExecutor
     * 2. SimpleAsyncTaskExecutor 每次调用都创建新线程
     * 3. 不重用线程，导致资源浪费和性能问题
     * 4. 没有线程数限制，可能导致系统崩溃
     * 
     * 配置了线程池后：
     * - 线程可重用，降低创建销毁开销
     * - 有最大线程数限制，保护系统资源
     * - 有队列缓冲任务，平滑流量峰值
     * - 有拒绝策略，防止系统过载
     * 
     * 【参数详解】
     * - corePoolSize: 5
     *   • 核心线程数，保持存活的最小线程数
     *   • 即使空闲也不会被回收（除非 allowCoreThreadTimeOut）
     *   • 适合中等规模的应用
     * 
     * - maxPoolSize: 20
     *   • 最大线程数，线程池的上限
     *   • 当队列满时，会创建新线程直到达到这个值
     *   • 超过核心线程数的线程会被回收
     * 
     * - queueCapacity: 100
     *   • 任务队列容量，用于缓冲待执行的任务
     *   • 当核心线程都在忙时，新任务进入队列
     *   • 队列满且线程数未达上限时，创建新线程
     * 
     * - keepAliveSeconds: 60
     *   • 非核心线程的空闲存活时间
     *   • 超过这个时间空闲，线程会被回收
     *   • 让线程池能够弹性应对流量波动
     * 
     * - threadNamePrefix: "async-task-"
     *   • 给线程起名字，方便日志排查
     *   • 例如：async-task-1, async-task-2
     * 
     * - rejectedExecutionHandler: CallerRunsPolicy
     *   • 当线程池满时的拒绝策略
     *   • CallerRunsPolicy: 由调用线程执行被拒绝的任务
     *   • 这是一种"反压"机制，降低提交速度
     * 
     * - waitForTasksToCompleteOnShutdown: true
     *   • 应用关闭时，等待任务执行完成
     *   • 防止数据不一致或资源未释放
     * 
     * - awaitTerminationSeconds: 60
     *   • 最多等待 60 秒
     *   • 超过时间强制关闭
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("[AsyncConfig] 初始化异步任务线程池...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：5
        // 计算公式参考：
        // - CPU 密集型：Runtime.getRuntime().availableProcessors() + 1
        // - IO 密集型：Runtime.getRuntime().availableProcessors() * 2
        executor.setCorePoolSize(5);
        
        // 最大线程数：20
        // 当队列满时，会创建新线程直到达到这个值
        executor.setMaxPoolSize(20);
        
        // 队列容量：100
        // 用于缓冲待执行的任务
        executor.setQueueCapacity(100);
        
        // 线程名前缀，方便日志追踪
        executor.setThreadNamePrefix("async-task-");
        
        // 空闲线程存活时间：60 秒
        // 超过核心线程数的线程，空闲 60 秒后会被回收
        executor.setKeepAliveSeconds(60);
        
        // 应用关闭时，等待任务执行完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 最多等待 60 秒
        executor.setAwaitTerminationSeconds(60);
        
        // 错误处理：任务抛出异常时的处理器
        executor.setErrorHandler(t -> {
            log.error("[AsyncConfig] 异步任务执行异常", t);
            // 这里可以添加额外的错误处理逻辑
            // 如：发送告警、记录数据库等
        });
        
        // 拒绝策略：当线程池满时的处理方式
        // CallerRunsPolicy: 由调用者线程执行，这是一种反压机制
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("[AsyncConfig] 线程池已满，使用 CallerRuns 策略");
            if (!e.isShutdown()) {
                r.run();
            }
        });
        
        // 是否允许核心线程超时
        // false: 核心线程即使空闲也不会被回收
        executor.setAllowCoreThreadTimeOut(false);
        
        executor.initialize();
        
        log.info("[AsyncConfig] 异步任务线程池初始化完成，core={}, max={}, queue={}", 
                executor.getCorePoolSize(), 
                executor.getMaxPoolSize(), 
                executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 配置全局异步异常处理器
     * 
     * 【关键代码段注释】
     * 什么时候使用这个处理器？
     * 1. 当@Async 方法的返回值是 void 时
     * 2. 方法内部抛出异常，无法通过 Future 获取
     * 3. 需要统一的异常处理逻辑
     * 
     * 如果返回 CompletableFuture：
     * - 异常会被包装在 Future 中
     * - 调用方通过 future.get() 可以捕获
     * - 不需要这个全局处理器
     * 
     * 【生产环境建议】
     * - 所有异步方法都应该有异常处理
     * - void 方法使用这个全局处理器
     * - 返回 Future 的方法在调用方处理
     * - 记录日志并告警
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
    
    /**
     * 自定义异步异常处理器
     * 
     * 【内部类注释】
     * 这是全局的异常处理器，处理所有 void 类型的@Async 方法
     */
    static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        /**
         * 处理异步方法抛出的异常
         * 
         * 【方法注释】
         * 输入约束：ex 不能为 null
         * 输出语义：记录异常信息，不重新抛出
         * 异常场景：无（这里是处理异常的最后一道防线）
         * 性能注意点：日志记录要快速，避免阻塞
         * 
         * @param ex 异常对象
         * @param method 抛出异常的方法
         * @param params 方法的参数
         */
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            // 【关键代码段注释】
            // 为什么不能重新抛出异常？
            // 1. 异步方法已经在新线程中执行完毕
            // 2. 调用方早就返回了，无法捕获这个异常
            // 3. 重新抛出也没有意义，只会让线程终止
            // 4. 最好的方式是记录日志并告警
            
            log.error(
                "[AsyncUncaughtExceptionHandler] 异步方法执行异常\n" +
                "  方法：{}.{}()\n" +
                "  参数：{}\n" +
                "  异常：{}\n" +
                "  堆栈：{}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                params != null ? params.length : 0,
                ex.getMessage(),
                ex.getStackTrace()
            );
            
            // TODO: 在实际项目中，这里可以添加更多处理逻辑
            // 1. 发送告警邮件/短信
            // 2. 记录到数据库异常表
            // 3. 通知运维人员
            // 4. 触发降级或熔断机制
        }
    }
}
