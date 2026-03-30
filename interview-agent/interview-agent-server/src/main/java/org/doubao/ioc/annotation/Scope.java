package org.doubao.ioc.annotation;

import java.lang.annotation.*;

/**
 * 作用域注解 - 标识 Bean 的作用域
 * <p>
 * 类似 Spring 的 @Scope
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {
    
    /**
     * 作用域类型
     * singleton: 单例（默认）
     * prototype: 原型（每次获取新实例）
     */
    String value() default "singleton";
}
