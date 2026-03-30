package org.doubao.ioc.annotation;

import java.lang.annotation.*;

/**
 * 组件注解 - 标识一个类是 Spring 管理的 Bean
 * <p>
 * 类似 Spring 的 @Component
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    
    /**
     * Bean 的名称
     * 默认为空则使用类名首字母小写
     */
    String value() default "";
}
