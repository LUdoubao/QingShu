//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.web.reactive.result.method.annotation;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.result.method.InvocableHandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux 框架中支持 {@code @RequestMapping} 注解的处理器适配器。
 * 该适配器负责将请求分发到控制器方法，并处理方法参数解析、返回值处理、模型初始化以及异常处理。
 *
 * <p>主要功能：
 * <ul>
 *   <li>支持通过 {@code @RequestMapping} 及其组合注解（如 {@code @GetMapping}）标注的处理器方法。</li>
 *   <li>解析方法参数：利用 {@link ControllerMethodResolver} 解析出方法参数解析器（如 {@code @RequestBody}、{@code @RequestParam}、{@code @PathVariable} 等）。</li>
 *   <li>处理异步返回值：通过 {@link ReactiveAdapterRegistry} 支持 Mono、Flux 等响应式类型。</li>
 *   <li>模型初始化：通过 {@link ModelInitializer} 处理 {@code @ModelAttribute} 方法及模型属性。</li>
 *   <li>异常处理：支持 {@code @ExceptionHandler} 方法，用于处理控制器内抛出的异常。</li>
 * </ul>
 *
 * <p>该适配器在初始化（{@link #afterPropertiesSet()}）时需要从 Spring 容器中获取配置，并构建必要的解析器和初始器。
 * 通常由 {@link org.springframework.web.reactive.config.EnableWebFlux} 注解或 WebFlux 配置类自动注册。
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 * @see ControllerMethodResolver
 * @see InvocableHandlerMethod
 * @see ModelInitializer
 */
public class RequestMappingHandlerAdapter implements HandlerAdapter, ApplicationContextAware, InitializingBean {

	private static final Log logger = LogFactory.getLog(RequestMappingHandlerAdapter.class);

	/** HTTP 消息读取器列表，用于读取请求体并转换为方法参数（如 @RequestBody） */
	private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();

	/** Web 绑定初始化器，用于创建数据绑定和类型转换的上下文 */
	@Nullable
	private WebBindingInitializer webBindingInitializer;

	/** 参数解析器配置器，允许用户注册自定义的参数解析器 */
	@Nullable
	private ArgumentResolverConfigurer argumentResolverConfigurer;

	/** 响应式适配器注册表，用于处理 Mono、Flux 等响应式类型的返回值 */
	@Nullable
	private ReactiveAdapterRegistry reactiveAdapterRegistry;

	/** Spring 应用上下文，用于获取容器中的 Bean 和配置 */
	@Nullable
	private ConfigurableApplicationContext applicationContext;

	/** 控制器方法解析器，负责解析方法参数、返回值类型、异常处理方法等 */
	@Nullable
	private ControllerMethodResolver methodResolver;

	/** 模型初始化器，负责处理 @ModelAttribute 方法和模型属性的初始化 */
	@Nullable
	private ModelInitializer modelInitializer;

	/**
	 * 默认构造函数，无特殊配置。
	 */
	public RequestMappingHandlerAdapter() {
	}

	/**
	 * 设置 HTTP 消息读取器列表。如果不设置，默认会通过 {@link ServerCodecConfigurer#getReaders()} 获取。
	 *
	 * @param messageReaders 消息读取器列表
	 */
	public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
		Assert.notNull(messageReaders, "'messageReaders' must not be null");
		this.messageReaders = messageReaders;
	}

	/**
	 * 获取当前的消息读取器列表。
	 *
	 * @return 消息读取器列表
	 */
	public List<HttpMessageReader<?>> getMessageReaders() {
		return this.messageReaders;
	}

	/**
	 * 设置 Web 绑定初始化器，用于配置数据绑定、类型转换、验证器等。
	 *
	 * @param webBindingInitializer 绑定初始化器
	 */
	public void setWebBindingInitializer(@Nullable WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}

	/**
	 * 获取 Web 绑定初始化器。
	 *
	 * @return 绑定初始化器，可能为 null
	 */
	@Nullable
	public WebBindingInitializer getWebBindingInitializer() {
		return this.webBindingInitializer;
	}

	/**
	 * 设置参数解析器配置器，用于注册自定义的参数解析器。
	 *
	 * @param configurer 配置器实例
	 */
	public void setArgumentResolverConfigurer(@Nullable ArgumentResolverConfigurer configurer) {
		this.argumentResolverConfigurer = configurer;
	}

	/**
	 * 获取参数解析器配置器。
	 *
	 * @return 配置器实例，可能为 null
	 */
	@Nullable
	public ArgumentResolverConfigurer getArgumentResolverConfigurer() {
		return this.argumentResolverConfigurer;
	}

	/**
	 * 设置响应式适配器注册表，用于支持响应式类型的返回值。
	 *
	 * @param registry 注册表实例
	 */
	public void setReactiveAdapterRegistry(@Nullable ReactiveAdapterRegistry registry) {
		this.reactiveAdapterRegistry = registry;
	}

	/**
	 * 获取响应式适配器注册表。
	 *
	 * @return 注册表实例，可能为 null
	 */
	@Nullable
	public ReactiveAdapterRegistry getReactiveAdapterRegistry() {
		return this.reactiveAdapterRegistry;
	}

	/**
	 * 设置 Spring 应用上下文，该适配器需要从中获取 Bean 和配置。
	 * 如果传入的上下文是 ConfigurableApplicationContext，则保存；否则忽略。
	 *
	 * @param applicationContext 应用上下文
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		}
	}

	/**
	 * 在属性设置完成后进行初始化，构建方法解析器和模型初始化器。
	 * 该方法会在 Spring 容器完成依赖注入后自动调用。
	 *
	 * @throws Exception 如果初始化失败
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.applicationContext, "ApplicationContext is required");

		// 如果未设置消息读取器，则使用默认的 ServerCodecConfigurer 创建一组默认的读取器
		if (CollectionUtils.isEmpty(this.messageReaders)) {
			ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();
			this.messageReaders = codecConfigurer.getReaders();
		}

		// 如果未设置参数解析器配置器，则创建一个默认的（用于后续注册默认解析器）
		if (this.argumentResolverConfigurer == null) {
			this.argumentResolverConfigurer = new ArgumentResolverConfigurer();
		}

		// 如果未设置响应式适配器注册表，则使用全局共享实例
		if (this.reactiveAdapterRegistry == null) {
			this.reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
		}

		// 创建控制器方法解析器，它负责解析方法参数、返回值、异常处理方法等
		this.methodResolver = new ControllerMethodResolver(
				this.argumentResolverConfigurer,
				this.reactiveAdapterRegistry,
				this.applicationContext,
				this.messageReaders);

		// 创建模型初始化器，用于处理 @ModelAttribute 方法和模型属性
		this.modelInitializer = new ModelInitializer(this.methodResolver, this.reactiveAdapterRegistry);
	}

	/**
	 * 判断此适配器是否支持给定的处理器对象。
	 * 支持的类型为 {@link HandlerMethod}，即通过 {@link org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping}
	 * 映射到的处理器方法。
	 *
	 * @param handler 处理器对象
	 * @return true 如果支持
	 */
	@Override
	public boolean supports(Object handler) {
		return handler instanceof HandlerMethod;
	}

	/**
	 * 处理请求的核心方法。
	 * 该方法执行以下步骤：
	 * <ol>
	 *   <li>将处理器对象转换为 HandlerMethod</li>
	 *   <li>创建绑定上下文（包含数据绑定、验证等）</li>
	 *   <li>获取可调用的处理器方法（InvocableHandlerMethod）</li>
	 *   <li>通过模型初始化器初始化模型（调用 @ModelAttribute 方法等）</li>
	 *   <li>调用控制器方法，获取结果（Mono&lt;HandlerResult&gt;）</li>
	 *   <li>设置结果中的异常处理器（用于后续异常处理）</li>
	 *   <li>保存模型到绑定上下文</li>
	 *   <li>若调用过程中出现异常，则委托给 {@link #handleException} 处理</li>
	 * </ol>
	 *
	 * @param exchange 当前服务器交换对象，包含请求和响应
	 * @param handler  处理器对象，必须是 HandlerMethod 类型
	 * @return Mono 包装的 HandlerResult，表示处理结果
	 */
	@Override
	public Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler) {
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		// 确保已初始化
		Assert.state(this.methodResolver != null && this.modelInitializer != null, "Not initialized");

		// 创建绑定上下文，其中包含 @InitBinder 方法
		InitBinderBindingContext bindingContext = new InitBinderBindingContext(
				this.getWebBindingInitializer(),
				this.methodResolver.getInitBinderMethods(handlerMethod));

		// 获取可调用的处理器方法（已解析参数和返回值）
		InvocableHandlerMethod invocableMethod = this.methodResolver.getRequestMappingMethod(handlerMethod);

		// 定义异常处理函数：如果发生异常，调用 handleException 方法
		Function<Throwable, Mono<HandlerResult>> exceptionHandler = ex -> this.handleException(ex, handlerMethod, bindingContext, exchange);

		// 执行流程：初始化模型 → 调用控制器方法 → 设置异常处理器 → 保存模型 → 捕获异常并处理
		return this.modelInitializer.initModel(handlerMethod, bindingContext, exchange)
				.then(Mono.defer(() -> invocableMethod.invoke(exchange, bindingContext)))
				.doOnNext(result -> result.setExceptionHandler(exceptionHandler))
				.doOnNext(result -> bindingContext.saveModel())
				.onErrorResume(exceptionHandler);
	}

	/**
	 * 处理控制器方法执行过程中抛出的异常。
	 * 该方法会查找适用于当前异常和处理器方法的 {@code @ExceptionHandler} 方法，
	 * 如果找到则调用它，并返回处理结果；否则重新抛出原始异常。
	 *
	 * @param exception       抛出的异常
	 * @param handlerMethod   原始的处理器方法
	 * @param bindingContext  绑定上下文
	 * @param exchange        服务器交换对象
	 * @return 异常处理结果的 Mono，如果未找到处理器则返回错误 Mono
	 */
	private Mono<HandlerResult> handleException(Throwable exception,
												HandlerMethod handlerMethod,
												BindingContext bindingContext,
												ServerWebExchange exchange) {
		Assert.state(this.methodResolver != null, "Not initialized");

		// 清除可能已设置的 produces 媒体类型属性，避免影响异常响应的内容协商
		exchange.getAttributes().remove(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		// 清除响应头中的内容相关头信息，为异常响应做准备
		exchange.getResponse().getHeaders().clearContentHeaders();

		// 查找适用于当前异常和处理器方法的 @ExceptionHandler 方法
		InvocableHandlerMethod invocable = this.methodResolver.getExceptionHandlerMethod(exception, handlerMethod);
		if (invocable != null) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug(exchange.getLogPrefix() + "Using @ExceptionHandler " + invocable);
				}

				// 清除模型，避免之前模型中的属性干扰异常处理结果
				bindingContext.getModel().asMap().clear();

				// 获取异常的 cause，如果存在则一并传递给异常处理器方法
				Throwable cause = exception.getCause();
				if (cause != null) {
					// 调用异常处理方法，传递 exception、cause 和 handlerMethod 参数
					return invocable.invoke(exchange, bindingContext, exception, cause, handlerMethod);
				} else {
					return invocable.invoke(exchange, bindingContext, exception, handlerMethod);
				}
			} catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn(exchange.getLogPrefix() + "Failure in @ExceptionHandler " + invocable, ex);
				}
			}
		}

		// 如果没有找到合适的异常处理器，则继续传播原始异常
		return Mono.error(exception);
	}
}