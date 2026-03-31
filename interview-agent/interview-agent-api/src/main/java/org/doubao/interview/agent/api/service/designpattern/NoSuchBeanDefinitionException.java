package org.doubao.interview.agent.api.service.designpattern;

/**
 * Bean 不存在异常
 * 
 * 当请求的 Bean 在工厂中不存在时抛出
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class NoSuchBeanDefinitionException extends RuntimeException {
    
    /**
     * 构造方法
     * 
     * @param beanName 不存在的 Bean 名称
     */
    public NoSuchBeanDefinitionException(String beanName) {
        super("No bean named '" + beanName + "' available");
    }
}
