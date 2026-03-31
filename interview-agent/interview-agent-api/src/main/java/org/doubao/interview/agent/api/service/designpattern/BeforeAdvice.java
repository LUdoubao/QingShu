package org.doubao.interview.agent.api.service.designpattern;

/**
 * 前置通知接口
 * 
 * 模拟 Spring AOP 的 MethodBeforeAdvice
 * 在目标方法执行前调用
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface BeforeAdvice extends Advice {
    
    /**
     * 在方法执行前调用
     * 
     * @param method 方法名
     * @param args 参数
     * @param target 目标对象
     */
    void before(String method, Object[] args, Object target);
}
