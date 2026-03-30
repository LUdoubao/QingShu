//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.function.SingletonSupplier;

/**
 * {@link AsyncAnnotationBeanPostProcessor} 是 Spring 框架中用于处理 {@code @Async} 注解的
 * 核心后置处理器。它通过 AOP 为标注了异步注解的 Bean 创建代理，使得方法调用能够异步执行。
 *
 * <p>该类继承自 {@link AbstractBeanFactoryAwareAdvisingPostProcessor}，后者负责在 Bean 初始化后
 * 自动应用切面（Advisor）。具体来说，它会注册一个 {@link AsyncAnnotationAdvisor} 到 AOP 框架中，
 * 该通知器会拦截所有匹配的异步方法，并使用配置的 {@link Executor} 异步执行。
 *
 * <p>此类通常在以下情况下被注册：
 * <ul>
 *   <li>通过 {@code @EnableAsync} 注解的 PROXY 模式，由 {@link ProxyAsyncConfiguration} 配置类创建</li>
 *   <li>通过 XML 配置 {@code <task:annotation-driven/>} 时也会注册该后置处理器</li>
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @since 3.0
 * @see AsyncAnnotationAdvisor
 * @see #setAsyncAnnotationType
 * @see #setExecutor
 * @see #setExceptionHandler
 */
public class AsyncAnnotationBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

	/**
	 * 默认的异步任务执行器 Bean 名称。当没有显式指定执行器时，Spring 会尝试查找名为
	 * "taskExecutor" 的 Bean，如果不存在则使用一个简单的同步执行器（会退化为同步执行）。
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";

	/**
	 * 日志记录器，用于输出调试或警告信息。
	 */
	protected final Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 提供异步执行器的 Supplier。通过 {@link #configure} 或 {@link #setExecutor} 注入。
	 * 使用 Supplier 可以实现懒加载，避免提前创建可能不需要的实例。
	 */
	@Nullable
	private Supplier<Executor> executor;

	/**
	 * 提供异步方法未捕获异常处理器的 Supplier。当异步方法抛出异常且无法被调用方捕获时，
	 * 会交给该处理器处理。
	 */
	@Nullable
	private Supplier<AsyncUncaughtExceptionHandler> exceptionHandler;

	/**
	 * 要识别的异步注解类型，默认为 {@link Async}。可以通过 {@link #setAsyncAnnotationType}
	 * 设置自定义注解，使该后置处理器也能处理用户自定义的异步注解。
	 */
	@Nullable
	private Class<? extends Annotation> asyncAnnotationType;

	/**
	 * 无参构造器，设置 {@code beforeExistingAdvisors} 为 {@code true}。
	 * 该标志表示在应用所有现有的通知之前，先应用此后置处理器创建的通知，
	 * 确保异步通知能够正确拦截方法调用。
	 */
	public AsyncAnnotationBeanPostProcessor() {
		this.setBeforeExistingAdvisors(true);
	}

	/**
	 * 配置执行器和异常处理器（使用 Supplier 形式，支持懒加载）。
	 * 该方法通常由配置类（如 {@link ProxyAsyncConfiguration}）调用，将解析自
	 * {@code @EnableAsync} 注解的组件传入。
	 *
	 * @param executor        异步执行器的 Supplier
	 * @param exceptionHandler 异步异常处理器的 Supplier
	 */
	public void configure(@Nullable Supplier<Executor> executor,
						  @Nullable Supplier<AsyncUncaughtExceptionHandler> exceptionHandler) {
		this.executor = executor;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 设置异步执行器（直接传入实例）。
	 * 内部会将实例包装为 {@link SingletonSupplier}，使其符合 Supplier 接口。
	 *
	 * @param executor 异步执行器实例
	 */
	public void setExecutor(Executor executor) {
		this.executor = SingletonSupplier.of(executor);
	}

	/**
	 * 设置异步异常处理器（直接传入实例）。
	 * 内部会将实例包装为 {@link SingletonSupplier}。
	 *
	 * @param exceptionHandler 异常处理器实例
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = SingletonSupplier.of(exceptionHandler);
	}

	/**
	 * 设置要识别的异步注解类型。默认情况下，该后置处理器会处理 {@link Async} 注解。
	 * 如果需要支持自定义注解（例如 {@code @MyAsync}），可以通过此方法指定。
	 *
	 * @param asyncAnnotationType 自定义异步注解的 Class 对象
	 */
	public void setAsyncAnnotationType(Class<? extends Annotation> asyncAnnotationType) {
		Assert.notNull(asyncAnnotationType, "'asyncAnnotationType' must not be null");
		this.asyncAnnotationType = asyncAnnotationType;
	}

	/**
	 * 重写父类方法，在 BeanFactory 设置完成后，创建并配置 {@link AsyncAnnotationAdvisor}，
	 * 并将其赋值给父类中的 {@code advisor} 字段。父类 {@link AbstractBeanFactoryAwareAdvisingPostProcessor}
	 * 会在 Bean 初始化后自动将 advisor 应用到目标 Bean 上。
	 *
	 * <p>此方法执行以下步骤：
	 * <ol>
	 *   <li>调用父类方法，确保 BeanFactory 已被注入</li>
	 *   <li>创建 {@link AsyncAnnotationAdvisor} 实例，传入执行器和异常处理器的 Supplier</li>
	 *   <li>如果指定了自定义异步注解类型，则设置到 advisor 中</li>
	 *   <li>将 advisor 的 BeanFactory 设置为当前容器，以便 advisor 能够访问容器中的其他 Bean</li>
	 *   <li>将 advisor 赋值给父类的 {@code advisor} 字段，供 AOP 自动代理机制使用</li>
	 * </ol>
	 *
	 * @param beanFactory 当前 BeanFactory
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		// 调用父类方法，确保父类中的 beanFactory 字段被设置
		super.setBeanFactory(beanFactory);

		// 创建异步通知器，传入执行器和异常处理器的 Supplier
		AsyncAnnotationAdvisor advisor = new AsyncAnnotationAdvisor(this.executor, this.exceptionHandler);

		// 如果指定了自定义异步注解类型，则设置到通知器中
		if (this.asyncAnnotationType != null) {
			advisor.setAsyncAnnotationType(this.asyncAnnotationType);
		}

		// 将 BeanFactory 设置给通知器，以便通知器内部能解析执行器和异常处理器（如果需要）
		advisor.setBeanFactory(beanFactory);

		// 将创建好的通知器赋值给父类中的 advisor 字段
		// 父类会在 postProcessAfterInitialization 方法中调用 advisor 来为 Bean 创建代理
		this.advisor = advisor;
	}
}