/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Spring 单例Bean 注册中心【默认实现】
 * =============================================================================
 * 【核心定位】：Spring 单例Bean的**创建、缓存、依赖管理、生命周期**的底层实现类
 * 【@Autowired 强关联】：所有@Autowired注入的Bean，都从这个类的缓存中获取/创建
 * 【核心能力】：
 * 1. 三级缓存机制 → 解决Spring**循环依赖**（@Autowired循环注入的核心解决方案）
 * 2. 单例Bean的创建/获取/销毁 全生命周期管理
 * 3. Bean依赖关系维护（@Autowired注入的依赖绑定）
 * 4. 循环创建检测（防止Bean无限递归创建）
 * =============================================================================
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	/** 抑制异常的最大数量 */
	private static final int SUPPRESSED_EXCEPTIONS_LIMIT = 100;

	// ======================== 【核心：Spring 三级缓存】 ========================
	// 作用：解决@Autowired循环依赖（A注入B，B注入A），保证Bean提前暴露
	/**
	 * 一级缓存：成品单例Bean池
	 * key：beanName
	 * value：完整初始化完成的Bean（实例化+属性注入+初始化）
	 * 【@Autowired 最终获取Bean的地方】
	 */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/**
	 * 三级缓存：单例Bean工厂池
	 * key：beanName
	 * value：ObjectFactory（Lambda表达式，用于创建早期Bean）
	 * 【循环依赖核心】：提前暴露Bean，允许其他Bean注入半成品
	 */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/**
	 * 二级缓存：早期半成品Bean池
	 * key：beanName
	 * value：已实例化、未完成属性注入/初始化的早期Bean
	 * 【循环依赖时】：存放从三级缓存生成的早期Bean
	 */
	private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

	// ======================== 单例Bean注册标记 ========================
	/** 已注册的单例Bean名称集合（有序） */
	private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

	/** 【关键】当前正在创建中的Bean名称集合 → 检测循环依赖 */
	private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/** 创建检查排除集合（无需检测循环依赖的Bean） */
	private final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/** 抑制的异常集合（创建Bean时的内部异常） */
	@Nullable
	private Set<Exception> suppressedExceptions;

	/** 单例Bean是否正在销毁中 */
	private boolean singletonsCurrentlyInDestruction = false;

	// ======================== Bean 依赖/销毁管理 ========================
	/** 可销毁的Bean集合（实现DisposableBean的Bean） */
	private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

	/** 包含Bean的映射：外部Bean → 内部包含的Bean */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/**
	 * 依赖Bean映射：key=被依赖的Bean，value=依赖它的Bean
	 * 例：A@Autowired B → key=B, value={A}
	 * 【@Autowired 依赖关系存储】
	 */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/**
	 * 依赖项映射：key=当前Bean，value=它依赖的Bean
	 * 例：A@Autowired B → key=A, value={B}
	 * 【@Autowired 依赖关系存储】
	 */
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

	public DefaultSingletonBeanRegistry() {
	}

	/**
	 * 对外注册单例Bean
	 * 直接将成品Bean放入一级缓存
	 */
	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(singletonObject, "Singleton object must not be null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("Could not register object [" + singletonObject +
						"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
			}
			// 添加到一级缓存
			this.addSingleton(beanName, singletonObject);
		}
	}

	/**
	 * 【核心】添加成品Bean到一级缓存
	 * 同时清理二、三级缓存（Bean创建完成，无需早期暴露）
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.put(beanName, singletonObject);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.add(beanName);
		}
	}

	/**
	 * 【核心：循环依赖关键】添加三级缓存（Bean工厂）
	 * Bean刚实例化后调用，提前暴露ObjectFactory，允许其他Bean获取早期引用
	 * → 解决@Autowired循环注入
	 */
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		synchronized (this.singletonObjects) {
			if (!this.singletonObjects.containsKey(beanName)) {
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
				this.registeredSingletons.add(beanName);
			}
		}
	}

	/**
	 * 对外获取单例Bean（默认允许早期引用）
	 * @Autowired注入依赖时，底层调用此方法获取Bean
	 */
	@Override
	@Nullable
	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}

	/**
	 * 【核心：三级缓存获取Bean】
	 * 从三级缓存中按顺序获取Bean：一级 → 二级 → 三级
	 * @param allowEarlyReference 是否允许早期引用（循环依赖必须为true）
	 */
	@Nullable
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		// 1. 从一级缓存获取：成品Bean
		Object singletonObject = this.singletonObjects.get(beanName);
		// 2. 一级缓存无，且Bean正在创建中（循环依赖场景）
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			// 3. 从二级缓存获取：早期半成品Bean
			singletonObject = this.earlySingletonObjects.get(beanName);
			// 4. 二级缓存无，允许早期引用
			if (singletonObject == null && allowEarlyReference) {
				synchronized (this.singletonObjects) {
					// 双重检查，防止并发
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						singletonObject = this.earlySingletonObjects.get(beanName);
						if (singletonObject == null) {
							// 5. 从三级缓存获取：ObjectFactory
							ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
							if (singletonFactory != null) {
								// 6. 调用工厂生成早期Bean，放入二级缓存，删除三级缓存
								singletonObject = singletonFactory.getObject();
								this.earlySingletonObjects.put(beanName, singletonObject);
								this.singletonFactories.remove(beanName);
							}
						}
					}
				}
			}
		}
		return singletonObject;
	}

	/**
	 * 【核心】创建单例Bean（getSingleton的重载，带创建逻辑）
	 * Spring创建Bean的核心入口，@Autowired依赖不存在时触发创建
	 */
	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name must not be null");
		synchronized (this.singletonObjects) {
			// 先查缓存
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				// 销毁阶段禁止创建Bean
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while singletons of this factory are in destruction " +
									"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}

				// 【标记】Bean开始创建（加入正在创建集合）
				beforeSingletonCreation(beanName);
				boolean newSingleton = false;
				boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<>();
				}

				try {
					// 执行Bean创建（实例化+属性注入+初始化）
					singletonObject = singletonFactory.getObject();
					newSingleton = true;
				}
				catch (IllegalStateException ex) {
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						throw ex;
					}
				}
				catch (BeanCreationException ex) {
					// 异常处理
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				}
				finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					// 【标记】Bean创建完成（移除正在创建集合）
					afterSingletonCreation(beanName);
				}

				// 新创建的Bean，加入一级缓存
				if (newSingleton) {
					addSingleton(beanName, singletonObject);
				}
			}
			return singletonObject;
		}
	}

	/** 注册抑制异常 */
	protected void onSuppressedException(Exception ex) {
		synchronized (this.singletonObjects) {
			if (this.suppressedExceptions != null && this.suppressedExceptions.size() < SUPPRESSED_EXCEPTIONS_LIMIT) {
				this.suppressedExceptions.add(ex);
			}
		}
	}

	/** 移除单例Bean（清理所有缓存） */
	protected void removeSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.remove(beanName);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.remove(beanName);
		}
	}

	/** 判断是否包含成品单例Bean */
	@Override
	public boolean containsSingleton(String beanName) {
		return this.singletonObjects.containsKey(beanName);
	}

	/** 获取所有已注册单例Bean名称 */
	@Override
	public String[] getSingletonNames() {
		synchronized (this.singletonObjects) {
			return StringUtils.toStringArray(this.registeredSingletons);
		}
	}

	/** 获取单例Bean数量 */
	@Override
	public int getSingletonCount() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.size();
		}
	}

	/** 设置Bean是否正在创建 */
	public void setCurrentlyInCreation(String beanName, boolean inCreation) {
		Assert.notNull(beanName, "Bean name must not be null");
		if (!inCreation) {
			this.inCreationCheckExclusions.add(beanName);
		}
		else {
			this.inCreationCheckExclusions.remove(beanName);
		}
	}

	/** 判断Bean是否正在创建 */
	public boolean isCurrentlyInCreation(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
	}

	protected boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName);
	}

	/**
	 * 【公开方法】判断单例Bean是否正在创建
	 * 循环依赖检测核心
	 */
	public boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}

	/**
	 * Bean创建前：标记为【正在创建】
	 * 若已存在，抛出循环依赖异常
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) &&
				!this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

	/**
	 * Bean创建后：移除【正在创建】标记
	 */
	protected void afterSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) &&
				!this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
		}
	}

	/** 注册可销毁的Bean */
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

	/** 注册包含关系的Bean */
	public void registerContainedBean(String containedBeanName, String containingBeanName) {
		synchronized (this.containedBeanMap) {
			Set<String> containedBeans = this.containedBeanMap.computeIfAbsent(containingBeanName, k -> new LinkedHashSet<>(8));
			if (!containedBeans.add(containedBeanName)) {
				return;
			}
		}
		registerDependentBean(containedBeanName, containingBeanName);
	}

	/**
	 * 【核心】注册Bean依赖关系
	 * @Autowired注入时，自动调用此方法维护依赖
	 * @param beanName 被依赖的Bean
	 * @param dependentBeanName 依赖方Bean
	 */
	public void registerDependentBean(String beanName, String dependentBeanName) {
		String canonicalName = canonicalName(beanName);

		// 维护：被依赖Bean → 依赖方集合
		synchronized (this.dependentBeanMap) {
			Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
			if (!dependentBeans.add(dependentBeanName)) {
				return;
			}
		}

		// 维护：依赖方Bean → 被依赖集合
		synchronized (this.dependenciesForBeanMap) {
			Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
			dependenciesForBean.add(canonicalName);
		}
	}

	/** 判断是否存在依赖关系（循环依赖检测） */
	protected boolean isDependent(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			return isDependent(beanName, dependentBeanName, null);
		}
	}

	/** 递归判断依赖关系（支持传递依赖） */
	private boolean isDependent(String beanName, String dependentBeanName, @Nullable Set<String> alreadySeen) {
		if (alreadySeen != null && alreadySeen.contains(beanName)) {
			return false;
		}
		String canonicalName = canonicalName(beanName);
		Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
		if (dependentBeans == null) {
			return false;
		}
		if (dependentBeans.contains(dependentBeanName)) {
			return true;
		}
		for (String transitiveDependency : dependentBeans) {
			if (alreadySeen == null) {
				alreadySeen = new HashSet<>();
			}
			alreadySeen.add(beanName);
			if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
				return true;
			}
		}
		return false;
	}

	/** 判断是否有依赖Bean */
	protected boolean hasDependentBean(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}

	/** 获取依赖当前Bean的所有Bean */
	public String[] getDependentBeans(String beanName) {
		Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
		if (dependentBeans == null) {
			return new String[0];
		}
		synchronized (this.dependentBeanMap) {
			return StringUtils.toStringArray(dependentBeans);
		}
	}

	/** 获取当前Bean依赖的所有Bean */
	public String[] getDependenciesForBean(String beanName) {
		Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
		if (dependenciesForBean == null) {
			return new String[0];
		}
		synchronized (this.dependenciesForBeanMap) {
			return StringUtils.toStringArray(dependenciesForBean);
		}
	}

	/** 销毁所有单例Bean */
	public void destroySingletons() {
		if (logger.isTraceEnabled()) {
			logger.trace("Destroying singletons in " + this);
		}
		synchronized (this.singletonObjects) {
			this.singletonsCurrentlyInDestruction = true;
		}

		String[] disposableBeanNames;
		synchronized (this.disposableBeans) {
			disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
		}
		for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
			destroySingleton(disposableBeanNames[i]);
		}

		this.containedBeanMap.clear();
		this.dependentBeanMap.clear();
		this.dependenciesForBeanMap.clear();
		clearSingletonCache();
	}

	/** 清空所有三级缓存 */
	protected void clearSingletonCache() {
		synchronized (this.singletonObjects) {
			this.singletonObjects.clear();
			this.singletonFactories.clear();
			this.earlySingletonObjects.clear();
			this.registeredSingletons.clear();
			this.singletonsCurrentlyInDestruction = false;
		}
	}

	/** 销毁指定单例Bean */
	public void destroySingleton(String beanName) {
		removeSingleton(beanName);
		DisposableBean disposableBean;
		synchronized (this.disposableBeans) {
			disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);
	}

	/** 销毁Bean及其依赖Bean */
	protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
		// 先销毁依赖当前Bean的所有Bean
		Set<String> dependencies;
		synchronized (this.dependentBeanMap) {
			dependencies = this.dependentBeanMap.remove(beanName);
		}
		if (dependencies != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
			}
			for (String dependentBeanName : dependencies) {
				destroySingleton(dependentBeanName);
			}
		}

		// 执行Bean的销毁方法
		if (bean != null) {
			try {
				bean.destroy();
			}
			catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Destruction of bean with name '" + beanName + "' threw an exception", ex);
				}
			}
		}

		// 销毁内部包含的Bean
		Set<String> containedBeans;
		synchronized (this.containedBeanMap) {
			containedBeans = this.containedBeanMap.remove(beanName);
		}
		if (containedBeans != null) {
			for (String containedBeanName : containedBeans) {
				destroySingleton(containedBeanName);
			}
		}

		// 清理依赖关系
		synchronized (this.dependentBeanMap) {
			Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Set<String>> entry = it.next();
				Set<String> dependenciesToClean = entry.getValue();
				dependenciesToClean.remove(beanName);
				if (dependenciesToClean.isEmpty()) {
					it.remove();
				}
			}
		}
		this.dependenciesForBeanMap.remove(beanName);
	}

	/** 获取单例缓存锁对象 */
	public final Object getSingletonMutex() {
		return this.singletonObjects;
	}
}