package org.doubao.interview.agent.api.service.abstractinterface;

import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonRequest;
import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonResponse;

/**
 * 接口和抽象类区别演示服务接口
 * <p>
 * 提供接口和抽象类的对比演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface AbstractInterfaceComparisonService {

    /**
     * 演示接口或抽象类特性
     * <p>
     * 根据请求的类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的类型
     * @return 演示结果响应，包含特性说明、对比表格等
     */
    AbstractInterfaceComparisonResponse demonstrateFeature(AbstractInterfaceComparisonRequest request);
}
