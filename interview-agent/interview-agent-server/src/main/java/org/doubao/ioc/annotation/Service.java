package org.doubao.ioc.annotation;

import java.lang.annotation.*;

/**
 * 服务注解 - 标识一个类是 Service Bean
 * <p>
 * 类似 Spring 的 @Service
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component  // 本身也是 Component
public @interface Service {
    
    String value() default "";
}
