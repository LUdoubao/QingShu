package org.doubao.user.service.annotation;
import java.lang.annotation.*;
/**
 * 日志记录注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogRecord {

	/**
	 * 操作类型
	 */
	String operationType();

	/**
	 * 操作描述
	 */
	String description() default "";
}
