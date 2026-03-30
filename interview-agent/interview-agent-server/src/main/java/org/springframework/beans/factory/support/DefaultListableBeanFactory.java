//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.beans.factory.support;

// 修正反编译错误的导入，删除无效的 [Ljava.lang.String;;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.inject.Provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.CompositeIterator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring IoC 容器的默认实现，实现了 ConfigurableListableBeanFactory 和 BeanDefinitionRegistry 接口。
 * 它是 ApplicationContext 的底层核心 BeanFactory，负责 BeanDefinition 的注册、管理和依赖解析。
 * 该类支持 Bean 定义的覆盖、懒加载、类型匹配、依赖自动装配等功能，并维护了各种缓存以提升性能。
 *
 * 主要功能：
 * - 管理 BeanDefinition 的注册、获取、移除
 * - 根据类型或名称获取 Bean 实例
 * - 支持依赖注入（@Autowired、@Inject 等）
 * - 处理 FactoryBean 和 SmartFactoryBean
 * - 支持单例预实例化
 * - 缓存 Bean 类型和名称映射
 *
 * @author Rod Johnson, Juergen Hoeller, etc.
 * @since 16.04.2003
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	@Nullable
	private static Class<?> javaxInjectProviderClass;
	// 存储可序列化的 BeanFactory 实例，以序列化 ID 为键，弱引用为值
	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories;

	// ============ 序列化 ID，用于反序列化时恢复 BeanFactory ============
	// 当 BeanFactory 需要序列化时，通过这个 ID 可以在反序列化时找到对应的工厂实例
	@Nullable
	private String serializationId;
	// ============ 是否允许 BeanDefinition 覆盖 ============
	// 默认 true，当注册同名 Bean 时，新的 BeanDefinition 会覆盖旧的
	// 生产环境建议设置为 false，避免意外覆盖导致难以排查的问题
	private boolean allowBeanDefinitionOverriding = true;
	// ============ 是否允许提前加载类 ============
	// 控制是否在启动阶段就加载 Bean 对应的类，默认为 true
	// 设置为 false 可以延迟类加载到真正使用时，但可能影响性能
	private boolean allowEagerClassLoading = true;
	// ============ 依赖排序比较器 ============
	// 用于对依赖进行排序，支持 @Order、PriorityOrdered 等注解
	// 当存在多个候选 Bean 时，通过此比较器确定注入顺序
	@Nullable
	private Comparator<Object> dependencyComparator;
	// ============ 自动装配候选解析器 ============
	// 核心组件，负责判断一个 Bean 是否可以作为某个依赖注入点的候选
	// 支持 @Autowired、@Inject、@Qualifier、泛型等复杂场景
	private AutowireCandidateResolver autowireCandidateResolver;

	// ============ 可解析的依赖映射 ============
	// 存储一些特殊的依赖类型及其对应的值，如 Environment、ApplicationContext 等
	// 这些依赖不是通过 BeanDefinition 注册的，而是直接由容器提供
	// Key: 依赖类型，Value: 对应的实例或 ObjectFactory
	private final Map<Class<?>, Object> resolvableDependencies;

	// ============ 核心 BeanDefinition 存储 ============
	// 整个 IoC 容器的核心数据结构，存储所有 Bean 的定义信息
	// Key: beanName（Bean 的唯一标识），Value: BeanDefinition（Bean 的定义信息）
	// ConcurrentHashMap 保证线程安全，初始容量 256 以减少扩容开销
	private final Map<String, BeanDefinition> beanDefinitionMap;
	// ============ 合并后的 BeanDefinitionHolder 缓存 ============
	// 缓存经过合并处理的 BeanDefinitionHolder，包含别名等信息
	// 避免重复合并操作，提升性能
	// Key: beanName，Value: BeanDefinitionHolder（包含原始定义和别名数组）
	private final Map<String, BeanDefinitionHolder> mergedBeanDefinitionHolders;
	// ============ 类型到所有 Bean 名称的缓存 ============
	// 缓存按类型查找的所有 Bean 名称，包含单例、原型、FactoryBean 等
	// 避免每次都遍历所有 BeanDefinition，大幅提升 getBeansOfType 性能
	// Key: Bean 类型，Value: 该类型对应的所有 Bean 名称数组
	private final Map<Class<?>, String[]> allBeanNamesByType;
	// ============ 类型到单例 Bean 名称的缓存 ============
	// 专门缓存单例类型的 Bean 名称，与 allBeanNamesByType 分离以提升常用场景性能
	// Spring 中大部分 Bean 都是单例的，这个缓存使用频率更高
	// Key: Bean 类型，Value: 该类型对应的所有单例 Bean 名称数组
	private final Map<Class<?>, String[]> singletonBeanNamesByType;

	// ============ 所有 Bean 定义名称列表 ============
	// 按注册顺序存储所有 Bean 的名称，用于保证 Bean 的创建顺序
	// volatile 保证可见性，ArrayList 初始容量 256
	// 注意：这里只包含通过 BeanDefinition 注册的 Bean，不包含手动注册的单例
	private volatile List<String> beanDefinitionNames;
	// ============ 手动注册的单例名称集合 ============
	// 存储通过 registerSingleton() 方法手动注册的单例 Bean 名称
	// 这些 Bean 没有对应的 BeanDefinition，直接以实例形式存在
	// LinkedHashSet 保证顺序，初始容量 16
	private volatile Set<String> manualSingletonNames;
	// ============ 冻结后的 BeanDefinition 名称数组 ============
	// 当配置冻结后（freezeConfiguration），将 beanDefinitionNames 转为不可变数组
	// 冻结后不再允许修改 Bean 定义，提升运行时性能
	// null 表示配置未冻结，可以动态修改
	@Nullable
	private volatile String[] frozenBeanDefinitionNames;
	// ============ 配置是否已冻结标志 ============
	// true 表示配置已冻结，不允许再注册或修改 BeanDefinition
	// 通常在容器刷新完成后调用 freezeConfiguration() 设置
	// 冻结后可以安全地使用缓存，无需担心并发修改
	private volatile boolean configurationFrozen;

	// ======================== 构造方法 ======================================

	/**
	 * 默认构造函数，创建一个新的 DefaultListableBeanFactory。
	 * 初始化所有核心的数据结构和缓存 Map。
	 */
	public DefaultListableBeanFactory() {
		// 使用简单自动装配候选解析器，支持基本的 @Autowired 注解
		this.autowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE;
		// 初始化可解析依赖 Map，用于存储 Environment、ApplicationContext 等内置依赖
		this.resolvableDependencies = new ConcurrentHashMap<>(16);
		// 初始化 BeanDefinition 存储 Map，初始容量 256，减少扩容
		this.beanDefinitionMap = new ConcurrentHashMap<>(256);
		// 初始化合并后的 BeanDefinitionHolder 缓存，初始容量 256
		this.mergedBeanDefinitionHolders = new ConcurrentHashMap<>(256);
		// 初始化所有 Bean 名称按类型缓存，初始容量 64（相对较少）
		this.allBeanNamesByType = new ConcurrentHashMap<>(64);
		// 初始化单例 Bean 名称按类型缓存，初始容量 64
		this.singletonBeanNamesByType = new ConcurrentHashMap<>(64);
		// 初始化 Bean 定义名称列表，初始容量 256，保持注册顺序
		this.beanDefinitionNames = new ArrayList<>(256);
		// 初始化手动注册的单例名称集合，初始容量 16，LinkedHashSet 保证顺序
		this.manualSingletonNames = new LinkedHashSet<>(16);
	}

	/**
	 * 带父容器的构造函数，创建一个带有父 BeanFactory 的子容器。
	 * @param parentBeanFactory 父 BeanFactory，用于实现层级容器结构
	 */
	public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		// 调用父类构造函数，设置父容器，实现层级查找
		super(parentBeanFactory);
		// 使用简单自动装配候选解析器
		this.autowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE;
		// 初始化可解析依赖 Map
		this.resolvableDependencies = new ConcurrentHashMap<>(16);
		// 初始化 BeanDefinition 存储 Map
		this.beanDefinitionMap = new ConcurrentHashMap<>(256);
		// 初始化合并后的 BeanDefinitionHolder 缓存
		this.mergedBeanDefinitionHolders = new ConcurrentHashMap<>(256);
		// 初始化所有 Bean 名称按类型缓存
		this.allBeanNamesByType = new ConcurrentHashMap<>(64);
		// 初始化单例 Bean 名称按类型缓存
		this.singletonBeanNamesByType = new ConcurrentHashMap<>(64);
		// 初始化 Bean 定义名称列表
		this.beanDefinitionNames = new ArrayList<>(256);
		// 初始化手动注册的单例名称集合
		this.manualSingletonNames = new LinkedHashSet<>(16);
	}

	// ======================== 序列化支持 ======================================

	/**
	 * 设置序列化 ID，用于 BeanFactory 的序列化和反序列化。
	 * Spring 允许将 BeanFactory 序列化到磁盘或网络传输，反序列化时通过此 ID 恢复。
	 * 使用弱引用存储，避免内存泄漏。
	 * @param serializationId 序列化 ID，唯一标识一个 BeanFactory 实例
	 */
	public void setSerializationId(@Nullable String serializationId) {
		if (serializationId != null) {
			// 将当前工厂以弱引用方式存入静态 Map，key 为序列化 ID
			serializableFactories.put(serializationId, new WeakReference<>(this));
		} else if (this.serializationId != null) {
			// 如果 ID 为空且之前有值，从 Map 中移除旧记录
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}

	/**
	 * 获取当前的序列化 ID。
	 * @return 序列化 ID，若未设置则返回 null
	 */
	@Nullable
	public String getSerializationId() {
		return this.serializationId;
	}

	// ======================== 配置属性 ======================================

	/**
	 * 设置是否允许 BeanDefinition 覆盖。
	 * 生产环境强烈建议设置为 false，防止同名 Bean 意外覆盖导致难以排查的问题。
	 * 默认为 true 是为了向后兼容旧版本 Spring。
	 * @param allowBeanDefinitionOverriding true-允许覆盖，false-禁止覆盖
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * 检查是否允许 BeanDefinition 覆盖。
	 * @return true-允许覆盖，false-禁止覆盖
	 */
	public boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	/**
	 * 设置是否允许提前加载类。
	 * 提前加载可以在启动阶段发现类缺失等问题，但会增加启动时间和内存消耗。
	 * 设置为 false 可以延迟到真正使用时才加载类，适合大型应用优化启动速度。
	 * @param allowEagerClassLoading true-允许提前加载，false-延迟加载
	 */
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	/**
	 * 检查是否允许提前加载类。
	 * @return true-允许提前加载，false-延迟加载
	 */
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	/**
	 * 设置依赖比较器，用于对自动装配的候选 Bean 进行排序。
	 * 常用的比较器有 OrderComparator，支持 @Order、@Priority 等注解。
	 * 当存在多个同类型 Bean 时，通过此比较器确定注入优先级。
	 * @param dependencyComparator 依赖比较器，可为 null（使用默认 OrderComparator）
	 */
	public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	/**
	 * 获取当前的依赖比较器。
	 * @return 依赖比较器，若未设置则返回 null
	 */
	@Nullable
	public Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	/**
	 * 设置自动装配候选解析器，这是依赖注入的核心组件。
	 * 该解析器负责判断一个 Bean 是否可以作为某个依赖注入点的候选者。
	 * 支持的功能包括：
	 * - @Autowired、@Inject 等注解的识别
	 * - @Qualifier 限定符匹配
	 * - 泛型类型匹配
	 * - @Primary 主 Bean 标识
	 * 如果解析器实现了 BeanFactoryAware 接口，会自动注入当前工厂引用。
	 * @param autowireCandidateResolver 自动装配候选解析器，不能为 null
	 */
	public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
		// 非空校验
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		// 如果解析器需要 BeanFactory 引用，则注入
		if (autowireCandidateResolver instanceof BeanFactoryAware) {
			if (System.getSecurityManager() != null) {
				// 在安全管理器下使用特权动作
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
					return null;
				}, this.getAccessControlContext());
			} else {
				// 普通环境下直接设置
				((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	/**
	 * 获取当前的自动装配候选解析器。
	 * @return 自动装配候选解析器
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}

	/**
	 * 从另一个 ConfigurableBeanFactory 复制配置到当前工厂。
	 * 这个方法在创建子容器时非常有用，可以继承父容器的配置属性。
	 * 复制的内容包括：
	 * - 是否允许 BeanDefinition 覆盖
	 * - 是否允许提前加载类
	 * - 依赖比较器
	 * - 自动装配候选解析器
	 * - 可解析依赖映射
	 * @param otherFactory 源工厂，提供配置的模板
	 */
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		// 先调用父类方法复制基础配置（如类加载器、类型转换器等）
		super.copyConfigurationFrom(otherFactory);
		// 只有当源工厂也是 DefaultListableBeanFactory 时才复制其特有配置
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			// 复制 BeanDefinition 覆盖策略
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			// 复制类加载策略
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			// 复制依赖比较器
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			// 复制自动装配候选解析器（如果需要会克隆）
			this.setAutowireCandidateResolver(otherListableFactory.getAutowireCandidateResolver().cloneIfNecessary());
			// 复制所有可解析依赖
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}

	// ======================== BeanFactory 接口方法 ======================================

	/**
	 * 根据类型获取 Bean 实例。
	 * 这是 Spring IoC 容器最核心的方法之一，支持依赖注入。
	 * @param requiredType 所需的 Bean 类型
	 * @param <T> Bean 的类型参数
	 * @return 指定类型的 Bean 实例
	 * @throws BeansException 如果找不到对应的 Bean
	 */
	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		// 委托给带参数的 getBean 方法，不传递额外参数
		return this.getBean(requiredType, (Object[]) null);
	}

	/**
	 * 根据类型和构造参数获取 Bean 实例。
	 * 支持传入参数用于 Bean 的构造函数或工厂方法。
	 * @param requiredType 所需的 Bean 类型
	 * @param args 构造参数，可为 null
	 * @param <T> Bean 的类型参数
	 * @return 指定类型的 Bean 实例
	 * @throws BeansException 如果找不到对应的 Bean
	 */
	@Override
	public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
		// 非空校验
		Assert.notNull(requiredType, "Required type must not be null");
		// 解析 Bean，第三个参数 false 表示找不到时抛出异常而不是返回 null
		Object resolved = this.resolveBean(ResolvableType.forRawClass(requiredType), args, false);
		if (resolved == null) {
			// 未找到 Bean，抛出异常
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}

	/**
	 * 获取指定类型的 ObjectProvider。
	 * ObjectProvider 是 ObjectFactory 的增强版本，支持流式处理和懒加载。
	 * @param requiredType 所需的 Bean 类型
	 * @param <T> Bean 的类型参数
	 * @return ObjectProvider 实例，用于按需获取 Bean
	 */
	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		Assert.notNull(requiredType, "Required type must not be null");
		// 委托给内部实现方法，allowEagerInit=true 表示允许提前初始化
		return this.getBeanProvider(ResolvableType.forRawClass(requiredType), true);
	}

	/**
	 * 获取指定 ResolvableType 的 ObjectProvider。
	 * ResolvableType 支持泛型类型的解析，比 Class 更强大。
	 * @param requiredType 所需的可解析类型
	 * @param <T> Bean 的类型参数
	 * @return ObjectProvider 实例
	 */
	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		// 委托给内部实现方法，allowEagerInit=true
		return this.getBeanProvider(requiredType, true);
	}

	// ======================== BeanDefinitionRegistry 接口方法 ======================================

	/**
	 * 检查是否包含指定名称的 BeanDefinition。
	 * @param beanName Bean 的名称
	 * @return true-包含该 BeanDefinition，false-不包含
	 */
	@Override
	public boolean containsBeanDefinition(String beanName) {
		// 非空校验
		Assert.notNull(beanName, "Bean name must not be null");
		// 直接从核心 Map 中判断
		return this.beanDefinitionMap.containsKey(beanName);
	}

	/**
	 * 获取 BeanDefinition 的数量。
	 * @return 已注册的 BeanDefinition 总数
	 */
	@Override
	public int getBeanDefinitionCount() {
		// 直接返回核心 Map 的大小
		return this.beanDefinitionMap.size();
	}

	/**
	 * 获取所有 BeanDefinition 的名称数组。
	 * 如果配置已冻结，返回冻结后的数组副本；否则返回当前列表的副本。
	 * @return 所有 Bean 的名称数组，按注册顺序排列
	 */
	@Override
	public String[] getBeanDefinitionNames() {
		// 先获取冻结后的名称数组（如果已冻结）
		String[] frozenNames = this.frozenBeanDefinitionNames;
		// 如果已冻结，返回冻结数组的克隆（防止外部修改）
		return frozenNames != null ? frozenNames.clone() : StringUtils.toStringArray(this.beanDefinitionNames);
	}

	// ======================== 获取 ObjectProvider 的内部实现 ======================================

	/**
	 * 获取指定类型的 ObjectProvider，允许懒加载和流式处理。
	 * ObjectProvider 是 Spring 5.0 引入的新特性，支持更灵活的依赖获取方式。
	 * @param requiredType 所需类型
	 * @param allowEagerInit 是否允许提前初始化（包括懒加载的 Bean）
	 * @param <T> Bean 的类型
	 * @return ObjectProvider 实例
	 */
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		Assert.notNull(requiredType, "Required type must not be null");
		// 委托给 ResolvableType 版本的方法
		return this.getBeanProvider(ResolvableType.forRawClass(requiredType), allowEagerInit);
	}

	/**
	 * 获取指定 ResolvableType 的 ObjectProvider 实现。
	 * 这是一个匿名内部类，实现了 ObjectProvider 的所有方法。
	 * @param requiredType 可解析类型（支持泛型）
	 * @param allowEagerInit 是否允许提前初始化
	 * @param <T> Bean 的类型
	 * @return ObjectProvider 实例
	 */
	public <T> ObjectProvider<T> getBeanProvider(final ResolvableType requiredType, final boolean allowEagerInit) {
		// 返回一个匿名内部类实例，实现 ObjectProvider 接口
		return new BeanObjectProvider<T>() {
			/**
			 * 获取 Bean 实例，找不到时抛出异常。
			 */
			@Override
			public T getObject() throws BeansException {
				// 委托给 resolveBean 方法，nonUniqueAsNull=false 表示找不到抛异常
				T resolved = DefaultListableBeanFactory.this.resolveBean(requiredType, (Object[]) null, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}

			/**
			 * 带构造参数的 getObject 方法。
			 */
			@Override
			public T getObject(Object... args) throws BeansException {
				// 使用传入的参数解析 Bean
				T resolved = DefaultListableBeanFactory.this.resolveBean(requiredType, args, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}

			/**
			 * 如果可用则获取 Bean，否则返回 null（不抛异常）。
			 */
			@Nullable
			@Override
			public T getIfAvailable() throws BeansException {
				try {
					// 委托给 resolveBean，找不到返回 null
					return DefaultListableBeanFactory.this.resolveBean(requiredType, (Object[]) null, false);
				} catch (ScopeNotActiveException e) {
					// 如果作用域未激活（如 RequestScope 在非 Web 环境），返回 null
					return null;
				}
			}

			/**
			 * 如果可用则执行消费操作。
			 */
			@Override
			public void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
				// 先获取 Bean
				T dependency = this.getIfAvailable();
				if (dependency != null) {
					try {
						// 执行消费逻辑
						dependencyConsumer.accept(dependency);
					} catch (ScopeNotActiveException ignored) {
						// 忽略作用域未激活异常
					}
				}
			}

			/**
			 * 获取唯一的 Bean 实例，如果有多个则返回 null。
			 */
			@Nullable
			@Override
			public T getIfUnique() throws BeansException {
				try {
					// 第三个参数 true 表示当存在多个候选时返回 null 而不是抛异常
					return DefaultListableBeanFactory.this.resolveBean(requiredType, (Object[]) null, true);
				} catch (ScopeNotActiveException e) {
					return null;
				}
			}

			/**
			 * 如果唯一则执行消费操作。
			 */
			@Override
			public void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
				// 先获取唯一的 Bean
				T dependency = this.getIfUnique();
				if (dependency != null) {
					try {
						dependencyConsumer.accept(dependency);
					} catch (ScopeNotActiveException ignored) {
					}
				}
			}

			/**
			 * 以 Stream 形式获取所有匹配的 Bean。
			 */
			@Override
			public Stream<T> stream() {
				// 获取所有匹配类型的 Bean 名称，然后转为 Stream
				return (Stream<T>) Arrays.stream(DefaultListableBeanFactory.this.getBeanNamesForTypedStream(requiredType, allowEagerInit))
						.map(name -> DefaultListableBeanFactory.this.getBean(name))
						.filter(bean -> !(bean instanceof NullBean)); // 过滤掉 NullBean
			}

			/**
			 * 以有序 Stream 形式获取所有匹配的 Bean（按 @Order 排序）。
			 */
			@Override
			public Stream<T> orderedStream() {
				// 获取所有匹配的 Bean 名称
				String[] beanNames = DefaultListableBeanFactory.this.getBeanNamesForTypedStream(requiredType, allowEagerInit);
				if (beanNames.length == 0) {
					return Stream.empty();
				}
				// 创建 LinkedHashMap 保持顺序
				Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
				for (String beanName : beanNames) {
					Object beanInstance = DefaultListableBeanFactory.this.getBean(beanName);
					if (!(beanInstance instanceof NullBean)) {
						matchingBeans.put(beanName, (T) beanInstance);
					}
				}
				// 使用适配的比较器排序
				Stream<T> stream = matchingBeans.values().stream();
				return stream.sorted(DefaultListableBeanFactory.this.adaptOrderComparator(matchingBeans));
			}
		};
	}

	// 解析 Bean 的核心方法 ========================================================

	/**
	 * 解析一个 Bean，根据类型、参数和是否唯一性要求。
	 * @param requiredType 所需类型
	 * @param args 构造参数
	 * @param nonUniqueAsNull 当存在多个候选时，是否返回 null 而不是抛出异常
	 * @return Bean 实例，若未找到则返回 null
	 */
	@Nullable
	private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
		NamedBeanHolder<T> namedBean = this.resolveNamedBean(requiredType, args, nonUniqueAsNull);
		if (namedBean != null) {
			return namedBean.getBeanInstance();
		}
		BeanFactory parent = this.getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			return ((DefaultListableBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
		} else if (parent != null) {
			ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
			if (args != null) {
				return parentProvider.getObject(args);
			} else {
				return nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable();
			}
		} else {
			return null;
		}
	}

	/**
	 * 获取用于流式处理的 Bean 名称列表。
	 */
	private String[] getBeanNamesForTypedStream(ResolvableType requiredType, boolean allowEagerInit) {
		return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, allowEagerInit);
	}

	// 根据类型获取 Bean 名称（缓存支持）================================================

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		return this.getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		Class<?> resolved = type.resolve();
		if (resolved != null && !type.hasGenerics()) {
			return this.getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
		}
		return this.doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		return this.getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		// 如果配置已冻结且类型不为空且允许提前初始化，尝试从缓存获取
		if (this.isConfigurationFrozen() && type != null && allowEagerInit) {
			Map<Class<?>, String[]> cache = includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType;
			String[] resolvedBeanNames = cache.get(type);
			if (resolvedBeanNames != null) {
				return resolvedBeanNames;
			}
			resolvedBeanNames = this.doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
			if (ClassUtils.isCacheSafe(type, this.getBeanClassLoader())) {
				cache.put(type, resolvedBeanNames);
			}
			return resolvedBeanNames;
		}
		return this.doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
	}

	/**
	 * 实际执行类型匹配的 Bean 名称查找，遍历所有 BeanDefinition 和手动注册的单例。
	 */
	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();
		// 遍历所有 BeanDefinition
		for (String beanName : this.beanDefinitionNames) {
			if (this.isAlias(beanName)) {
				continue;
			}
			try {
				RootBeanDefinition mbd = this.getMergedLocalBeanDefinition(beanName);
				// 跳过抽象 Bean，且满足提前初始化条件
				if (mbd.isAbstract() || (!allowEagerInit &&
						((!mbd.hasBeanClass() && mbd.isLazyInit() && !this.isAllowEagerClassLoading()) ||
								this.requiresEagerInitForType(mbd.getFactoryBeanName())))) {
					continue;
				}
				boolean isFactoryBean = this.isFactoryBean(beanName, mbd);
				BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
				boolean matchFound = false;
				boolean allowFactoryBeanInit = allowEagerInit || this.containsSingleton(beanName);
				boolean isNonLazyDecorated = dbd != null && !mbd.isLazyInit();
				if (!isFactoryBean) {
					if (includeNonSingletons || this.isSingleton(beanName, mbd, dbd)) {
						matchFound = this.isTypeMatch(beanName, type, allowFactoryBeanInit);
					}
				} else {
					// 对于 FactoryBean，先检查实际 Bean 类型
					if (includeNonSingletons || isNonLazyDecorated || (allowFactoryBeanInit && this.isSingleton(beanName, mbd, dbd))) {
						matchFound = this.isTypeMatch(beanName, type, allowFactoryBeanInit);
					}
					if (!matchFound) {
						// 尝试 FactoryBean 本身
						beanName = "&" + beanName;
						matchFound = this.isTypeMatch(beanName, type, allowFactoryBeanInit);
					}
				}
				if (matchFound) {
					result.add(beanName);
				}
			} catch (BeanDefinitionStoreException | CannotLoadBeanClassException ex) {
				if (allowEagerInit) {
					throw ex;
				}
				LogMessage message = ex instanceof CannotLoadBeanClassException ?
						LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
						LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName);
				this.logger.trace(message, ex);
				this.onSuppressedException(ex);
			} catch (NoSuchBeanDefinitionException ignored) {
			}
		}

		// 遍历手动注册的单例
		for (String beanName : this.manualSingletonNames) {
			try {
				if (this.isFactoryBean(beanName)) {
					if ((includeNonSingletons || this.isSingleton(beanName)) && this.isTypeMatch(beanName, type)) {
						result.add(beanName);
						continue;
					}
					beanName = "&" + beanName;
				}
				if (this.isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			} catch (NoSuchBeanDefinitionException ex) {
				this.logger.trace(LogMessage.format("Failed to check manually registered singleton with name '%s'", beanName), ex);
			}
		}
		return StringUtils.toStringArray(result);
	}

	private boolean isSingleton(String beanName, RootBeanDefinition mbd, @Nullable BeanDefinitionHolder dbd) {
		return dbd != null ? mbd.isSingleton() : this.isSingleton(beanName);
	}

	private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
		return factoryBeanName != null && this.isFactoryBean(factoryBeanName) && !this.containsSingleton(factoryBeanName);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		return this.getBeansOfType(type, true, true);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
		String[] beanNames = this.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			try {
				Object beanInstance = this.getBean(beanName);
				if (!(beanInstance instanceof NullBean)) {
					result.put(beanName, (T) beanInstance);
				}
			} catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					String exBeanName = bce.getBeanName();
					if (exBeanName != null && this.isCurrentlyInCreation(exBeanName)) {
						if (this.logger.isTraceEnabled()) {
							this.logger.trace("Ignoring match to currently created bean '" + exBeanName + "': " + ex.getMessage());
						}
						this.onSuppressedException(ex);
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	// 注解支持 ===================================================================

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		List<String> result = new ArrayList<>();
		for (String beanName : this.beanDefinitionNames) {
			BeanDefinition bd = this.beanDefinitionMap.get(beanName);
			if (bd != null && !bd.isAbstract() && this.findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		for (String beanName : this.manualSingletonNames) {
			if (!result.contains(beanName) && this.findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		return StringUtils.toStringArray(result);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		String[] beanNames = this.getBeanNamesForAnnotation(annotationType);
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			Object beanInstance = this.getBean(beanName);
			if (!(beanInstance instanceof NullBean)) {
				result.put(beanName, beanInstance);
			}
		}
		return result;
	}

	@Nullable
	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
		return this.findAnnotationOnBean(beanName, annotationType, true);
	}

	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		return this.findMergedAnnotationOnBean(beanName, annotationType, allowFactoryBeanInit).synthesize(MergedAnnotation::isPresent).orElse(null);
	}

	private <A extends Annotation> MergedAnnotation<A> findMergedAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) {
		Class<?> beanType = this.getType(beanName, allowFactoryBeanInit);
		if (beanType != null) {
			MergedAnnotation<A> annotation = MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		if (this.containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = this.getMergedLocalBeanDefinition(beanName);
			if (bd.hasBeanClass()) {
				Class<?> beanClass = bd.getBeanClass();
				if (beanClass != beanType) {
					MergedAnnotation<A> annotation = MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
					if (annotation.isPresent()) {
						return annotation;
					}
				}
			}
			Method factoryMethod = bd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				MergedAnnotation<A> annotation = MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
				if (annotation.isPresent()) {
					return annotation;
				}
			}
		}
		return MergedAnnotation.missing();
	}

	// 注册可解析依赖 ==============================================================

	@Override
	public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
		Assert.notNull(dependencyType, "Dependency type must not be null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory) && !dependencyType.isInstance(autowiredValue)) {
				throw new IllegalArgumentException("Value [" + autowiredValue + "] does not implement specified dependency type [" + dependencyType.getName() + "]");
			}
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}

	// 自动装配候选判断 ============================================================

	@Override
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {
		return this.isAutowireCandidate(beanName, descriptor, this.getAutowireCandidateResolver());
	}

	protected boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver) throws NoSuchBeanDefinitionException {
		String bdName = BeanFactoryUtils.transformedBeanName(beanName);
		if (this.containsBeanDefinition(bdName)) {
			return this.isAutowireCandidate(beanName, this.getMergedLocalBeanDefinition(bdName), descriptor, resolver);
		} else if (this.containsSingleton(beanName)) {
			return this.isAutowireCandidate(beanName, new RootBeanDefinition(this.getType(beanName)), descriptor, resolver);
		} else {
			BeanFactory parent = this.getParentBeanFactory();
			if (parent instanceof DefaultListableBeanFactory) {
				return ((DefaultListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor, resolver);
			} else if (parent instanceof ConfigurableListableBeanFactory) {
				return ((ConfigurableListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor);
			} else {
				return true;
			}
		}
	}

	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {
		String bdName = BeanFactoryUtils.transformedBeanName(beanName);
		this.resolveBeanClass(mbd, bdName);
		if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		BeanDefinitionHolder holder = beanName.equals(bdName) ?
				this.mergedBeanDefinitionHolders.computeIfAbsent(beanName, key -> new BeanDefinitionHolder(mbd, beanName, this.getAliases(bdName))) :
				new BeanDefinitionHolder(mbd, beanName, this.getAliases(bdName));
		return resolver.isAutowireCandidate(holder, descriptor);
	}

	// BeanDefinition 操作 =======================================================

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<>();
		iterator.add(this.beanDefinitionNames.iterator());
		iterator.add(this.manualSingletonNames.iterator());
		return iterator;
	}

	@Override
	protected void clearMergedBeanDefinition(String beanName) {
		super.clearMergedBeanDefinition(beanName);
		this.mergedBeanDefinitionHolders.remove(beanName);
	}

	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		this.mergedBeanDefinitionHolders.clear();
		this.clearByTypeCache();
	}

	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName);
	}

	// 单例预实例化 ================================================================

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Pre-instantiating singletons in " + this);
		}
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
		for (String beanName : beanNames) {
			RootBeanDefinition bd = this.getMergedLocalBeanDefinition(beanName);
			if (bd.isAbstract() || !bd.isSingleton() || bd.isLazyInit()) {
				continue;
			}
			if (this.isFactoryBean(beanName)) {
				Object bean = this.getBean("&" + beanName);
				if (bean instanceof FactoryBean) {
					FactoryBean<?> factory = (FactoryBean<?>) bean;
					boolean isEagerInit;
					if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
						isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>) ((SmartFactoryBean<?>) factory)::isEagerInit, this.getAccessControlContext());
					} else {
						isEagerInit = (factory instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factory).isEagerInit());
					}
					if (isEagerInit) {
						this.getBean(beanName);
					}
				}
			} else {
				this.getBean(beanName);
			}
		}
		// 触发 SmartInitializingSingleton 回调
		for (String beanName : beanNames) {
			Object singletonInstance = this.getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				StartupStep smartInitialize = this.getApplicationStartup().start("spring.beans.smart-initialize")
						.tag("beanName", beanName);
				SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((java.security.PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, this.getAccessControlContext());
				} else {
					smartSingleton.afterSingletonsInstantiated();
				}
				smartInitialize.end();
			}
		}
	}

	// BeanDefinitionRegistry 方法实现 =============================================

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			} catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}
		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		if (existingDefinition != null) {
			if (!this.isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			// 记录覆盖日志
			if (existingDefinition.getRole() < beanDefinition.getRole()) {
				if (this.logger.isInfoEnabled()) {
					this.logger.info("Overriding user-defined bean definition for bean '" + beanName +
							"' with a framework-generated bean definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			} else if (!beanDefinition.equals(existingDefinition)) {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Overriding bean definition for bean '" + beanName +
							"' with a different definition: replacing [" + existingDefinition +
							"] with [" + beanDefinition + "]");
				}
			} else if (this.logger.isTraceEnabled()) {
				this.logger.trace("Overriding bean definition for bean '" + beanName +
						"' with an equivalent definition: replacing [" + existingDefinition +
						"] with [" + beanDefinition + "]");
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);
		} else {
			// 第一次注册
			if (this.hasBeanCreationStarted()) {
				synchronized (this.beanDefinitionMap) {
					this.beanDefinitionMap.put(beanName, beanDefinition);
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					this.removeManualSingletonName(beanName);
				}
			} else {
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				this.removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}
		if (existingDefinition == null && !this.containsSingleton(beanName)) {
			if (this.isConfigurationFrozen()) {
				this.clearByTypeCache();
			}
		} else {
			this.resetBeanDefinition(beanName);
		}
	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");
		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		if (this.hasBeanCreationStarted()) {
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
				updatedDefinitions.remove(beanName);
				this.beanDefinitionNames = updatedDefinitions;
			}
		} else {
			this.beanDefinitionNames.remove(beanName);
		}
		this.frozenBeanDefinitionNames = null;
		this.resetBeanDefinition(beanName);
	}

	protected void resetBeanDefinition(String beanName) {
		this.clearMergedBeanDefinition(beanName);
		this.destroySingleton(beanName);
		for (MergedBeanDefinitionPostProcessor processor : this.getBeanPostProcessorCache().mergedDefinition) {
			processor.resetBeanDefinition(beanName);
		}
		// 重置所有依赖此 Bean 的子 BeanDefinition
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				if (bd != null && beanName.equals(bd.getParentName())) {
					this.resetBeanDefinition(bdName);
				}
			}
		}
	}

	@Override
	protected boolean allowAliasOverriding() {
		return this.isAllowBeanDefinitionOverriding();
	}

	@Override
	protected void checkForAliasCircle(String name, String alias) {
		super.checkForAliasCircle(name, alias);
		if (!this.isAllowBeanDefinitionOverriding() && this.containsBeanDefinition(alias)) {
			throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" + name +
					"': Alias would override bean definition '" + alias + "'");
		}
	}

	// 手动注册单例 ================================================================

	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);
		this.updateManualSingletonNames(set -> set.add(beanName), set -> !this.beanDefinitionMap.containsKey(beanName));
		this.clearByTypeCache();
	}

	@Override
	public void destroySingletons() {
		super.destroySingletons();
		this.updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
		this.clearByTypeCache();
	}

	@Override
	public void destroySingleton(String beanName) {
		super.destroySingleton(beanName);
		this.removeManualSingletonName(beanName);
		this.clearByTypeCache();
	}

	private void removeManualSingletonName(String beanName) {
		this.updateManualSingletonNames(set -> set.remove(beanName), set -> set.contains(beanName));
	}

	private void updateManualSingletonNames(Consumer<Set<String>> action, Predicate<Set<String>> condition) {
		if (this.hasBeanCreationStarted()) {
			synchronized (this.beanDefinitionMap) {
				if (condition.test(this.manualSingletonNames)) {
					Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
					action.accept(updatedSingletons);
					this.manualSingletonNames = updatedSingletons;
				}
			}
		} else if (condition.test(this.manualSingletonNames)) {
			action.accept(this.manualSingletonNames);
		}
	}

	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}

	// 解析命名 Bean ===============================================================

	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		NamedBeanHolder<T> namedBean = this.resolveNamedBean(ResolvableType.forRawClass(requiredType), (Object[]) null, false);
		if (namedBean != null) {
			return namedBean;
		}
		BeanFactory parent = this.getParentBeanFactory();
		if (parent instanceof AutowireCapableBeanFactory) {
			return ((AutowireCapableBeanFactory) parent).resolveNamedBean(requiredType);
		}
		throw new NoSuchBeanDefinitionException(requiredType);
	}

	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		String[] candidateNames = this.getBeanNamesForType(requiredType);
		// 如果候选多于一个，过滤出 autowire-candidate 为 true 的
		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				if (!this.containsBeanDefinition(beanName) || this.getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = StringUtils.toStringArray(autowireCandidates);
			}
		}
		if (candidateNames.length == 1) {
			return this.resolveNamedBean(candidateNames[0], requiredType, args);
		} else if (candidateNames.length > 1) {
			Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.length);
			for (String beanName : candidateNames) {
				if (this.containsSingleton(beanName) && args == null) {
					Object beanInstance = this.getBean(beanName);
					candidates.put(beanName, beanInstance instanceof NullBean ? null : beanInstance);
				} else {
					candidates.put(beanName, this.getType(beanName));
				}
			}
			String candidateName = this.determinePrimaryCandidate(candidates, requiredType.toClass());
			if (candidateName == null) {
				candidateName = this.determineHighestPriorityCandidate(candidates, requiredType.toClass());
			}
			if (candidateName != null) {
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance == null) {
					return null;
				}
				if (beanInstance instanceof Class) {
					return this.resolveNamedBean(candidateName, requiredType, args);
				}
				return new NamedBeanHolder<>(candidateName, (T) beanInstance);
			}
			if (!nonUniqueAsNull) {
				throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
			}
		}
		return null;
	}

	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(String beanName, ResolvableType requiredType, @Nullable Object[] args) throws BeansException {
		Object bean = this.getBean(beanName, null, args);
		return bean instanceof NullBean ? null : new NamedBeanHolder<>(beanName, this.adaptBeanInstance(beanName, bean, requiredType.toClass()));
	}

	// ======================== 依赖解析核心方法 ============================================

	/**
	 * 解析依赖的核心入口方法。
	 * 根据 DependencyDescriptor 解析并返回匹配的 Bean 实例。
	 * 支持多种依赖类型：Optional、ObjectFactory、ObjectProvider、javax.inject.Provider 等。
	 * @param descriptor 依赖描述符，包含依赖的元数据信息（类型、注解、参数名等）
	 * @param requestingBeanName 请求依赖的 Bean 名称，用于避免自引用和循环依赖检测
	 * @param autowiredBeanNames 记录被自动装配的 Bean 名称集合，用于依赖检查
	 * @param typeConverter 类型转换器，用于将值转换为目标类型
	 * @return 解析后的 Bean 实例，若无法解析则返回 null 或抛出异常
	 * @throws BeansException 当依赖无法解析时抛出
	 */
	@Nullable
	@Override
	public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
									@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
		// 初始化参数名发现器，用于获取构造参数的实际名称（支持 @Inject 等注解）
		descriptor.initParameterNameDiscovery(this.getParameterNameDiscoverer());
		// 判断是否为 Optional 类型依赖，如果是则创建 Optional 包装的依赖
		if (Optional.class == descriptor.getDependencyType()) {
			// 创建可选依赖，找不到 Bean 时返回 Optional.empty() 而不是抛异常
			return this.createOptionalDependency(descriptor, requestingBeanName);
		} else if (ObjectFactory.class != descriptor.getDependencyType() && ObjectProvider.class != descriptor.getDependencyType()) {
			// 如果不是 ObjectFactory 或 ObjectProvider 类型，继续判断其他特殊类型
			// 判断是否为 JSR-330 (javax.inject.Provider) 类型依赖
			if (javaxInjectProviderClass == descriptor.getDependencyType()) {
				// 创建 JSR-330 Provider 实现，支持标准的依赖注入规范
				return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
			} else {
				// 普通依赖类型，先尝试获取懒代理对象（如果配置了懒加载）
				Object result = this.getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor, requestingBeanName);
				// 如果没有懒代理，则执行完整的依赖解析流程
				if (result == null) {
					// 调用核心依赖解析方法，这是自动装配的真正实现
					result = this.doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
				}
				return result;
			}
		} else {
			// ObjectFactory 或 ObjectProvider 类型，直接封装为 DependencyObjectProvider
			// 这种类型的依赖不会立即解析，而是在调用 getObject() 时才真正解析
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		}
	}

	/**
	 * 执行实际的依赖解析逻辑，这是 Spring 自动装配最核心的方法。
	 * 解析流程：快捷方式 -> @Value 注解 -> 复合类型（数组/Collection/Map） -> 单一 Bean
	 * @param descriptor 依赖描述符
	 * @param beanName 当前 Bean 名称
	 * @param autowiredBeanNames 记录被装配的 Bean 名称
	 * @param typeConverter 类型转换器
	 * @return 解析后的 Bean 实例
	 * @throws BeansException 解析失败时抛出异常
	 */
	@Nullable
	public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
									  @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
		// 设置当前注入点，用于后续的错误处理和日志记录
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			// 尝试解析快捷方式，某些特殊依赖可以直接返回而不需要完整流程
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				// 快捷方式命中，直接返回（如 Environment、ApplicationContext 等内置依赖）
				return shortcut;
			}

			// 获取依赖的目标类型
			Class<?> type = descriptor.getDependencyType();
			// 从解析器获取建议的值，主要用于处理 @Value 注解
			Object value = this.getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				// 如果值是字符串类型（通常是 @Value("${property}") 或 @Value("#{SpEL}")）
				if (value instanceof String) {
					// 解析占位符 ${...}，将配置文件的值替换进来
					String strVal = this.resolveEmbeddedValue((String) value);
					// 获取 BeanDefinition，用于评估 SpEL 表达式
					BeanDefinition bd = beanName != null && this.containsBean(beanName) ? this.getMergedBeanDefinition(beanName) : null;
					// 评估 SpEL 表达式 #{...}，得到最终的值
					value = this.evaluateBeanDefinitionString(strVal, bd);
				}
				// 获取类型转换器，优先使用传入的转换器，否则使用容器的默认转换器
				TypeConverter converter = typeConverter != null ? typeConverter : this.getTypeConverter();
				try {
					// 将值转换为目标类型，如 String 转 Integer、String 转 Boolean 等
					return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
				} catch (UnsupportedOperationException e) {
					// 如果通用转换方法不支持，则根据字段或方法参数分别处理
					return (descriptor.getField() != null ?
							// 字段注入场景，使用 Field 进行类型转换
							converter.convertIfNecessary(value, type, descriptor.getField()) :
							// 方法参数注入场景，使用 MethodParameter 进行类型转换
							converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
				}
			}

			// 处理复合类型依赖：数组、Collection、Map、Stream 等
			// 这些类型需要找到所有匹配的 Bean 而不仅仅是单个
			Object multipleBeans = this.resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
			if (multipleBeans != null) {
				// 复合类型解析成功，直接返回（如 String[]、List<String>、Map<String, Service>）
				return multipleBeans;
			}

			// 查找所有匹配的候选 Bean
			// 返回 Map<String, Object>，key 是 Bean 名称，value 是 Bean 实例或 Class 类型
			Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				// 未找到任何匹配的 Bean
				if (this.isRequired(descriptor)) {
					// 如果依赖是必需的（没有 @Nullable 注解），抛出异常
					this.raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				// 非必需依赖返回 null
				return null;
			}

			// 声明要注入的 Bean 名称和实例
			String autowiredBeanName;
			Object instanceCandidate;
			if (matchingBeans.size() > 1) {
				// 存在多个候选 Bean，需要确定使用哪一个
				// 按优先级：@Primary > @Order/@Priority > byName 匹配
				autowiredBeanName = this.determineAutowireCandidate(matchingBeans, descriptor);
				if (autowiredBeanName == null) {
					// 无法确定主候选 Bean
					if (!this.isRequired(descriptor) && this.indicatesMultipleBeans(type)) {
						// 非必需依赖且类型表示多元素，返回 null
						return null;
					}
					// 抛出 NoUniqueBeanDefinitionException 异常
					return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
				}
				// 获取选中的 Bean 实例
				instanceCandidate = matchingBeans.get(autowiredBeanName);
			} else {
				// 只有一个候选 Bean，直接使用
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				autowiredBeanName = entry.getKey();
				instanceCandidate = entry.getValue();
			}

			// 如果需要记录自动装配的 Bean 名称，则添加到集合中
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(autowiredBeanName);
			}
			// 如果候选者是 Class 类型，说明还未实例化，需要解析为 Bean 实例
			if (instanceCandidate instanceof Class) {
				// 调用 getBean 方法获取真正的 Bean 实例
				instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
			}
			// 保存最终结果
			Object result = instanceCandidate;
			if (result instanceof NullBean) {
				// 如果是 NullBean（Spring 内部标记空值的特殊类）
				if (this.isRequired(descriptor)) {
					// 必需依赖抛异常
					this.raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				// 否则设置为 null
				result = null;
			}
			// 最后校验类型是否匹配
			if (!ClassUtils.isAssignableValue(type, result)) {
				// 类型不匹配，抛出 BeanNotOfRequiredTypeException
				throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
			}
			// 返回解析后的 Bean 实例
			return result;
		} finally {
			// 恢复之前的注入点上下文，确保线程安全
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	/**
	 * 解析多元素依赖：数组、Collection、Map、Stream 等复合类型。
	 * 当依赖是集合类型时，需要找到所有匹配的 Bean 并组装成对应的数据结构。
	 * @param descriptor 依赖描述符
	 * @param beanName 当前 Bean 名称
	 * @param autowiredBeanNames 记录被装配的 Bean 名称
	 * @param typeConverter 类型转换器
	 * @return 组装好的集合对象，若不是集合类型则返回 null
	 */
	@Nullable
	private Object resolveMultipleBeans(DependencyDescriptor descriptor, @Nullable String beanName,
										@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {
		// 获取依赖的类型
		Class<?> type = descriptor.getDependencyType();
		// 判断是否为 Stream 类型（Java 8+ 特性）
		if (descriptor instanceof StreamDependencyDescriptor) {
			// 查找所有匹配的 Bean
			Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, type, descriptor);
			// 记录所有被自动装配的 Bean 名称
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 创建 Stream 流，过滤掉 NullBean
			Stream<Object> stream = matchingBeans.keySet().stream()
					// 解析每个 Bean 实例
					.map(name -> descriptor.resolveCandidate(name, type, this))
					// 过滤 NullBean
					.filter(bean -> !(bean instanceof NullBean));
			// 如果是有序 Stream，则按 @Order 排序
			if (((StreamDependencyDescriptor) descriptor).isOrdered()) {
				// 使用适配的比较器排序，考虑 FactoryBean 和目标类型
				stream = stream.sorted(this.adaptOrderComparator(matchingBeans));
			}
			// 返回 Stream
			return stream;
		} else if (type.isArray()) {
			// 数组类型处理，如 UserService[]
			// 获取数组组件类型（元素类型）
			Class<?> componentType = type.getComponentType();
			// 获取可解析的类型信息（支持泛型）
			ResolvableType resolvableType = descriptor.getResolvableType();
			// 解析实际的值类型
			Class<?> valueType = resolvableType.resolve(type);
			// 如果解析后的类型与原类型不同（说明有泛型），重新获取组件类型
			if (valueType != type) {
				componentType = resolvableType.getComponentType().resolve();
			}
			// 如果组件类型为 null，无法处理
			if (componentType == null) {
				return null;
			}
			// 查找所有匹配组件类型的 Bean
			Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, componentType, new MultiElementDescriptor(descriptor));
			// 如果没有匹配的 Bean，返回 null
			if (matchingBeans.isEmpty()) {
				return null;
			}
			// 记录所有被装配的 Bean 名称
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 获取类型转换器
			TypeConverter converter = typeConverter != null ? typeConverter : this.getTypeConverter();
			// 将所有 Bean 实例转换为数组类型
			Object result = converter.convertIfNecessary(matchingBeans.values(), valueType);
			// 如果结果是数组类型，且有多个元素，则排序
			if (result instanceof Object[]) {
				// 适配依赖比较器，考虑 FactoryBean 和目标类型
				Comparator<Object> comparator = this.adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					// 使用比较器排序数组元素
					Arrays.sort((Object[]) result, comparator);
				}
			}
			// 返回数组
			return result;
		} else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			// Collection 接口类型处理，如 List<Service>、Set<Service>
			// 获取集合的泛型元素类型
			Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
			// 如果无法解析元素类型，返回 null
			if (elementType == null) {
				return null;
			}
			// 查找所有匹配元素类型的 Bean
			Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, elementType, new MultiElementDescriptor(descriptor));
			// 如果没有匹配的 Bean，返回 null
			if (matchingBeans.isEmpty()) {
				return null;
			}
			// 记录所有被装配的 Bean 名称
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 获取类型转换器
			TypeConverter converter = typeConverter != null ? typeConverter : this.getTypeConverter();
			// 将所有 Bean 实例转换为集合类型
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			// 如果结果是 List 且有多个元素，则排序
			if (result instanceof List && ((List<?>) result).size() > 1) {
				// 适配依赖比较器
				Comparator<Object> comparator = this.adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					// 对 List 进行排序
					((List<Object>) result).sort(comparator);
				}
			}
			// 返回集合
			return result;
		} else if (Map.class == type) {
			// Map 类型处理，如 Map<String, Service>
			// 获取 Map 的可解析类型
			ResolvableType mapType = descriptor.getResolvableType().asMap();
			// 获取 Map 的键类型（必须是 String）
			Class<?> keyType = mapType.resolveGeneric(0);
			// Spring 只支持 String 作为键名的 Map 注入
			if (String.class != keyType) {
				return null;
			}
			// 获取 Map 的值类型
			Class<?> valueType = mapType.resolveGeneric(1);
			// 如果值类型为 null，无法处理
			if (valueType == null) {
				return null;
			}
			// 查找所有匹配值类型的 Bean
			Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, valueType, new MultiElementDescriptor(descriptor));
			// 如果没有匹配的 Bean，返回 null
			if (matchingBeans.isEmpty()) {
				return null;
			}
			// 记录所有被装配的 Bean 名称
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			// 直接返回 Map，key 是 Bean 名称，value 是 Bean 实例
			return matchingBeans;
		} else {
			// 不是上述任何复合类型，返回 null
			return null;
		}
	}

	private boolean isRequired(DependencyDescriptor descriptor) {
		return this.getAutowireCandidateResolver().isRequired(descriptor);
	}

	private boolean indicatesMultipleBeans(Class<?> type) {
		return type.isArray() || (type.isInterface() && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)));
	}

	@Nullable
	private Comparator<Object> adaptDependencyComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> comparator = this.getDependencyComparator();
		return comparator instanceof OrderComparator ?
				((OrderComparator) comparator).withSourceProvider(this.createFactoryAwareOrderSourceProvider(matchingBeans)) :
				comparator;
	}

	private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> dependencyComparator = this.getDependencyComparator();
		OrderComparator comparator = dependencyComparator instanceof OrderComparator ?
				(OrderComparator) dependencyComparator : OrderComparator.INSTANCE;
		return comparator.withSourceProvider(this.createFactoryAwareOrderSourceProvider(matchingBeans));
	}

	private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
		beans.forEach((beanName, instance) -> instancesToBeanNames.put(instance, beanName));
		return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
	}

	/**
	 * 查找所有匹配指定类型的候选 Bean。
	 * @param beanName 请求 Bean 的名称（用于避免自引用）
	 * @param requiredType 所需类型
	 * @param descriptor 依赖描述符
	 * @return Bean 名称到实例或类型的映射
	 */
	protected Map<String, Object> findAutowireCandidates(@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, descriptor.isEager());
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(candidateNames.length);
		// 添加可解析依赖（如 Environment、ApplicationContext）
		for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
			Class<?> autowiringType = classObjectEntry.getKey();
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = classObjectEntry.getValue();
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		// 添加候选 Bean
		for (String candidate : candidateNames) {
			if (!this.isSelfReference(beanName, candidate) && this.isAutowireCandidate(candidate, descriptor)) {
				this.addCandidateEntry(result, candidate, descriptor, requiredType);
			}
		}
		// 如果未找到，尝试放宽条件（fallback 匹配）
		if (result.isEmpty()) {
			boolean multiple = this.indicatesMultipleBeans(requiredType);
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for (String candidate : candidateNames) {
				if (!this.isSelfReference(beanName, candidate) && this.isAutowireCandidate(candidate, fallbackDescriptor) &&
						(!multiple || this.getAutowireCandidateResolver().hasQualifier(descriptor))) {
					this.addCandidateEntry(result, candidate, descriptor, requiredType);
				}
			}
			// 最后尝试自引用
			if (result.isEmpty() && !multiple) {
				for (String candidate : candidateNames) {
					if (this.isSelfReference(beanName, candidate) &&
							(!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) &&
							this.isAutowireCandidate(candidate, fallbackDescriptor)) {
						this.addCandidateEntry(result, candidate, descriptor, requiredType);
					}
				}
			}
		}
		return result;
	}

	private void addCandidateEntry(Map<String, Object> candidates, String candidateName, DependencyDescriptor descriptor, Class<?> requiredType) {
		if (descriptor instanceof MultiElementDescriptor) {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			if (!(beanInstance instanceof NullBean)) {
				candidates.put(candidateName, beanInstance);
			}
		} else if (!this.containsSingleton(candidateName) && (!(descriptor instanceof StreamDependencyDescriptor) || !((StreamDependencyDescriptor) descriptor).isOrdered())) {
			candidates.put(candidateName, this.getType(candidateName));
		} else {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			candidates.put(candidateName, beanInstance instanceof NullBean ? null : beanInstance);
		}
	}

	/**
	 * 确定自动装配的主候选 Bean。
	 * 当存在多个匹配的 Bean 时，按优先级策略选择一个：
	 * 1. @Primary 标注的主 Bean（最高优先级）
	 * 2. @Order/@Priority 指定的优先级
	 * 3. byName 匹配（依赖名称与 Bean 名称或别名匹配）
	 * 4. 可解析依赖（如 Environment、ApplicationContext 等内置依赖）
	 * @param candidates 候选 Bean 映射，key 为 Bean 名称，value 为 Bean 实例或 Class
	 * @param descriptor 依赖描述符，包含依赖的元数据信息
	 * @return 选中的 Bean 名称，若无法确定则返回 null
	 */
	@Nullable
	protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		// 获取依赖的目标类型
		Class<?> requiredType = descriptor.getDependencyType();
		// 第一步：查找 @Primary 标注的主 Bean
		String primaryCandidate = this.determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			// 找到主 Bean，直接返回
			return primaryCandidate;
		}
		// 第二步：查找优先级最高的 Bean（@Order 或 @Priority）
		String priorityCandidate = this.determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			// 找到优先级最高的 Bean，返回
			return priorityCandidate;
		}
		// 第三步：尝试 byName 匹配或可解析依赖匹配
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			// 两种情况可以直接选中：
			// 1. 是可解析依赖（容器内置的特殊依赖，如 Environment）
			// 2. Bean 名称与依赖名称匹配（包括别名匹配）
			if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) ||
					this.matchesBeanName(candidateName, descriptor.getDependencyName())) {
				return candidateName;
			}
		}
		// 所有策略都无法确定，返回 null
		return null;
	}

	/**
	 * 确定 @Primary 标注的主候选 Bean。
	 * Spring 允许通过 @Primary 注解标记某个 Bean 为首选，当存在多个同类型 Bean 时优先注入。
	 * @param candidates 候选 Bean 映射
	 * @param requiredType 所需类型
	 * @return 主 Bean 的名称，若没有 @Primary Bean 则返回 null
	 * @throws NoUniqueBeanDefinitionException 当发现多个 @Primary Bean 时抛出异常
	 */
	@Nullable
	protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		// 记录主 Bean 的名称
		String primaryBeanName = null;
		// 遍历所有候选 Bean
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			// 检查当前 Bean 是否被 @Primary 标注
			if (this.isPrimary(candidateBeanName, beanInstance)) {
				// 如果已经找到过一个主 Bean
				if (primaryBeanName != null) {
					// 检查两个主 Bean 是否都在当前容器中定义
					boolean candidateLocal = this.containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = this.containsBeanDefinition(primaryBeanName);
					// 如果两个都是本地定义的 @Primary Bean，抛异常（配置冲突）
					if (candidateLocal && primaryLocal) {
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
								"more than one 'primary' bean found among candidates: " + candidates.keySet());
					}
					// 如果当前候选是本地定义的，优先使用本地的（忽略父容器的）
					if (candidateLocal) {
						primaryBeanName = candidateBeanName;
					}
				} else {
					// 第一个找到的主 Bean，记录下来
					primaryBeanName = candidateBeanName;
				}
			}
		}
		// 返回主 Bean 名称，若没有则返回 null
		return primaryBeanName;
	}

	/**
	 * 确定优先级最高的候选 Bean（基于 @Order 或 @Priority 注解）。
	 * 优先级数值越小，优先级越高（升序排序）。
	 * @param candidates 候选 Bean 映射
	 * @param requiredType 所需类型
	 * @return 优先级最高的 Bean 名称，若没有找到带优先级的 Bean 则返回 null
	 * @throws NoUniqueBeanDefinitionException 当发现多个相同优先级的 Bean 时抛出异常
	 */
	@Nullable
	protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		// 记录最高优先级的 Bean 名称
		String highestPriorityBeanName = null;
		// 记录最高优先级的数值（值越小优先级越高）
		Integer highestPriority = null;
		// 遍历所有候选 Bean
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			// 只处理非空的 Bean 实例
			if (beanInstance != null) {
				// 获取当前 Bean 的优先级值
				Integer candidatePriority = this.getPriority(beanInstance);
				// 如果该 Bean 有明确的优先级定义
				if (candidatePriority != null) {
					// 如果已经记录过优先级
					if (highestPriorityBeanName != null) {
						// 检查优先级是否相同
						if (candidatePriority.equals(highestPriority)) {
							// 优先级相同，配置冲突，抛异常
							throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
									"Multiple beans found with the same priority ('" + highestPriority + "') among candidates: " + candidates.keySet());
						}
						// 如果当前优先级更高（数值更小），更新记录
						if (candidatePriority < highestPriority) {
							highestPriorityBeanName = candidateBeanName;
							highestPriority = candidatePriority;
						}
					} else {
						// 第一个有优先级的 Bean，直接记录
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				}
			}
		}
		// 返回优先级最高的 Bean 名称
		return highestPriorityBeanName;
	}

	/**
	 * 判断指定 Bean 是否被 @Primary 标注。
	 * 支持从 BeanDefinition 中获取 @Primary 信息，也支持父子容器的层级查找。
	 * @param beanName Bean 名称
	 * @param beanInstance Bean 实例
	 * @return true-是主 Bean，false-不是主 Bean
	 */
	protected boolean isPrimary(String beanName, Object beanInstance) {
		// 转换 Bean 名称，去除 FactoryBean 的前缀 &
		String transformedBeanName = this.transformedBeanName(beanName);
		// 如果当前容器包含该 Bean 的定义
		if (this.containsBeanDefinition(transformedBeanName)) {
			// 从合并后的 BeanDefinition 中检查是否标记为 primary
			return this.getMergedLocalBeanDefinition(transformedBeanName).isPrimary();
		}
		// 当前容器没有定义，委托给父容器检查（层级查找）
		BeanFactory parent = this.getParentBeanFactory();
		// 如果父容器也是 DefaultListableBeanFactory，递归调用 isPrimary 方法
		return parent instanceof DefaultListableBeanFactory &&
				((DefaultListableBeanFactory) parent).isPrimary(transformedBeanName, beanInstance);
	}

	/**
	 * 获取 Bean 的优先级值。
	 * 通过依赖比较器（通常是 OrderComparator）来提取 Bean 的优先级。
	 * 支持 @Order、@Priority 等注解以及 Ordered 接口。
	 * @param beanInstance Bean 实例
	 * @return 优先级数值，若未定义优先级则返回 null
	 */
	@Nullable
	protected Integer getPriority(Object beanInstance) {
		// 获取当前配置的依赖比较器
		Comparator<Object> comparator = this.getDependencyComparator();
		// 如果是 OrderComparator（Spring 默认的比较器），则提取优先级
		return comparator instanceof OrderComparator ? ((OrderComparator) comparator).getPriority(beanInstance) : null;
	}

	/**
	 * 检查 Bean 名称是否与依赖名称匹配。
	 * 支持直接名称匹配和别名匹配两种方式。
	 * @param beanName Bean 的名称
	 * @param candidateName 依赖的名称（通常是字段名或参数名）
	 * @return true-名称匹配，false-名称不匹配
	 */
	protected boolean matchesBeanName(String beanName, @Nullable String candidateName) {
		// candidateName 为空则无法匹配
		// 匹配条件：
		// 1. 依赖名称与 Bean 名称完全相同
		// 2. 依赖名称与 Bean 的某个别名相同
		return candidateName != null && (candidateName.equals(beanName) ||
				ObjectUtils.containsElement(this.getAliases(beanName), candidateName));
	}

	private boolean isSelfReference(@Nullable String beanName, @Nullable String candidateName) {
		return beanName != null && candidateName != null &&
				(beanName.equals(candidateName) ||
						(this.containsBeanDefinition(candidateName) &&
								beanName.equals(this.getMergedLocalBeanDefinition(candidateName).getFactoryBeanName())));
	}

	private void raiseNoMatchingBeanFound(Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor) throws BeansException {
		this.checkBeanNotOfRequiredType(type, descriptor);
		throw new NoSuchBeanDefinitionException(resolvableType,
				"expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: " +
						ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
		for (String beanName : this.beanDefinitionNames) {
			try {
				RootBeanDefinition mbd = this.getMergedLocalBeanDefinition(beanName);
				Class<?> targetType = mbd.getTargetType();
				if (targetType != null && type.isAssignableFrom(targetType) &&
						this.isAutowireCandidate(beanName, mbd, descriptor, this.getAutowireCandidateResolver())) {
					Object beanInstance = this.getSingleton(beanName, false);
					Class<?> beanType = beanInstance != null && beanInstance.getClass() != NullBean.class ?
							beanInstance.getClass() : this.predictBeanType(beanName, mbd);
					if (beanType != null && !type.isAssignableFrom(beanType)) {
						throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
					}
				}
			} catch (NoSuchBeanDefinitionException ignored) {
			}
		}
		BeanFactory parent = this.getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) parent).checkBeanNotOfRequiredType(type, descriptor);
		}
	}

	private Optional<?> createOptionalDependency(DependencyDescriptor descriptor, @Nullable String beanName, final Object... args) {
		DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {
			@Override
			public boolean isRequired() {
				return false;
			}

			@Override
			public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
				return !ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, args) :
						super.resolveCandidate(beanName, requiredType, beanFactory);
			}
		};
		Object result = this.doResolveDependency(descriptorToUse, beanName, null, null);
		return result instanceof Optional ? (Optional<?>) result : Optional.ofNullable(result);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
		sb.append("]; ");
		BeanFactory parent = this.getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		} else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - just a SerializedBeanFactoryReference is");
	}

	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		} else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}

	static {
		try {
			javaxInjectProviderClass = ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			javaxInjectProviderClass = null;
		}
		serializableFactories = new ConcurrentHashMap<>(8);
	}

	// 内部类 =====================================================================

	/**
	 * 用于提供排序源，以便根据工厂方法或目标类型确定顺序。
	 */
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {
		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Nullable
		@Override
		public Object getOrderSource(Object obj) {
			String beanName = this.instancesToBeanNames.get(obj);
			if (beanName != null && DefaultListableBeanFactory.this.containsBeanDefinition(beanName)) {
				RootBeanDefinition beanDefinition = DefaultListableBeanFactory.this.getMergedLocalBeanDefinition(beanName);
				List<Object> sources = new ArrayList<>(2);
				Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
				if (factoryMethod != null) {
					sources.add(factoryMethod);
				}
				Class<?> targetType = beanDefinition.getTargetType();
				if (targetType != null && targetType != obj.getClass()) {
					sources.add(targetType);
				}
				return sources.toArray();
			}
			return null;
		}
	}

	/**
	 * JSR-330 (javax.inject) 支持。
	 */
	private class Jsr330Factory implements Serializable {
		public Object createDependencyProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			return new Jsr330Provider(descriptor, beanName);
		}

		private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {
			public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
				super(descriptor, beanName);
			}

			@Nullable
			@Override
			public Object get() throws BeansException {
				return this.getValue();
			}
		}
	}

	/**
	 * ObjectProvider 的实现，用于懒加载和流式访问。
	 */
	private class DependencyObjectProvider implements BeanObjectProvider<Object> {
		private final DependencyDescriptor descriptor;
		private final boolean optional;
		@Nullable
		private final String beanName;

		public DependencyObjectProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			this.descriptor = new NestedDependencyDescriptor(descriptor);
			this.optional = this.descriptor.getDependencyType() == Optional.class;
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			if (this.optional) {
				return DefaultListableBeanFactory.this.createOptionalDependency(this.descriptor, this.beanName);
			}
			Object result = DefaultListableBeanFactory.this.doResolveDependency(this.descriptor, this.beanName, null, null);
			if (result == null) {
				throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
			}
			return result;
		}

		@Override
		public Object getObject(final Object... args) throws BeansException {
			if (this.optional) {
				return DefaultListableBeanFactory.this.createOptionalDependency(this.descriptor, this.beanName, args);
			}
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
					return beanFactory.getBean(beanName, args);
				}
			};
			Object result = DefaultListableBeanFactory.this.doResolveDependency(descriptorToUse, this.beanName, null, null);
			if (result == null) {
				throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
			}
			return result;
		}

		@Nullable
		@Override
		public Object getIfAvailable() throws BeansException {
			try {
				if (this.optional) {
					return DefaultListableBeanFactory.this.createOptionalDependency(this.descriptor, this.beanName);
				}
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
					@Override
					public boolean isRequired() {
						return false;
					}
				};
				return DefaultListableBeanFactory.this.doResolveDependency(descriptorToUse, this.beanName, null, null);
			} catch (ScopeNotActiveException e) {
				return null;
			}
		}

		@Override
		public void ifAvailable(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = this.getIfAvailable();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				} catch (ScopeNotActiveException ignored) {
				}
			}
		}

		@Nullable
		@Override
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}

				@Nullable
				@Override
				public Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) {
					return null;
				}
			};
			try {
				return this.optional ?
						DefaultListableBeanFactory.this.createOptionalDependency(descriptorToUse, this.beanName) :
						DefaultListableBeanFactory.this.doResolveDependency(descriptorToUse, this.beanName, null, null);
			} catch (ScopeNotActiveException e) {
				return null;
			}
		}

		@Override
		public void ifUnique(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = this.getIfUnique();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				} catch (ScopeNotActiveException ignored) {
				}
			}
		}

		@Nullable
		protected Object getValue() throws BeansException {
			return this.optional ?
					DefaultListableBeanFactory.this.createOptionalDependency(this.descriptor, this.beanName) :
					DefaultListableBeanFactory.this.doResolveDependency(this.descriptor, this.beanName, null, null);
		}

		@Override
		public Stream<Object> stream() {
			return this.resolveStream(false);
		}

		@Override
		public Stream<Object> orderedStream() {
			return this.resolveStream(true);
		}

		private Stream<Object> resolveStream(boolean ordered) {
			DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
			Object result = DefaultListableBeanFactory.this.doResolveDependency(descriptorToUse, this.beanName, null, null);
			return result instanceof Stream ? (Stream<Object>) result : Stream.of(result);
		}
	}

	private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
	}

	private static class StreamDependencyDescriptor extends DependencyDescriptor {
		private final boolean ordered;

		public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
			super(original);
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}

	private static class MultiElementDescriptor extends NestedDependencyDescriptor {
		public MultiElementDescriptor(DependencyDescriptor original) {
			super(original);
		}
	}

	private static class NestedDependencyDescriptor extends DependencyDescriptor {
		public NestedDependencyDescriptor(DependencyDescriptor original) {
			super(original);
			this.increaseNestingLevel();
		}
	}

	private static class SerializedBeanFactoryReference implements Serializable {
		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = DefaultListableBeanFactory.serializableFactories.get(this.id);
			if (ref != null) {
				Object result = ref.get();
				if (result != null) {
					return result;
				}
			}
			DefaultListableBeanFactory dummyFactory = new DefaultListableBeanFactory();
			dummyFactory.serializationId = this.id;
			return dummyFactory;
		}
	}
}