package org.doubao.interview.agent.api.dto.method;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 方法重载和重写演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的方法特性类型
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
public class MethodFeatureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的特征类型
     * 可选值：OVERLOAD(重载)、OVERRIDE(重写)
     */
    private String featureType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据
     */
    private String demoParameter;
}
