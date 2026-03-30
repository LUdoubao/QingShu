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

/**
 * @Async 使用注意点演示服务
 * 
 * 【类注释】
 * 职责：演示@Async 的常见陷阱和错误用法
 * 边界：仅用于学习和演示，展示哪些用法是错误的
 * 线程安全：展示了线程不安全的情况
 * 是否幂等：部分方法不是幂等的
 * 
 * 【面试知识点 - 问题 044】
 * 这个类展示了@Async 的三个关键注意点：
 * 1. 自调用问题：同类中方法互相调用时，@Async 不生效
 * 2. 异常处理问题：异步方法的异常不能默认忽略
 * 3. 线程池问题：必须配置合适的线程池
 * 
 * 【核心原理】
 * @Async 基于 Spring AOP 代理实现：
 * - 只有外部调用经过代理对象时，@Async 才生效
 * - 内部调用（this.xxx()）绕过代理，直接执行原方法
 * - 这就是"自调用失效"的根本原因
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class AsyncPitfallService {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 演示自调用问题（错误示例）
     * 
     * 【关键代码段注释】
     * 为什么@Async 不生效？
     * 1. callInternalMethod() 是外部调用的入口
     * 2. 它调用了 this.asyncMethod()（自调用）
     * 3. this.asyncMethod() 绕过了 Spring 代理
     * 4. 直接在当前线程执行，没有异步效果
     * 
     * 【调用链路】
     * Controller.call() → Proxy.callInternalMethod() → [代理生效]
     *   → this.asyncMethod() → RealObject.asyncMethod() → [不生效，同步执行]
     * 
     * 【解决方案】
     * 见下一个正确的示例
     */
    public String callInternalMethod(AsyncTaskRequest request) {
        log.info("[Pitfall-SelfCall] [线程：{}] 开始调用内部异步方法", 
                Thread.currentThread().getName());
        
        // ❌ 错误示范：在同一个类中调用@Async 方法
        // 这样调用不会走代理，@Async 不生效！
        asyncMethod(request);
        
        log.info("[Pitfall-SelfCall] [线程：{}] 调用完成（实际是同步执行的）", 
                Thread.currentThread().getName());
        
        return "调用完成（但注意：asyncMethod 是同步执行的！）";
    }
    
    /**
     * 被调用的异步方法（但因为自调用，实际不异步）
     */
    @Async
    public void asyncMethod(AsyncTaskRequest request) {
        log.info("[Pitfall-SelfCall-Async] [线程：{}] 执行异步方法", 
                Thread.currentThread().getName());
        
        try {
            Thread.sleep(request.getSleepTime());
            log.info("[Pitfall-SelfCall-Async] 异步方法执行完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Pitfall-SelfCall-Async] 任务被中断", e);
        }
    }
    
    /**
     * 正确的自调用解决方案
     * 
     * 【关键代码段注释】
     * 如何让自调用也支持异步？
     * 方案 1：注入自身 Bean（推荐）
     * 方案 2：使用 AopContext.currentProxy()
     * 方案 3：拆分到不同的 Service 中
     * 
     * 这里演示方案 1：注入自身
     */
    // 将在 BasicAsyncService 中演示正确方式
}
