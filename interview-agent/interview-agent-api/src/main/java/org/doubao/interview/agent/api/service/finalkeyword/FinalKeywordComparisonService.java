package org.doubao.interview.agent.api.service.finalkeyword;

import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonRequest;
import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonResponse;

/**
 * final、finally、finalize 区别演示服务接口
 * <p>
 * 提供三个关键字的对比演示功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
public interface FinalKeywordComparisonService {

    /**
     * 演示 final、finally、finalize 特性
     * <p>
     * 根据请求的关键字类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的关键字类型
     * @return 演示结果响应，包含特性说明、对比表格等
     */
    FinalKeywordComparisonResponse demonstrateFeature(FinalKeywordComparisonRequest request);
}
