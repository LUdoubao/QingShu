package org.doubao.interview.agent.api.service.question005;

import org.doubao.interview.agent.api.dto.question005.ContainerDemoRequest;
import org.doubao.interview.agent.api.dto.question005.ContainerDemoResponse;

/**
 * Spring 容器演示服务接口
 * 
 * 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
 * 
 * 【类注释】
 * 职责：提供 Spring 容器功能演示，对比 BeanFactory 和 ApplicationContext 的差异
 * 边界：仅用于演示和学习，不包含真实业务系统的复杂处理
 * 线程安全：接口本身不保证线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface SpringContainerDemoService {

    /**
     * 演示 Spring 容器功能
     * 
     * 【方法注释】
     * 输入约束：containerType 必须是 BeanFactory 或 ApplicationContext
     * 输出语义：返回容器功能对比和 Bean 信息
     * 异常场景：参数校验失败时抛出 IllegalArgumentException
     * 
     * @param request 演示请求参数
     * @return 容器功能对比响应
     */
    ContainerDemoResponse demoContainer(ContainerDemoRequest request);

    /**
     * 获取两种容器的详细对比说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的详细说明文档
     * 异常场景：无
     * 
     * @return 详细对比说明
     */
    String getDetailedComparison();
}
