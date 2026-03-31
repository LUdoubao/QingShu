//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.context.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.NativeDetector;
import org.springframework.core.ResolvableType;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Spring IoC 容器的抽象实现，实现了 {@link ConfigurableApplicationContext} 接口。
 * 该类提供了上下文的核心功能：配置刷新、BeanFactory 管理、事件发布、国际化支持、
 * 生命周期管理等。具体的 BeanFactory 创建和配置由子类实现（如
 * {@link org.springframework.context.support.AbstractRefreshableApplicationContext} 和
 * {@link org.springframework.context.support.GenericApplicationContext}）。
 *
 * <p>该类遵循模板方法设计模式，定义了刷新容器的标准流程：
 * <ol>
 *   <li>准备刷新（prepareRefresh）</li>
 *   <li>获取 BeanFactory（obtainFreshBeanFactory）</li>
 *   <li>准备 BeanFactory（prepareBeanFactory）</li>
 *   <li>后置处理 BeanFactory（postProcessBeanFactory）</li>
 *   <li>调用 BeanFactoryPostProcessor（invokeBeanFactoryPostProcessors）</li>
 *   <li>注册 BeanPostProcessor（registerBeanPostProcessors）</li>
 *   <li>初始化消息源（initMessageSource）</li>
 *   <li>初始化事件多播器（initApplicationEventMulticaster）</li>
 *   <li>刷新模板方法（onRefresh）</li>
 *   <li>注册监听器（registerListeners）</li>
 *   <li>完成 BeanFactory 初始化（finishBeanFactoryInitialization）</li>
 *   <li>完成刷新（finishRefresh）</li>
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @since 1.1.2
 * @see #refresh()
 * @see ConfigurableApplicationContext#refresh()
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

	// ==================== 常量定义 ====================

	/** 消息源 Bean 在 BeanFactory 中的默认名称 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

	/** 生命周期处理器 Bean 的默认名称 */
	public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";

	/** 应用事件多播器 Bean 的默认名称 */
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	/** 是否忽略 Spring Expression Language (SpEL) 的全局标志 */
	private static final boolean shouldIgnoreSpel = SpringProperties.getFlag("spring.spel.ignore");

	// ==================== 成员变量 ====================

	/** 日志记录器，子类也可以使用 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 上下文的唯一标识符 */
	private String id = ObjectUtils.identityToString(this);

	/** 上下文的显示名称，用于日志和调试 */
	private String displayName = ObjectUtils.identityToString(this);

	/** 父应用上下文 */
	@Nullable
	private ApplicationContext parent;

	/** 可配置的环境对象 */
	@Nullable
	private ConfigurableEnvironment environment;

	/** 容器中注册的 BeanFactoryPostProcessor 列表（非 Bean 定义方式添加的） */
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

	/** 上下文启动时间戳（毫秒） */
	private long startupDate;

	/** 标志位：上下文是否处于活动状态（已经刷新且未被关闭） */
	private final AtomicBoolean active = new AtomicBoolean();

	/** 标志位：上下文是否已关闭 */
	private final AtomicBoolean closed = new AtomicBoolean();

	/** 启动/关闭监控对象，用于同步刷新和关闭操作 */
	private final Object startupShutdownMonitor = new Object();

	/** JVM 关闭钩子线程，用于优雅关闭 Spring 容器 */
	@Nullable
	private Thread shutdownHook;

	/** 资源模式解析器，支持通配符路径 */
	private ResourcePatternResolver resourcePatternResolver;

	/** 生命周期处理器，用于管理容器的启动和停止 */
	@Nullable
	private LifecycleProcessor lifecycleProcessor;

	/** 消息源（国际化支持） */
	@Nullable
	private MessageSource messageSource;

	/** 应用事件多播器 */
	@Nullable
	private ApplicationEventMulticaster applicationEventMulticaster;

	/** 应用启动监控（用于收集启动指标） */
	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

	/** 显式注册的应用程序监听器集合（非通过 Bean 定义注册的） */
	private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

	/** 早期应用事件监听器快照（用于在事件多播器初始化前记录监听器） */
	@Nullable
	private Set<ApplicationListener<?>> earlyApplicationListeners;

	/** 早期应用事件集合（在事件多播器初始化前发布的事件会暂存于此） */
	@Nullable
	private Set<ApplicationEvent> earlyApplicationEvents;

	// ==================== 构造方法 ====================

	/**
	 * 无参构造器，创建一个空的上下文实例。
	 * 资源模式解析器默认为 PathMatchingResourcePatternResolver。
	 */
	public AbstractApplicationContext() {
		this.resourcePatternResolver = getResourcePatternResolver();
	}

	/**
	 * 指定父上下文构造器。
	 *
	 * @param parent 父应用上下文
	 */
	public AbstractApplicationContext(@Nullable ApplicationContext parent) {
		this();
		setParent(parent);
	}

	// ==================== 配置方法 ====================

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getApplicationName() {
		return "";
	}

	public void setDisplayName(String displayName) {
		Assert.hasLength(displayName, "Display name must not be empty");
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	@Nullable
	public ApplicationContext getParent() {
		return this.parent;
	}

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = createEnvironment();
		}
		return this.environment;
	}

	/**
	 * 创建默认的环境实例（StandardEnvironment）。
	 * 子类可以重写以提供自定义的环境实现。
	 *
	 * @return 新的环境对象
	 */
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardEnvironment();
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return getBeanFactory();
	}

	@Override
	public long getStartupDate() {
		return this.startupDate;
	}

	// ==================== 事件发布方法 ====================

	@Override
	public void publishEvent(ApplicationEvent event) {
		publishEvent(event, null);
	}

	@Override
	public void publishEvent(Object event) {
		publishEvent(event, null);
	}

	/**
	 * 发布事件的核心方法。
	 * 如果事件多播器尚未初始化（处于早期刷新阶段），则将事件暂存到 earlyApplicationEvents 中；
	 * 否则，通过事件多播器广播给所有监听器。同时，也会将事件传播给父上下文。
	 *
	 * @param event     事件对象（可以是 ApplicationEvent 或任意对象，后者会被包装为 PayloadApplicationEvent）
	 * @param eventType 事件的解析类型（可选，用于泛型事件）
	 */
	protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");

		// 将普通对象包装成 ApplicationEvent
		Object applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		} else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// 早期事件暂存
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add((ApplicationEvent) applicationEvent);
		} else {
			getApplicationEventMulticaster().multicastEvent((ApplicationEvent) applicationEvent, eventType);
		}

		// 向上传播给父上下文
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			} else {
				this.parent.publishEvent(event);
			}
		}
	}

	/**
	 * 获取应用事件多播器（确保已经初始化）。
	 *
	 * @return ApplicationEventMulticaster 实例
	 * @throws IllegalStateException 如果多播器未初始化
	 */
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}

	// ==================== 启动监控 ====================

	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		Assert.notNull(applicationStartup, "applicationStartup should not be null");
		this.applicationStartup = applicationStartup;
	}

	@Override
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	/**
	 * 获取生命周期处理器（确保已初始化）。
	 */
	LifecycleProcessor getLifecycleProcessor() throws IllegalStateException {
		if (this.lifecycleProcessor == null) {
			throw new IllegalStateException("LifecycleProcessor not initialized - call 'refresh' before invoking lifecycle methods via the context: " + this);
		}
		return this.lifecycleProcessor;
	}

	/**
	 * 获取资源模式解析器（默认为 PathMatchingResourcePatternResolver）。
	 *
	 * @return ResourcePatternResolver 实例
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}

	// ==================== 父子上下文操作 ====================

	@Override
	public void setParent(@Nullable ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}

	// ==================== BeanFactoryPostProcessor 管理 ====================

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		Assert.notNull(postProcessor, "BeanFactoryPostProcessor must not be null");
		this.beanFactoryPostProcessors.add(postProcessor);
	}

	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}

	// ==================== 监听器管理 ====================

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		Assert.notNull(listener, "ApplicationListener must not be null");
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		}
		this.applicationListeners.add(listener);
	}

	public Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.applicationListeners;
	}

	// ==================== 核心刷新方法 ====================

	/**
	 * 刷新 Spring 应用上下文，这是容器启动的核心入口。
	 * 整个过程是同步的，通过 startupShutdownMonitor 保证线程安全。
	 *
	 * @throws BeansException 如果刷新过程中发生任何 Bean 相关异常
	 * @throws IllegalStateException 如果上下文已经处于活动状态且不允许重复刷新
	 */
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// 启动步骤监控
			StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

			// 1. 准备刷新：设置启动时间、激活标志、初始化属性源等
			prepareRefresh();

			// 2. 获取（或创建）新的 BeanFactory
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// 3. 准备 BeanFactory：设置类加载器、表达式解析器、忽略某些依赖接口、注册可解析依赖等
			prepareBeanFactory(beanFactory);

			try {
				// 4. 后置处理 BeanFactory（允许子类添加自定义操作）
				postProcessBeanFactory(beanFactory);

				// 5. 调用 BeanFactoryPostProcessor 的钩子（包括手动添加的和容器中注册的）
				StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
				invokeBeanFactoryPostProcessors(beanFactory);
				registerBeanPostProcessors(beanFactory);
				beanPostProcess.end();

				// 6. 初始化消息源（国际化）
				initMessageSource();

				// 7. 初始化应用事件多播器
				initApplicationEventMulticaster();

				// 8. 刷新模板方法（子类可覆盖，例如初始化特殊的 Bean）
				onRefresh();

				// 9. 注册监听器（从容器中获取 ApplicationListener Bean 并添加到多播器）
				registerListeners();

				// 10. 完成 BeanFactory 初始化：实例化所有非懒加载的单例 Bean
				finishBeanFactoryInitialization(beanFactory);

				// 11. 完成刷新：初始化生命周期处理器、发布 ContextRefreshedEvent 等
				finishRefresh();
			} catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + ex);
				}
				// 销毁已创建的单例 Bean
				destroyBeans();
				// 取消刷新（设置 active 为 false）
				cancelRefresh(ex);
				throw ex;
			} finally {
				// 重置公共缓存（如反射缓存、注解缓存等）
				resetCommonCaches();
				contextRefresh.end();
			}
		}
	}

	/**
	 * 准备刷新：初始化上下文状态、属性源校验、保存早期监听器等。
	 */
	protected void prepareRefresh() {
		this.startupDate = System.currentTimeMillis();
		this.closed.set(false);
		this.active.set(true);

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Refreshing " + this);
			} else {
				logger.debug("Refreshing " + getDisplayName());
			}
		}

		// 初始化属性源（子类可覆盖）
		initPropertySources();

		// 验证必需属性是否已设置
		getEnvironment().validateRequiredProperties();

		// 保存早期监听器快照
		if (this.earlyApplicationListeners == null) {
			this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
		} else {
			this.applicationListeners.clear();
			this.applicationListeners.addAll(this.earlyApplicationListeners);
		}

		// 初始化早期事件集合（用于暂存多播器未就绪时发布的事件）
		this.earlyApplicationEvents = new LinkedHashSet<>();
	}

	/**
	 * 初始化属性源，子类可以重写此方法以添加自定义的属性源。
	 */
	protected void initPropertySources() {
		// 默认为空实现
	}

	/**
	 * 获取新的 BeanFactory，会先调用 refreshBeanFactory() 刷新内部的 BeanFactory。
	 *
	 * @return 可配置的 BeanFactory
	 */
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
		return getBeanFactory();
	}

	/**
	 * 准备 BeanFactory：设置常用的基础设施，忽略特定的 Aware 接口，注册可解析依赖等。
	 *
	 * @param beanFactory 需要准备的 BeanFactory
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 设置类加载器
		beanFactory.setBeanClassLoader(getClassLoader());

		// 设置 SpEL 表达式解析器（除非全局禁用了 SpEL）
		if (!shouldIgnoreSpel) {
			beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
		}

		// 注册属性编辑器注册器（用于资源、URL 等类型的属性编辑）
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// 添加 ApplicationContextAwareProcessor，用于处理各种 Aware 回调
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

		// 忽略某些 Aware 接口（由 ApplicationContextAwareProcessor 统一处理，不通过自动装配注入）
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationStartupAware.class);

		// 注册可解析的依赖（用于 @Autowired 等注入）
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// 添加 ApplicationListenerDetector，用于检测实现了 ApplicationListener 的 Bean 并自动注册
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

		// 处理加载时织入（LoadTimeWeaver）相关，仅在不处于 GraalVM 原生镜像且容器中存在 "loadTimeWeaver" Bean 时启用
		if (!NativeDetector.inNativeImage() && beanFactory.containsBean("loadTimeWeaver")) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// 注册默认的单例 Bean：environment、systemProperties、systemEnvironment、applicationStartup
		if (!beanFactory.containsLocalBean("environment")) {
			beanFactory.registerSingleton("environment", getEnvironment());
		}
		if (!beanFactory.containsLocalBean("systemProperties")) {
			beanFactory.registerSingleton("systemProperties", getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean("systemEnvironment")) {
			beanFactory.registerSingleton("systemEnvironment", getEnvironment().getSystemEnvironment());
		}
		if (!beanFactory.containsLocalBean("applicationStartup")) {
			beanFactory.registerSingleton("applicationStartup", getApplicationStartup());
		}
	}

	/**
	 * 后置处理 BeanFactory，供子类扩展，用于在 BeanFactory 创建完成后添加一些特殊的 BeanPostProcessor 等。
	 *
	 * @param beanFactory BeanFactory
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 默认为空
	}

	/**
	 * 调用所有注册的 BeanFactoryPostProcessor（包括手动添加和容器中的）。
	 *
	 * @param beanFactory BeanFactory
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
		if (!NativeDetector.inNativeImage() && beanFactory.getTempClassLoader() == null && beanFactory.containsBean("loadTimeWeaver")) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}

	/**
	 * 注册 BeanPostProcessor 到 BeanFactory。
	 *
	 * @param beanFactory BeanFactory
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
	}

	/**
	 * 初始化消息源（国际化）。如果容器中定义了 "messageSource" Bean，则使用它；
	 * 否则使用一个空的 DelegatingMessageSource。
	 */
	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// 如果是 HierarchicalMessageSource 且父消息源为空，则设置为父上下文的消息源
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Using MessageSource [" + this.messageSource + "]");
			}
		} else {
			// 没有自定义的消息源，使用默认的 DelegatingMessageSource
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
			}
		}
	}

	/**
	 * 初始化应用事件多播器。如果容器中定义了 "applicationEventMulticaster" Bean，则使用它；
	 * 否则使用 SimpleApplicationEventMulticaster。
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster = beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		} else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using [" +
						this.applicationEventMulticaster.getClass().getSimpleName() + "]");
			}
		}
	}

	/**
	 * 初始化生命周期处理器。如果容器中定义了 "lifecycleProcessor" Bean，则使用它；
	 * 否则使用 DefaultLifecycleProcessor。
	 */
	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		} else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using [" +
						this.lifecycleProcessor.getClass().getSimpleName() + "]");
			}
		}
	}

	/**
	 * 模板方法，在 BeanFactory 初始化之后、实例化单例之前调用，子类可覆盖以执行额外的刷新操作。
	 *
	 * @throws BeansException 如果发生异常
	 */
	protected void onRefresh() throws BeansException {
		// 默认为空
	}

	/**
	 * 注册监听器：将手动添加的监听器和容器中定义的 ApplicationListener Bean 添加到事件多播器，
	 * 并处理早期暂存的事件。
	 */
	protected void registerListeners() {
		// 先注册手动添加的监听器
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// 再注册容器中定义的所有 ApplicationListener Bean（非懒加载、非抽象的）
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// 处理早期事件（如果存在）
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}

	/**
	 * 完成 BeanFactory 的初始化：注册 ConversionService、添加嵌入式值解析器、
	 * 实例化 LoadTimeWeaverAware Bean、冻结配置、预实例化所有非懒加载的单例 Bean。
	 *
	 * @param beanFactory BeanFactory
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// 注册 ConversionService（如果存在）
		if (beanFactory.containsBean("conversionService") &&
				beanFactory.isTypeMatch("conversionService", ConversionService.class)) {
			beanFactory.setConversionService(beanFactory.getBean("conversionService", ConversionService.class));
		}

		// 如果没有默认的嵌入式值解析器，则添加一个使用 Environment 解析占位符的解析器
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// 提前初始化 LoadTimeWeaverAware 类型的 Bean
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// 释放临时类加载器
		beanFactory.setTempClassLoader(null);

		// 冻结 BeanDefinition 配置（不允许再修改）
		beanFactory.freezeConfiguration();

		// 实例化所有非懒加载的单例 Bean
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * 完成刷新：清除资源缓存、初始化生命周期处理器、触发 ContextRefreshedEvent、注册 LiveBeansView。
	 */
	protected void finishRefresh() {
		// 清除资源缓存（如 Resource 解析缓存）
		clearResourceCaches();

		// 初始化生命周期处理器
		initLifecycleProcessor();

		// 调用生命周期处理器的 onRefresh 方法（启动所有 SmartLifecycle 的 start 方法）
		getLifecycleProcessor().onRefresh();

		// 发布 ContextRefreshedEvent 事件
		publishEvent(new ContextRefreshedEvent(this));

		// 如果不是原生镜像，向 LiveBeansView 注册当前上下文（用于 JMX 管理）
		if (!NativeDetector.inNativeImage()) {
			LiveBeansView.registerApplicationContext(this);
		}
	}

	/**
	 * 取消刷新：将 active 标志设置为 false。
	 *
	 * @param ex 导致取消的异常
	 */
	protected void cancelRefresh(BeansException ex) {
		this.active.set(false);
	}

	/**
	 * 重置公共缓存，以避免类加载器泄漏。
	 */
	protected void resetCommonCaches() {
		ReflectionUtils.clearCache();
		AnnotationUtils.clearCache();
		ResolvableType.clearCache();
		CachedIntrospectionResults.clearClassLoader(getClassLoader());
	}

	// ==================== 关闭和销毁 ====================

	/**
	 * 注册 JVM 关闭钩子，确保容器在 JVM 退出时能正确关闭。
	 */
	@Override
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			this.shutdownHook = new Thread("SpringContextShutdownHook") {
				@Override
				public void run() {
					synchronized (startupShutdownMonitor) {
						doClose();
					}
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	/**
	 * 销毁上下文（兼容旧 API）。
	 *
	 * @deprecated 请使用 {@link #close()}
	 */
	@Deprecated
	public void destroy() {
		close();
	}

	/**
	 * 关闭上下文，释放所有资源。
	 */
	@Override
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				} catch (IllegalStateException ignored) {
					// 可能已经在关闭中，忽略
				}
			}
		}
	}

	/**
	 * 实际执行关闭操作：发布 ContextClosedEvent、关闭生命周期处理器、销毁单例 Bean、关闭 BeanFactory 等。
	 */
	protected void doClose() {
		// 确保只执行一次
		if (this.active.get() && this.closed.compareAndSet(false, true)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing " + this);
			}

			// 从 LiveBeansView 中注销
			if (!NativeDetector.inNativeImage()) {
				LiveBeansView.unregisterApplicationContext(this);
			}

			// 发布 ContextClosedEvent（忽略异常）
			try {
				publishEvent(new ContextClosedEvent(this));
			} catch (Throwable ex) {
				logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
			}

			// 停止生命周期处理器
			if (this.lifecycleProcessor != null) {
				try {
					this.lifecycleProcessor.onClose();
				} catch (Throwable ex) {
					logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
				}
			}

			// 销毁所有单例 Bean
			destroyBeans();

			// 关闭 BeanFactory
			closeBeanFactory();

			// 子类关闭钩子
			onClose();

			// 恢复早期监听器（用于重新启动）
			if (this.earlyApplicationListeners != null) {
				this.applicationListeners.clear();
				this.applicationListeners.addAll(this.earlyApplicationListeners);
			}

			// 设置 active 为 false
			this.active.set(false);
		}
	}

	/**
	 * 销毁所有单例 Bean。
	 */
	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}

	/**
	 * 子类关闭时的回调。
	 */
	protected void onClose() {
		// 默认为空
	}

	// ==================== 状态查询 ====================

	@Override
	public boolean isActive() {
		return this.active.get();
	}

	/**
	 * 确保 BeanFactory 处于活动状态，否则抛出异常。
	 */
	protected void assertBeanFactoryActive() {
		if (!this.active.get()) {
			if (this.closed.get()) {
				throw new IllegalStateException(getDisplayName() + " has been closed already");
			} else {
				throw new IllegalStateException(getDisplayName() + " has not been refreshed yet");
			}
		}
	}

	// ==================== BeanFactory 委托方法 ====================
	// 以下方法都通过检查 active 状态后委托给内部的 BeanFactory 实现

	@Override
	public Object getBean(String name) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, args);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType, args);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isSingleton(name);
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isPrototype(name);
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	@Nullable
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().getType(name);
	}

	@Override
	@Nullable
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().getType(name, allowFactoryBeanInit);
	}

	@Override
	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType, allowEagerInit);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForAnnotation(annotationType);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansWithAnnotation(annotationType);
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType, allowFactoryBeanInit);
	}

	// ==================== 父级 BeanFactory 访问 ====================

	@Override
	@Nullable
	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	@Override
	public boolean containsLocalBean(String name) {
		return getBeanFactory().containsLocalBean(name);
	}

	/**
	 * 获取内部的父级 BeanFactory（如果父上下文是 ConfigurableApplicationContext，则获取其 BeanFactory；
	 * 否则直接返回父上下文本身）。
	 *
	 * @return 父级 BeanFactory 或 null
	 */
	@Nullable
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent());
	}

	// ==================== 国际化消息方法 ====================

	@Override
	public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}

	/**
	 * 获取消息源（确保已初始化）。
	 */
	private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
	}

	/**
	 * 获取内部的父消息源（用于 HierarchicalMessageSource 的父级设置）。
	 *
	 * @return 父消息源，可能为 null
	 */
	@Nullable
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext ?
				((AbstractApplicationContext) getParent()).messageSource : getParent());
	}

	// ==================== 资源访问方法 ====================

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}

	// ==================== 生命周期方法 ====================

	@Override
	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}

	@Override
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}

	@Override
	public boolean isRunning() {
		return (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning());
	}

	// ==================== 抽象方法（由子类实现） ====================

	/**
	 * 刷新内部的 BeanFactory。子类必须实现该方法来创建或刷新 BeanFactory。
	 *
	 * @throws BeansException 如果刷新失败
	 * @throws IllegalStateException 如果上下文已经处于活动状态且不允许重复刷新
	 */
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	/**
	 * 关闭 BeanFactory。子类必须实现以释放资源。
	 */
	protected abstract void closeBeanFactory();

	/**
	 * 返回当前上下文的 BeanFactory（ConfigurableListableBeanFactory）。
	 *
	 * @return BeanFactory 实例
	 * @throws IllegalStateException 如果 BeanFactory 尚未初始化
	 */
	@Override
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

	// ==================== 其他 ====================

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		sb.append(", started on ").append(new Date(getStartupDate()));
		ApplicationContext parent = getParent();
		if (parent != null) {
			sb.append(", parent: ").append(parent.getDisplayName());
		}
		return sb.toString();
	}

	static {
		// 确保 ContextClosedEvent 类被加载（用于某些工具类）
		ContextClosedEvent.class.getName();
	}
}