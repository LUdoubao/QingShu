package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 权限拦截器 - 责任链模式实现
 * 
 * 模拟 Spring MVC 的拦截器
 * 职责：用户权限校验、登录检查
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    
    /**
     * 在请求处理前进行权限校验
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return true-有权限继续，false-无权限中断
     */
    @Override
    public boolean preHandle(Object request, Object response, Object handler) throws Exception {
        log.info("========== [权限拦截器] 开始权限校验 ==========");
        
        // 模拟获取用户 token
        String token = getTokenFromRequest(request);
        log.info("获取到 Token: {}", maskToken(token));
        
        // 1. 检查是否登录
        if (!isLoggedIn(token)) {
            log.warn("[权限拦截器] 用户未登录，拒绝请求");
            sendUnauthorizedResponse(response);
            return false; // 中断请求
        }
        
        // 2. 检查是否有访问权限
        if (!hasPermission(token, request)) {
            log.warn("[权限拦截器] 用户无权限访问，拒绝请求");
            sendForbiddenResponse(response);
            return false; // 中断请求
        }
        
        log.info("[权限拦截器] 权限校验通过");
        return true; // 继续执行
    }
    
    @Override
    public void postHandle(Object request, Object response, Object handler, Object modelAndView) 
            throws Exception {
        log.debug("[权限拦截器] 后处理完成");
    }
    
    @Override
    public void afterCompletion(Object request, Object response, Object handler, Exception ex) 
            throws Exception {
        log.debug("[权限拦截器] 请求完成");
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 从请求中获取 Token
     */
    private String getTokenFromRequest(Object request) {
        // 简化实现，实际会从 Header 中获取
        return "mock_token_12345";
    }
    
    /**
     * 脱敏显示 Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
    
    /**
     * 判断用户是否已登录
     */
    private boolean isLoggedIn(String token) {
        // 模拟登录检查（实际会验证 JWT 或查询 Session）
        log.info("验证 Token 有效性...");
        return token != null && !token.isEmpty();
    }
    
    /**
     * 判断用户是否有权限
     */
    private boolean hasPermission(String token, Object request) {
        // 模拟权限检查（实际会查询用户角色和权限表）
        log.info("检查用户权限...");
        return true; // 简化实现，假设有权限
    }
    
    /**
     * 发送 401 未授权响应
     */
    private void sendUnauthorizedResponse(Object response) {
        log.error("[权限拦截器] 返回 401 未授权响应");
        // 实际实现会设置 HTTP 状态码和响应体
    }
    
    /**
     * 发送 403 禁止访问响应
     */
    private void sendForbiddenResponse(Object response) {
        log.error("[权限拦截器] 返回 403 禁止访问响应");
        // 实际实现会设置 HTTP 状态码和响应体
    }
}
