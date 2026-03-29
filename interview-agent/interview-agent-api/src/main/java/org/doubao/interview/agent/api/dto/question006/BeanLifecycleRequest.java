package org.doubao.interview.agent.api.dto.question006;

import lombok.Data;

/**
 * Spring Bean 生命周期演示请求 DTO
 * 
 * 对应面试知识点：问题 006 - Spring Bean 的生命周期
 * 
 * 【类注释】
 * 职责：封装 Bean 生命周期演示的请求参数
 * 边界：仅用于演示场景
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class BeanLifecycleRequest {

    /**
     * Bean 名称
     */
    private String beanName;

    /**
     * 是否演示完整生命周期
     * true: 演示从创建到销毁的完整流程
     * false: 仅演示关键节点
     */
    private Boolean fullLifecycle = true;

    /**
     * 是否包含后处理器演示
     */
    private Boolean includePostProcessor = true;

    /**
     * 备注说明
     */
    private String remark;
}
