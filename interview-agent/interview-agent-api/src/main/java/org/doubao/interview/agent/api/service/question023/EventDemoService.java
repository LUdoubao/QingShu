package org.doubao.interview.agent.api.service.question023;

import org.doubao.interview.agent.api.dto.question023.UserRegisterRequest;
import org.doubao.interview.agent.api.dto.question023.UserRegisterResponse;

/**
 * Spring 事件机制演示服务接口
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：提供用户注册功能，演示 Spring 事件的发布和监听
 * 边界：仅用于演示和学习，不包含真实业务系统的复杂处理
 * 线程安全：接口本身不保证线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface EventDemoService {

    /**
     * 用户注册 - 发布事件并触发监听器
     * 
     * 【方法注释】
     * 输入约束：userName 和 email 不能为空
     * 输出语义：返回注册结果和事件处理信息
     * 异常场景：参数校验失败时抛出 BusinessException
     * 
     * @param request 注册请求参数
     * @return 注册响应，包含事件处理信息
     */
    UserRegisterResponse register(UserRegisterRequest request);

    /**
     * 获取 Spring 事件机制的详细说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的详细说明文档
     * 异常场景：无
     * 
     * @return 详细说明
     */
    String getEventMechanismExplanation();
}
