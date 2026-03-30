package org.doubao.interview.agent.api.dto.question043;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务调度演示响应对象
 * 
 * 【类注释】
 * 职责：返回定时任务的执行状态和统计信息
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
public class ScheduledTaskResponse {
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务类型
     * 可选值：fixedRate, fixedDelay, cron
     */
    private String taskType;
    
    /**
     * 是否正在运行
     */
    private boolean running;
    
    /**
     * 已执行次数
     */
    private long executeCount;
    
    /**
     * 最后一次执行时间
     */
    private String lastExecutionTime;
    
    /**
     * 下一次执行时间（仅适用于 fixedRate）
     */
    private String nextExecutionTime;
    
    /**
     * 详细说明
     */
    private String explanation;
}
