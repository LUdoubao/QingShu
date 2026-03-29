package org.doubao.interview.agent.server.listener.question023;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.server.event.question023.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 邮件发送监听器 - 演示 Spring 事件机制
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：监听用户注册事件，发送邮件通知
 * 边界：仅用于演示，不实际发送邮件
 * 线程安全：Spring 容器管理实例，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Component
public class EmailSendListener {

    /**
     * 监听用户注册事件 - 同步方式
     * 
     * 【方法注释】
     * 触发时机：UserRegisteredEvent 发布后
     * 处理方式：同步执行（阻塞主流程）
     * 使用场景：需要立即执行且耗时短的操作
     * 
     * @param event 用户注册事件
     */
    @EventListener(classes = UserRegisteredEvent.class)
    public void handleEmailSend(UserRegisteredEvent event) {
        long startTime = System.currentTimeMillis();
        
        log.info("【同步监听器】开始发送邮件，userId={}, userName={}, email={}", 
                event.getUserId(), event.getUserName(), event.getEmail());
        
        // 模拟发送邮件操作
        try {
            Thread.sleep(100); // 模拟邮件发送耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long costTime = System.currentTimeMillis() - startTime;
        
        log.info("【同步监听器】邮件发送成功，userId={}, 耗时={}ms", 
                event.getUserId(), costTime);
        
        // 实际业务中会调用邮件服务发送邮件
        // emailService.sendWelcomeEmail(event.getEmail(), event.getUserName());
    }

    /**
     * 监听用户注册事件 - 异步方式
     * 
     * 【方法注释】
     * 触发时机：UserRegisteredEvent 发布后
     * 处理方式：异步执行（不阻塞主流程）
     * 使用场景：耗时长、不需要立即得到结果的操作
     * 注意：需要配置@EnableAsync 开启异步支持
     * 
     * @param event 用户注册事件
     */
    @Async
    @EventListener(classes = UserRegisteredEvent.class)
    public void handleEmailSendAsync(UserRegisteredEvent event) {
        long startTime = System.currentTimeMillis();
        
        log.info("【异步监听器】开始发送邮件，userId={}, userName={}, email={}", 
                event.getUserId(), event.getUserName(), event.getEmail());
        
        // 模拟发送邮件操作
        try {
            Thread.sleep(200); // 模拟邮件发送耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long costTime = System.currentTimeMillis() - startTime;
        
        log.info("【异步监听器】邮件发送成功，userId={}, 耗时={}ms", 
                event.getUserId(), costTime);
    }
}
