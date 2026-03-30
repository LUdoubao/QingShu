package org.doubao.interview.agent.api.dto.reflection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Java 反射机制演示 - 响应 DTO
 * <p>
 * 返回反射操作的演示结果和说明
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
public class ReflectionDemoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作名称（中文）
     */
    private String operationName;

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
     * 反射 API 使用示例
     */
    private String reflectionApiUsage;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 实际运行结果（如果有）
     */
    private Map<String, Object> actualResult;

    /**
     * 是否演示成功
     */
    private Boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
