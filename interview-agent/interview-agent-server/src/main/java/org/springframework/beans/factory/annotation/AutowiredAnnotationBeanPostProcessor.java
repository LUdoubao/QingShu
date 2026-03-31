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
package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring {@code @Autowired} 注解核心处理器
 * 【Spring 自动装配的底层引擎】
 *
 * 核心职责：
 * 1. 扫描 Bean 中的 {@code @Autowired/@Value/@Inject} 注解
 * 2. 构建注入元数据（InjectionMetadata）
 * 3. 执行构造器/字段/方法的依赖注入
 * 4. 缓存注入元数据，提升性能
 *
 * 实现的核心接口：
 * {@link SmartInstantiationAwareBeanPostProcessor}：处理构造器注入、属性注入
 * {@link MergedBeanDefinitionPostProcessor}：合并 Bean 定义，扫描注入注解
 * {@link PriorityOrdered}：最高优先级执行，保证注入优先处理
 * {@link BeanFactoryAware}：获取 BeanFactory 进行依赖查找
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @since 2.5
 */
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 支持的自动装配注解类型集合
	 * 默认支持：@Autowired、@Value、@Inject(JSR-330)
	 */
	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

	/**
	 * 注解中 required 属性的名称（默认：required）
	 */
	private String requiredParameterName = "required";

	/**
	 * 注解中 required 属性的默认值（默认：true = 必须注入）
	 */
	private boolean requiredParameterValue = true;

	/**
	 * 处理器执行顺序：最高优先级（Integer.MAX_VALUE - 5）
	 * 保证在所有后置处理器前执行注入
	 */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;

	/**
	 * Spring 核心工厂：用于依赖查找、Bean 获取
	 */
	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * 已检查的 @Lookup 方法缓存（线程安全）
	 */
	private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

	/**
	 * 候选构造器缓存：Key=Bean Class，Value=标注 @Autowired 的构造器数组
	 */
	private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

	/**
	 * 注入元数据缓存：Key=BeanName，Value=该 Bean 的所有注入元素（字段/方法）
	 * 核心缓存：避免重复扫描注解，大幅提升性能
	 */
	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

	/**
	 * 构造方法：注册默认支持的自动装配注解
	 * 1. 添加 @Autowired
	 * 2. 添加 @Value
	 * 3. 兼容 JSR-330，添加 @Inject
	 */
	public AutowiredAnnotationBeanPostProcessor() {
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);

		// 兼容 JSR-330 @Inject 注解（Java 依赖注入标准）
		try {
			Class<?> injectAnnotation = ClassUtils.forName("javax.inject.Inject", getClass().getClassLoader());
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>) injectAnnotation);
			logger.trace("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		} catch (ClassNotFoundException ex) {
			// 无 JSR-330 依赖，忽略
		}
	}

	// ------------------------ 配置方法 ------------------------
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}

	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 注入 BeanFactory
	 * 必须是 ConfigurableListableBeanFactory（支持依赖解析、注入）
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	// ------------------------ 核心：合并 Bean 定义（注解扫描入口） ------------------------
	/**
	 * Bean 定义合并后回调
	 * 作用：扫描 Bean 中的 @Autowired 注解，构建并检查注入元数据
	 */
	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		// 查找/构建当前 Bean 的注入元数据（扫描所有 @Autowired 注解）
		InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
		// 注册注入成员，防止重复处理
		metadata.checkConfigMembers(beanDefinition);
	}

	/**
	 * 重置 Bean 定义：清空缓存
	 */
	@Override
	public void resetBeanDefinition(String beanName) {
		this.lookupMethodsChecked.remove(beanName);
		this.injectionMetadataCache.remove(beanName);
	}

	// ------------------------ 核心：构造器注入 ------------------------
	/**
	 * 确定候选构造器（@Autowired 构造器注入核心）
	 * Spring 创建 Bean 实例时，会调用此方法选择注入构造器
	 */
	@Override
	@Nullable
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeanCreationException {
		// 处理 @Lookup 注解
		if (!this.lookupMethodsChecked.contains(beanName)) {
			if (AnnotationUtils.isCandidateClass(beanClass, Lookup.class)) {
				Class<?> targetClass = beanClass;
				do {
					ReflectionUtils.doWithLocalMethods(targetClass, method -> {
						Lookup lookup = method.getAnnotation(Lookup.class);
						if (lookup != null) {
							Assert.state(this.beanFactory != null, "No BeanFactory available");
							LookupOverride override = new LookupOverride(method, lookup.value());
							RootBeanDefinition mbd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
							mbd.getMethodOverrides().addOverride(override);
						}
					});
					targetClass = targetClass.getSuperclass();
				} while (targetClass != null && targetClass != Object.class);
			}
			this.lookupMethodsChecked.add(beanName);
		}

		// 先从缓存获取构造器
		Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					// 获取 Bean 所有声明构造器
					Constructor<?>[] rawCandidates = beanClass.getDeclaredConstructors();
					List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
					Constructor<?> requiredConstructor = null;
					Constructor<?> defaultConstructor = null;
					Constructor<?> primaryConstructor = BeanUtils.findPrimaryConstructor(beanClass);
					int nonSyntheticConstructors = 0;

					// 遍历所有构造器
					for (Constructor<?> candidate : rawCandidates) {
						if (!candidate.isSynthetic()) {
							nonSyntheticConstructors++;
						} else if (primaryConstructor != null) {
							continue;
						}

						// 查找构造器上的 @Autowired 注解
						MergedAnnotation<?> ann = findAutowiredAnnotation(candidate);
						if (ann == null) {
							Class<?> userClass = ClassUtils.getUserClass(beanClass);
							if (userClass != beanClass) {
								try {
									Constructor<?> superCtor = userClass.getDeclaredConstructor(candidate.getParameterTypes());
									ann = findAutowiredAnnotation(superCtor);
								} catch (NoSuchMethodException ex) {
									// 忽略
								}
							}
						}

						// 构造器标注了 @Autowired
						if (ann != null) {
							boolean required = determineRequiredStatus(ann);
							if (required) {
								if (requiredConstructor != null) {
									throw new BeanCreationException(beanName,
											"多个@Autowired(required=true)构造器，违反Spring规则");
								}
								requiredConstructor = candidate;
							}
							candidates.add(candidate);
						}
						// 无参默认构造器
						else if (candidate.getParameterCount() == 0) {
							defaultConstructor = candidate;
						}
					}

					// 构造器规则处理
					if (!candidates.isEmpty()) {
						if (requiredConstructor == null && defaultConstructor != null) {
							candidates.add(defaultConstructor);
						}
						candidateConstructors = candidates.toArray(new Constructor<?>[0]);
					} else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0) {
						// 单参构造器：Spring 自动视为 @Autowired 构造器
						candidateConstructors = new Constructor<?>[]{rawCandidates[0]};
					} else {
						candidateConstructors = new Constructor<?>[0];
					}

					// 写入缓存
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return candidateConstructors.length > 0 ? candidateConstructors : null;
	}

	// ------------------------ 核心：属性/方法注入（@Autowired 最终执行入口） ------------------------
	/**
	 * 后置处理属性：【@Autowired 注入的总入口】
	 * Spring 创建 Bean 实例后，调用此方法完成所有 @Autowired 注入
	 */
	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
		// 获取注入元数据（所有 @Autowired 字段/方法）
		InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
		try {
			// 【核心】执行依赖注入
			metadata.inject(bean, beanName, pvs);
		} catch (BeanCreationException ex) {
			throw ex;
		} catch (Throwable ex) {
			// throw new BeanCreationException(beanName, "Autowired 依赖注入失败", ex);
		}
		return pvs;
	}

	/**
	 * 已废弃：替代方法 postProcessProperties
	 */
	@Deprecated
	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
		return postProcessProperties(pvs, bean, beanName);
	}

	/**
	 * 主动执行注入（对外工具方法）
	 */
	public void processInjection(Object bean) throws BeanCreationException {
		Class<?> clazz = bean.getClass();
		InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
		try {
			metadata.inject(bean, null, null);
		} catch (BeanCreationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BeanCreationException("类 [" + clazz + "] 的 Autowired 注入失败", ex);
		}
	}

	// ------------------------ 核心：查找/构建注入元数据 ------------------------
	/**
	 * 查找自动装配元数据（带双重检查缓存）
	 * 先查缓存，缓存不存在则构建新的元数据
	 */
	private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
		String cacheKey = StringUtils.hasLength(beanName) ? beanName : clazz.getName();
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);

		// 判断是否需要刷新元数据
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					// 【核心】构建元数据（扫描注解）
					metadata = buildAutowiringMetadata(clazz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	/**
	 * 【核心】构建自动装配元数据
	 * 扫描类及其父类的所有字段、方法，查找 @Autowired 注解
	 */
	private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
		// 类不包含任何支持的注解，直接返回空元数据
		if (!AnnotationUtils.isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
			return InjectionMetadata.EMPTY;
		}

		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		// 递归扫描当前类 + 所有父类
		do {
			List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			// 1. 扫描【字段】上的 @Autowired 注解
			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				MergedAnnotation<?> ann = findAutowiredAnnotation(field);
				if (ann != null) {
					// 静态字段不支持 @Autowired
					if (Modifier.isStatic(field.getModifiers())) {
						logger.info("静态字段不支持 @Autowired 注解：" + field);
						return;
					}
					boolean required = determineRequiredStatus(ann);
					// 封装为 字段注入元素
					currElements.add(new AutowiredFieldElement(field, required));
				}
			});

			// 2. 扫描【方法】上的 @Autowired 注解
			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					return;
				}
				MergedAnnotation<?> ann = findAutowiredAnnotation(bridgedMethod);
				if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
					// 静态方法不支持 @Autowired
					if (Modifier.isStatic(method.getModifiers())) {
						logger.info("静态方法不支持 @Autowired 注解：" + method);
						return;
					}
					// 无参方法不支持 @Autowired
					if (method.getParameterCount() == 0) {
						logger.info("@Autowired 仅支持带参数的方法：" + method);
					}
					boolean required = determineRequiredStatus(ann);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
					// 封装为 方法注入元素
					currElements.add(new AutowiredMethodElement(method, required, pd));
				}
			});

			// 父类元素添加到头部
			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);

		// 生成最终注入元数据
		return InjectionMetadata.forElements(elements, clazz);
	}

	/**
	 * 查找成员（字段/方法/构造器）上的自动装配注解
	 */
	@Nullable
	private MergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao) {
		MergedAnnotations annotations = MergedAnnotations.from(ao);
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			MergedAnnotation<?> annotation = annotations.get(type);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * 解析 @Autowired(required = ?)
	 * 默认：true = 必须注入，找不到 Bean 则报错
	 */
	protected boolean determineRequiredStatus(MergedAnnotation<?> ann) {
		return this.determineRequiredStatus((AnnotationAttributes)ann.asMap((mergedAnnotation) -> {
			return new AnnotationAttributes(mergedAnnotation.getType());
		}, new MergedAnnotation.Adapt[0]));
	}

	@Deprecated
	protected boolean determineRequiredStatus(AnnotationAttributes ann) {
		return !ann.containsKey(this.requiredParameterName)
				|| this.requiredParameterValue == ann.getBoolean(this.requiredParameterName);
	}

	/**
	 * 查找容器中指定类型的所有 Bean
	 */
	protected <T> Map<String, T> findAutowireCandidates(Class<T> type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory available");
		}
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
	}

	/**
	 * 注册依赖 Bean 关系
	 */
	private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null && this.beanFactory != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				logger.trace("Autowiring 依赖：" + beanName + " -> " + autowiredBeanName);
			}
		}
	}

	/**
	 * 解析缓存的依赖参数
	 */
	@Nullable
	private Object resolvedCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
			Assert.state(this.beanFactory != null, "No BeanFactory available");
			return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
		} else {
			return cachedArgument;
		}
	}

	/**
	 * 快捷依赖描述符：缓存 BeanName，直接根据名称获取 Bean
	 */
	private static class ShortcutDependencyDescriptor extends DependencyDescriptor {
		private final String shortcut;
		private final Class<?> requiredType;

		public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType) {
			super(original);
			this.shortcut = shortcut;
			this.requiredType = requiredType;
		}

		@Override
		public Object resolveShortcut(BeanFactory beanFactory) {
			return beanFactory.getBean(this.shortcut, this.requiredType);
		}
	}

	// ------------------------ 内部类：@Autowired 方法注入元素 ------------------------
	/**
	 * 方法注入元素：处理 {@code @Autowired} 标注的方法
	 */
	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {
		/** 是否必须注入 */
		private final boolean required;
		/** 是否已缓存方法参数 */
		private volatile boolean cached;
		/** 缓存的方法参数 */
		@Nullable
		private volatile Object[] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
		}

		/**
		 * 执行方法注入
		 */
		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			if (!checkPropertySkipping(pvs)) {
				Method method = (Method) this.member;
				Object[] arguments;

				// 优先使用缓存参数
				if (this.cached) {
					arguments = resolveCachedArguments(beanName);
				} else {
					// 解析方法参数（从 Spring 容器查找依赖）
					arguments = resolveMethodArguments(method, bean, beanName);
				}

				if (arguments != null) {
					// 反射执行方法，完成注入
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				}
			}
		}

		/**
		 * 解析缓存的方法参数
		 */
		@Nullable
		private Object[] resolveCachedArguments(@Nullable String beanName) {
			Object[] cachedMethodArguments = this.cachedMethodArguments;
			if (cachedMethodArguments == null) {
				return null;
			}
			Object[] arguments = new Object[cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolvedCachedArgument(beanName, cachedMethodArguments[i]);
			}
			return arguments;
		}

		/**
		 * 解析方法参数：从 Spring 容器获取依赖 Bean
		 */
		@Nullable
		private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
			int paramCount = method.getParameterCount();
			Object[] arguments = new Object[paramCount];
			DependencyDescriptor[] descriptors = new DependencyDescriptor[paramCount];
			Set<String> autowiredBeans = new LinkedHashSet<>(paramCount);
			TypeConverter converter = beanFactory.getTypeConverter();

			for (int i = 0; i < paramCount; i++) {
				MethodParameter param = new MethodParameter(method, i);
				DependencyDescriptor desc = new DependencyDescriptor(param, required);
				desc.setContainingClass(bean.getClass());
				descriptors[i] = desc;

				// 【核心】从容器解析依赖
				Object arg = beanFactory.resolveDependency(desc, beanName, autowiredBeans, converter);
				if (arg == null && required) {
					throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(param),
							"找不到 @Autowired 依赖的 Bean");
				}
				arguments[i] = arg;
			}

			// 缓存参数
			synchronized (this) {
				if (!cached) {
					cachedMethodArguments = descriptors;
					registerDependentBeans(beanName, autowiredBeans);
					cached = true;
				}
			}
			return arguments;
		}
	}

	// ------------------------ 内部类：@Autowired 字段注入元素 ------------------------
	/**
	 * 字段注入元素：处理 {@code @Autowired} 标注的字段
	 */
	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {
		/** 是否必须注入 */
		private final boolean required;
		/** 是否已缓存字段值 */
		private volatile boolean cached;
		/** 缓存的字段值 */
		@Nullable
		private volatile Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		/**
		 * 执行字段注入
		 */
		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value;

			// 优先使用缓存值
			if (this.cached) {
				value = resolvedCachedArgument(beanName, this.cachedFieldValue);
			} else {
				// 解析字段值（从 Spring 容器查找依赖）
				value = resolveFieldValue(field, bean, beanName);
			}

			// 反射赋值，完成注入
			if (value != null) {
				ReflectionUtils.makeAccessible(field);
				field.set(bean, value);
			}
		}

		/**
		 * 解析字段值：从 Spring 容器获取依赖 Bean
		 */
		@Nullable
		private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
			// 封装依赖描述符
			DependencyDescriptor desc = new DependencyDescriptor(field, required);
			desc.setContainingClass(bean.getClass());
			Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
			TypeConverter typeConverter = beanFactory.getTypeConverter();

			// 【核心】从 Spring 容器解析依赖
			Object value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);

			// 缓存依赖描述符
			synchronized (this) {
				if (!cached) {
					cachedFieldValue = desc;
					registerDependentBeans(beanName, autowiredBeanNames);
					cached = true;
				}
			}
			return value;
		}
	}
}