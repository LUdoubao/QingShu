package org.doubao.interview.agent.api.service.designpattern;

/**
 * 应用事件监听器接口 - 观察者模式
 * 
 * 模拟 Spring 的 ApplicationListener
 * 用于监听并处理应用事件
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface ApplicationListener<T extends ApplicationEvent> {
    
    /**
     * 处理事件
     * 
     * @param event 事件对象
     */
    void onApplicationEvent(T event);
}
