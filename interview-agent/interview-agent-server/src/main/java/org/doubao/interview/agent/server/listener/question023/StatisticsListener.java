package org.doubao.interview.agent.server.listener.question023;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.server.event.question023.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 数据统计监听器 - 演示 Spring 事件机制
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：监听用户注册事件，进行数据统计
 * 边界：仅用于演示，不实际存储数据
 * 线程安全：Spring 容器管理实例，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Component
public class StatisticsListener {

    /**
     * 监听用户注册事件 - 数据统计
     * 
     * 【方法注释】
     * 触发时机：UserRegisteredEvent 发布后
     * 处理方式：同步执行
     * 使用场景：数据统计、日志记录等
     * 
     * @param event 用户注册事件
     */
    @EventListener(classes = UserRegisteredEvent.class)
    public void handleStatistics(UserRegisteredEvent event) {
        long startTime = System.currentTimeMillis();
        
        log.info("【统计监听器】开始统计数据，userId={}, userName={}", 
                event.getUserId(), event.getUserName());
        
        // 模拟数据统计操作
        try {
            Thread.sleep(50); // 模拟统计耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long costTime = System.currentTimeMillis() - startTime;
        
        log.info("【统计监听器】统计完成，userId={}, 耗时={}ms", 
                event.getUserId(), costTime);
        
        // 实际业务中会更新统计数据
        // statisticsService.incrementUserCount();
        // statisticsService.saveUserRegisterLog(event);
    }
}
