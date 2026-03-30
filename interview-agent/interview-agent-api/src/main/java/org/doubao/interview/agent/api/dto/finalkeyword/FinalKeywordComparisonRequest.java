package org.doubao.interview.agent.api.dto.finalkeyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * final、finally、finalize 区别演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的关键字类型
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
public class FinalKeywordComparisonRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的关键字类型
     * 可选值：FINAL(final)、FINALLY(finally)、FINALIZE(finalize)
     */
    private String keywordType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据
     */
    private String demoParameter;
}
