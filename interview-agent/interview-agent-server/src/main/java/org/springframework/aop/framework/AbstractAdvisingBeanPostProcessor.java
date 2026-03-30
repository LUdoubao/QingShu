//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;

/**
 * 抽象的后置处理器，用于将给定的 {@link Advisor}（通知器）应用到 Bean 上。
 * 如果 Bean 已经是一个代理（实现了 {@link Advised} 接口），则直接添加该通知器；
 * 否则，创建一个新的代理工厂并生成代理对象，将通知器织入其中。
 *
 * <p>此类是 Spring AOP 基础设施的一部分，通常用于处理基于注解的切面（如 @Async、@Transactional）。
 * 子类可以继承并定制通知器的创建和代理工厂的配置。
 *
 * <p>它实现了 {@link BeanPostProcessor}，在 Bean 初始化后（{@link #postProcessAfterInitialization}）
 * 进行代理处理。同时继承自 {@link ProxyProcessorSupport}，以获取代理配置支持（如类加载器、代理目标类标志等）。
 *
 * @author Juergen Hoeller
 * @since 3.1
 * @see #advisor
 * @see #setBeforeExistingAdvisors
 */
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor {

	/**
	 * 要应用的通知器（Advisor），由子类负责初始化。
	 * 该通知器定义了切面逻辑（如拦截器、前置/后置通知等）。
	 */
	@Nullable
	protected Advisor advisor;

	/**
	 * 是否将通知器添加到现有通知器列表的最前面（即优先执行）。
	 * 默认值为 false，表示添加到列表末尾（后执行）。
	 */
	protected boolean beforeExistingAdvisors = false;

	/**
	 * 缓存每个类是否适用（eligible）此通知器的结果，避免重复的 AOP 工具判断。
	 * 键为目标类，值为 Boolean（是否适用）。
	 */
	private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);

	/**
	 * 默认构造器，供子类使用。
	 */
	public AbstractAdvisingBeanPostProcessor() {
	}

	/**
	 * 设置是否将当前通知器添加到现有通知器列表的最前面。
	 * 如果为 true，则新添加的通知器将优先于已有的通知器执行。
	 *
	 * @param beforeExistingAdvisors 是否添加到列表头部
	 */
	public void setBeforeExistingAdvisors(boolean beforeExistingAdvisors) {
		this.beforeExistingAdvisors = beforeExistingAdvisors;
	}

	/**
	 * 在 Bean 初始化之前不做任何处理，直接返回原 Bean。
	 * 因为代理的创建是在初始化之后进行的。
	 *
	 * @param bean     原始 Bean 实例
	 * @param beanName Bean 名称
	 * @return 原 Bean 实例（不变）
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * 在 Bean 初始化之后进行处理。
	 * 如果该 Bean 不需要被代理（如基础架构 Bean），则直接返回原 Bean；
	 * 否则，如果 Bean 已经是代理对象（Advised），则将当前通知器添加到该代理的通知器列表中；
	 * 如果不是代理对象，则创建一个新的代理工厂并生成代理对象，将通知器织入。
	 *
	 * @param bean     初始化的 Bean 实例
	 * @param beanName Bean 名称
	 * @return 最终返回的 Bean（可能是原 Bean 或代理对象）
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		// 如果未设置 advisor，或者 Bean 是基础架构 Bean（不应被代理），则跳过处理
		if (this.advisor != null && !(bean instanceof AopInfrastructureBean)) {
			// 如果 Bean 已经是 Advised（即已经是 Spring 代理对象）
			if (bean instanceof Advised) {
				Advised advised = (Advised) bean;
				// 如果代理不是冻结状态（允许修改通知器列表）且当前类适用于此通知器
				if (!advised.isFrozen() && this.isEligible(AopUtils.getTargetClass(bean))) {
					// 根据 beforeExistingAdvisors 标志决定添加位置
					if (this.beforeExistingAdvisors) {
						advised.addAdvisor(0, this.advisor);
					} else {
						advised.addAdvisor(this.advisor);
					}
					return bean;
				}
			}

			// 如果 Bean 不是代理对象，或者已经是代理但无法添加通知器（如冻结状态），则创建新代理
			if (this.isEligible(bean, beanName)) {
				// 创建一个代理工厂，并设置目标对象
				ProxyFactory proxyFactory = this.prepareProxyFactory(bean, beanName);
				// 如果代理工厂没有强制指定代理目标类（proxyTargetClass），则根据接口评估是否需要 JDK 代理
				if (!proxyFactory.isProxyTargetClass()) {
					this.evaluateProxyInterfaces(bean.getClass(), proxyFactory);
				}
				// 添加当前通知器
				proxyFactory.addAdvisor(this.advisor);
				// 允许子类定制代理工厂（例如设置额外的拦截器或配置）
				this.customizeProxyFactory(proxyFactory);

				// 确定使用的类加载器
				ClassLoader classLoader = this.getProxyClassLoader();
				// 如果类加载器是 SmartClassLoader，可能获取其原始类加载器（用于兼容性）
				if (classLoader instanceof SmartClassLoader && classLoader != bean.getClass().getClassLoader()) {
					classLoader = ((SmartClassLoader) classLoader).getOriginalClassLoader();
				}

				// 生成代理对象并返回
				return proxyFactory.getProxy(classLoader);
			}
		}
		// 不满足条件，直接返回原 Bean
		return bean;
	}

	/**
	 * 判断给定的 Bean 是否适用于此通知器。
	 * 默认实现委托给 {@link #isEligible(Class)} 方法。
	 *
	 * @param bean     Bean 实例
	 * @param beanName Bean 名称
	 * @return true 表示应该应用通知器
	 */
	protected boolean isEligible(Object bean, String beanName) {
		return this.isEligible(bean.getClass());
	}

	/**
	 * 判断给定的目标类是否适用于此通知器。
	 * 使用缓存避免重复计算，通过 {@link AopUtils#canApply(Advisor, Class)} 判断。
	 *
	 * @param targetClass 目标类
	 * @return true 表示该类应该被代理以应用通知器
	 */
	protected boolean isEligible(Class<?> targetClass) {
		// 先从缓存中获取
		Boolean eligible = this.eligibleBeans.get(targetClass);
		if (eligible != null) {
			return eligible;
		}
		// 如果没有 advisor，直接返回 false
		if (this.advisor == null) {
			return false;
		}
		// 使用 AOP 工具判断 advisor 是否能应用到该目标类
		eligible = AopUtils.canApply(this.advisor, targetClass);
		// 缓存结果
		this.eligibleBeans.put(targetClass, eligible);
		return eligible;
	}

	/**
	 * 为给定的 Bean 准备代理工厂。
	 * 创建一个新的 ProxyFactory 实例，并从当前对象复制代理配置（如 proxyTargetClass、exposeProxy 等）。
	 * 然后将目标 Bean 设置为代理工厂的目标对象。
	 *
	 * @param bean     Bean 实例
	 * @param beanName Bean 名称
	 * @return 配置好的代理工厂
	 */
	protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
		ProxyFactory proxyFactory = new ProxyFactory();
		// 从当前对象复制配置（继承自 ProxyProcessorSupport）
		proxyFactory.copyFrom(this);
		// 设置代理的目标对象
		proxyFactory.setTarget(bean);
		return proxyFactory;
	}

	/**
	 * 允许子类在代理工厂创建后进行额外定制（如添加额外的通知器、设置是否暴露代理等）。
	 * 默认实现为空。
	 *
	 * @param proxyFactory 代理工厂，子类可以进一步配置
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}
}