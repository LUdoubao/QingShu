package org.doubao.interview.agent.api.service.oop;

import org.doubao.interview.agent.api.dto.oop.OopFeatureRequest;
import org.doubao.interview.agent.api.dto.oop.OopFeatureResponse;

/**
 * 面向对象三大特征演示服务接口
 * <p>
 * 提供面向对象编程三大基本特征（封装、继承、多态）的演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface OopFeatureService {

    /**
     * 演示面向对象特征
     * <p>
     * 根据请求的特征类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的特征类型
     * @return 演示结果响应，包含特征说明、示例代码等
     */
    OopFeatureResponse demonstrateFeature(OopFeatureRequest request);
}
