package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志拦截器 - 责任链模式实现
 * 
 * 模拟 Spring MVC 的拦截器
 * 职责：记录请求日志、性能监控
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    /**
     * 使用 ThreadLocal 存储请求开始时间
     * 用于计算请求耗时
     */
    private final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();
    
    @Override
    public boolean preHandle(Object request, Object response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        startTimeHolder.set(startTime);
        
        log.info("========== [日志拦截器] 请求开始 ==========");
        log.info("请求时间：{}", new java.util.Date());
        log.info("开始时间戳：{}", startTime);
        
        return true; // 继续执行
    }
    
    @Override
    public void postHandle(Object request, Object response, Object handler, Object modelAndView) 
            throws Exception {
        Long startTime = startTimeHolder.get();
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("========== [日志拦截器] 请求处理完成 ==========");
            log.info("处理耗时：{}ms", duration);
        }
    }
    
    @Override
    public void afterCompletion(Object request, Object response, Object handler, Exception ex) 
            throws Exception {
        startTimeHolder.remove(); // 清理 ThreadLocal，防止内存泄漏
        
        log.info("========== [日志拦截器] 请求完成 ==========");
        if (ex != null) {
            log.error("请求处理异常：", ex);
        }
    }
}
