package org.doubao.ioc.container;

/**
 * Bean 定义 - 存储 Bean 的元数据信息
 * <p>
 * 类似 Spring 的 BeanDefinition
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
public class BeanDefinition {
    
    /**
     * Bean 对应的 Class
     */
    private final Class<?> beanClass;
    
    /**
     * Bean 的作用域
     * singleton: 单例
     * prototype: 原型
     */
    private final String scope;
    
    /**
     * 构造函数
     *
     * @param beanClass Bean 对应的 Class
     * @param scope 作用域
     */
    public BeanDefinition(Class<?> beanClass, String scope) {
        this.beanClass = beanClass;
        this.scope = scope;
    }
    
    /**
     * 获取 Bean 对应的 Class
     *
     * @return Class 对象
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }
    
    /**
     * 获取作用域
     *
     * @return 作用域
     */
    public String getScope() {
        return scope;
    }
    
    /**
     * 是否是单例
     *
     * @return true-单例，false-非单例
     */
    public boolean isSingleton() {
        return "singleton".equals(scope);
    }
    
    /**
     * 是否是原型
     *
     * @return true-原型，false-非原型
     */
    public boolean isPrototype() {
        return "prototype".equals(scope);
    }
}
