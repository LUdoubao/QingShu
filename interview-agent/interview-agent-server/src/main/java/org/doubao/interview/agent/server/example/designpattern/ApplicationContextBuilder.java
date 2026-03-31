package org.doubao.interview.agent.server.example.designpattern;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 建造者模式示例 - ApplicationContext 构建器
 * 
 * 用于演示 Spring 中复杂对象的构建过程
 * 场景：构建包含多个配置项的 ApplicationContext
 * 
 * 设计要点：
 * 1. 链式调用，代码简洁易读
 * 2. 分步构建复杂对象
 * 3. 支持可选参数和默认值
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@Data
@Accessors(chain = true)
public class ApplicationContextBuilder {
    
    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * 配置文件路径
     */
    private String configLocation;
    
    /**
     * 是否启用懒加载
     */
    private boolean lazyInit = false;
    
    /**
     * 是否扫描注解
     */
    private boolean enableAnnotationScan = true;
    
    /**
     * 基础包路径（用于扫描）
     */
    private String basePackage;
    
    /**
     * 环境标识（dev/test/prod）
     */
    private String activeProfile = "dev";
    
    /**
     * 私有构造函数，强制使用 Builder
     */
    private ApplicationContextBuilder() {
    }
    
    /**
     * 静态工厂方法，获取 Builder 实例
     * 
     * @return ApplicationContextBuilder 实例
     */
    public static ApplicationContextBuilder newInstance() {
        return new ApplicationContextBuilder();
    }
    
    /**
     * 设置应用名称
     * 
     * @param name 应用名称
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder withApplicationName(String name) {
        this.applicationName = name;
        return this;
    }
    
    /**
     * 设置配置文件路径
     * 
     * @param location 配置文件路径（如 classpath:application.yml）
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder withConfigLocation(String location) {
        this.configLocation = location;
        return this;
    }
    
    /**
     * 启用懒加载
     * 
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder withLazyInit() {
        this.lazyInit = true;
        return this;
    }
    
    /**
     * 禁用注解扫描
     * 
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder disableAnnotationScan() {
        this.enableAnnotationScan = false;
        return this;
    }
    
    /**
     * 设置基础包路径
     * 
     * @param pkg 基础包路径（如 org.doubao.example）
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder withBasePackage(String pkg) {
        this.basePackage = pkg;
        return this;
    }
    
    /**
     * 设置激活的环境
     * 
     * @param profile 环境标识（dev/test/prod）
     * @return 返回当前 Builder 实例，支持链式调用
     */
    public ApplicationContextBuilder withActiveProfile(String profile) {
        this.activeProfile = profile;
        return this;
    }
    
    /**
     * 构建最终对象
     * 
     * @return 构建完成的 MockApplicationContext 对象
     * @throws IllegalStateException 当必要参数缺失时抛出
     */
    public MockApplicationContext build() {
        // 参数校验
        if (applicationName == null || applicationName.trim().isEmpty()) {
            throw new IllegalStateException("applicationName is required");
        }
        
        // 创建并返回最终对象
        return new MockApplicationContext(
            applicationName,
            configLocation,
            lazyInit,
            enableAnnotationScan,
            basePackage,
            activeProfile
        );
    }
}
