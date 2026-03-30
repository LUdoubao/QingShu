package org.doubao.interview.agent.api.dto.stringbuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * String、StringBuffer、StringBuilder 区别演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的字符串类类型
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
public class StringBuilderComparisonRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的字符串类类型
     * 可选值：STRING(String)、STRINGBUFFER(StringBuffer)、STRINGBUILDER(StringBuilder)
     */
    private String stringType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据，如循环次数
     */
    private Integer loopCount;
}
