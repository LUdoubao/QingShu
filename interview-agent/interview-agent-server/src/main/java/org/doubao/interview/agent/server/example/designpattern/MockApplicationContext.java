package org.doubao.interview.agent.server.example.designpattern;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 模拟应用上下文（建造者模式构建的最终对象）
 * 
 * 对应 Spring 的 ApplicationContext
 * 包含应用的完整配置信息
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@Data
@Slf4j
public class MockApplicationContext {
    
    /**
     * 应用名称
     */
    private final String applicationName;
    
    /**
     * 配置文件路径
     */
    private final String configLocation;
    
    /**
     * 是否启用懒加载
     */
    private final boolean lazyInit;
    
    /**
     * 是否启用注解扫描
     */
    private final boolean enableAnnotationScan;
    
    /**
     * 基础包路径
     */
    private final String basePackage;
    
    /**
     * 激活的环境
     */
    private final String activeProfile;
    
    /**
     * 私有构造函数，只能通过 Builder 创建
     * 
     * @param applicationName 应用名称
     * @param configLocation 配置文件路径
     * @param lazyInit 是否懒加载
     * @param enableAnnotationScan 是否启用注解扫描
     * @param basePackage 基础包路径
     * @param activeProfile 激活的环境
     */
    public MockApplicationContext(String applicationName, String configLocation, 
                                  boolean lazyInit, boolean enableAnnotationScan,
                                  String basePackage, String activeProfile) {
        this.applicationName = applicationName;
        this.configLocation = configLocation;
        this.lazyInit = lazyInit;
        this.enableAnnotationScan = enableAnnotationScan;
        this.basePackage = basePackage;
        this.activeProfile = activeProfile;
        
        log.info("应用上下文已创建：{}", applicationName);
        log.info("环境：{}, 懒加载：{}, 注解扫描：{}", activeProfile, lazyInit, enableAnnotationScan);
    }
    
    /**
     * 初始化应用上下文
     * 
     * 模拟 Spring 容器的初始化过程
     */
    public void init() {
        log.info("开始初始化应用上下文：{}", applicationName);
        
        // 1. 加载配置文件
        if (configLocation != null) {
            log.info("加载配置文件：{}", configLocation);
        }
        
        // 2. 扫描注解
        if (enableAnnotationScan && basePackage != null) {
            log.info("扫描注解包：{}", basePackage);
        }
        
        // 3. 注册 Bean
        log.info("注册 Bean 定义");
        
        // 4. 实例化单例 Bean
        if (!lazyInit) {
            log.info("预实例化所有单例 Bean");
        }
        
        log.info("应用上下文初始化完成：{}", applicationName);
    }
    
    /**
     * 关闭应用上下文
     * 
     * 模拟 Spring 容器的销毁过程
     */
    public void close() {
        log.info("开始关闭应用上下文：{}", applicationName);
        log.info("销毁所有 Bean 实例");
        log.info("释放资源");
        log.info("应用上下文已关闭：{}", applicationName);
    }
}
