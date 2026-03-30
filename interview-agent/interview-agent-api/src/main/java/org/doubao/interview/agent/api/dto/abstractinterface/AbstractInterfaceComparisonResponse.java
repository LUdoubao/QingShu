package org.doubao.interview.agent.api.dto.abstractinterface;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 接口和抽象类区别演示 - 响应 DTO
 * <p>
 * 返回接口和抽象类的对比结果和说明
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
public class AbstractInterfaceComparisonResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 类型名称（中文）
     */
    private String typeName;

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
     * 是否演示成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
