package org.doubao.interview.agent.api.dto.question043;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 任务调度控制请求对象
 * 
 * 【类注释】
 * 职责：封装客户端对定时任务的控制请求（启动、停止等）
 * 边界：仅用于学习和演示场景
 * 线程安全：无状态对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
public class TaskControlRequest {
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    
    /**
     * 操作类型
     * 可选值：start, stop, pause, resume
     */
    @NotBlank(message = "操作类型不能为空")
    private String operation;
}
