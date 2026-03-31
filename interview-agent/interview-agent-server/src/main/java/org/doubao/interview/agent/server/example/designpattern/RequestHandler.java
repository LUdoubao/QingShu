package org.doubao.interview.agent.server.example.designpattern;

import lombok.Data;

/**
 * 示例 Bean - 请求处理器（原型模式）
 * 
 * 用于演示 Spring 原型模式的示例类
 * 特点：
 * 1. 有状态对象，每个请求需要独立的实例
 * 2. 避免多线程共享状态导致的线程安全问题
 * 3. 每次 getBean() 都创建新的实例
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@Data
public class RequestHandler {
    
    /**
     * 请求 ID（每个实例独立）
     * 这是有状态变量，不能共享
     */
    private String requestId;
    
    /**
     * 请求参数（每个实例独立）
     */
    private String requestParam;
    
    /**
     * 请求时间戳（每个实例独立）
     */
    private long timestamp;
    
    /**
     * 默认构造函数
     */
    public RequestHandler() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param requestId 请求 ID
     * @param requestParam 请求参数
     */
    public RequestHandler(String requestId, String requestParam) {
        this.requestId = requestId;
        this.requestParam = requestParam;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 处理请求
     * 
     * @return 处理结果
     */
    public String handle() {
        return String.format("Request[%s] handled at %d with param: %s", 
                           requestId, timestamp, requestParam);
    }
}
