package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.ApplicationEvent;
import org.doubao.interview.agent.api.service.designpattern.ApplicationEventPublisher;
import org.doubao.interview.agent.api.service.designpattern.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用事件发布器实现 - 观察者模式的核心
 * 
 * 模拟 Spring 的 ApplicationEventMulticaster
 * 职责：管理监听器并发布事件
 * 
 * 设计要点：
 * 1. 维护监听器列表
 * 2. 支持动态添加/移除监听器
 * 3. 发布事件时通知所有匹配的监听器
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class SimpleApplicationEventMulticaster implements ApplicationEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleApplicationEventMulticaster.class);
    
    /**
     * 监听器列表
     * 使用 List 允许同一个监听器注册多次
     */
    private final List<ApplicationListener<?>> listeners = new ArrayList<>();
    
    /**
     * 添加监听器
     * 
     * @param listener 应用事件监听器
     */
    public void addListener(ApplicationListener<?> listener) {
        listeners.add(listener);
        log.info("注册监听器：{}", listener.getClass().getSimpleName());
    }
    
    /**
     * 移除监听器
     * 
     * @param listener 应用事件监听器
     */
    public void removeListener(ApplicationListener<?> listener) {
        listeners.remove(listener);
        log.info("移除监听器：{}", listener.getClass().getSimpleName());
    }
    
    /**
     * 发布事件（核心方法）
     * 
     * 遍历所有监听器并调用 onApplicationEvent 方法
     * 
     * @param event 事件对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public void publishEvent(ApplicationEvent event) {
        log.info("========== 开始发布事件 ==========");
        log.info("事件类型：{}", event.getClass().getSimpleName());
        log.info("事件源：{}", event.getSource().getClass().getSimpleName());
        log.info("监听器数量：{}", listeners.size());
        
        // 遍历所有监听器
        for (ApplicationListener<?> listener : listeners) {
            try {
                // 判断监听器是否匹配当前事件类型
                if (supportsEventType(listener, event)) {
                    log.info("通知监听器：{}", listener.getClass().getSimpleName());
                    
                    // 安全地调用监听器的处理方法
                    invokeListener(listener, event);
                } else {
                    log.debug("监听器 {} 不处理事件 {}", 
                             listener.getClass().getSimpleName(), 
                             event.getClass().getSimpleName());
                }
                
            } catch (Exception e) {
                // 单个监听器异常不影响其他监听器
                log.error("监听器处理事件失败：{}", listener.getClass().getSimpleName(), e);
            }
        }
        
        log.info("========== 事件发布完成 ==========");
    }
    
    /**
     * 安全地调用监听器方法
     * 
     * @param listener 监听器
     * @param event 事件
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        // 通过反射获取泛型类型，确保类型安全
        try {
            Class<?> listenerClass = listener.getClass();
            java.lang.reflect.Type[] genericInterfaces = listenerClass.getGenericInterfaces();
            
            for (java.lang.reflect.Type type : genericInterfaces) {
                if (type instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType parameterizedType = 
                        (java.lang.reflect.ParameterizedType) type;
                    
                    if (parameterizedType.getRawType() == ApplicationListener.class) {
                        java.lang.reflect.Type actualTypeArgument = 
                            parameterizedType.getActualTypeArguments()[0];
                        
                        if (actualTypeArgument instanceof Class) {
                            Class<?> eventType = (Class<?>) actualTypeArgument;
                            
                            // 如果事件类型匹配，进行强制转换后调用
                            if (eventType.isInstance(event)) {
                                // 使用原始类型避免编译错误
                                ((ApplicationListener) listener).onApplicationEvent(event);
                                return;
                            }
                        }
                    }
                }
            }
            
            // 如果没有找到泛型信息，直接调用（兜底方案）
            ((ApplicationListener) listener).onApplicationEvent(event);
            
        } catch (Exception e) {
            log.warn("调用监听器方法时发生异常，使用原始方式调用", e.getMessage());
            ((ApplicationListener) listener).onApplicationEvent(event);
        }
    }
    
    /**
     * 判断监听器是否支持该事件类型
     * 
     * 通过反射获取泛型参数类型进行判断
     * 
     * @param listener 监听器
     * @param event 事件
     * @return true-支持，false-不支持
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean supportsEventType(ApplicationListener<?> listener, ApplicationEvent event) {
        // 通过反射获取监听器实现的接口泛型参数
        try {
            // 获取监听器的类对象
            Class<?> listenerClass = listener.getClass();
            
            // 获取所有接口
            java.lang.reflect.Type[] genericInterfaces = listenerClass.getGenericInterfaces();
            
            // 遍历接口，查找 ApplicationListener
            for (java.lang.reflect.Type type : genericInterfaces) {
                if (type instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType parameterizedType = 
                        (java.lang.reflect.ParameterizedType) type;
                    
                    // 判断是否是 ApplicationListener 接口
                    if (parameterizedType.getRawType() == ApplicationListener.class) {
                        // 获取泛型参数类型
                        java.lang.reflect.Type actualTypeArgument = 
                            parameterizedType.getActualTypeArguments()[0];
                        
                        // 如果是 Class 类型，检查事件是否是其子类或相同类型
                        if (actualTypeArgument instanceof Class) {
                            Class<?> eventType = (Class<?>) actualTypeArgument;
                            return eventType.isInstance(event);
                        }
                    }
                }
            }
            
            // 如果没有找到泛型信息，尝试直接调用（兼容处理）
            ((ApplicationListener) listener).onApplicationEvent(event);
            return true;
            
        } catch (ClassCastException e) {
            return false;
        } catch (Exception e) {
            // 其他异常也认为不支持
            log.debug("判断事件类型支持性时发生异常：{}", e.getMessage());
            return false;
        }
    }
}
