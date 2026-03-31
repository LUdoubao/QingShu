package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.ApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 容器刷新事件 - 具体事件类
 * 
 * 模拟 Spring 的 ContextRefreshedEvent
 * 当应用上下文初始化完成时触发
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class ContextRefreshedEvent implements ApplicationEvent {
    
    private static final Logger log = LoggerFactory.getLogger(ContextRefreshedEvent.class);
    
    /**
     * 事件发生的时间戳
     */
    private final long timestamp;
    
    /**
     * 事件源（通常是 ApplicationContext）
     */
    private final Object source;
    
    /**
     * 构造函数
     * 
     * @param source 事件源
     */
    public ContextRefreshedEvent(Object source) {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
        log.info("创建容器刷新事件：source={}", source.getClass().getSimpleName());
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public Object getSource() {
        return source;
    }
}
