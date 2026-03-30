package org.doubao.ioc.annotation;

import java.lang.annotation.*;

/**
 * 自动注入注解 - 标识需要依赖注入的字段
 * <p>
 * 类似 Spring 的 @Autowired
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    
    /**
     * 是否必需注入
     * 默认 true，如果找不到 Bean 则抛异常
     */
    boolean required() default true;
}
