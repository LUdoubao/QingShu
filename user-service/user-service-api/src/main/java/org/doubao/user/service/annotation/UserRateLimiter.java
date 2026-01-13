package org.doubao.user.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRateLimiter {
	String key(); // 限流键（支持SpEL表达式）
	int count() default 10; // 周期内最大次�?
	int period() default 60; // 周期（秒�?
}
