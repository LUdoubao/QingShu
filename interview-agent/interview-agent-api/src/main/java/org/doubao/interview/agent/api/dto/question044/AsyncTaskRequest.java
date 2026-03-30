package org.doubao.interview.agent.api.dto.question044;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 异步任务请求对象
 * 
 * 【类注释】
 * 职责：封装客户端对异步任务的请求参数
 * 边界：仅用于学习和演示场景
 * 线程安全：无状态对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
public class AsyncTaskRequest {
    
    /**
     * 任务类型
     * 可选值：normal, selfInvoke, timeout, exception, future
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    
    /**
     * 模拟执行耗时（毫秒）
     */
    private Long sleepTime = 1000L;
    
    /**
     * 是否抛出异常
     */
    private boolean throwException = false;
    
    /**
     * 自定义消息
     */
    private String message;
}
