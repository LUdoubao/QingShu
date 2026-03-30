package org.doubao.interview.agent.api.dto.oop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 面向对象三大特征演示 - 响应 DTO
 * <p>
 * 返回面向对象特征的演示结果和说明
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
public class OopFeatureResponse implements Serializable {

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
     * 是否演示成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
