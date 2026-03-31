package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.ApplicationEvent;
import org.doubao.interview.agent.api.service.designpattern.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存预热监听器 - 观察者模式实现
 * 
 * 模拟 Spring 中使用@EventListener 注解的监听器
 * 职责：监听容器刷新事件，执行缓存预热逻辑
 * 
 * 设计要点：
 * 1. 实现 ApplicationListener 接口
 * 2. 指定监听的事件类型（ContextRefreshedEvent）
 * 3. 在 onApplicationEvent 中处理事件
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class CacheWarmupListener implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(CacheWarmupListener.class);
    
    /**
     * 处理容器刷新事件
     * 
     * 当 Spring 容器初始化完成后，自动触发缓存预热
     * 
     * @param event 容器刷新事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("========== 接收到容器刷新事件 ==========");
        log.info("事件源：{}", event.getSource().getClass().getSimpleName());
        log.info("事件时间：{}", new java.util.Date(event.getTimestamp()));
        
        // 执行缓存预热逻辑
        warmupCache();
    }
    
    /**
     * 缓存预热逻辑
     * 
     * 模拟常见的缓存初始化场景：
     * 1. 加载热点数据到 Redis
     * 2. 预加载配置信息
     * 3. 初始化本地缓存
     */
    private void warmupCache() {
        log.info("开始执行缓存预热...");
        
        // 1. 加载热点商品数据
        log.info("[缓存预热] 加载热点商品数据到 Redis...");
        try {
            Thread.sleep(50); // 模拟加载耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[缓存预热] 热点商品数据加载完成，共 1000 条");
        
        // 2. 加载系统配置
        log.info("[缓存预热] 加载系统配置信息...");
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[缓存预热] 系统配置加载完成");
        
        // 3. 初始化本地缓存
        log.info("[缓存预热] 初始化本地缓存（Caffeine/Guava）...");
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[缓存预热] 本地缓存初始化完成");
        
        log.info("========== 缓存预热全部完成 ==========");
    }
}
