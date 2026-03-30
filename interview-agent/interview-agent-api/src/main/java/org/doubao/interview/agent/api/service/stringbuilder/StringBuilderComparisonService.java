package org.doubao.interview.agent.api.service.stringbuilder;

import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonRequest;
import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonResponse;

/**
 * String、StringBuffer、StringBuilder 区别演示服务接口
 * <p>
 * 提供三种字符串类的对比演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface StringBuilderComparisonService {

    /**
     * 演示字符串类特性
     * <p>
     * 根据请求的字符串类类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的字符串类类型
     * @return 演示结果响应，包含特性说明、性能测试、对比表格等
     */
    StringBuilderComparisonResponse demonstrateFeature(StringBuilderComparisonRequest request);
}
