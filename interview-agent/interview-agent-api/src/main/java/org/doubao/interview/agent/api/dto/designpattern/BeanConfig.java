package org.doubao.interview.agent.api.dto.designpattern;

import lombok.Data;

/**
 * 工厂模式示例 - Bean 配置信息
 * 
 * 模拟 Spring 中 BeanDefinition 的配置信息
 * 用于工厂模式创建 Bean 的元数据
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@Data
public class BeanConfig {
    
    /**
     * Bean 的名称，全局唯一标识
     * 对应 Spring 中的 beanName
     */
    private String beanName;
    
    /**
     * Bean 的类型（全限定类名）
     * 对应 Spring 中的 beanClass
     */
    private String beanClass;
    
    /**
     * Bean 的作用域
     * singleton: 单例模式，全局唯一实例
     * prototype: 原型模式，每次获取都创建新实例
     * 对应 Spring 中的 scope 属性
     */
    private String scope = "singleton";
    
    /**
     * 是否为懒加载
     * true: 第一次使用时才创建 Bean
     * false: 容器启动时就创建 Bean
     * 对应 Spring 中的 lazy-init 属性
     */
    private boolean lazyInit = false;
    
    /**
     * 构造函数参数
     * 用于工厂方法根据参数创建 Bean 实例
     */
    private Object[] constructorArgs;
    
    /**
     * 工厂方法名（可选）
     * 对应 Spring 中的 factory-method 属性
     */
    private String factoryMethodName;
}
