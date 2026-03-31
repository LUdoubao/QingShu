package org.doubao.interview.agent.server.service.designpattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前端控制器 - 委派模式实现
 * 
 * 模拟 Spring MVC 的 DispatcherServlet
 * 职责：接收请求并委派给各个组件处理
 * 
 * 设计要点：
 * 1. 统一入口，集中处理请求
 * 2. 将具体任务委派给专业组件
 * 3. 解耦请求和处理逻辑
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class DispatcherServlet {
    
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);
    
    /**
     * 处理器映射器（负责查找处理器）
     */
    private final HandlerMapping handlerMapping;
    
    /**
     * 处理器适配器（负责执行处理器）
     */
    private final HandlerAdapter handlerAdapter;
    
    /**
     * 视图解析器（负责解析视图）
     */
    private final ViewResolver viewResolver;
    
    /**
     * 拦截器链（责任链模式）
     */
    private final InterceptorChain interceptorChain;
    
    /**
     * 构造函数
     */
    public DispatcherServlet() {
        this.handlerMapping = new MockHandlerMapping();
        this.handlerAdapter = new MockHandlerAdapter();
        this.viewResolver = new MockViewResolver();
        this.interceptorChain = new InterceptorChain();
        
        // 注册拦截器到责任链
        interceptorChain.addInterceptor(new LoggingInterceptor());
        interceptorChain.addInterceptor(new AuthInterceptor());
        
        log.info("DispatcherServlet 初始化完成");
    }
    
    /**
     * 处理请求（核心方法）
     * 
     * @param request 请求路径
     * @return 响应结果
     */
    public String handleRequest(String request) {
        log.info("========== 接收到请求：{} ==========", request);
        
        try {
            // ========== 步骤 1：执行拦截器 preHandle ==========
            if (!interceptorChain.applyPreHandle(request, null, null)) {
                log.warn("请求被拦截器拦截，终止处理");
                return "403 Forbidden";
            }
            
            // ========== 步骤 2：查找处理器（Handler Mapping）==========
            Object handler = handlerMapping.getHandler(request);
            if (handler == null) {
                log.warn("未找到处理器：{}", request);
                return "404 Not Found";
            }
            log.info("找到处理器：{}", handler.getClass().getSimpleName());
            
            // ========== 步骤 3：执行处理器（Handler Adapter）==========
            Object modelAndView = handlerAdapter.handle(handler, request);
            log.info("处理器执行完成，返回模型：{}", modelAndView);
            
            // ========== 步骤 4：执行拦截器 postHandle ==========
            interceptorChain.applyPostHandle(request, null, handler, modelAndView);
            
            // ========== 步骤 5：解析视图（View Resolver）==========
            String viewName = viewResolver.resolveView(modelAndView);
            log.info("视图解析结果：{}", viewName);
            
            // ========== 步骤 6：执行拦截器 afterCompletion ==========
            interceptorChain.applyAfterCompletion(request, null, handler, null);
            
            log.info("========== 请求处理完成：{} ==========", request);
            return viewName;
            
        } catch (Exception e) {
            log.error("请求处理失败：{}", request, e);
            
            // 执行异常时的 afterCompletion
            try {
                interceptorChain.applyAfterCompletion(request, null, null, e);
            } catch (Exception ex) {
                log.error("afterCompletion 执行失败", ex);
            }
            
            return "500 Internal Server Error: " + e.getMessage();
        }
    }
}
