package org.doubao.interview.agent.api.dto.finalkeyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * final、finally、finalize 区别演示 - 响应 DTO
 * <p>
 * 返回三个关键字的对比结果和说明
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalKeywordComparisonResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键字类型
     */
    private String keywordType;

    /**
     * 关键字名称
     */
    private String keywordName;

    /**
     * 演示结果描述
     */
    private String description;

    /**
     * 核心特性说明
     */
    private String keyFeatures;

    /**
     * 示例代码片段
     */
    private String codeExample;

    /**
     * 对比表格（Markdown 格式）
     */
    private String comparisonTable;

    /**
     * 使用场景建议
     */
    private String usageScenarios;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 是否演示成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
