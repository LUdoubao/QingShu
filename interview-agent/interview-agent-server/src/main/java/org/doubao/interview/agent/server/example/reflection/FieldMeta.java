package org.doubao.interview.agent.server.example.reflection;

import java.lang.annotation.*;

/**
 * 自定义注解 - 演示反射处理注解
 * <p>
 * 用于标记需要特殊处理的字段
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldMeta {
    
    /**
     * 字段的中文名称
     */
    String chineseName() default "";
    
    /**
     * 是否必填
     */
    boolean required() default false;
    
    /**
     * 最小长度
     */
    int minLength() default 0;
    
    /**
     * 最大长度
     */
    int maxLength() default Integer.MAX_VALUE;
}
