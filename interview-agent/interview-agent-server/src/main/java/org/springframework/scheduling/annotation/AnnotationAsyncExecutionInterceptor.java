//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.scheduling.annotation;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;

/**
 * {@link AnnotationAsyncExecutionInterceptor} 是 Spring 中用于支持 {@code @Async} 注解的
 * 异步执行拦截器。它扩展自 {@link AsyncExecutionInterceptor}，专门负责处理标注了 {@link Async}
 * 注解的方法，从中提取执行器（Executor）的限定符（qualifier），以决定使用哪个执行器来异步执行方法。
 *
 * <p>该拦截器会在运行时拦截被 {@code @Async} 注解标记的方法，并将其委托给配置的 {@link Executor}
 * 异步执行。如果注解中指定了 value（例如 {@code @Async("myExecutor")}），拦截器会根据该值
 * 从容器中查找对应的执行器 Bean；如果没有指定，则使用默认执行器。
 *
 * <p>此类由 {@link AsyncAnnotationAdvisor} 创建并注册到 AOP 框架中，通过 {@code @EnableAsync}
 * 或 XML 配置启用异步功能时自动生效。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see AsyncExecutionInterceptor
 * @see Async
 */
public class AnnotationAsyncExecutionInterceptor extends AsyncExecutionInterceptor {

	/**
	 * 构造器，接收一个默认执行器（可能为 null）。
	 * 如果为 null，则会在运行时尝试从 Spring 容器中查找名为 "taskExecutor" 的执行器。
	 *
	 * @param defaultExecutor 默认的异步执行器
	 */
	public AnnotationAsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 构造器，同时接收默认执行器和未捕获异常处理器。
	 *
	 * @param defaultExecutor    默认的异步执行器
	 * @param exceptionHandler   异步方法未捕获异常处理器
	 */
	public AnnotationAsyncExecutionInterceptor(@Nullable Executor defaultExecutor,
											   AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}

	/**
	 * 从目标方法上获取执行器的限定符（qualifier）。重写父类方法以支持从 {@code @Async} 注解
	 * 的 value 属性中提取执行器 Bean 的名称。
	 *
	 * <p>查找逻辑如下：
	 * <ol>
	 *   <li>首先在方法上查找合并后的 {@link Async} 注解（支持组合注解）</li>
	 *   <li>如果方法上没有，则在其声明类上查找</li>
	 *   <li>如果找到，返回注解的 value 值（即执行器 Bean 的名称）</li>
	 *   <li>如果未找到，返回 null，表示使用默认执行器</li>
	 * </ol>
	 *
	 * @param method 要执行的目标方法
	 * @return 执行器的限定符（Bean 名称），若未指定则返回 null
	 */
	@Nullable
	@Override
	protected String getExecutorQualifier(Method method) {
		// 在方法上查找合并的 @Async 注解（支持 Spring 的组合注解语义）
		Async async = AnnotatedElementUtils.findMergedAnnotation(method, Async.class);
		if (async == null) {
			// 如果方法上没有，则在其类上查找
			async = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Async.class);
		}
		// 如果找到了注解，返回其 value() 属性（即执行器名称）
		return async != null ? async.value() : null;
	}
}