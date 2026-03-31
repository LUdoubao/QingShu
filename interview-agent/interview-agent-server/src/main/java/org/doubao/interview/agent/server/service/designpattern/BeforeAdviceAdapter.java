package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.Advice;
import org.doubao.interview.agent.api.service.designpattern.BeforeAdvice;
import org.doubao.interview.agent.api.service.designpattern.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通知适配器 - 适配器模式实现
 * 
 * 模拟 Spring AOP 的 AdvisorAdapter
 * 职责：将 BeforeAdvice 适配为 Interceptor 接口
 * 
 * 设计要点：
 * 1. 解决接口不兼容问题（BeforeAdvice -> Interceptor）
 * 2. 统一拦截器调用方式
 * 3. 符合开闭原则，新增通知类型只需增加适配器
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class BeforeAdviceAdapter implements Advice {
    
    private static final Logger log = LoggerFactory.getLogger(BeforeAdviceAdapter.class);
    
    /**
     * 被适配的 BeforeAdvice 对象
     */
    private final BeforeAdvice beforeAdvice;
    
    /**
     * 构造函数
     * 
     * @param beforeAdvice 前置通知对象
     */
    public BeforeAdviceAdapter(BeforeAdvice beforeAdvice) {
        this.beforeAdvice = beforeAdvice;
        log.info("创建 BeforeAdvice 适配器，包装：{}", beforeAdvice.getClass().getSimpleName());
    }
    
    @Override
    public String getAdviceType() {
        return "before";
    }
    
    /**
     * 将 BeforeAdvice 转换为 Interceptor
     * 
     * 这是适配器的核心方法
     * 
     * @return 适配后的拦截器
     */
    public Interceptor toInterceptor() {
        log.info("将 BeforeAdvice 适配为 Interceptor");
        
        return new Interceptor() {
            @Override
            public Object intercept(String method, Object[] args, Object target) throws Exception {
                log.info("[适配器] 开始拦截方法：{}", method);
                
                // 1. 调用 BeforeAdvice 的前置逻辑
                beforeAdvice.before(method, args, target);
                
                // 2. 通过反射调用目标方法
                log.info("[适配器] 执行目标方法：{}", method);
                Object result = invokeTargetMethod(method, args, target);
                
                log.info("[适配器] 方法执行完成：{}", method);
                return result;
            }
            
            /**
             * 调用目标方法（简化实现）
             */
            private Object invokeTargetMethod(String method, Object[] args, Object target) 
                    throws Exception {
                // 实际实现会使用反射调用
                // Method targetMethod = target.getClass().getMethod(method, ...);
                // return targetMethod.invoke(target, args);
                return null;
            }
        };
    }
}
