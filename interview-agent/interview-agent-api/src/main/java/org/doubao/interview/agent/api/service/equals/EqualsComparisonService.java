package org.doubao.interview.agent.api.service.equals;

import org.doubao.interview.agent.api.dto.equals.EqualsComparisonRequest;
import org.doubao.interview.agent.api.dto.equals.EqualsComparisonResponse;

/**
 * ==和 equals() 区别演示服务接口
 * <p>
 * 提供==和 equals() 方法比较的演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface EqualsComparisonService {

    /**
     * 演示比较操作
     * <p>
     * 根据请求的比较类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的比较类型
     * @return 演示结果响应，包含比较说明、示例代码、对比表格等
     */
    EqualsComparisonResponse demonstrateComparison(EqualsComparisonRequest request);
}
