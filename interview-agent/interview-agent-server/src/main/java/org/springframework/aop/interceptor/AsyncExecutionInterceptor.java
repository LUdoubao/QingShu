//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Spring AOP 异步方法执行拦截器。
 * 该类实现了 {@link MethodInterceptor}，用于拦截被异步注解（如 {@code @Async}）标记的方法，
 * 并将方法调用提交到指定的异步执行器（Executor）中执行，从而实现非阻塞的异步处理。
 *
 * <p>它继承自 {@link AsyncExecutionAspectSupport}，该父类提供了执行器解析、异常处理等公共功能。
 * 在运行时，此拦截器会通过 AOP 代理对目标方法进行包装，将同步调用转换为异步任务。
 *
 * <p>该类实现了 {@link Ordered} 接口，并返回最小的顺序值（{@link Integer#MIN_VALUE}），
 * 以确保它优先于其他拦截器执行，从而正确拦截异步方法调用。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see AsyncExecutionAspectSupport
 * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor
 * @see org.springframework.scheduling.annotation.Async
 */
public class AsyncExecutionInterceptor extends AsyncExecutionAspectSupport implements MethodInterceptor, Ordered {

	/**
	 * 构造函数，接收默认的执行器。该执行器将用于没有显式指定执行器的异步方法。
	 *
	 * @param defaultExecutor 默认执行器，如果为 {@code null}，则使用 {@link SimpleAsyncTaskExecutor}
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 构造函数，同时接收默认执行器和未捕获异常处理器。
	 *
	 * @param defaultExecutor   默认执行器
	 * @param exceptionHandler  异步方法抛出未捕获异常时的处理器
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor,
									 AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}

	/**
	 * AOP 拦截方法，将同步调用转换为异步执行。
	 *
	 * <p>执行流程：
	 * <ol>
	 *   <li>获取目标类和方法（处理桥接方法）</li>
	 *   <li>通过父类方法确定要使用的异步执行器（Executor）</li>
	 *   <li>创建一个 {@link Callable} 任务，在任务内部调用原始方法并处理异常</li>
	 *   <li>通过父类的 {@link #doSubmit} 方法将任务提交给执行器</li>
	 *   <li>返回结果（如果是 {@link Future} 类型，则等待结果；否则返回 {@code null}）</li>
	 * </ol>
	 *
	 * @param invocation 方法调用信息，包含目标对象、方法、参数等
	 * @return 如果异步方法返回 {@link Future}，则返回对应的 {@link Future} 对象；否则返回 {@code null}
	 * @throws Throwable 如果执行过程中发生异常（在异步执行中不会抛出到调用方，但内部会处理）
	 */
	@Nullable
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		// 获取目标类（可能为代理对象的真实类）
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
		// 获取最具体的方法（考虑继承关系）
		Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
		// 获取用户声明的原始方法（处理桥接方法，如泛型擦除导致的桥接方法）
		Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

		// 确定执行此异步任务的执行器（Executor）
		AsyncTaskExecutor executor = this.determineAsyncExecutor(userDeclaredMethod);
		if (executor == null) {
			// 如果没有找到任何执行器，抛出异常（在配置中必须提供执行器）
			throw new IllegalStateException(
					"No executor specified and no default executor set on AsyncExecutionInterceptor either");
		}

		// 定义异步任务（Callable）
		Callable<Object> task = () -> {
			try {
				// 执行原始方法调用（通过 AOP 调用链）
				Object result = invocation.proceed();

				// 如果原始方法返回的是 Future，则等待结果（确保异步任务完成）
				if (result instanceof Future) {
					return ((Future<?>) result).get();
				}
			} catch (ExecutionException ex) {
				// 处理执行异常（Future.get() 抛出的包装异常）
				this.handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
			} catch (Throwable ex) {
				// 处理其他异常（方法执行中抛出的异常）
				this.handleError(ex, userDeclaredMethod, invocation.getArguments());
			}
			return null;
		};

		// 将任务提交给执行器，并根据返回类型决定是否返回 Future
		return this.doSubmit(task, executor, invocation.getMethod().getReturnType());
	}

	/**
	 * 获取执行器的限定符（qualifier）。子类可以重写此方法以从注解中提取执行器名称。
	 * 默认实现返回 {@code null}，表示使用默认执行器。
	 *
	 * @param method 目标方法
	 * @return 执行器名称，或 {@code null} 表示使用默认执行器
	 */
	@Nullable
	protected String getExecutorQualifier(Method method) {
		return null;
	}

	/**
	 * 获取默认执行器。如果父类获取不到任何执行器，则返回一个 {@link SimpleAsyncTaskExecutor} 实例。
	 * 该执行器会为每个任务创建一个新线程（不重用线程），适用于简单场景。
	 *
	 * @param beanFactory 当前的 BeanFactory（可能为 {@code null}）
	 * @return 默认执行器，永远不为 {@code null}
	 */
	@Nullable
	protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		// 先尝试从父类获取默认执行器（可能是通过 BeanFactory 查找的“taskExecutor”）
		Executor defaultExecutor = super.getDefaultExecutor(beanFactory);
		// 如果父类没有找到，则创建一个 SimpleAsyncTaskExecutor
		return (defaultExecutor != null ? defaultExecutor : new SimpleAsyncTaskExecutor());
	}

	/**
	 * 返回拦截器的顺序值。返回最小值（{@link Integer#MIN_VALUE}）以确保该拦截器
	 * 在拦截器链中优先执行，这样异步切面能够尽早拦截方法调用。
	 *
	 * @return 顺序值，最小优先级（最高优先权）
	 */
	public int getOrder() {
		return Integer.MIN_VALUE;
	}
}