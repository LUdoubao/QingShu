package org.doubao.interview.agent.api.dto.method;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 方法重载和重写演示 - 响应 DTO
 * <p>
 * 返回方法重载和重写的演示结果和说明
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
public class MethodFeatureResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 特征类型
     */
    private String featureType;

    /**
     * 特征名称（中文）
     */
    private String featureName;

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
     * 对比表格（Markdown 格式）
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
