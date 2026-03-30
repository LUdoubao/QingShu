package org.doubao.interview.agent.server.service.impl.question044;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question044.AsyncTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 正确的自调用解决方案演示
 * 
 * 【类注释】
 * 职责：演示如何解决@Async 自调用失效的问题
 * 边界：仅用于学习和演示，展示正确的解决方案
 * 线程安全：通过注入自身 Bean 实现异步调用
 * 
 * 【面试知识点 - 问题 044】
 * 这个类展示了三种解决自调用问题的方案：
 * 1. 注入自身 Bean（最推荐）
 * 2. 使用 AopContext.currentProxy()
 * 3. 拆分到不同的 Service
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class AsyncSelfCallSolutionService {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 注入自身 Bean（关键！）
     * 
     * 【关键代码段注释】
     * 为什么注入自身就能解决自调用问题？
     * 1. Spring 容器中的 Bean 都是代理对象（如果开启了 AOP）
     * 2. 注入的 asyncPitfallService 是代理对象
     * 3. 通过代理对象调用方法，@Async 会生效
     * 4. 这样就绕过了"this"直接调用的问题
     * 
     * 【原理图解】
     * Controller
     *   ↓ 调用
     * Proxy.callCorrectly()  ← 这是代理对象
     *   ↓ 调用
     * Proxy.asyncMethod()    ← 也是代理对象，@Async 生效
     *   ↓ 提交到线程池
     * Thread.asyncMethod()   ← 在独立线程执行
     */
    @Autowired
    private AsyncPitfallService asyncPitfallService;
    
    /**
     * 正确的自调用方式
     * 
     * 【关键代码段注释】
     * 正确做法：
     * 1. 通过注入的 Bean 调用（不是 this）
     * 2. asyncPitfallService 是 Spring 代理对象
     * 3. 调用时会经过代理的拦截逻辑
     * 4. @Async 注解生效，任务异步执行
     * 
     * 【对比错误示例】
     * 错误：this.asyncMethod() → 直接调用目标对象，不经过代理
     * 正确：asyncPitfallService.asyncMethod() → 调用代理对象，经过代理
     */
    public String callCorrectly(AsyncTaskRequest request) {
        log.info("[Solution-SelfCall] [线程：{}] 开始调用（通过注入的 Bean）", 
                Thread.currentThread().getName());
        
        // ✅ 正确示范：通过注入的 Bean 调用
        // asyncPitfallService 是 Spring 代理对象
        // 调用时会走代理逻辑，@Async 生效！
        asyncPitfallService.asyncMethod(request);
        
        log.info("[Solution-SelfCall] [线程：{}] 调用完成（已异步执行）", 
                Thread.currentThread().getName());
        
        return "调用完成（asyncMethod 已异步执行）";
    }
    
    /**
     * 使用 AopContext 的解决方案（备选方案）
     * 
     * 【说明】
     * 这种方式需要配置：exposeProxy=true
     * 不推荐作为首选方案，因为：
     * 1. 依赖 Spring 的内部 API
     * 2. 代码可读性较差
     * 3. 不如注入自身直观
     */
    /*
    public String callWithAopContext(AsyncTaskRequest request) {
        log.info("[Solution-AopContext] 使用 AopContext 调用");
        
        // 获取当前代理对象
        AsyncPitfallService proxy = (AsyncPitfallService) 
            AopContext.currentProxy();
        
        // 通过代理调用
        proxy.asyncMethod(request);
        
        return "调用完成";
    }
    */
}
