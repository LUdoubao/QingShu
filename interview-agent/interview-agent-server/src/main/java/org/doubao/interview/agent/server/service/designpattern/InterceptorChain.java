package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器链 - 责任链模式的核心
 * 
 * 模拟 Spring MVC 的 HandlerInterceptorChain
 * 职责：管理拦截器列表，按顺序执行
 * 
 * 设计要点：
 * 1. 多个拦截器形成责任链
 * 2. 每个拦截器可以决定是否继续执行
 * 3. preHandle 按注册顺序执行，afterCompletion 按相反顺序执行
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class InterceptorChain {
    
    private static final Logger log = LoggerFactory.getLogger(InterceptorChain.class);
    
    /**
     * 拦截器列表
     */
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    
    /**
     * 添加拦截器
     * 
     * @param interceptor 拦截器实例
     */
    public void addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        log.info("添加拦截器到责任链：{}, 当前数量：{}", 
                interceptor.getClass().getSimpleName(), interceptors.size());
    }
    
    /**
     * 执行所有拦截器的 preHandle 方法
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return true-所有拦截器都通过，false-有拦截器中断
     * @throws Exception 执行异常
     */
    public boolean applyPreHandle(Object request, Object response, Object handler) throws Exception {
        log.info("========== 开始执行拦截器链 (preHandle) ==========");
        log.info("拦截器数量：{}", interceptors.size());
        
        // 按顺序执行所有拦截器的 preHandle
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            
            log.info("执行第 {} 个拦截器：{}", i + 1, interceptor.getClass().getSimpleName());
            
            boolean result = interceptor.preHandle(request, response, handler);
            
            if (!result) {
                log.warn("第 {} 个拦截器 {} 中断了请求", i + 1, interceptor.getClass().getSimpleName());
                // 如果某个拦截器返回 false，需要反向执行已执行的 afterCompletion
                triggerAfterCompletion(request, response, handler, null, i - 1);
                return false;
            }
        }
        
        log.info("========== 所有拦截器 preHandle 通过 ==========");
        return true;
    }
    
    /**
     * 执行所有拦截器的 postHandle 方法
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param modelAndView 模型视图
     * @throws Exception 执行异常
     */
    public void applyPostHandle(Object request, Object response, Object handler, Object modelAndView) 
            throws Exception {
        log.info("========== 开始执行拦截器链 (postHandle) ==========");
        
        // 逆序执行 postHandle
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            log.info("执行拦截器 postHandle: {}", interceptor.getClass().getSimpleName());
            interceptor.postHandle(request, response, handler, modelAndView);
        }
        
        log.info("========== 拦截器链 postHandle 完成 ==========");
    }
    
    /**
     * 执行所有拦截器的 afterCompletion 方法
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常对象
     * @throws Exception 执行异常
     */
    public void applyAfterCompletion(Object request, Object response, Object handler, Exception ex) 
            throws Exception {
        log.info("========== 开始执行拦截器链 (afterCompletion) ==========");
        triggerAfterCompletion(request, response, handler, ex, interceptors.size() - 1);
        log.info("========== 拦截器链 afterCompletion 完成 ==========");
    }
    
    /**
     * 从指定位置反向执行 afterCompletion（用于中断场景）
     */
    private void triggerAfterCompletion(Object request, Object response, Object handler, 
                                       Exception ex, int endIndex) throws Exception {
        for (int i = endIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            log.debug("执行拦截器 afterCompletion: {}", interceptor.getClass().getSimpleName());
            interceptor.afterCompletion(request, response, handler, ex);
        }
    }
    
    /**
     * 获取拦截器数量
     */
    public int getInterceptorCount() {
        return interceptors.size();
    }
}
