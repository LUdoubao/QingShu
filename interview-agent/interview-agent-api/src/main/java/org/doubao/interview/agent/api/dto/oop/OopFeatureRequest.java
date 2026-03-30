package org.doubao.interview.agent.api.dto.oop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 面向对象三大特征演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的面向对象特征类型
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
public class OopFeatureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的特征类型
     * 可选值：ENCAPSULATION(封装)、INHERITANCE(继承)、POLYMORPHISM(多态)
     */
    private String featureType;

    /**
     * 演示参数（可选）
     * 用于传递特定的演示数据
     */
    private String demoParameter;
}
