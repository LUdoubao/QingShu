package org.doubao.interview.agent.api.service.reflection;

import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoRequest;
import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoResponse;

/**
 * Java 反射机制演示服务接口
 * <p>
 * 提供反射机制的对比演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface ReflectionDemoService {

    /**
     * 演示反射机制特性
     * <p>
     * 根据请求的操作类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的操作类型
     * @return 演示结果响应，包含特性说明、API 使用示例等
     */
    ReflectionDemoResponse demonstrateFeature(ReflectionDemoRequest request);
}
