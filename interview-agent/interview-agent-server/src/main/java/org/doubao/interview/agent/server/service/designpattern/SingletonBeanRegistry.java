package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.dto.designpattern.BeanConfig;
import org.doubao.interview.agent.api.service.designpattern.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例 Bean 注册器 - 实现单例模式的核心组件
 * 
 * 模拟 Spring 的 DefaultSingletonBeanRegistry
 * 职责：维护单例 Bean 缓存池，保证单例 Bean 全局唯一
 * 
 * 核心设计：
 * 1. 使用 ConcurrentHashMap 保证线程安全
 * 2. 通过 synchronized + DCL（双重检查锁）避免并发创建
 * 3. 提供单例 Bean 的注册和获取方法
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class SingletonBeanRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(SingletonBeanRegistry.class);
    
    /**
     * 单例 Bean 缓存池
     * key: beanName（Bean 的名称）
     * value: singletonObject（单例 Bean 实例）
     * 
     * 使用 ConcurrentHashMap 的原因：
     * 1. 线程安全：支持多线程并发访问
     * 2. 高性能：读操作无需加锁，写操作使用 CAS+synchronized
     * 3. 弱一致性：适合 Bean 缓存场景，不要求强一致性
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    /**
     * 正在创建中的 Bean 集合（用于解决循环依赖）
     * key: beanName
     * value: true（仅作为标记使用）
     */
    private final Map<String, Object> currentlyInCreation = new ConcurrentHashMap<>();
    
    /**
     * 从缓存中获取单例 Bean
     * 
     * @param beanName Bean 的名称
     * @return 返回单例 Bean 实例，如果不存在则返回 null
     */
    public Object getSingleton(String beanName) {
        Object singletonObject = singletonObjects.get(beanName);
        
        if (singletonObject != null) {
            log.debug("从单例缓存中获取 Bean: {}, hashcode={}", beanName, singletonObject.hashCode());
        } else {
            log.debug("单例缓存中未找到 Bean: {}", beanName);
        }
        
        return singletonObject;
    }
    
    /**
     * 注册单例 Bean（线程安全版本）
     * 
     * 使用双重检查锁（DCL）保证线程安全：
     * 1. 第一次检查：无锁状态下检查缓存，避免不必要的同步开销
     * 2. 加锁：如果缓存不存在，进入同步块
     * 3. 第二次检查：在同步块中再次检查，避免重复创建
     * 4. 创建并注册：创建 Bean 实例并放入缓存
     * 
     * @param beanName Bean 的名称
     * @param singletonObject 单例 Bean 实例
     * @throws BeanCreationException 如果 Bean 已存在或创建失败
     */
    public void registerSingleton(String beanName, Object singletonObject) {
        // 第一次检查：快速判断是否已存在
        Object existingObject = singletonObjects.get(beanName);
        if (existingObject != null) {
            throw new BeanCreationException(
                String.format("Bean '%s' already exists - cannot register singleton", beanName));
        }
        
        // 加锁并进行第二次检查
        synchronized (this) {
            existingObject = singletonObjects.get(beanName);
            if (existingObject != null) {
                // 双重检查：避免多个线程重复创建
                throw new BeanCreationException(
                    String.format("Bean '%s' already exists - concurrent registration detected", beanName));
            }
            
            // 注册单例 Bean 到缓存池
            singletonObjects.put(beanName, singletonObject);
            log.info("成功注册单例 Bean: {}, instance hashcode={}", beanName, singletonObject.hashCode());
        }
    }
    
    /**
     * 标记 Bean 正在创建中（用于解决循环依赖）
     * 
     * @param beanName Bean 的名称
     */
    public void markBeanAsCurrentlyInCreation(String beanName) {
        currentlyInCreation.put(beanName, Boolean.TRUE);
        log.debug("标记 Bean 正在创建中：{}", beanName);
    }
    
    /**
     * 清除 Bean 的创建中标记
     * 
     * @param beanName Bean 的名称
     */
    public void clearCurrentlyInCreation(String beanName) {
        currentlyInCreation.remove(beanName);
        log.debug("清除 Bean 创建标记：{}", beanName);
    }
    
    /**
     * 判断 Bean 是否正在创建中
     * 
     * @param beanName Bean 的名称
     * @return true-正在创建，false-未创建
     */
    public boolean isCurrentlyInCreation(String beanName) {
        return currentlyInCreation.containsKey(beanName);
    }
    
    /**
     * 移除单例 Bean（用于容器关闭时清理资源）
     * 
     * @param beanName Bean 的名称
     * @return 被移除的 Bean 实例，如果不存在则返回 null
     */
    public Object removeSingleton(String beanName) {
        Object removedObject = singletonObjects.remove(beanName);
        if (removedObject != null) {
            log.info("移除单例 Bean: {}", beanName);
        }
        return removedObject;
    }
    
    /**
     * 获取当前缓存的单例 Bean 数量
     * 
     * @return 单例 Bean 数量
     */
    public int getSingletonCount() {
        return singletonObjects.size();
    }
    
    /**
     * 判断指定名称的 Bean 是否存在于缓存中
     * 
     * @param beanName Bean 的名称
     * @return true-存在，false-不存在
     */
    public boolean containsSingleton(String beanName) {
        return singletonObjects.containsKey(beanName);
    }
}
