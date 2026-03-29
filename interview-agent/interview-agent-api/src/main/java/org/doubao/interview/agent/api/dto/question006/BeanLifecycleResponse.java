package org.doubao.interview.agent.api.dto.question006;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Spring Bean 生命周期演示响应 DTO
 * 
 * 对应面试知识点：问题 006 - Spring Bean 的生命周期
 * 
 * 【类注释】
 * 职责：返回 Bean 生命周期的各个阶段和详细信息
 * 边界：仅展示生命周期流程，不涉及底层实现细节
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeanLifecycleResponse {

    /**
     * 演示是否成功
     */
    private Boolean success;

    /**
     * Bean 名称
     */
    private String beanName;

    /**
     * 生命周期阶段列表
     */
    private List<LifecyclePhase> lifecyclePhases;

    /**
     * 回调方法信息
     */
    private CallbackInfo callbackInfo;

    /**
     * 生命周期阶段类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifecyclePhase {
        /**
         * 阶段序号
         */
        private Integer phaseOrder;

        /**
         * 阶段名称
         */
        private String phaseName;

        /**
         * 阶段描述
         */
        private String description;

        /**
         * 对应的注解或接口
         */
        private String annotationOrInterface;

        /**
         * 是否已执行
         */
        private Boolean executed;

        /**
         * 执行时间（毫秒）
         */
        private Long executionTime;
    }

    /**
     * 回调方法信息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackInfo {
        /**
         * 初始化回调方法
         */
        private String initMethod;

        /**
         * 销毁回调方法
         */
        private String destroyMethod;

        /**
         * BeanPostProcessor 前置处理
         */
        private String postProcessorBefore;

        /**
         * BeanPostProcessor 后置处理
         */
        private String postProcessorAfter;

        /**
         * Aware 接口回调
         */
        private List<String> awareCallbacks;
    }
}
