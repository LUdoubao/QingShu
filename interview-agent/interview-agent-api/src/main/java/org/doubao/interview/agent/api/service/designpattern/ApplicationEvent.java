package org.doubao.interview.agent.api.service.designpattern;

/**
 * 应用事件接口 - 观察者模式
 * 
 * 模拟 Spring 的 ApplicationEvent
 * 所有事件的顶级父接口
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface ApplicationEvent {
    
    /**
     * 获取事件发生的时间戳
     * 
     * @return 时间戳
     */
    long getTimestamp();
    
    /**
     * 获取事件源（触发事件的对象）
     * 
     * @return 事件源
     */
    Object getSource();
}
