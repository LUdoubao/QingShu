package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.dto.designpattern.BeanConfig;
import org.doubao.interview.agent.api.service.designpattern.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean 工厂实现 - 工厂模式的核心实现类
 * <p>
 * 模拟 Spring 的 AbstractAutowireCapableBeanFactory
 * 职责：根据 BeanConfig 配置创建和管理 Bean 实例
 * <p>
 * 核心设计：
 * 1. 工厂模式：封装 Bean 创建逻辑，提供统一的 getBean 接、
 * 2. 单例模式：通过 SingletonBeanRegistry 维护单例 Bean 缓存
 * 3. 原型模式：支持 prototype 作用域，每次返回新实例
 * 4. 反射机制：通过反射动态创建 Bean 实例
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class DefaultBeanFactory implements BeanFactory {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultBeanFactory.class);
    
    /**
     * Bean 定义注册表
     * key: beanName（Bean 的名称）
     * value: beanConfig（Bean 的配置信息）
     * 
     * 存储所有已注册的 Bean 定义，用于后续创建 Bean 实例
     */
    private final Map<String, BeanConfig> beanDefinitionMap = new ConcurrentHashMap<>(256);
    
    /**
     * 单例 Bean 注册器（组合模式）
     * 用于管理和缓存单例 Bean 实例
     */
    private final SingletonBeanRegistry singletonBeanRegistry = new SingletonBeanRegistry();
    
    /**
     * 默认构造函数
     */
    public DefaultBeanFactory() {
        log.info("初始化 Bean 工厂");
    }
    
    /**
     * 注册 Bean 定义
     * 
     * @param beanName Bean 的名称
     * @param beanConfig Bean 的配置信息
     */
    public void registerBeanDefinition(String beanName, BeanConfig beanConfig) {
        beanDefinitionMap.put(beanName, beanConfig);
        log.info("注册 Bean 定义：{}, class={}", beanName, beanConfig.getBeanClass());
    }
    
    @Override
    public Object getBean(String beanName) {
        log.info("开始获取 Bean: {}", beanName);
        
        // 1. 检查 Bean 定义是否存在
        BeanConfig beanConfig = beanDefinitionMap.get(beanName);
        if (beanConfig == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        
        // 2. 根据作用域决定获取方式
        if ("prototype".equals(beanConfig.getScope())) {
            // 原型模式：每次创建新实例
            return createPrototypeBean(beanConfig);
        } else {
            // 单例模式：从缓存获取或创建
            return getOrCreateSingletonBean(beanName, beanConfig);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        log.info("根据类型获取 Bean: {}", requiredType.getName());
        
        // 遍历 Bean 定义，查找匹配类型的 Bean
        for (Map.Entry<String, BeanConfig> entry : beanDefinitionMap.entrySet()) {
            BeanConfig config = entry.getValue();
            try {
                // 通过反射判断类型是否匹配
                Class<?> beanClass = Class.forName(config.getBeanClass());
                if (requiredType.isAssignableFrom(beanClass)) {
                    // 找到匹配的 Bean，递归调用 getBean(String)
                    return (T) getBean(entry.getKey());
                }
            } catch (ClassNotFoundException e) {
                log.warn("找不到 Bean 类：{}", config.getBeanClass(), e);
            }
        }
        
        throw new BeanCreationException(
            String.format("No bean of type [%s] found", requiredType.getName()));
    }
    
    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        Object bean = getBean(beanName);
        
        // 类型校验
        if (!requiredType.isInstance(bean)) {
            throw new BeanCreationException(
                String.format("Bean '%s' is not of required type [%s]", beanName, requiredType.getName()));
        }
        
        return (T) bean;
    }
    
    @Override
    public boolean containsBean(String beanName) {
        return beanDefinitionMap.containsKey(beanName) || 
               singletonBeanRegistry.containsSingleton(beanName);
    }
    
    @Override
    public boolean isSingleton(String beanName) throws NoSuchBeanDefinitionException {
        BeanConfig beanConfig = beanDefinitionMap.get(beanName);
        if (beanConfig == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return "singleton".equals(beanConfig.getScope());
    }
    
    /**
     * 获取或创建单例 Bean
     * 
     * 使用双重检查锁机制保证线程安全：
     * 1. 先从缓存获取，如果存在则直接返回
     * 2. 如果不存在，同步创建并注册到缓存
     * 
     * @param beanName Bean 的名称
     * @param beanConfig Bean 的配置信息
     * @return 单例 Bean 实例
     */
    private Object getOrCreateSingletonBean(String beanName, BeanConfig beanConfig) {
        // 第一次检查：从缓存获取
        Object singletonInstance = singletonBeanRegistry.getSingleton(beanName);
        if (singletonInstance != null) {
            return singletonInstance;
        }
        
        // 同步创建（简化版本，未完全实现 DCL）
        synchronized (this) {
            // 第二次检查：避免重复创建
            singletonInstance = singletonBeanRegistry.getSingleton(beanName);
            if (singletonInstance != null) {
                return singletonInstance;
            }
            
            // 标记正在创建（解决循环依赖）
            singletonBeanRegistry.markBeanAsCurrentlyInCreation(beanName);
            try {
                // 创建 Bean 实例
                singletonInstance = doCreateBean(beanConfig);
                
                // 注册到单例缓存
                singletonBeanRegistry.registerSingleton(beanName, singletonInstance);
                
                log.info("成功创建并注册单例 Bean: {}, hashcode={}", 
                        beanName, singletonInstance.hashCode());
                
                return singletonInstance;
            } finally {
                // 清除创建标记
                singletonBeanRegistry.clearCurrentlyInCreation(beanName);
            }
        }
    }
    
    /**
     * 创建原型 Bean 实例
     * 
     * 每次调用都创建新的实例，适用于有状态的对象
     * 
     * @param beanConfig Bean 的配置信息
     * @return 新的 Bean 实例
     */
    private Object createPrototypeBean(BeanConfig beanConfig) {
        Object prototypeInstance = doCreateBean(beanConfig);
        log.info("创建原型 Bean 实例：{}, hashcode={}, prototypeInstance={}",
                beanConfig.getBeanName(), prototypeInstance.hashCode(), prototypeInstance);
        return prototypeInstance;
    }
    
    /**
     * 执行 Bean 创建的核心逻辑
     * 
     * 使用反射机制：
     * 1. 加载 Bean 类
     * 2. 获取构造函数
     * 3. 实例化对象
     * 
     * @param beanConfig Bean 的配置信息
     * @return 新创建的 Bean 实例
     * @throws BeanCreationException 当反射创建失败时抛出
     */
    private Object doCreateBean(BeanConfig beanConfig) {
        try {
            // 1. 加载 Bean 类
            Class<?> beanClass = Class.forName(beanConfig.getBeanClass());
            log.info("加载 Bean 类：{}", beanConfig.getBeanClass());
            
            // 2. 获取构造函数（优先使用有参构造）
            Constructor<?> constructor;
            if (beanConfig.getConstructorArgs() != null && 
                beanConfig.getConstructorArgs().length > 0) {
                // 根据参数类型匹配构造函数
                Class<?>[] paramTypes = new Class[beanConfig.getConstructorArgs().length];
                for (int i = 0; i < beanConfig.getConstructorArgs().length; i++) {
                    paramTypes[i] = beanConfig.getConstructorArgs()[i].getClass();
                }
                constructor = beanClass.getDeclaredConstructor(paramTypes);
            } else {
                // 使用无参构造函数
                constructor = beanClass.getDeclaredConstructor();
            }
            
            // 3. 设置可访问（处理私有构造函数）
            constructor.setAccessible(true);
            
            // 4. 实例化 Bean
            Object beanInstance;
            if (beanConfig.getConstructorArgs() != null) {
                beanInstance = constructor.newInstance(beanConfig.getConstructorArgs());
            } else {
                beanInstance = constructor.newInstance();
            }
            
            log.info("通过反射创建 Bean 实例：{}, class={}",
                     beanInstance.getClass().getName(), beanInstance.hashCode());
            
            return beanInstance;
            
        } catch (Exception e) {
            log.error("创建 Bean 失败：{}, error={}", beanConfig.getBeanName(), e.getMessage());
            throw new BeanCreationException(
                String.format("Failed to create bean '%s'", beanConfig.getBeanName()), e);
        }
    }
}
