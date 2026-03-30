//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.function.SingletonSupplier;

/**
 * 异步执行切面的基础支持类，为 {@link AsyncExecutionInterceptor} 和 AspectJ 版本的异步切面提供公共功能。
 * 负责：
 * <ul>
 *   <li>根据方法上的注解确定要使用的异步执行器（Executor）</li>
 *   <li>将任务提交给执行器，并根据方法返回类型（Future、CompletableFuture、ListenableFuture、void）返回相应结果</li>
 *   <li>处理异步方法执行中抛出的异常（特别是无返回值的异步方法）</li>
 * </ul>
 *
 * <p>此类实现了 {@link BeanFactoryAware}，以便能够从 Spring 容器中按名称或类型查找执行器 Bean。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.0
 * @see AsyncExecutionInterceptor
 * @see org.springframework.scheduling.aspectj.AbstractAsyncExecutionAspect
 */
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {

	/**
	 * 默认的任务执行器 Bean 名称。当没有显式指定执行器且容器中存在多个 TaskExecutor 时，
	 * 会尝试使用此名称的 Bean。
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";

	/**
	 * 日志记录器，用于输出调试或警告信息。
	 */
	protected final Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 方法到执行器的缓存，避免重复解析执行器。
	 * 键为 Method，值为对应的 AsyncTaskExecutor 实例。
	 */
	private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);

	/**
	 * 默认的执行器 Supplier。优先使用通过构造器或 {@link #configure} 方法传入的执行器，
	 * 如果没有，则使用 {@link #getDefaultExecutor} 从容器中获取。
	 */
	private SingletonSupplier<Executor> defaultExecutor;

	/**
	 * 异步方法未捕获异常处理器的 Supplier。如果未指定，默认使用 {@link SimpleAsyncUncaughtExceptionHandler}。
	 */
	private SingletonSupplier<AsyncUncaughtExceptionHandler> exceptionHandler;

	/**
	 * 当前 BeanFactory，用于从容器中查找执行器 Bean。
	 */
	@Nullable
	private BeanFactory beanFactory;

	/**
	 * 构造器，指定默认执行器。
	 *
	 * @param defaultExecutor 默认执行器（可为 null）
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor) {
		// 创建 SingletonSupplier，当需要执行器时，优先使用传入的实例，否则调用 getDefaultExecutor
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		// 异常处理器默认使用 SimpleAsyncUncaughtExceptionHandler
		this.exceptionHandler = SingletonSupplier.of(SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 构造器，指定默认执行器和异常处理器。
	 *
	 * @param defaultExecutor   默认执行器
	 * @param exceptionHandler  异步异常处理器
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor,
									   AsyncUncaughtExceptionHandler exceptionHandler) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 配置执行器和异常处理器（使用 Supplier 形式，支持懒加载）。
	 * 通常在子类初始化时调用，从注解元数据中解析配置。
	 *
	 * @param defaultExecutor   默认执行器 Supplier
	 * @param exceptionHandler  异常处理器 Supplier
	 */
	public void configure(@Nullable Supplier<Executor> defaultExecutor,
						  @Nullable Supplier<AsyncUncaughtExceptionHandler> exceptionHandler) {
		this.defaultExecutor = new SingletonSupplier<>(defaultExecutor, () -> getDefaultExecutor(this.beanFactory));
		this.exceptionHandler = new SingletonSupplier<>(exceptionHandler, SimpleAsyncUncaughtExceptionHandler::new);
	}

	/**
	 * 直接设置默认执行器（实例）。
	 *
	 * @param defaultExecutor 执行器实例
	 */
	public void setExecutor(Executor defaultExecutor) {
		this.defaultExecutor = SingletonSupplier.of(defaultExecutor);
	}

	/**
	 * 直接设置异常处理器（实例）。
	 *
	 * @param exceptionHandler 异常处理器实例
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 设置 BeanFactory，用于从容器中获取执行器 Bean。
	 *
	 * @param beanFactory 当前 BeanFactory
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * 为指定的方法确定异步执行器（AsyncTaskExecutor）。
	 * 该方法会先从缓存中获取，如果没有，则通过 {@link #getExecutorQualifier} 获取执行器限定符，
	 * 然后查找对应的执行器 Bean，并将其包装为 AsyncTaskExecutor（如果还不是）。
	 * 结果会被缓存起来，避免重复查找。
	 *
	 * @param method 目标方法
	 * @return 对应的 AsyncTaskExecutor，如果无法确定则返回 null
	 */
	@Nullable
	protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
		// 先从缓存中获取
		AsyncTaskExecutor executor = this.executors.get(method);
		if (executor == null) {
			// 获取方法上的执行器限定符（由子类实现，例如从 @Async 注解的 value 属性获取）
			String qualifier = this.getExecutorQualifier(method);
			Executor targetExecutor;
			if (StringUtils.hasLength(qualifier)) {
				// 如果有限定符，则根据限定符查找执行器 Bean
				targetExecutor = this.findQualifiedExecutor(this.beanFactory, qualifier);
			} else {
				// 没有限定符，使用默认执行器
				targetExecutor = this.defaultExecutor.get();
			}
			if (targetExecutor == null) {
				return null;
			}
			// 确保目标执行器是 AsyncTaskExecutor 类型
			// 如果已经是 AsyncListenableTaskExecutor，直接使用；否则用 TaskExecutorAdapter 包装
			executor = (targetExecutor instanceof AsyncListenableTaskExecutor ?
					(AsyncListenableTaskExecutor) targetExecutor :
					new TaskExecutorAdapter(targetExecutor));
			// 缓存结果
			this.executors.put(method, executor);
		}
		return executor;
	}

	/**
	 * 获取方法的执行器限定符（qualifier）。子类必须实现此方法，从方法或类上的注解中提取执行器名称。
	 * 例如，对于 {@code @Async("myExecutor")}，应返回 "myExecutor"。
	 *
	 * @param method 目标方法
	 * @return 执行器限定符，可能为 null（表示使用默认执行器）
	 */
	@Nullable
	protected abstract String getExecutorQualifier(Method method);

	/**
	 * 根据限定符在 BeanFactory 中查找指定类型的执行器 Bean。
	 *
	 * @param beanFactory 当前 BeanFactory
	 * @param qualifier   执行器限定符（Bean 名称或 @Qualifier 值）
	 * @return 查找到的 Executor 实例
	 * @throws IllegalStateException 如果 BeanFactory 未设置或找不到符合条件的 Bean
	 */
	@Nullable
	protected Executor findQualifiedExecutor(@Nullable BeanFactory beanFactory, String qualifier) {
		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory must be set on " +
					this.getClass().getSimpleName() + " to access qualified executor '" + qualifier + "'");
		}
		return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, Executor.class, qualifier);
	}

	/**
	 * 获取默认执行器。当没有显式指定执行器且方法上没有限定符时，会调用此方法。
	 * 默认实现会尝试从 BeanFactory 中获取一个 TaskExecutor 类型的 Bean，
	 * 如果有多个，会尝试获取名为 "taskExecutor" 的 Bean；如果只有一个，则返回它。
	 *
	 * @param beanFactory 当前 BeanFactory（可能为 null）
	 * @return 默认执行器，可能为 null
	 */
	@Nullable
	protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		if (beanFactory != null) {
			try {
				// 尝试获取 TaskExecutor 类型的唯一 Bean
				return beanFactory.getBean(TaskExecutor.class);
			} catch (NoUniqueBeanDefinitionException ex) {
				// 有多个 TaskExecutor 类型的 Bean，尝试获取名为 "taskExecutor" 的
				logger.debug("Could not find unique TaskExecutor bean", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				} catch (NoSuchBeanDefinitionException e) {
					if (logger.isInfoEnabled()) {
						logger.info("More than one TaskExecutor bean found within the context, and none is named 'taskExecutor'. " +
								"Mark one of them as primary or name it 'taskExecutor' (possibly as an alias) in order to use it for async processing: " +
								ex.getBeanNamesFound());
					}
				}
			} catch (NoSuchBeanDefinitionException ex) {
				// 没有 TaskExecutor 类型的 Bean，尝试获取名为 "taskExecutor" 的 Executor
				logger.debug("Could not find default TaskExecutor bean", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				} catch (NoSuchBeanDefinitionException e) {
					logger.info("No task executor bean found for async processing: " +
							"no bean of type TaskExecutor and no bean named 'taskExecutor' either");
				}
			}
		}
		return null;
	}

	/**
	 * 将任务提交给执行器，并根据方法的返回类型决定返回什么。
	 *
	 * @param task       要执行的 Callable 任务
	 * @param executor   异步任务执行器
	 * @param returnType 方法的返回类型
	 * @return 执行结果：
	 *         <ul>
	 *           <li>如果返回类型是 {@link CompletableFuture}，则返回一个 CompletableFuture，任务在 CompletableFuture 中执行</li>
	 *           <li>如果返回类型是 {@link ListenableFuture}，则调用 {@link AsyncListenableTaskExecutor#submitListenable} 提交</li>
	 *           <li>如果返回类型是 {@link Future}，则调用 {@link AsyncTaskExecutor#submit} 提交</li>
	 *           <li>否则，直接提交任务，不等待结果，返回 null</li>
	 *         </ul>
	 */
	@Nullable
	protected Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
		// 处理 CompletableFuture 返回类型
		if (CompletableFuture.class.isAssignableFrom(returnType)) {
			// 使用 CompletableFuture.supplyAsync 提交任务，并将可能的异常包装为 CompletionException
			return CompletableFuture.supplyAsync(() -> {
				try {
					return task.call();
				} catch (Throwable ex) {
					throw new CompletionException(ex);
				}
			}, executor);
		}
		// 处理 ListenableFuture 返回类型（Spring 的扩展 Future）
		else if (ListenableFuture.class.isAssignableFrom(returnType)) {
			// 要求 executor 必须是 AsyncListenableTaskExecutor
			return ((AsyncListenableTaskExecutor) executor).submitListenable(task);
		}
		// 处理 Future 返回类型（包括 JDK Future）
		else if (Future.class.isAssignableFrom(returnType)) {
			return executor.submit(task);
		}
		// 无返回值类型（void 或其他），直接提交任务，不关心结果
		else {
			executor.submit(task);
			return null;
		}
	}

	/**
	 * 处理异步方法执行中抛出的异常。
	 * 如果方法返回类型是 Future 的子类，异常将被重新抛出（以便调用方通过 Future.get() 捕获）；
	 * 否则，将异常交给 AsyncUncaughtExceptionHandler 处理。
	 *
	 * @param ex     抛出的异常
	 * @param method 被执行的方法
	 * @param params 方法参数
	 * @throws Exception 如果方法返回 Future，则重新抛出异常
	 */
	protected void handleError(Throwable ex, Method method, Object... params) throws Exception {
		// 如果方法返回 Future 类型，说明调用方可以通过 Future.get() 获取异常，所以直接重新抛出
		if (Future.class.isAssignableFrom(method.getReturnType())) {
			ReflectionUtils.rethrowException(ex);
		}
		// 否则，调用异常处理器处理
		else {
			try {
				this.exceptionHandler.obtain().handleUncaughtException(ex, method, params);
			} catch (Throwable ex2) {
				// 如果异常处理器本身抛出异常，只记录日志，不向上传播
				logger.warn("Exception handler for async method '" + method.toGenericString() +
						"' threw unexpected exception itself", ex2);
			}
		}
	}
}