package org.doubao.interview.agent.api.service.method;

import org.doubao.interview.agent.api.dto.method.MethodFeatureRequest;
import org.doubao.interview.agent.api.dto.method.MethodFeatureResponse;

/**
 * 方法重载和重写演示服务接口
 * <p>
 * 提供方法重载（Overload）和方法重写（Override）的演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface MethodFeatureService {

    /**
     * 演示方法特性
     * <p>
     * 根据请求的特性类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的特性类型
     * @return 演示结果响应，包含特性说明、示例代码、对比表格等
     */
    MethodFeatureResponse demonstrateFeature(MethodFeatureRequest request);
}
