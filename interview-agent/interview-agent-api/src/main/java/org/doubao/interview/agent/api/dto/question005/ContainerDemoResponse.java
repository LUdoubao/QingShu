package org.doubao.interview.agent.api.dto.question005;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Spring 容器演示响应 DTO
 * 
 * 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
 * 
 * 【类注释】
 * 职责：返回 Spring 容器的功能和 Bean 信息
 * 边界：仅展示容器特性，不涉及底层实现细节
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerDemoResponse {

    /**
     * 演示是否成功
     */
    private Boolean success;

    /**
     * 容器类型
     */
    private String containerType;

    /**
     * 容器功能对比
     */
    private ContainerComparison comparison;

    /**
     * Bean 定义列表
     */
    private List<BeanInfo> beanList;

    /**
     * 额外功能演示结果
     */
    private Map<String, Object> additionalFeatures;

    /**
     * Bean 信息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeanInfo {
        /**
         * Bean 名称
         */
        private String beanName;

        /**
         * Bean 类型
         */
        private String beanType;

        /**
         * 作用域（singleton/prototype）
         */
        private String scope;

        /**
         * 是否单例
         */
        private Boolean singleton;

        /**
         * 初始化方法
         */
        private String initMethod;

        /**
         * 销毁方法
         */
        private String destroyMethod;
    }

    /**
     * 容器功能对比类
     * 
     * 【类注释】
     * 职责：展示 BeanFactory 和 ApplicationContext 的功能差异
     * 
     * BeanFactory: 基础容器，提供最核心的 Bean 管理
     * ApplicationContext: 高级容器，在 BeanFactory 基础上扩展了更多企业级功能
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerComparison {
        /**
         * Bean 实例化时机
         * BeanFactory: 延迟加载（第一次使用时创建）
         * ApplicationContext: 启动时预加载（所有 singleton Bean）
         */
        private String initializationTiming;

        /**
         * Bean 生命周期管理
         * BeanFactory: 不完整（需要手动调用 close）
         * ApplicationContext: 完整（自动管理）
         */
        private String lifecycleManagement;

        /**
         * 国际化支持（MessageSource）
         */
        private Boolean i18nSupport;

        /**
         * 事件机制（ApplicationEvent）
         */
        private Boolean eventMechanism;

        /**
         * AOP 集成支持
         */
        private Boolean aopSupport;

        /**
         * 资源访问抽象（ResourceLoader）
         */
        private Boolean resourceAccess;

        /**
         * 父容器支持（层次化容器）
         */
        private Boolean parentContainerSupport;

        /**
         * 使用场景建议
         */
        private String recommendedUsage;
    }
}
