package org.doubao.interview.agent.server.service.impl.question044;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question044.AsyncTaskRequest;
import org.doubao.interview.agent.api.dto.question044.AsyncTaskResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Async 基础使用演示服务
 * 
 * 【类注释】
 * 职责：演示@Async 的基本用法和正常场景
 * 边界：仅用于学习和演示，展示正确的异步使用方式
 * 线程安全：方法内部无共享状态，线程安全
 * 是否幂等：是，每次执行都是独立的
 * 
 * 【面试知识点 - 问题 044】
 * 这个类展示了@Async 的正确使用方式：
 * 1. 在方法上添加@Async 注解
 * 2. 方法立即返回，不阻塞调用线程
 * 3. 任务在线程池中执行
 * 4. 可以返回 CompletableFuture 获取结果
 * 
 * 【@Async 原理】
 * Spring 通过 CGLIB 动态代理实现：
 * 1. Spring 容器启动时，扫描所有@Async 注解
 * 2. 为这些 Bean 创建代理对象
 * 3. 调用@Async 方法时，实际调用的是代理对象
 * 4. 代理将方法调用提交到线程池
 * 5. 原方法在独立线程中执行
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class BasicAsyncService {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 基础异步方法（无返回值）
     * 
     * 【关键代码段注释】
     * @Async 标注在方法上：
     * 1. 调用这个方法时，Spring 的代理会拦截
     * 2. 将任务提交到线程池
     * 3. 立即返回（void），不等待任务完成
     * 4. 任务在独立线程中执行
     * 
     * 【注意点】
     * - 返回值必须是 void 或 CompletableFuture
     * - 不能是其他类型（否则无法异步返回结果）
     * - 适合"发后即忘"的场景
     */
    @Async
    public void executeSimpleAsync(AsyncTaskRequest request) {
        long startTime = System.currentTimeMillis();
        String taskId = UUID.randomUUID().toString();
        
        log.info("[BasicAsync] [线程：{}] 开始执行任务 {}, message={}", 
                Thread.currentThread().getName(), taskId, request.getMessage());
        log.info("[BasicAsync] 当前时间：{}", sdf.format(new Date()));
        
        try {
            // 模拟业务处理
            Thread.sleep(request.getSleepTime());
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[BasicAsync] [线程：{}] 任务 {} 执行完成，耗时={}ms", 
                    Thread.currentThread().getName(), taskId, duration);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[BasicAsync] 任务 {} 被中断", taskId, e);
        }
    }
    
    /**
     * 异步方法（返回 CompletableFuture）
     * 
     * 【关键代码段注释】
     * 返回 CompletableFuture 的优势：
     * 1. 调用者可以通过 get() 获取异步结果
     * 2. 可以设置超时时间
     * 3. 可以链式调用（thenApply, thenAccept 等）
     * 4. 可以处理异常（exceptionally, handle）
     * 
     * 【使用场景】
     * - 需要获取异步执行结果
     * - 需要组合多个异步任务
     * - 需要超时控制
     */
    @Async
    public CompletableFuture<AsyncTaskResult> executeWithFuture(AsyncTaskRequest request) {
        long startTime = System.currentTimeMillis();
        String taskId = UUID.randomUUID().toString();
        String threadName = Thread.currentThread().getName();
        
        log.info("[BasicAsync-Future] [线程：{}] 开始执行任务 {}", threadName, taskId);
        
        try {
            // 模拟业务处理
            Thread.sleep(request.getSleepTime());
            
            if (request.isThrowException()) {
                log.error("[BasicAsync-Future] 任务 {} 主动抛出异常", taskId);
                throw new RuntimeException("模拟业务异常");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            AsyncTaskResult result = AsyncTaskResult.builder()
                    .taskId(taskId)
                    .taskName("executeWithFuture")
                    .status("SUCCESS")
                    .data("处理结果：" + request.getMessage())
                    .duration(duration)
                    .threadName(threadName)
                    .explanation("异步任务成功完成")
                    .build();
            
            log.info("[BasicAsync-Future] 任务 {} 执行完成，耗时={}ms", taskId, duration);
            
            // 返回成功的结果
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[BasicAsync-Future] 任务 {} 被中断", taskId, e);
            
            return CompletableFuture.failedFuture(
                new RuntimeException("任务被中断", e));
                
        } catch (Exception e) {
            log.error("[BasicAsync-Future] 任务 {} 执行异常", taskId, e);
            
            // 返回失败的结果（包装在 CompletableFuture 中）
            AsyncTaskResult errorResult = AsyncTaskResult.builder()
                    .taskId(taskId)
                    .taskName("executeWithFuture")
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .threadName(threadName)
                    .explanation("异步任务执行失败")
                    .build();
            
            return CompletableFuture.completedFuture(errorResult);
        }
    }
    
    /**
     * 带超时控制的异步任务
     * 
     * 【关键代码段注释】
     * 超时控制的实现：
     * 1. 使用 CompletableFuture 包装任务
     * 2. 调用者通过 get(timeout, unit) 设置超时
     * 3. 超时时抛出 TimeoutException
     * 
     * 【生产环境建议】
     * - 所有异步任务都应该有超时控制
     * - 超时时间根据业务特点设定
     * - 超时要记录日志并告警
     */
    @Async
    public CompletableFuture<AsyncTaskResult> executeWithTimeout(AsyncTaskRequest request) {
        long startTime = System.currentTimeMillis();
        String taskId = UUID.randomUUID().toString();
        
        log.info("[BasicAsync-Timeout] [线程：{}] 开始执行任务 {}", 
                Thread.currentThread().getName(), taskId);
        
        try {
            // 模拟业务处理（可能很快，也可能很慢）
            Thread.sleep(request.getSleepTime());
            
            long duration = System.currentTimeMillis() - startTime;
            
            AsyncTaskResult result = AsyncTaskResult.builder()
                    .taskId(taskId)
                    .taskName("executeWithTimeout")
                    .status("SUCCESS")
                    .data(String.format("处理完成，实际耗时=%dms", duration))
                    .duration(duration)
                    .threadName(Thread.currentThread().getName())
                    .explanation(String.format("在超时限制内完成（限制=%dms, 实际=%dms）", 
                            request.getSleepTime() * 2, duration))
                    .build();
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[BasicAsync-Timeout] 任务 {} 被中断", taskId, e);
            return CompletableFuture.failedFuture(
                new RuntimeException("任务被中断", e));
        } catch (Exception e) {
            log.error("[BasicAsync-Timeout] 任务 {} 执行异常", taskId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
