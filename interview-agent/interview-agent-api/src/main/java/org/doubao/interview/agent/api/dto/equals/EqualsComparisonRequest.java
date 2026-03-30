package org.doubao.interview.agent.api.dto.equals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ==和 equals() 区别演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的比较类型
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
public class EqualsComparisonRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的比较类型
     * 可选值：BASIC(基本类型)、STRING(字符串)、WRAPPER(包装类)、CUSTOM(自定义对象)
     */
    private String comparisonType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据
     */
    private String demoParameter;
}
