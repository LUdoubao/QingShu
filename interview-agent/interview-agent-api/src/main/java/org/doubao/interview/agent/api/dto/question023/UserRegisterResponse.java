package org.doubao.interview.agent.api.dto.question023;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户注册响应 DTO
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：返回用户注册结果和事件处理信息
 * 边界：展示事件机制的处理流程
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {

    /**
     * 注册是否成功
     */
    private Boolean success;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 事件处理信息列表
     */
    private List<EventProcessInfo> eventProcessInfos;

    /**
     * 事件处理信息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventProcessInfo {
        /**
         * 监听器名称
         */
        private String listenerName;

        /**
         * 处理方式（同步/异步）
         */
        private String processType;

        /**
         * 处理状态
         */
        private String status;

        /**
         * 处理耗时（毫秒）
         */
        private Long processTime;

        /**
         * 处理结果描述
         */
        private String description;
    }
}
