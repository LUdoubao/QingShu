package org.doubao.interview.agent.api.service.question006;

import org.doubao.interview.agent.api.dto.question006.BeanLifecycleRequest;
import org.doubao.interview.agent.api.dto.question006.BeanLifecycleResponse;

/**
 * Spring Bean 生命周期演示服务接口
 * 
 * 对应面试知识点：问题 006 - Spring Bean 的生命周期
 * 
 * 【类注释】
 * 职责：提供 Bean 生命周期的完整演示流程
 * 边界：仅用于演示和学习，不包含真实业务系统的复杂处理
 * 线程安全：接口本身不保证线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface BeanLifecycleDemoService {

    /**
     * 演示 Bean 生命周期
     * 
     * 【方法注释】
     * 输入约束：beanName 不能为空
     * 输出语义：返回完整的生命周期阶段列表
     * 异常场景：参数校验失败时抛出 IllegalArgumentException
     * 
     * @param request 演示请求参数
     * @return Bean 生命周期响应
     */
    BeanLifecycleResponse demoLifecycle(BeanLifecycleRequest request);

    /**
     * 获取 Bean 生命周期的详细说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的详细说明文档
     * 异常场景：无
     * 
     * @return 详细说明
     */
    String getDetailedExplanation();
}
