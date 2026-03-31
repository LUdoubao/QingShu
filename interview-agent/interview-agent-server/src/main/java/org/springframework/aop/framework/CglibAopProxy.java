//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Dispatcher;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.core.KotlinDetector;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Spring AOP 的 CGLIB 代理实现。
 * 当目标类没有实现任何接口时，或者用户强制使用 CGLIB 代理（proxyTargetClass=true）时，
 * Spring 会使用此代理方式。CGLIB 通过动态生成目标类的子类来实现代理，能够代理普通类（无需接口）。
 *
 * <p>该类实现了 {@link AopProxy} 接口，并利用 CGLIB 的 {@link Enhancer} 生成代理类。
 * 与 JDK 动态代理不同，CGLIB 可以代理 final 方法以外的任何方法，并且能够处理构造器注入等场景。
 *
 * <p>为了性能优化，CGLibAopProxy 会根据 AOP 配置（如是否冻结、是否静态目标源、是否有通知等）选择不同的
 * 回调（Callback）策略，避免在每次方法调用时重新构建拦截器链。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Ramnivas Laddad
 * @see AdvisedSupport
 * @see JdkDynamicAopProxy
 */
class CglibAopProxy implements AopProxy, Serializable {

	// ========== 回调索引常量 ==========
	/** 索引 0：标准的 AOP 拦截器（DynamicAdvisedInterceptor） */
	private static final int AOP_PROXY = 0;
	/** 索引 1：无通知且不需要暴露代理时的目标调用 */
	private static final int INVOKE_TARGET = 1;
	/** 索引 2：无操作（NoOp），用于不需要拦截的方法（如 finalize） */
	private static final int NO_OVERRIDE = 2;
	/** 索引 3：静态分发目标（StaticDispatcher） */
	private static final int DISPATCH_TARGET = 3;
	/** 索引 4：分发 Advised 接口方法的回调 */
	private static final int DISPATCH_ADVISED = 4;
	/** 索引 5：处理 equals 方法的回调 */
	private static final int INVOKE_EQUALS = 5;
	/** 索引 6：处理 hashCode 方法的回调 */
	private static final int INVOKE_HASHCODE = 6;

	protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

	/**
	 * 类验证缓存：为了避免重复验证同一个类（检查 final 方法、跨类加载器包可见方法等），
	 * 将已验证的类缓存起来，键为目标类，值为 Boolean.TRUE。
	 */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();

	/** AOP 配置，持有目标源、通知器链、代理配置等 */
	protected final AdvisedSupport advised;

	/** 构造器参数（用于创建代理实例时传递） */
	@Nullable
	protected Object[] constructorArgs;
	/** 构造器参数类型 */
	@Nullable
	protected Class<?>[] constructorArgTypes;

	/** 用于处理 Advised 接口方法（如 getAdvisors、getTargetSource 等）的分发器 */
	private final transient AdvisedDispatcher advisedDispatcher;

	/**
	 * 当配置被冻结且目标源为静态时，会生成固定拦截器映射：方法 -> 回调数组中的索引。
	 * 这样可以在方法调用时直接选择对应的回调，而不需要每次都计算拦截器链。
	 */
	private transient Map<Method, Integer> fixedInterceptorMap = Collections.emptyMap();
	/** 固定拦截器在回调数组中的起始偏移量（mainCallbacks 的长度） */
	private transient int fixedInterceptorOffset;

	/**
	 * 构造 CGLIB 代理，使用给定的 AOP 配置。
	 *
	 * @param config AOP 配置（必须包含 Advisor 或 TargetSource）
	 * @throws AopConfigException 如果没有配置任何通知器且 TargetSource 为空时抛出
	 */
	public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisorCount() == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
		this.advisedDispatcher = new AdvisedDispatcher(this.advised);
	}

	/**
	 * 设置构造器参数，用于创建代理实例时调用特定的构造器。
	 * 通常用于目标类没有无参构造器的场景。
	 *
	 * @param constructorArgs   构造器参数值
	 * @param constructorArgTypes 构造器参数类型
	 */
	public void setConstructorArguments(@Nullable Object[] constructorArgs, @Nullable Class<?>[] constructorArgTypes) {
		if (constructorArgs != null && constructorArgTypes != null) {
			if (constructorArgs.length != constructorArgTypes.length) {
				throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
						") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
			}
			this.constructorArgs = constructorArgs;
			this.constructorArgTypes = constructorArgTypes;
		} else {
			throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
		}
	}

	/**
	 * 创建代理对象，使用默认的类加载器。
	 *
	 * @return 生成的代理对象
	 */
	@Override
	public Object getProxy() {
		return getProxy(null);
	}

	/**
	 * 创建代理对象，使用指定的类加载器。
	 * 主要步骤：
	 * <ol>
	 *   <li>确定代理的超类（如果目标类是 CGLIB 生成的类，则取其父类，并添加其接口）</li>
	 *   <li>验证目标类是否适合 CGLIB 代理（检查 final 方法、跨类加载器可见性）</li>
	 *   <li>创建 Enhancer，设置超类、接口、命名策略、回调过滤器</li>
	 *   <li>生成回调数组，并创建代理实例</li>
	 * </ol>
	 *
	 * @param classLoader 用于生成代理类的类加载器
	 * @return 生成的代理对象
	 */
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating CGLIB proxy: " + this.advised.getTargetSource());
		}

		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");
			Class<?> proxySuperClass = rootClass;
			// 如果目标类本身是 CGLIB 代理类（名称包含 "$$"），则取它的父类作为新代理的超类，
			// 并将原类实现的接口也添加到 advised 中，以保证接口方法的代理。
			if (rootClass.getName().contains("$$")) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}

			// 验证目标类是否适合 CGLIB 代理（非 final、方法可见性等）
			validateClassIfNecessary(proxySuperClass, classLoader);

			// 创建 CGLIB Enhancer
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				// 如果类加载器支持类重载（如热部署），则禁用 Enhancer 缓存
				if (classLoader instanceof SmartClassLoader && ((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
			enhancer.setSuperclass(proxySuperClass);
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));

			// 获取回调数组（核心部分）
			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}

			// 设置回调过滤器，用于根据方法选择对应的回调
			enhancer.setCallbackFilter(new ProxyCallbackFilter(
					this.advised.getConfigurationOnlyCopy(),
					this.fixedInterceptorMap,
					this.fixedInterceptorOffset));
			enhancer.setCallbackTypes(types);

			// 创建代理类并实例化
			return createProxyClassAndInstance(enhancer, callbacks);
		} catch (IllegalArgumentException | CodeGenerationException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of " + this.advised.getTargetClass() +
					": Common causes of this problem include using a final class or a non-visible class", ex);
		} catch (Throwable ex) {
			// 其他意外异常
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}

	/**
	 * 创建代理实例（子类可覆盖以自定义实例化过程）。
	 *
	 * @param enhancer 配置好的 Enhancer
	 * @param callbacks 回调数组
	 * @return 代理实例
	 */
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		// 在构造代理实例期间不拦截方法（避免循环依赖）
		enhancer.setInterceptDuringConstruction(false);
		enhancer.setCallbacks(callbacks);
		if (this.constructorArgs != null && this.constructorArgTypes != null) {
			return enhancer.create(this.constructorArgTypes, this.constructorArgs);
		} else {
			return enhancer.create();
		}
	}

	/**
	 * 创建 Enhancer 实例（子类可覆盖以提供自定义 Enhancer）。
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}

	/**
	 * 验证目标类是否适合 CGLIB 代理（仅在 info 日志级别且未优化时执行）。
	 * 检查目标类及其父类中是否有 final 方法、跨类加载器的包可见方法等，并输出警告。
	 *
	 * @param proxySuperClass 代理的超类
	 * @param proxyClassLoader 代理类加载器
	 */
	private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
		// 如果配置了 optimize（优化模式），则不进行验证
		if (!this.advised.isOptimize() && logger.isInfoEnabled()) {
			synchronized (validatedClasses) {
				if (!validatedClasses.containsKey(proxySuperClass)) {
					doValidateClass(proxySuperClass, proxyClassLoader,
							ClassUtils.getAllInterfacesForClassAsSet(proxySuperClass));
					validatedClasses.put(proxySuperClass, Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * 递归执行类验证，检查所有方法。
	 *
	 * @param proxySuperClass 当前要检查的类
	 * @param proxyClassLoader 代理类加载器
	 * @param ifcs 该类实现的接口集合
	 */
	private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
		if (proxySuperClass != Object.class) {
			Method[] methods = proxySuperClass.getDeclaredMethods();
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
					// 检查 final 方法（CGLIB 无法代理 final 方法）
					if (Modifier.isFinal(mod)) {
						if (implementsInterface(method, ifcs)) {
							logger.info("Unable to proxy interface-implementing method [" + method +
									"] because it is marked as final: Consider using interface-based JDK proxies instead!");
						}
						if (logger.isDebugEnabled()) {
							logger.debug("Final method [" + method +
									"] cannot get proxied via CGLIB: Calls to this method will NOT be routed to the target instance " +
									"and might lead to NPEs against uninitialized fields in the proxy instance.");
						}
					}
					// 检查跨类加载器的包可见方法（非 public/protected）
					else if (logger.isDebugEnabled() && !Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
							proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
						logger.debug("Method [" + method +
								"] is package-visible across different ClassLoaders and cannot get proxied via CGLIB: " +
								"Declare this method as public or protected if you need to support invocations through the proxy.");
					}
				}
			}
			// 递归检查父类
			doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, ifcs);
		}
	}

	/**
	 * 获取代理的回调数组。根据配置的不同（是否暴露代理、是否冻结、目标源是否静态等），
	 * 会生成不同数量和类型的回调，以达到最佳性能。
	 *
	 * @param rootClass 原始目标类
	 * @return 回调数组
	 * @throws Exception 如果获取目标对象失败
	 */
	private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
		boolean exposeProxy = this.advised.isExposeProxy();
		boolean isFrozen = this.advised.isFrozen();
		boolean isStatic = this.advised.getTargetSource().isStatic();

		// 标准 AOP 拦截器（DynamicAdvisedInterceptor），处理有通知器链的方法
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

		// 目标调用拦截器（无通知时直接调用目标方法）
		Object targetInterceptor;
		if (exposeProxy) {
			targetInterceptor = isStatic ?
					new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
		} else {
			targetInterceptor = isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
		}

		// 静态目标分发器（当目标源为静态且配置冻结时，使用 StaticDispatcher 直接返回目标对象）
		Callback targetDispatcher = isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) :
				new SerializableNoOp();

		// 主要回调数组（固定顺序，对应常量索引）
		Callback[] mainCallbacks = new Callback[]{
				aopInterceptor,               // 0: AOP_PROXY
				(Callback) targetInterceptor, // 1: INVOKE_TARGET
				new SerializableNoOp(),       // 2: NO_OVERRIDE
				targetDispatcher,             // 3: DISPATCH_TARGET
				this.advisedDispatcher,       // 4: DISPATCH_ADVISED
				new EqualsInterceptor(this.advised),   // 5: INVOKE_EQUALS
				new HashCodeInterceptor(this.advised)  // 6: INVOKE_HASHCODE
		};

		Callback[] callbacks;
		// 如果配置冻结且目标源静态，可以为每个方法生成固定的回调，避免重复构建拦截器链
		if (isStatic && isFrozen) {
			Method[] methods = rootClass.getMethods();
			Callback[] fixedCallbacks = new Callback[methods.length];
			this.fixedInterceptorMap = CollectionUtils.newHashMap(methods.length);

			for (int x = 0; x < methods.length; x++) {
				Method method = methods[x];
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, rootClass);
				fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
				this.fixedInterceptorMap.put(method, x);
			}

			// 合并主回调数组和固定回调数组
			callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
			System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
			System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
			this.fixedInterceptorOffset = mainCallbacks.length;
		} else {
			callbacks = mainCallbacks;
		}
		return callbacks;
	}

	/**
	 * equals 方法，比较两个 CglibAopProxy 是否相等（基于 AdvisedSupport 配置）。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof CglibAopProxy &&
				AopProxyUtils.equalsInProxy(this.advised, ((CglibAopProxy) other).advised)));
	}

	/**
	 * hashCode 方法。
	 */
	@Override
	public int hashCode() {
		return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

	/**
	 * 判断某个方法是否在给定的接口集合中被声明。
	 */
	private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
		for (Class<?> ifc : ifcs) {
			if (ClassUtils.hasMethod(ifc, method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 处理方法返回值：如果返回值是目标对象本身且方法返回类型兼容代理类型，则替换为代理对象；
	 * 如果返回值是 null 但方法返回原始类型，则抛出异常。
	 *
	 * @param proxy       代理对象
	 * @param target      目标对象
	 * @param method      方法
	 * @param returnValue 原始返回值
	 * @return 处理后的返回值
	 */
	@Nullable
	private static Object processReturnType(Object proxy, @Nullable Object target, Method method, @Nullable Object returnValue) {
		if (returnValue != null && returnValue == target && !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
			returnValue = proxy;
		}
		Class<?> returnType = method.getReturnType();
		if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"Null return value from advice does not match primitive return type for: " + method);
		}
		return returnValue;
	}

	// ======================== 内部回调类 ========================

	/**
	 * 回调过滤器，根据方法选择对应的回调索引。
	 * 此过滤器充分利用了 CGLIB 的多回调机制，为不同方法分配不同的回调，
	 * 从而避免每次调用都进行条件判断，提高性能。
	 */
	private static class ProxyCallbackFilter implements CallbackFilter {
		private final AdvisedSupport advised;
		private final Map<Method, Integer> fixedInterceptorMap;
		private final int fixedInterceptorOffset;

		public ProxyCallbackFilter(AdvisedSupport advised, Map<Method, Integer> fixedInterceptorMap, int fixedInterceptorOffset) {
			this.advised = advised;
			this.fixedInterceptorMap = fixedInterceptorMap;
			this.fixedInterceptorOffset = fixedInterceptorOffset;
		}

		@Override
		public int accept(Method method) {
			// finalize() 方法不进行任何增强，返回 NO_OVERRIDE
			if (AopUtils.isFinalizeMethod(method)) {
				logger.trace("Found finalize() method - using NO_OVERRIDE");
				return NO_OVERRIDE;
			}
			// 处理 Advised 接口的方法（如果代理不透明，则直接分发到 advisedDispatcher）
			if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Method is declared on Advised interface: " + method);
				}
				return DISPATCH_ADVISED;
			}
			// equals 方法
			if (AopUtils.isEqualsMethod(method)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Found 'equals' method: " + method);
				}
				return INVOKE_EQUALS;
			}
			// hashCode 方法
			if (AopUtils.isHashCodeMethod(method)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Found 'hashCode' method: " + method);
				}
				return INVOKE_HASHCODE;
			}

			Class<?> targetClass = this.advised.getTargetClass();
			List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			boolean haveAdvice = !chain.isEmpty();
			boolean exposeProxy = this.advised.isExposeProxy();
			boolean isStatic = this.advised.getTargetSource().isStatic();
			boolean isFrozen = this.advised.isFrozen();

			// 无通知且配置冻结的情况
			if (!haveAdvice && isFrozen) {
				// 如果不需要暴露代理且目标源静态，并且方法返回类型兼容目标类型（可能返回 this），则使用 INVOKE_TARGET
				if (!exposeProxy && isStatic) {
					Class<?> returnType = method.getReturnType();
					if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Method return type is assignable from target type and may therefore return 'this' - using INVOKE_TARGET: " + method);
						}
						return INVOKE_TARGET;
					} else {
						if (logger.isTraceEnabled()) {
							logger.trace("Method return type ensures 'this' cannot be returned - using DISPATCH_TARGET: " + method);
						}
						return DISPATCH_TARGET;
					}
				} else {
					return INVOKE_TARGET;
				}
			}
			// 需要暴露代理的情况，必须使用 AOP_PROXY（DynamicAdvisedInterceptor）来设置 AopContext
			else if (exposeProxy) {
				if (logger.isTraceEnabled()) {
					logger.trace("Must expose proxy on advised method: " + method);
				}
				return AOP_PROXY;
			}
			// 冻结、静态且有固定拦截器映射的情况，使用对应的固定回调
			else if (isStatic && isFrozen && this.fixedInterceptorMap.containsKey(method)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Method has advice and optimizations are enabled: " + method);
				}
				int index = this.fixedInterceptorMap.get(method);
				return index + this.fixedInterceptorOffset;
			}
			// 默认使用 AOP_PROXY（动态拦截器）
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Unable to apply any optimizations to advised method: " + method);
				}
				return AOP_PROXY;
			}
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) return true;
			if (!(other instanceof ProxyCallbackFilter)) return false;
			ProxyCallbackFilter otherFilter = (ProxyCallbackFilter) other;
			AdvisedSupport otherAdvised = otherFilter.advised;
			// 比较关键配置是否相同
			if (this.advised.isFrozen() != otherAdvised.isFrozen()) return false;
			if (this.advised.isExposeProxy() != otherAdvised.isExposeProxy()) return false;
			if (this.advised.getTargetSource().isStatic() != otherAdvised.getTargetSource().isStatic()) return false;
			if (!AopProxyUtils.equalsProxiedInterfaces(this.advised, otherAdvised)) return false;
			if (this.advised.getAdvisorCount() != otherAdvised.getAdvisorCount()) return false;

			Advisor[] thisAdvisors = this.advised.getAdvisors();
			Advisor[] thatAdvisors = otherAdvised.getAdvisors();
			for (int i = 0; i < thisAdvisors.length; i++) {
				Advisor thisAdvisor = thisAdvisors[i];
				Advisor thatAdvisor = thatAdvisors[i];
				if (!equalsAdviceClasses(thisAdvisor, thatAdvisor)) return false;
				if (!equalsPointcuts(thisAdvisor, thatAdvisor)) return false;
			}
			return true;
		}

		private static boolean equalsAdviceClasses(Advisor a, Advisor b) {
			return a.getAdvice().getClass() == b.getAdvice().getClass();
		}

		private static boolean equalsPointcuts(Advisor a, Advisor b) {
			return (!(a instanceof PointcutAdvisor) ||
					(b instanceof PointcutAdvisor && ObjectUtils.nullSafeEquals(((PointcutAdvisor) a).getPointcut(), ((PointcutAdvisor) b).getPointcut())));
		}

		@Override
		public int hashCode() {
			int hashCode = 0;
			Advisor[] advisors = this.advised.getAdvisors();
			for (Advisor advisor : advisors) {
				Advice advice = advisor.getAdvice();
				hashCode = 13 * hashCode + advice.getClass().hashCode();
			}
			hashCode = 13 * hashCode + (this.advised.isFrozen() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isExposeProxy() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isOptimize() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isOpaque() ? 1 : 0);
			return hashCode;
		}
	}

	/**
	 * CGLIB 方法调用的具体实现，扩展自 ReflectiveMethodInvocation。
	 * 使用 MethodProxy 进行快速调用（避免反射）。
	 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {
		@Nullable
		private final MethodProxy methodProxy;

		public CglibMethodInvocation(Object proxy, @Nullable Object target, Method method, Object[] arguments,
									 @Nullable Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers,
									 MethodProxy methodProxy) {
			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = isMethodProxyCompatible(method) ? methodProxy : null;
		}

		@Override
		@Nullable
		public Object proceed() throws Throwable {
			try {
				return super.proceed();
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				// 对于 Kotlin 挂起函数或方法声明中未声明的异常，包装为 UndeclaredThrowableException
				if (!ReflectionUtils.declaresException(getMethod(), ex.getClass()) &&
						!KotlinDetector.isKotlinType(getMethod().getDeclaringClass())) {
					throw new UndeclaredThrowableException(ex);
				} else {
					throw ex;
				}
			}
		}

		/**
		 * 调用连接点（目标方法）。优先使用 MethodProxy（快速调用），失败时回退到反射调用。
		 */
		@Override
		protected Object invokeJoinpoint() throws Throwable {
			if (this.methodProxy != null) {
				try {
					return this.methodProxy.invoke(this.target, this.arguments);
				} catch (CodeGenerationException ex) {
					logFastClassGenerationFailure(this.method);
				}
			}
			return super.invokeJoinpoint();
		}

		/**
		 * 判断方法是否适合使用 MethodProxy（要求方法是 public 的，且不是 Object 中的 equals/hashCode/toString）。
		 */
		static boolean isMethodProxyCompatible(Method method) {
			return Modifier.isPublic(method.getModifiers()) &&
					method.getDeclaringClass() != Object.class &&
					!AopUtils.isEqualsMethod(method) &&
					!AopUtils.isHashCodeMethod(method) &&
					!AopUtils.isToStringMethod(method);
		}

		static void logFastClassGenerationFailure(Method method) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to generate CGLIB fast class for method: " + method);
			}
		}
	}

	/**
	 * 动态通知拦截器，用于处理有通知器链的方法。
	 * 这是 CGLIB 代理的核心拦截器，它会在每次方法调用时获取拦截器链，并创建 MethodInvocation 执行。
	 * 实现了 MethodInterceptor 接口（CGLIB 接口）。
	 */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {
		private final AdvisedSupport advised;

		public DynamicAdvisedInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			boolean setProxyContext = false;
			Object target = null;
			TargetSource targetSource = this.advised.getTargetSource();

			try {
				// 如果需要暴露代理，则设置到 AopContext 中
				if (this.advised.isExposeProxy()) {
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// 获取目标对象（可能每次都不同）
				target = targetSource.getTarget();
				Class<?> targetClass = (target != null ? target.getClass() : null);
				// 获取拦截器链（通知器链）
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

				Object retVal;
				// 如果没有拦截器且方法适合 MethodProxy，则直接使用 CGLIB 快速调用
				if (chain.isEmpty() && CglibMethodInvocation.isMethodProxyCompatible(method)) {
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					try {
						retVal = methodProxy.invoke(target, argsToUse);
					} catch (CodeGenerationException ex) {
						CglibMethodInvocation.logFastClassGenerationFailure(method);
						retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
					}
				} else {
					retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
				}
				retVal = processReturnType(proxy, target, method, retVal);
				return retVal;
			} finally {
				// 释放目标对象（如果 TargetSource 不是静态的）
				if (target != null && !targetSource.isStatic()) {
					targetSource.releaseTarget(target);
				}
				if (setProxyContext) {
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof DynamicAdvisedInterceptor &&
					this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
		}

		@Override
		public int hashCode() {
			return this.advised.hashCode();
		}
	}

	/**
	 * 固定链静态目标拦截器，用于冻结配置且目标源静态时的优化。
	 * 每个方法对应一个固定的拦截器链，避免重复获取。
	 */
	private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {
		private final List<Object> adviceChain;
		@Nullable
		private final Object target;
		@Nullable
		private final Class<?> targetClass;

		public FixedChainStaticTargetInterceptor(List<Object> adviceChain, @Nullable Object target, @Nullable Class<?> targetClass) {
			this.adviceChain = adviceChain;
			this.target = target;
			this.targetClass = targetClass;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args,
					this.targetClass, this.adviceChain, methodProxy);
			Object retVal = invocation.proceed();
			retVal = processReturnType(proxy, this.target, method, retVal);
			return retVal;
		}
	}

	/**
	 * hashCode 拦截器，返回固定的哈希码。
	 */
	private static class HashCodeInterceptor implements MethodInterceptor, Serializable {
		private final AdvisedSupport advised;

		public HashCodeInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
		}
	}

	/**
	 * equals 拦截器，实现代理对象的相等性比较。
	 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {
		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			Object other = args[0];
			if (proxy == other) {
				return true;
			}
			// 如果 other 是 CGLIB 代理（Factory 实例），获取其对应的 EqualsInterceptor 并比较配置
			if (other instanceof Factory) {
				Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
				if (!(callback instanceof EqualsInterceptor)) {
					return false;
				}
				AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
				return AopProxyUtils.equalsInProxy(this.advised, otherAdvised);
			}
			return false;
		}
	}

	/**
	 * 分发 Advised 接口方法的回调（Dispatcher），直接返回 AdvisedSupport 对象。
	 */
	private static class AdvisedDispatcher implements Dispatcher, Serializable {
		private final AdvisedSupport advised;

		public AdvisedDispatcher(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object loadObject() {
			return this.advised;
		}
	}

	/**
	 * 静态目标分发器，直接返回静态目标对象。
	 */
	private static class StaticDispatcher implements Dispatcher, Serializable {
		@Nullable
		private final Object target;

		public StaticDispatcher(@Nullable Object target) {
			this.target = target;
		}

		@Nullable
		@Override
		public Object loadObject() {
			return this.target;
		}
	}

	/**
	 * 动态无通知拦截器（暴露代理版本），在调用目标前将当前代理设置到 AopContext。
	 */
	private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {
		private final TargetSource targetSource;

		public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			Object target = this.targetSource.getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			} finally {
				AopContext.setCurrentProxy(oldProxy);
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}

	/**
	 * 动态无通知拦截器（非暴露代理版本）。
	 */
	private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {
		private final TargetSource targetSource;

		public DynamicUnadvisedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = this.targetSource.getTarget();
			try {
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			} finally {
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}

	/**
	 * 静态无通知拦截器（暴露代理版本）。
	 */
	private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {
		@Nullable
		private final Object target;

		public StaticUnadvisedExposedInterceptor(@Nullable Object target) {
			this.target = target;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(this.target, args);
				return processReturnType(proxy, this.target, method, retVal);
			} finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

	/**
	 * 静态无通知拦截器（非暴露代理版本）。
	 */
	private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {
		@Nullable
		private final Object target;

		public StaticUnadvisedInterceptor(@Nullable Object target) {
			this.target = target;
		}

		@Nullable
		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object retVal = methodProxy.invoke(this.target, args);
			return processReturnType(proxy, this.target, method, retVal);
		}
	}

	/**
	 * 可序列化的 NoOp 回调，用于不需要任何操作的方法（如 finalize）。
	 */
	public static class SerializableNoOp implements NoOp, Serializable {
		public SerializableNoOp() {
		}
	}
}