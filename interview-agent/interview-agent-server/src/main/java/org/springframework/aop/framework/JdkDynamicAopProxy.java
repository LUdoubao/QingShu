//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DecoratingProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * JDK 动态代理实现 Spring AOP 的核心类。
 * 该类实现了 {@link AopProxy} 和 {@link InvocationHandler} 接口，
 * 负责为目标对象生成 JDK 动态代理，并在代理的 invoke 方法中应用配置的通知器（Advisor）链。
 *
 * <p>使用 JDK 动态代理的前提是目标对象至少实现了一个接口。
 * 如果目标对象没有实现任何接口，Spring 会回退到 CGLIB 代理（{@link CglibAopProxy}）。
 *
 * <p>该类通过 {@link AdvisedSupport} 持有 AOP 配置信息，包括目标源（TargetSource）、
 * 通知器列表、是否暴露代理、是否不透明等。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Adrian Colyer
 * @see AdvisedSupport
 * @see CglibAopProxy
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	private static final long serialVersionUID = 5531744639992436476L;

	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/** AOP 配置信息，包含目标源、通知器列表、代理配置等 */
	private final AdvisedSupport advised;

	/** 代理将要实现的所有接口（包括 Spring AOP 内部接口） */
	private final Class<?>[] proxiedInterfaces;

	/** 标记代理接口中是否定义了 equals 方法，用于优化 equals 调用 */
	private boolean equalsDefined;

	/** 标记代理接口中是否定义了 hashCode 方法，用于优化 hashCode 调用 */
	private boolean hashCodeDefined;

	/**
	 * 构造一个新的 JdkDynamicAopProxy，使用给定的配置。
	 *
	 * @param config AOP 配置（必须包含 Advisor 或 TargetSource）
	 * @throws AopConfigException 如果没有配置任何通知器且 TargetSource 为空时抛出
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisorCount() == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
		// 计算代理需要实现的所有接口（包括 Spring 内部接口，如 Advised、DecoratingProxy 等）
		this.proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
		// 检查代理接口中是否定义了 equals/hashCode 方法，以便后续调用优化
		findDefinedEqualsAndHashCodeMethods(this.proxiedInterfaces);
	}

	/**
	 * 创建代理对象，使用默认的类加载器。
	 *
	 * @return 生成的代理对象
	 */
	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 创建代理对象，使用指定的类加载器。
	 *
	 * @param classLoader 用于定义代理类的类加载器
	 * @return 生成的代理对象
	 */
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
		}
		// 通过 Proxy.newProxyInstance 生成代理实例，this 作为 InvocationHandler
		return Proxy.newProxyInstance(classLoader, this.proxiedInterfaces, this);
	}

	/**
	 * 遍历所有代理接口，检查其中是否声明了 equals 和 hashCode 方法。
	 * 如果接口中已经定义了这些方法，则标记对应的标志为 true，
	 * 这样在 invoke 中可以直接调用目标方法，而不需要走 AOP 通知链。
	 *
	 * @param proxiedInterfaces 代理将要实现的接口数组
	 */
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			Method[] methods = proxiedInterface.getDeclaredMethods();
			for (Method method : methods) {
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}

	/**
	 * 代理的 invoke 方法，当代理对象的方法被调用时，此方法会被执行。
	 * 负责执行通知器链（拦截器链），最终调用目标方法。
	 *
	 * @param proxy  代理对象本身
	 * @param method 被调用的方法
	 * @param args   方法参数
	 * @return 方法执行结果
	 * @throws Throwable 如果调用过程中抛出异常
	 */
	@Nullable
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Object target = null;

		try {
			// 处理 equals 方法：如果代理接口中没有定义 equals，则执行代理自身的 equals 逻辑
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				return equals(args[0]);
			}
			// 处理 hashCode 方法：如果代理接口中没有定义 hashCode，则执行代理自身的 hashCode 逻辑
			if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				return hashCode();
			}

			// 如果方法来自 DecoratingProxy 接口，直接返回目标类的原始 Class（用于装饰模式）
			if (method.getDeclaringClass() == DecoratingProxy.class) {
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}

			// 如果代理是不透明的（opaque=false）且方法来自 Advised 接口，则直接调用配置对象的对应方法
			// 这允许用户通过代理直接访问 AOP 配置信息（如获取通知器、目标源等）
			if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// 使用反射调用 AdvisedSupport 的方法，而不是走 AOP 链
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			// 如果配置要求暴露代理对象（exposeProxy = true），则将当前代理设置到 AopContext 中
			if (this.advised.exposeProxy) {
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// 获取目标对象实例（注意：每次调用都可能返回不同的实例，例如原型模式或池化目标源）
			target = targetSource.getTarget();
			Class<?> targetClass = (target != null ? target.getClass() : null);

			// 获取适用于当前方法和目标类的拦截器链（通知器链）
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			Object retVal;
			// 如果链为空，直接调用目标方法（没有 AOP 增强）
			if (chain.isEmpty()) {
				// 适配参数（如果需要，例如泛型桥接方法参数转换）
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			} else {
				// 构建一个 ReflectiveMethodInvocation，它将负责依次调用拦截器链，最后调用目标方法
				MethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				retVal = invocation.proceed();
			}

			// 处理返回值：如果返回值是目标对象本身，并且方法返回类型兼容代理类型，
			// 并且方法不是来自 RawTargetAccess 接口，则将返回值替换为代理对象（保持代理一致性）
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target && returnType != Object.class &&
					returnType.isInstance(proxy) && !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				retVal = proxy;
			}
			// 检查返回值：如果方法返回原始类型（primitive）但返回值为 null，则抛出异常
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			// 释放目标对象（如果 TargetSource 不是静态的，例如原型或池化目标源）
			if (target != null && !targetSource.isStatic()) {
				targetSource.releaseTarget(target);
			}
			// 恢复之前保存的代理上下文
			if (setProxyContext) {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

	/**
	 * 判断当前代理是否与另一个对象相等。
	 * 如果另一个对象也是 JdkDynamicAopProxy 或者其 InvocationHandler 是 JdkDynamicAopProxy，
	 * 则比较它们持有的 AdvisedSupport 配置是否相等。
	 *
	 * @param other 要比较的对象
	 * @return 是否相等
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}
		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		} else {
			// 如果 other 是 JDK 代理对象，则获取其 InvocationHandler 再比较
			if (!Proxy.isProxyClass(other.getClass())) {
				return false;
			}
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		// 比较两个 AdvisedSupport 是否相等（委托给工具方法）
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * 计算代理对象的哈希码。
	 * 使用 JdkDynamicAopProxy 类的哈希码乘以 13 再加上目标源的哈希码。
	 *
	 * @return 哈希码
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}
}