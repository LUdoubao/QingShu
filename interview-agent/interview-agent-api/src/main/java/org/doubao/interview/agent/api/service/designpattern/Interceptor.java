package org.doubao.interview.agent.api.service.designpattern;

/**
 * 拦截器接口
 * 
 * 模拟 Spring AOP 的 MethodInterceptor
 * 用于统一拦截方法调用
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface Interceptor {
    
    /**
     * 拦截方法调用
     * 
     * @param method 方法名
     * @param args 参数
     * @param target 目标对象
     * @return 方法返回值
     * @throws Exception 执行异常
     */
    Object intercept(String method, Object[] args, Object target) throws Exception;
}
