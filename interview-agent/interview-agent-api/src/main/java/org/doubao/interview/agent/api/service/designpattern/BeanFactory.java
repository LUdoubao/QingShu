package org.doubao.interview.agent.api.service.designpattern;

/**
 * 工厂模式 - Bean 工厂接口
 * 
 * 模拟 Spring 的 BeanFactory，是 IoC 容器的核心接口
 * 职责：根据 Bean 配置创建和管理 Bean 实例
 * 
 * 设计要点：
 * 1. 隐藏 Bean 创建细节（实例化、依赖注入、初始化）
 * 2. 实现对象创建与使用的解耦
 * 3. 支持不同的作用域（单例、原型等）
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface BeanFactory {
    
    /**
     * 根据名称获取 Bean 实例
     * 
     * @param beanName Bean 的名称，用于唯一标识一个 Bean 定义
     * @return 返回 Bean 实例，如果不存在则返回 null
     * @throws BeanCreationException 当 Bean 创建失败时抛出异常
     */
    Object getBean(String beanName);
    
    /**
     * 根据类型获取 Bean 实例
     * 
     * @param requiredType Bean 的类型
     * @param <T> 泛型类型，由 requiredType 推断
     * @return 返回指定类型的 Bean 实例
     * @throws BeanCreationException 当 Bean 创建失败或找不到匹配类型时抛出异常
     */
    <T> T getBean(Class<T> requiredType);
    
    /**
     * 根据名称和类型获取 Bean 实例
     * 
     * @param beanName Bean 的名称
     * @param requiredType Bean 的类型
     * @param <T> 泛型类型
     * @return 返回指定名称和类型的 Bean 实例
     * @throws BeanCreationException 当 Bean 创建失败或类型不匹配时抛出异常
     */
    <T> T getBean(String beanName, Class<T> requiredType);
    
    /**
     * 判断容器中是否包含指定名称的 Bean
     * 
     * @param beanName Bean 的名称
     * @return true-包含该 Bean，false-不包含
     */
    boolean containsBean(String beanName);
    
    /**
     * 判断指定名称的 Bean 是否为单例
     * 
     * @param beanName Bean 的名称
     * @return true-单例，false-原型或其他作用域
     * @throws NoSuchBeanDefinitionException 当 Bean 不存在时抛出
     */
    boolean isSingleton(String beanName) throws NoSuchBeanDefinitionException;
}
