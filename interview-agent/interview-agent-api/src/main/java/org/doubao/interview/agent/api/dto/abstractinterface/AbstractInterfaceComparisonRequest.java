package org.doubao.interview.agent.api.dto.abstractinterface;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 接口和抽象类区别演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的类型
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
public class AbstractInterfaceComparisonRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的类型
     * 可选值：ABSTRACT_CLASS(抽象类)、INTERFACE(接口)
     */
    private String targetType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据
     */
    private String demoParameter;
}
