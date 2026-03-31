//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.WebUtils;

/**
 * Spring Web MVC 的前端控制器（Front Controller），负责接收所有的 HTTP 请求，
 * 并将其分发给合适的处理器（Handler，如 Controller 中的方法），然后处理视图渲染。
 * 它是 Spring MVC 的核心组件，整个请求处理流程的调度中心。
 *
 * <p>DispatcherServlet 继承自 {@link FrameworkServlet}，而 FrameworkServlet 继承自 {@link HttpServlet}，
 * 因此它本质上是一个 Servlet，需要在 web.xml 或通过 Servlet 注册器（如 Spring Boot）配置。
 *
 * <p>主要功能：
 * <ul>
 *   <li>初始化并管理各种策略组件：MultipartResolver、LocaleResolver、ThemeResolver、
 *       HandlerMapping、HandlerAdapter、HandlerExceptionResolver、ViewResolver、FlashMapManager 等。</li>
 *   <li>根据请求找到对应的处理器（通过 HandlerMapping）。</li>
 *   <li>调用处理器执行请求（通过 HandlerAdapter）。</li>
 *   <li>处理异常（通过 HandlerExceptionResolver）。</li>
 *   <li>解析视图并渲染响应（通过 ViewResolver 和 View）。</li>
 *   <li>支持文件上传、国际化、主题、Flash 属性等。</li>
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @since 1.2
 * @see #initStrategies(org.springframework.context.ApplicationContext)
 * @see #doDispatch(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
public class DispatcherServlet extends FrameworkServlet {

	// ==================== 常量定义 ====================

	/** Bean 名称常量：multipartResolver */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";
	/** Bean 名称常量：localeResolver */
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";
	/** Bean 名称常量：themeResolver */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";
	/** Bean 名称常量：handlerMapping（单个时使用） */
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";
	/** Bean 名称常量：handlerAdapter（单个时使用） */
	public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";
	/** Bean 名称常量：handlerExceptionResolver（单个时使用） */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";
	/** Bean 名称常量：viewNameTranslator */
	public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";
	/** Bean 名称常量：viewResolver（单个时使用） */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";
	/** Bean 名称常量：flashMapManager */
	public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

	/** 在 request 中存放 WebApplicationContext 的键 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";
	/** 在 request 中存放 LocaleResolver 的键 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";
	/** 在 request 中存放 ThemeResolver 的键 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";
	/** 在 request 中存放 ThemeSource 的键 */
	public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";
	/** 在 request 中存放输入 FlashMap 的键 */
	public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";
	/** 在 request 中存放输出 FlashMap 的键 */
	public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";
	/** 在 request 中存放 FlashMapManager 的键 */
	public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";
	/** 在 request 中存放异常的键（在错误页面中使用） */
	public static final String EXCEPTION_ATTRIBUTE = DispatcherServlet.class.getName() + ".EXCEPTION";

	/** 用于记录“未找到处理器”日志的类别 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/** 默认策略配置文件的路径（位于 spring-webmvc 模块中） */
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";
	/** 默认策略配置文件中类名的包前缀（用于补全全限定名） */
	private static final String DEFAULT_STRATEGIES_PREFIX = "org.springframework.web.servlet";

	/** 专门用于记录“未找到处理器”的日志记录器 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	/** 加载的默认策略（类名映射） */
	@Nullable
	private static Properties defaultStrategies;

	// ==================== 配置属性 ====================

	/** 是否检测所有 HandlerMapping Bean（包括祖先上下文），默认 true */
	private boolean detectAllHandlerMappings = true;
	/** 是否检测所有 HandlerAdapter Bean，默认 true */
	private boolean detectAllHandlerAdapters = true;
	/** 是否检测所有 HandlerExceptionResolver Bean，默认 true */
	private boolean detectAllHandlerExceptionResolvers = true;
	/** 是否检测所有 ViewResolver Bean，默认 true */
	private boolean detectAllViewResolvers = true;
	/** 当找不到处理器时是否抛出异常（而不是返回 404），默认 false */
	private boolean throwExceptionIfNoHandlerFound = false;
	/** 是否在 include 请求后清理属性，默认 true */
	private boolean cleanupAfterInclude = true;

	// ==================== 策略组件实例 ====================

	@Nullable
	private MultipartResolver multipartResolver;                     // 文件上传解析器
	@Nullable
	private LocaleResolver localeResolver;                           // 国际化解析器
	@Nullable
	private ThemeResolver themeResolver;                             // 主题解析器
	@Nullable
	private List<HandlerMapping> handlerMappings;                    // 处理器映射器列表
	@Nullable
	private List<HandlerAdapter> handlerAdapters;                    // 处理器适配器列表
	@Nullable
	private List<HandlerExceptionResolver> handlerExceptionResolvers;// 异常解析器列表
	@Nullable
	private RequestToViewNameTranslator viewNameTranslator;          // 视图名转换器（当没有显式视图名时使用）
	@Nullable
	private FlashMapManager flashMapManager;                         // Flash 属性管理器
	@Nullable
	private List<ViewResolver> viewResolvers;                        // 视图解析器列表

	/** 标记是否使用路径模式解析请求（由 HandlerMapping 决定） */
	private boolean parseRequestPath;

	// ==================== 构造器 ====================

	public DispatcherServlet() {
		// 设置父类属性：允许处理 OPTIONS 请求
		this.setDispatchOptionsRequest(true);
	}

	public DispatcherServlet(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
		this.setDispatchOptionsRequest(true);
	}

	// ==================== Setter 方法 ====================

	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
		this.detectAllHandlerAdapters = detectAllHandlerAdapters;
	}

	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	public void setThrowExceptionIfNoHandlerFound(boolean throwExceptionIfNoHandlerFound) {
		this.throwExceptionIfNoHandlerFound = throwExceptionIfNoHandlerFound;
	}

	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}

	// ==================== 初始化方法 ====================

	/**
	 * 当 ApplicationContext 刷新后调用，初始化所有策略组件。
	 * 此方法由父类 FrameworkServlet 在 refresh 时调用。
	 *
	 * @param context 当前 WebApplicationContext
	 */
	@Override
	protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}

	/**
	 * 初始化所有策略组件：multipartResolver、localeResolver、themeResolver、
	 * handlerMappings、handlerAdapters、handlerExceptionResolvers、
	 * viewNameTranslator、viewResolvers、flashMapManager。
	 *
	 * @param context 当前 WebApplicationContext
	 */
	protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
	}

	// --- 以下是各个策略的初始化方法 ---

	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.multipartResolver);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.multipartResolver.getClass().getSimpleName());
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// 如果没有配置，则设置为 null（不支持文件上传）
			this.multipartResolver = null;
			if (logger.isTraceEnabled()) {
				logger.trace("No MultipartResolver '" + MULTIPART_RESOLVER_BEAN_NAME + "' declared");
			}
		}
	}

	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.localeResolver);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.localeResolver.getClass().getSimpleName());
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// 使用默认策略（AcceptHeaderLocaleResolver）
			this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No LocaleResolver '" + LOCALE_RESOLVER_BEAN_NAME + "': using default [" +
						this.localeResolver.getClass().getSimpleName() + "]");
			}
		}
	}

	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.themeResolver);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.themeResolver.getClass().getSimpleName());
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// 使用默认策略（FixedThemeResolver）
			this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No ThemeResolver '" + THEME_RESOLVER_BEAN_NAME + "': using default [" +
						this.themeResolver.getClass().getSimpleName() + "]");
			}
		}
	}

	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;
		if (this.detectAllHandlerMappings) {
			// 查找所有 HandlerMapping 类型的 Bean（包括祖先上下文）
			Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// 根据 @Order 或 Ordered 接口排序
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		} else {
			try {
				// 只查找指定名称的 Bean
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			} catch (NoSuchBeanDefinitionException ex) {
				// 忽略
			}
		}

		// 如果仍没有找到，则使用默认策略（从 DispatcherServlet.properties 中加载）
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerMappings declared for servlet '" + getServletName() +
						"': using default strategies from DispatcherServlet.properties");
			}
		}

		// 检查是否有任何一个 HandlerMapping 使用路径模式（用于解析请求路径）
		for (HandlerMapping mapping : this.handlerMappings) {
			if (mapping.usesPathPatterns()) {
				this.parseRequestPath = true;
				break;
			}
		}
	}

	private void initHandlerAdapters(ApplicationContext context) {
		this.handlerAdapters = null;
		if (this.detectAllHandlerAdapters) {
			Map<String, HandlerAdapter> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<>(matchingBeans.values());
				AnnotationAwareOrderComparator.sort(this.handlerAdapters);
			}
		} else {
			try {
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			} catch (NoSuchBeanDefinitionException ex) {
				// 忽略
			}
		}
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerAdapters declared for servlet '" + getServletName() +
						"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;
		if (this.detectAllHandlerExceptionResolvers) {
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
				AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
			}
		} else {
			try {
				HandlerExceptionResolver her = context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			} catch (NoSuchBeanDefinitionException ex) {
				// 忽略
			}
		}
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +
						"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator = context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.viewNameTranslator.getClass().getSimpleName());
			} else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.viewNameTranslator);
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// 使用默认策略（DefaultRequestToViewNameTranslator）
			this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No RequestToViewNameTranslator '" + REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME +
						"': using default [" + this.viewNameTranslator.getClass().getSimpleName() + "]");
			}
		}
	}

	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;
		if (this.detectAllViewResolvers) {
			Map<String, ViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					context, ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList<>(matchingBeans.values());
				AnnotationAwareOrderComparator.sort(this.viewResolvers);
			}
		} else {
			try {
				ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			} catch (NoSuchBeanDefinitionException ex) {
				// 忽略
			}
		}
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No ViewResolvers declared for servlet '" + getServletName() +
						"': using default strategies from DispatcherServlet.properties");
			}
		}
	}

	private void initFlashMapManager(ApplicationContext context) {
		try {
			this.flashMapManager = context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.flashMapManager.getClass().getSimpleName());
			} else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.flashMapManager);
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// 使用默认策略（SessionFlashMapManager）
			this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No FlashMapManager '" + FLASH_MAP_MANAGER_BEAN_NAME +
						"': using default [" + this.flashMapManager.getClass().getSimpleName() + "]");
			}
		}
	}

	// ==================== 策略组件获取方法 ====================

	/**
	 * 获取主题源（如果 ApplicationContext 实现了 ThemeSource 接口）。
	 */
	@Nullable
	public final ThemeSource getThemeSource() {
		return (getWebApplicationContext() instanceof ThemeSource ? (ThemeSource) getWebApplicationContext() : null);
	}

	/**
	 * 返回 MultipartResolver（可能为 null）。
	 */
	@Nullable
	public final MultipartResolver getMultipartResolver() {
		return this.multipartResolver;
	}

	/**
	 * 返回不可修改的 HandlerMapping 列表。
	 */
	@Nullable
	public final List<HandlerMapping> getHandlerMappings() {
		return (this.handlerMappings != null ? Collections.unmodifiableList(this.handlerMappings) : null);
	}

	// ==================== 默认策略加载（从配置文件） ====================

	/**
	 * 获取指定策略接口的单个默认实现。
	 * 如果配置文件中定义了多个实现，但要求只有一个，则抛出异常。
	 *
	 * @param context           当前 ApplicationContext
	 * @param strategyInterface 策略接口类型
	 * @param <T>               策略类型
	 * @return 默认策略实例
	 */
	protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
		List<T> strategies = getDefaultStrategies(context, strategyInterface);
		if (strategies.size() != 1) {
			throw new BeanInitializationException(
					"DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}

	/**
	 * 获取指定策略接口的所有默认实现（从 DispatcherServlet.properties 中读取）。
	 * 此方法会懒加载 properties 文件。
	 *
	 * @param context           当前 ApplicationContext
	 * @param strategyInterface 策略接口类型
	 * @param <T>               策略类型
	 * @return 默认策略列表（可能为空）
	 */
	protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
		// 加载配置文件（静态）
		if (defaultStrategies == null) {
			try {
				ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
				defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
			} catch (IOException ex) {
				throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
			}
		}

		String key = strategyInterface.getName();
		String value = defaultStrategies.getProperty(key);
		if (value == null) {
			return Collections.emptyList();
		}
		String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
		List<T> strategies = new ArrayList<>(classNames.length);
		for (String className : classNames) {
			try {
				Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
				Object strategy = createDefaultStrategy(context, clazz);
				strategies.add((T) strategy);
			} catch (ClassNotFoundException ex) {
				throw new BeanInitializationException(
						"Could not find DispatcherServlet's default strategy class [" + className +
								"] for interface [" + key + "]", ex);
			} catch (LinkageError err) {
				throw new BeanInitializationException(
						"Unresolvable class definition for DispatcherServlet's default strategy class [" +
								className + "] for interface [" + key + "]", err);
			}
		}
		return strategies;
	}

	/**
	 * 使用当前 ApplicationContext 的 AutowireCapableBeanFactory 创建默认策略实例。
	 * 这样可以使得默认策略也支持依赖注入。
	 *
	 * @param context ApplicationContext
	 * @param clazz   要创建的类
	 * @return 实例
	 */
	protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
		return context.getAutowireCapableBeanFactory().createBean(clazz);
	}

	// ==================== 核心方法：请求分发 ====================

	/**
	 * 实际处理请求的入口（由父类 FrameworkServlet 调用）。
	 * 在执行 doDispatch 前后，会进行一些预处理，如保存 include 前的属性、设置请求属性等。
	 *
	 * @param request  HTTP 请求
	 * @param response HTTP 响应
	 * @throws Exception 处理过程中的异常
	 */
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logRequest(request);

		// 如果是 include 请求，保存原有属性的快照，以便之后恢复
		Map<String, Object> attributesSnapshot = null;
		if (WebUtils.isIncludeRequest(request)) {
			attributesSnapshot = new HashMap<>();
			Enumeration<?> attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}

		// 将一些组件放入 request 属性中，便于后续处理（如 JSP 标签库）使用
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

		if (this.flashMapManager != null) {
			FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
			if (inputFlashMap != null) {
				request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
			}
			request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
			request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
		}

		// 如果任何 HandlerMapping 使用路径模式，则解析并缓存请求路径
		RequestPath previousRequestPath = null;
		if (this.parseRequestPath) {
			previousRequestPath = (RequestPath) request.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE);
			ServletRequestPathUtils.parseAndCache(request);
		}

		try {
			// 核心分发逻辑
			doDispatch(request, response);
		} finally {
			// 如果不是异步处理开始，且存在 include 快照，则恢复属性
			if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted() && attributesSnapshot != null) {
				restoreAttributesAfterInclude(request, attributesSnapshot);
			}
			// 恢复请求路径属性
			if (this.parseRequestPath) {
				ServletRequestPathUtils.setParsedRequestPath(previousRequestPath, request);
			}
		}
	}

	/**
	 * 记录请求信息（用于调试/追踪）。
	 */
	private void logRequest(HttpServletRequest request) {
		LogFormatUtils.traceDebug(logger, traceOn -> {
			String params;
			if (StringUtils.startsWithIgnoreCase(request.getContentType(), "multipart/")) {
				params = "multipart";
			} else if (isEnableLoggingRequestDetails()) {
				params = request.getParameterMap().entrySet().stream()
						.map(entry -> entry.getKey() + ":" + Arrays.toString(entry.getValue()))
						.collect(Collectors.joining(", "));
			} else {
				params = request.getParameterMap().isEmpty() ? "" : "masked";
			}
			String queryString = request.getQueryString();
			String queryClause = (StringUtils.hasLength(queryString) ? "?" + queryString : "");
			String dispatchType = (!DispatcherType.REQUEST.equals(request.getDispatcherType()) ?
					"\"" + request.getDispatcherType() + "\" dispatch for " : "");
			String message = dispatchType + request.getMethod() + " \"" + getRequestUri(request) + queryClause +
					"\", parameters={" + params + "}";
			if (traceOn) {
				List<String> values = Collections.list(request.getHeaderNames());
				String headers = values.size() > 0 ? "masked" : "";
				if (isEnableLoggingRequestDetails()) {
					headers = values.stream()
							.map(name -> name + ":" + Collections.list(request.getHeaders(name)))
							.collect(Collectors.joining(", "));
				}
				return message + ", headers={" + headers + "} in DispatcherServlet '" + getServletName() + "'";
			} else {
				return message;
			}
		});
	}

	/**
	 * 核心分发方法：查找处理器、执行处理器、处理结果、处理异常、渲染视图。
	 *
	 * @param request  HTTP 请求
	 * @param response HTTP 响应
	 * @throws Exception 处理异常
	 */
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		boolean multipartRequestParsed = false;
		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				// 1. 检查是否是 multipart 请求，如果是则转换为 MultipartHttpServletRequest
				processedRequest = checkMultipart(request);
				multipartRequestParsed = (processedRequest != request);

				// 2. 根据请求获取 HandlerExecutionChain（处理器+拦截器）
				mappedHandler = getHandler(processedRequest);
				if (mappedHandler == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// 3. 获取支持该处理器的 HandlerAdapter
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// 4. 处理 Last-Modified 缓存（对于 GET/HEAD 请求）
				String method = request.getMethod();
				boolean isGet = HttpMethod.GET.matches(method);
				if (isGet || HttpMethod.HEAD.matches(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}

				// 5. 执行拦截器的 preHandle
				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				// 6. 实际执行处理器（调用 Controller 方法）
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				// 如果异步处理已经开始，则直接返回（等待异步结果）
				if (asyncManager.isConcurrentHandlingStarted()) {
					return;
				}

				// 7. 如果没有视图名，应用默认视图名（通过 RequestToViewNameTranslator）
				applyDefaultViewName(processedRequest, mv);

				// 8. 执行拦截器的 postHandle
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			} catch (Exception ex) {
				dispatchException = ex;
			} catch (Throwable err) {
				// 包装非 Exception 的 Throwable
				dispatchException = new NestedServletException("Handler dispatch failed", err);
			}

			// 9. 处理分发结果（包括异常渲染）
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		} catch (Exception ex) {
			// 如果发生异常且还没有触发 afterCompletion，则触发
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		} catch (Throwable err) {
			triggerAfterCompletion(processedRequest, response, mappedHandler,
					new NestedServletException("Handler processing failed", err));
		} finally {
			// 如果异步处理已经开始，则调用拦截器的 afterConcurrentHandlingStarted
			if (asyncManager.isConcurrentHandlingStarted()) {
				if (mappedHandler != null) {
					mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				}
			} else if (multipartRequestParsed) {
				// 清理 multipart 资源
				cleanupMultipart(processedRequest);
			}
		}
	}

	/**
	 * 如果没有显式指定视图名，使用 RequestToViewNameTranslator 生成默认视图名。
	 */
	private void applyDefaultViewName(HttpServletRequest request, @Nullable ModelAndView mv) throws Exception {
		if (mv != null && !mv.hasView()) {
			String defaultViewName = getDefaultViewName(request);
			if (defaultViewName != null) {
				mv.setViewName(defaultViewName);
			}
		}
	}

	/**
	 * 处理分发结果：处理异常、渲染视图、触发 afterCompletion。
	 */
	private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
									   @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
									   @Nullable Exception exception) throws Exception {
		boolean errorView = false;

		if (exception != null) {
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			} else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}

		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("No view rendering, null ModelAndView returned.");
			}
		}

		if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			if (mappedHandler != null) {
				mappedHandler.triggerAfterCompletion(request, response, null);
			}
		}
	}

	// ==================== 辅助方法 ====================

	/**
	 * 构建 LocaleContext（用于国际化）。
	 */
	protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
		LocaleResolver lr = this.localeResolver;
		if (lr instanceof LocaleContextResolver) {
			return ((LocaleContextResolver) lr).resolveLocaleContext(request);
		}
		return () -> (lr != null ? lr.resolveLocale(request) : request.getLocale());
	}

	/**
	 * 检查是否为 multipart 请求，如果是则解析并返回 MultipartHttpServletRequest。
	 *
	 * @param request 原始请求
	 * @return 可能被包装的请求（如果为 multipart）
	 * @throws MultipartException 解析失败时抛出
	 */
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			// 如果已经是 MultipartHttpServletRequest（例如由 Filter 处理），则不再处理
			if (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null) {
				if (DispatcherType.REQUEST.equals(request.getDispatcherType())) {
					logger.trace("Request already resolved to MultipartHttpServletRequest, e.g. by MultipartFilter");
				}
			} else if (hasMultipartException(request)) {
				// 如果之前已经因为 multipart 解析失败而进入错误页面，则跳过重新解析
				logger.debug("Multipart resolution previously failed for current request - " +
						"skipping re-resolution for undisturbed error rendering");
			} else {
				try {
					return this.multipartResolver.resolveMultipart(request);
				} catch (MultipartException ex) {
					if (request.getAttribute("javax.servlet.error.exception") == null) {
						throw ex;
					}
					// 对于错误分发（error dispatch），不重新抛出异常，仅记录日志
					logger.debug("Multipart resolution failed for error dispatch", ex);
				}
			}
		}
		return request;
	}

	/**
	 * 判断当前请求是否已经包含 MultipartException（用于错误页面场景）。
	 */
	private boolean hasMultipartException(HttpServletRequest request) {
		for (Throwable error = (Throwable) request.getAttribute("javax.servlet.error.exception");
			 error != null; error = error.getCause()) {
			if (error instanceof MultipartException) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 清理 multipart 资源（如果已经解析）。
	 */
	protected void cleanupMultipart(HttpServletRequest request) {
		if (this.multipartResolver != null) {
			MultipartHttpServletRequest multipartRequest =
					WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
			if (multipartRequest != null) {
				this.multipartResolver.cleanupMultipart(multipartRequest);
			}
		}
	}

	/**
	 * 通过 HandlerMapping 查找处理器执行链。
	 *
	 * @param request HTTP 请求
	 * @return 处理器执行链，如果没有找到则返回 null
	 * @throws Exception 查找过程中可能抛出的异常
	 */
	@Nullable
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
			for (HandlerMapping mapping : this.handlerMappings) {
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

	/**
	 * 当找不到处理器时，根据配置返回 404 或抛出异常。
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
		}
		if (this.throwExceptionIfNoHandlerFound) {
			throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
					new ServletServerHttpRequest(request).getHeaders());
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * 查找支持给定处理器的 HandlerAdapter。
	 *
	 * @param handler 处理器对象
	 * @return 适配器
	 * @throws ServletException 如果没有适配器支持
	 */
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter adapter : this.handlerAdapters) {
				if (adapter.supports(handler)) {
					return adapter;
				}
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}

	/**
	 * 处理处理器抛出的异常，通过 HandlerExceptionResolver 获取 ModelAndView。
	 *
	 * @param request  HTTP 请求
	 * @param response HTTP 响应
	 * @param handler  处理器（可能为 null）
	 * @param ex       异常
	 * @return 异常对应的 ModelAndView，如果无法处理则返回 null
	 * @throws Exception 如果异常没有被任何 resolver 处理，则重新抛出
	 */
	@Nullable
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
												   @Nullable Object handler, Exception ex) throws Exception {
		// 清除可能存在的 produces 媒体类型属性，避免影响异常响应的内容协商
		request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

		ModelAndView exMv = null;
		if (this.handlerExceptionResolvers != null) {
			for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
				exMv = resolver.resolveException(request, response, handler, ex);
				if (exMv != null) {
					break;
				}
			}
		}
		if (exMv != null) {
			if (exMv.isEmpty()) {
				request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
				return null;
			}
			if (!exMv.hasView()) {
				String defaultViewName = getDefaultViewName(request);
				if (defaultViewName != null) {
					exMv.setViewName(defaultViewName);
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Using resolved error view: " + exMv, ex);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Using resolved error view: " + exMv);
			}
			WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
			return exMv;
		} else {
			throw ex;
		}
	}

	/**
	 * 渲染视图：解析视图名，获取 View 对象，并调用其 render 方法。
	 *
	 * @param mv        ModelAndView
	 * @param request   HTTP 请求
	 * @param response  HTTP 响应
	 * @throws Exception 渲染异常
	 */
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 设置响应语言（Locale）
		Locale locale = (this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
		response.setLocale(locale);

		View view;
		String viewName = mv.getViewName();
		if (viewName != null) {
			view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		} else {
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv +
						"] neither contains a view name nor a View object in servlet with name '" + getServletName() + "'");
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Rendering view [" + view + "] ");
		}
		try {
			if (mv.getStatus() != null) {
				request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, mv.getStatus());
				response.setStatus(mv.getStatus().value());
			}
			view.render(mv.getModelInternal(), request, response);
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error rendering view [" + view + "]", ex);
			}
			throw ex;
		}
	}

	/**
	 * 获取默认视图名（通过 RequestToViewNameTranslator）。
	 */
	@Nullable
	protected String getDefaultViewName(HttpServletRequest request) throws Exception {
		return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
	}

	/**
	 * 解析视图名：遍历所有 ViewResolver，返回第一个非 null 的 View。
	 */
	@Nullable
	protected View resolveViewName(String viewName, @Nullable Map<String, Object> model,
								   Locale locale, HttpServletRequest request) throws Exception {
		if (this.viewResolvers != null) {
			for (ViewResolver viewResolver : this.viewResolvers) {
				View view = viewResolver.resolveViewName(viewName, locale);
				if (view != null) {
					return view;
				}
			}
		}
		return null;
	}

	/**
	 * 触发拦截器的 afterCompletion（在异常情况下）。
	 */
	private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
										@Nullable HandlerExecutionChain mappedHandler, Exception ex) throws Exception {
		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	/**
	 * 恢复 include 请求前的属性。
	 */
	private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?, ?> attributesSnapshot) {
		Set<String> attrsToCheck = new HashSet<>();
		Enumeration<?> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
				attrsToCheck.add(attrName);
			}
		}
		attrsToCheck.addAll((Collection<? extends String>) attributesSnapshot.keySet());
		for (String attrName : attrsToCheck) {
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue == null) {
				request.removeAttribute(attrName);
			} else if (attrValue != request.getAttribute(attrName)) {
				request.setAttribute(attrName, attrValue);
			}
		}
	}

	/**
	 * 获取请求 URI（考虑 include 情况）。
	 */
	private static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute("javax.servlet.include.request_uri");
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}
}