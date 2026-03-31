package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK 动态代理工厂 - 代理模式的核心实现
 * 
 * 模拟 Spring AOP 的 JdkDynamicAopProxy
 * 职责：为目标对象创建代理，植入横切逻辑（日志、事务、权限等）
 * 
 * 设计要点：
 * 1. 基于 JDK 动态代理，目标类必须实现接口
 * 2. 通过 InvocationHandler 拦截方法调用
 * 3. 在方法执行前后添加横切逻辑
 * 4. 不修改目标对象代码，符合开闭原则
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class JdkProxyFactory {
    
    private static final Logger log = LoggerFactory.getLogger(JdkProxyFactory.class);
    
    /**
     * 为目标对象创建代理实例
     * 
     * @param target 目标对象（需要被代理的真实对象）
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        log.info("开始为对象创建代理：{}", target.getClass().getName());
        
        // 获取目标对象的 ClassLoader 和接口
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();
        
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(
                "JDK 动态代理要求目标类必须实现至少一个接口：" + targetClass.getName());
        }
        
        log.info("目标类实现的接口：");
        for (Class<?> iface : interfaces) {
            log.info("  - {}", iface.getName());
        }
        
        // 创建 InvocationHandler（代理逻辑的核心处理器）
        InvocationHandler handler = new ProxyInvocationHandler(target);
        
        // 使用 JDK 反射 API 生成代理类
        // Proxy.newProxyInstance 会动态生成一个实现了所有接口的代理类
        Object proxyInstance = Proxy.newProxyInstance(
            targetClass.getClassLoader(),  // 类加载器
            interfaces,                     // 要实现的接口数组
            handler                         // 调用处理器
        );
        
        log.info("代理对象创建成功：{}", proxyInstance.getClass().getName());
        return (T) proxyInstance;
    }
    
    /**
     * 代理调用处理器（内部类）
     * 
     * 负责拦截所有方法调用，并在前后添加横切逻辑
     */
    private static class ProxyInvocationHandler implements InvocationHandler {
        
        /**
         * 目标对象（真实的业务对象）
         */
        private final Object target;
        
        public ProxyInvocationHandler(Object target) {
            this.target = target;
        }
        
        /**
         * 拦截方法调用
         * 
         * @param proxy 代理对象本身
         * @param method 被调用的方法
         * @param args 方法参数
         * @return 方法返回值
         * @throws Throwable 方法执行异常
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            log.info("========== 代理方法调用开始 ==========");
            log.info("方法名：{}", methodName);
            log.info("参数：{}", args != null ? args.length : 0);
            
            // ========== 前置增强（Before Advice）==========
            // 在方法执行前执行的横切逻辑
            beforeMethod(method, args);
            
            long startTime = System.currentTimeMillis();
            Object result = null;
            
            try {
                // ========== 核心业务逻辑 ==========
                // 通过反射调用目标对象的方法
                result = method.invoke(target, args);
                
                // ========== 返回后增强（After Returning Advice）==========
                afterReturning(method, result);
                
            } catch (Exception e) {
                // ========== 异常增强（Throws Advice）==========
                // 捕获异常并处理
                log.error("方法执行异常：{}, error={}", methodName, e.getMessage());
                afterThrowing(method, e);
                throw e.getCause() != null ? e.getCause() : e;
            } finally {
                // ========== 最终增强（After Advice）==========
                // 无论是否异常都会执行
                long endTime = System.currentTimeMillis();
                afterFinally(method, endTime - startTime);
            }
            
            log.info("========== 代理方法调用结束 ==========");
            return result;
        }
        
        /**
         * 前置通知：方法执行前调用
         * 
         * 模拟 Spring 的 BeforeAdvice
         * 应用场景：日志记录、参数校验、权限检查
         */
        private void beforeMethod(Method method, Object[] args) {
            log.info("[前置通知] 方法 {} 即将执行", method.getName());
            
            // 模拟权限检查
            if ("deleteOrder".equals(method.getName())) {
                log.info("[权限检查] 验证用户是否有删除权限...");
                // 实际场景中这里会检查用户角色
            }
            
            // 模拟事务开启
            if ("createOrder".equals(method.getName())) {
                log.info("[事务管理] 开启数据库事务...");
            }
        }
        
        /**
         * 返回后通知：方法正常返回后调用
         * 
         * 模拟 Spring 的 AfterReturningAdvice
         * 应用场景：结果缓存、日志记录
         */
        private void afterReturning(Method method, Object result) {
            log.info("[返回后通知] 方法 {} 执行成功，返回值：{}", method.getName(), result);
        }
        
        /**
         * 异常通知：方法抛出异常时调用
         * 
         * 模拟 Spring 的 ThrowsAdvice
         * 应用场景：异常日志、事务回滚
         */
        private void afterThrowing(Method method, Throwable ex) {
            log.error("[异常通知] 方法 {} 执行失败：{}", method.getName(), ex.getMessage());
            log.info("[事务管理] 回滚数据库事务...");
        }
        
        /**
         * 最终通知：方法执行完成后调用（无论是否异常）
         * 
         * 模拟 Spring 的 AfterAdvice
         * 应用场景：资源清理、性能监控
         */
        private void afterFinally(Method method, long duration) {
            log.info("[最终通知] 方法 {} 执行完成，耗时：{}ms", method.getName(), duration);
            
            // 模拟事务提交或关闭
            if ("createOrder".equals(method.getName()) || "deleteOrder".equals(method.getName())) {
                log.info("[事务管理] 提交/关闭数据库事务...");
            }
        }
    }
}
