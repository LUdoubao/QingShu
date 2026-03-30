package org.doubao.interview.agent.api.dto.equals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ==和 equals() 区别演示 - 响应 DTO
 * <p>
 * 返回==和 equals() 比较的演示结果和说明
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
public class EqualsComparisonResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 比较类型
     */
    private String comparisonType;

    /**
     * 类型名称（中文）
     */
    private String typeName;

    /**
     * 演示结果描述
     */
    private String description;

    /**
     * 核心要点说明
     */
    private String keyPoints;

    /**
     * 示例代码片段
     */
    private String codeExample;

    /**
     * 比较结果表格（Markdown 格式）
     */
    private String comparisonTable;

    /**
     * 是否演示成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
