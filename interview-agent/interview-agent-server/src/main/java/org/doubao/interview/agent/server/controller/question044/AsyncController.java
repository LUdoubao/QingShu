package org.doubao.interview.agent.server.controller.question044;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question044.AsyncTaskRequest;
import org.doubao.interview.agent.api.dto.question044.AsyncTaskResult;
import org.doubao.interview.agent.server.service.impl.question044.BasicAsyncService;
import org.doubao.interview.agent.server.service.impl.question044.AsyncPitfallService;
import org.doubao.interview.agent.server.service.impl.question044.AsyncSelfCallSolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Async 原理与使用注意点演示 Controller
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示@Async 的原理、正确用法和常见陷阱
 * 边界：仅用于学习和演示目的，不应用于生产环境
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * 【面试知识点 - 问题 044】
 * 这个 Controller 展示了：
 * 1. @Async 的基本用法和原理
 * 2. 自调用失效问题及解决方案
 * 3. 异常处理的正确方式
 * 4. 线程池配置的重要性
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question044")
public class AsyncController {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Autowired
    private BasicAsyncService basicAsyncService;
    
    @Autowired
    private AsyncPitfallService asyncPitfallService;
    
    @Autowired
    private AsyncSelfCallSolutionService asyncSelfCallSolutionService;
    
    /**
     * 获取@Async 底层原理解析
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回源码级的@Async 工作机制说明
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 详细的原理说明
     */
    @GetMapping("/principle")
    public String getPrinciple() {
        log.info("[AsyncController] 获取@Async 底层原理说明");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== @Async 底层工作原理（源码级解析）===\n\n");
        
        sb.append("【一、核心原理概述】\n");
        sb.append("@Async 基于 Spring AOP 代理模式实现异步方法调用。\n");
        sb.append("本质是：将方法调用提交到线程池，立即返回，不等待执行结果。\n\n");
        
        sb.append("【二、Spring 处理流程】\n\n");
        
        sb.append("步骤 1: 启用异步支持\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// 在配置类或启动类上添加\n");
        sb.append("@EnableAsync  // 关键注解！\n");
        sb.append("public class Application {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        SpringApplication.run(Application.class, args);\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("步骤 2: Bean 后处理器注册\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AnnotationConfigUtils.registerAnnotationConfigProcessors()\n");
        sb.append("if (!registry.containsBeanDefinition(ASYNC_ANNOTATION_PROCESSOR_BEAN_NAME)) {\n");
        sb.append("    RootBeanDefinition def = new RootBeanDefinition(\n");
        sb.append("        AsyncAnnotationBeanPostProcessor.class);\n");
        sb.append("    registry.registerBeanDefinition(\n");
        sb.append("        ASYNC_ANNOTATION_PROCESSOR_BEAN_NAME, def);\n");
        sb.append("}\n\n");
        
        sb.append("步骤 3: 创建代理对象（关键！）\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AbstractAdvisorAutoProxyCreator.postProcessAfterInitialization()\n");
        sb.append("@Override\n");
        sb.append("public Object postProcessAfterInitialization(Object bean, String beanName) {\n");
        sb.append("    if (bean instanceof Advised) {\n");
        sb.append("        // 检查 Bean 中是否有@Async 注解的方法\n");
        sb.append("        if (hasAsyncMethods(bean)) {\n");
        sb.append("            // 创建代理对象\n");
        sb.append("            return createProxy(bean);\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return bean;\n");
        sb.append("}\n\n");
        
        sb.append("步骤 4: 拦截方法调用\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AsyncAnnotationAdvisor.buildInterceptor()\n");
        sb.append("protected MethodInterceptor buildInterceptor() {\n");
        sb.append("    return new AnnotationAsyncExecutionInterceptor(\n");
        sb.append("        this.executor, this.exceptionHandler);\n");
        sb.append("}\n\n");
        
        sb.append("// AnnotationAsyncExecutionInterceptor.invoke()\n");
        sb.append("@Override\n");
        sb.append("public Object invoke(MethodInvocation invocation) throws Throwable {\n");
        sb.append("    // 获取@Async 注解\n");
        sb.append("    Async async = findAsyncAnnotation(invocation.getMethod());\n");
        sb.append("    \n");
        sb.append("    if (async != null) {\n");
        sb.append("        // 【核心】将方法调用提交到线程池\n");
        sb.append("        return submitCallable(invocation, async);\n");
        sb.append("    }\n");
        sb.append("    return invocation.proceed();\n");
        sb.append("}\n\n");
        
        sb.append("步骤 5: 提交到线程池执行\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AnnotationAsyncExecutionInterceptor.submitCallable()\n");
        sb.append("protected Object submitCallable(\n");
        sb.append("        MethodInvocation invocation, Async async) throws Exception {\n");
        sb.append("    \n");
        sb.append("    // 获取配置的线程池（Executor）\n");
        sb.append("    Executor executor = getExecutor(async);\n");
        sb.append("    \n");
        sb.append("    // 创建任务并.submit()\n");
        sb.append("    Future<?> future = executor.submit(() -> {\n");
        sb.append("        try {\n");
        sb.append("            // 执行目标方法\n");
        sb.append("            return invocation.proceed();\n");
        sb.append("        } catch (Throwable ex) {\n");
        sb.append("            // 处理异常\n");
        sb.append("            handleException(ex);\n");
        sb.append("        }\n");
        sb.append("    });\n");
        sb.append("    \n");
        sb.append("    // 立即返回（不等待结果）\n");
        sb.append("    return future;\n");
        sb.append("}\n\n");
        
        sb.append("【三、为什么自调用会失效？】\n\n");
        
        sb.append("错误示例：\n");
        sb.append("class Service {\n");
        sb.append("    public void method1() {\n");
        sb.append("        this.method2();  // ❌ 自调用，不经过代理\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    @Async\n");
        sb.append("    public void method2() { ... }\n");
        sb.append("}\n\n");
        
        sb.append("调用链路分析：\n");
        sb.append("1. 外部调用 method1() → Proxy.method1() [代理生效]\n");
        sb.append("2. method1() 内部调用 this.method2()\n");
        sb.append("3. this 指向目标对象（不是代理对象）\n");
        sb.append("4. 直接执行 method2()，不经过代理\n");
        sb.append("5. @Async 不生效，同步执行\n\n");
        
        sb.append("根本原因：\n");
        sb.append("- Spring AOP 是代理模式\n");
        sb.append("- 只有经过代理对象的调用，@Async 才生效\n");
        sb.append("- this.xxx() 绕过代理，直接调用目标对象\n");
        sb.append("- 这就是自调用失效的本质\n\n");
        
        sb.append("【四、解决方案】\n\n");
        
        sb.append("方案 1：注入自身 Bean（推荐）⭐\n");
        sb.append("class Service {\n");
        sb.append("    @Autowired\n");
        sb.append("    private Service self;  // 注入的是代理对象\n");
        sb.append("    \n");
        sb.append("    public void method1() {\n");
        sb.append("        self.method2();  // ✅ 通过代理调用\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    @Async\n");
        sb.append("    public void method2() { ... }\n");
        sb.append("}\n\n");
        
        sb.append("方案 2：使用 AopContext\n");
        sb.append("class Service {\n");
        sb.append("    public void method1() {\n");
        sb.append("        ((Service)AopContext.currentProxy())\n");
        sb.append("            .method2();  // ✅ 通过代理调用\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("// 需要配置：exposeProxy=true\n\n");
        
        sb.append("方案 3：拆分到不同 Service\n");
        sb.append("class ServiceA {\n");
        sb.append("    @Autowired\n");
        sb.append("    private ServiceB serviceB;\n");
        sb.append("    \n");
        sb.append("    public void method1() {\n");
        sb.append("        serviceB.method2();  // ✅ 跨 Bean 调用\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("class ServiceB {\n");
        sb.append("    @Async\n");
        sb.append("    public void method2() { ... }\n");
        sb.append("}\n\n");
        
        sb.append("【五、使用注意点总结】\n\n");
        
        sb.append("1. 必须配置线程池 ⚠️\n");
        sb.append("   - 默认使用 SimpleAsyncTaskExecutor\n");
        sb.append("   - 每次调用都创建新线程，不可控\n");
        sb.append("   - 必须自定义 ThreadPoolTaskExecutor\n\n");
        
        sb.append("2. 自调用失效 ⚠️\n");
        sb.append("   - 同类中互相调用不生效\n");
        sb.append("   - 必须通过代理对象调用\n\n");
        
        sb.append("3. 异常处理 ⚠️\n");
        sb.append("   - 异步方法的异常不会传播到调用方\n");
        sb.append("   - 必须显式捕获和处理\n");
        sb.append("   - 可以配置 AsyncUncaughtExceptionHandler\n\n");
        
        sb.append("4. 返回值限制 ⚠️\n");
        sb.append("   - 只能是 void 或 CompletableFuture<T>\n");
        sb.append("   - 不能返回其他类型（无法异步获取结果）\n\n");
        
        sb.append("5. 访问修饰符 ⚠️\n");
        sb.append("   - @Async 方法必须是 public 的\n");
        sb.append("   - protected、private 不生效\n\n");
        
        sb.append("6. static 方法 ⚠️\n");
        sb.append("   - static 方法不能使用@Async\n");
        sb.append("   - 静态方法不属于对象实例，代理无法拦截\n\n");
        
        return sb.toString();
    }
    
    /**
     * 演示基础异步用法
     */
    @PostMapping("/basic/execute")
    public String executeBasic(@Valid @RequestBody AsyncTaskRequest request) {
        log.info("[AsyncController] [线程：{}] 收到基础异步请求，message={}, sleepTime={}ms", 
                Thread.currentThread().getName(), request.getMessage(), request.getSleepTime());
        
        // 调用异步方法（立即返回）
        basicAsyncService.executeSimpleAsync(request);
        
        log.info("[AsyncController] 已提交异步任务，立即返回");
        
        return "异步任务已提交 | 线程：" + Thread.currentThread().getName() + 
               " | 请观察控制台日志";
    }
    
    /**
     * 演示返回 CompletableFuture 的异步用法
     */
    @PostMapping("/future/execute")
    public CompletableFuture<AsyncTaskResult> executeWithFuture(
            @Valid @RequestBody AsyncTaskRequest request) {
        
        log.info("[AsyncController] [线程：{}] 收到 Future 异步请求", 
                Thread.currentThread().getName());
        
        // 返回 CompletableFuture，调用者可以获取结果
        CompletableFuture<AsyncTaskResult> future = 
            basicAsyncService.executeWithFuture(request);
        
        log.info("[AsyncController] 已返回 CompletableFuture");
        
        return future;
    }
    
    /**
     * 演示带超时控制的异步用法
     */
    @PostMapping("/timeout/execute")
    public AsyncTaskResult executeWithTimeout(
            @Valid @RequestBody AsyncTaskRequest request) throws Exception {
        
        log.info("[AsyncController] [线程：{}] 收到超时控制异步请求", 
                Thread.currentThread().getName());
        
        long startTime = System.currentTimeMillis();
        
        // 获取异步结果（带超时）
        CompletableFuture<AsyncTaskResult> future = 
            basicAsyncService.executeWithTimeout(request);
        
        // 设置超时时间：实际耗时的 2 倍
        AsyncTaskResult result = future.get(request.getSleepTime() * 2, TimeUnit.MILLISECONDS);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("[AsyncController] 异步任务完成，总耗时={}ms", duration);
        
        return result;
    }
    
    /**
     * 演示自调用失效问题（错误示例）
     */
    @PostMapping("/pitfall/self-call")
    public String demonstrateSelfCallProblem(@Valid @RequestBody AsyncTaskRequest request) {
        log.info("[AsyncController] [线程：{}] 演示自调用问题（错误示例）", 
                Thread.currentThread().getName());
        
        String result = asyncPitfallService.callInternalMethod(request);
        
        log.info("[AsyncController] ❌ 注意：虽然调用了@Async 方法，但实际是同步执行的！");
        
        return result + "\n❌ 警告：这是错误的用法，asyncMethod 是同步执行的！";
    }
    
    /**
     * 演示正确的自调用解决方案
     */
    @PostMapping("/solution/self-call")
    public String demonstrateSelfCallSolution(@Valid @RequestBody AsyncTaskRequest request) {
        log.info("[AsyncController] [线程：{}] 演示正确的自调用解决方案", 
                Thread.currentThread().getName());
        
        String result = asyncSelfCallSolutionService.callCorrectly(request);
        
        log.info("[AsyncController] ✅ 正确！通过注入的 Bean 调用，@Async 生效");
        
        return result + "\n✅ 正确：通过注入的 Bean 调用，已异步执行！";
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        log.info("[AsyncController] 健康检查，当前时间={}", sdf.format(new Date()));
        return "Question044: @Async Principle - OK | Time: " + sdf.format(new Date());
    }
}
