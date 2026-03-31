package org.doubao.interview.agent.api.service.designpattern;

/**
 * Bean 创建异常
 * 
 * 当 Bean 工厂无法创建 Bean 时抛出此异常
 * 对应 Spring 的 BeansException 体系
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class BeanCreationException extends RuntimeException {
    
    /**
     * 构造方法
     * 
     * @param message 异常信息
     */
    public BeanCreationException(String message) {
        super(message);
    }
    
    /**
     * 构造方法
     * 
     * @param message 异常信息
     * @param cause 根本原因
     */
    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
