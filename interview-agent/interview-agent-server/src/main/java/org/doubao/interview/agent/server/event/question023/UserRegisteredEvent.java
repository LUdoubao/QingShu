package org.doubao.interview.agent.server.event.question023;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件 - 用于演示 Spring 事件机制
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：表示用户注册成功的事件，携带用户相关信息
 * 边界：仅用于演示 Spring 事件发布/订阅机制
 * 线程安全：事件对象本身不可变，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private final Long userId;

    /**
     * 用户名称
     */
    private final String userName;

    /**
     * 用户邮箱
     */
    private final String email;

    /**
     * 构造函数
     * 
     * @param source 事件源（通常是发布事件的对象）
     * @param userId 用户 ID
     * @param userName 用户名称
     * @param email 用户邮箱
     */
    public UserRegisteredEvent(Object source, Long userId, String userName, String email) {
        super(source);
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }
}
