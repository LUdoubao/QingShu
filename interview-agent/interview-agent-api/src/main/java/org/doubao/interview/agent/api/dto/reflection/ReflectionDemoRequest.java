package org.doubao.interview.agent.api.dto.reflection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Java 反射机制演示 - 请求 DTO
 * <p>
 * 用于接收客户端请求，指定要演示的反射操作类型
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
public class ReflectionDemoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要演示的反射操作类型
     * 可选值：GET_CLASS(获取 Class 对象)、CREATE_INSTANCE(创建实例)、
     * INVOKE_METHOD(调用方法)、ACCESS_FIELD(访问属性)、
     * GET_CONSTRUCTOR(获取构造器)
     */
    private String operationType;

    /**
     * 类名（全限定名）
     */
    private String className;

    /**
     * 方法名（可选）
     */
    private String methodName;

    /**
     * 属性名（可选）
     */
    private String fieldName;

    /**
     * 方法参数值（可选，JSON 字符串）
     */
    private String methodParams;

    /**
     * 属性值（可选）
     */
    private String fieldValue;
}
