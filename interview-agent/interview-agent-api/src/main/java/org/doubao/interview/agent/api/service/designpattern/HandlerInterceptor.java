package org.doubao.interview.agent.api.service.designpattern;

/**
 * 拦截器接口 - 责任链模式
 * 
 * 模拟 Spring MVC 的 HandlerInterceptor
 * 用于在请求处理的不同阶段进行拦截
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface HandlerInterceptor {
    
    /**
     * 在请求处理之前调用
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return true-继续执行，false-中断请求
     * @throws Exception 处理异常
     */
    boolean preHandle(Object request, Object response, Object handler) throws Exception;
    
    /**
     * 在请求处理之后调用
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param modelAndView 模型视图
     * @throws Exception 处理异常
     */
    void postHandle(Object request, Object response, Object handler, Object modelAndView) throws Exception;
    
    /**
     * 在完成之后调用（渲染完成后）
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常对象（如果有）
     * @throws Exception 处理异常
     */
    void afterCompletion(Object request, Object response, Object handler, Exception ex) throws Exception;
}
