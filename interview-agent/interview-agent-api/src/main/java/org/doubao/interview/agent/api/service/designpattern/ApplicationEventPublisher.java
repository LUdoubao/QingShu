package org.doubao.interview.agent.api.service.designpattern;

/**
 * 应用事件发布器接口
 * 
 * 模拟 Spring 的 ApplicationEventPublisher
 * 用于发布应用事件
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface ApplicationEventPublisher {
    
    /**
     * 发布事件
     * 
     * @param event 事件对象
     */
    void publishEvent(ApplicationEvent event);
}
