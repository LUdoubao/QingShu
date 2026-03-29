package org.doubao.interview.agent.api.dto.question005;

import lombok.Data;

/**
 * Spring 容器演示请求 DTO
 * 
 * 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
 * 
 * 【类注释】
 * 职责：封装 Spring 容器演示的请求参数
 * 边界：仅用于演示场景
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class ContainerDemoRequest {

    /**
     * 容器类型
     * BeanFactory: 基础容器
     * ApplicationContext: 高级容器
     */
    private String containerType = "ApplicationContext";

    /**
     * Bean 名称
     */
    private String beanName;

    /**
     * 是否延迟加载
     */
    private Boolean lazyInit = false;

    /**
     * 备注说明
     */
    private String remark;
}
