package org.doubao.interview.agent.api.dto.question044;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异步任务执行结果响应对象
 * 
 * 【类注释】
 * 职责：封装异步任务的执行结果和状态信息
 * 边界：仅用于学习和演示场景
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskResult {
    
    /**
     * 任务 ID
     */
    private String taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 执行状态
     * 可选值：SUCCESS, FAILED, RUNNING, TIMEOUT
     */
    private String status;
    
    /**
     * 执行结果数据
     */
    private Object data;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 执行线程名称
     */
    private String threadName;
    
    /**
     * 详细说明
     */
    private String explanation;
}
