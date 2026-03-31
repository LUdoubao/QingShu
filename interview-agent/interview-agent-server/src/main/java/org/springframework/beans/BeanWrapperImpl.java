//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Spring 框架中 {@link BeanWrapper} 接口的默认实现。
 * BeanWrapper 是一个核心接口，用于以统一的方式操作 JavaBean 的属性（读取、设置、类型转换等）。
 * 它封装了 Java 内省（Introspector）机制，并提供属性访问、类型转换、嵌套属性路径支持等功能。
 *
 * <p>BeanWrapperImpl 还实现了 {@link ConfigurablePropertyAccessor} 接口，支持属性编辑器（PropertyEditor）注册、
 * 类型转换服务（ConversionService）集成等高级功能。它通常被 Spring 的 DataBinder 和 BeanUtils 等类内部使用，
 * 也可以直接用于程序化地操作 JavaBean。
 *
 * <p>特点：
 * <ul>
 *   <li>支持嵌套属性路径，例如 "address.street"</li>
 *   <li>自动进行类型转换（字符串到数字、日期等）</li>
 *   <li>缓存内省结果（CachedIntrospectionResults）以提高性能</li>
 *   <li>支持安全访问控制（AccessControlContext）</li>
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 15 April 2003
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyDescriptor
 * @see CachedIntrospectionResults
 */
public class BeanWrapperImpl extends AbstractNestablePropertyAccessor implements BeanWrapper {

	/**
	 * 缓存的 JavaBean 内省结果，包含属性描述符、方法等元数据。
	 * 使用懒加载方式初始化。
	 */
	@Nullable
	private CachedIntrospectionResults cachedIntrospectionResults;

	/**
	 * 安全访问控制上下文，用于在安全管理器启用时执行受保护的操作。
	 */
	@Nullable
	private AccessControlContext acc;

	/**
	 * 默认构造器，创建一个包装器，不关联具体的 Bean 实例。
	 * 可以通过后续调用 {@link #setWrappedInstance(Object)} 设置目标对象。
	 */
	public BeanWrapperImpl() {
		this(true);
	}

	/**
	 * 构造器，指定是否注册默认的属性编辑器。
	 *
	 * @param registerDefaultEditors 是否注册默认的属性编辑器（例如将字符串转换为数字、日期等）
	 */
	public BeanWrapperImpl(boolean registerDefaultEditors) {
		super(registerDefaultEditors);
	}

	/**
	 * 构造器，包装给定的 JavaBean 实例。
	 *
	 * @param object 要包装的目标对象
	 */
	public BeanWrapperImpl(Object object) {
		super(object);
	}

	/**
	 * 构造器，仅指定目标类的 Class 对象，用于尚未有实例但需要内省信息的场景。
	 *
	 * @param clazz 目标类的 Class
	 */
	public BeanWrapperImpl(Class<?> clazz) {
		super(clazz);
	}

	/**
	 * 构造器，用于创建嵌套属性访问器，供内部递归使用。
	 *
	 * @param object     当前层级的对象
	 * @param nestedPath 嵌套路径（例如 "address"）
	 * @param rootObject 根对象
	 */
	public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
		super(object, nestedPath, rootObject);
	}

	/**
	 * 私有构造器，用于创建嵌套属性访问器，并继承父包装器的安全上下文。
	 *
	 * @param object     当前层级的对象
	 * @param nestedPath 嵌套路径
	 * @param parent     父包装器
	 */
	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl parent) {
		super(object, nestedPath, parent);
		// 继承父级的安全上下文
		this.setSecurityContext(parent.acc);
	}

	/**
	 * 设置要包装的 Bean 实例。
	 * 此方法会重置内省缓存，并重新初始化类型转换委托。
	 *
	 * @param object 新的 Bean 实例
	 */
	public void setBeanInstance(Object object) {
		this.wrappedObject = object;
		this.rootObject = object;
		this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
		// 更新内省类为目标对象的类
		this.setIntrospectionClass(object.getClass());
	}

	/**
	 * 重写父类方法，设置包装实例和嵌套路径，并更新内省类。
	 *
	 * @param object     当前层级对象
	 * @param nestedPath 嵌套路径
	 * @param rootObject 根对象
	 */
	@Override
	public void setWrappedInstance(Object object, @Nullable String nestedPath, @Nullable Object rootObject) {
		super.setWrappedInstance(object, nestedPath, rootObject);
		this.setIntrospectionClass(this.getWrappedClass());
	}

	/**
	 * 设置需要内省的类。如果缓存的类与当前类不同，则清除缓存。
	 *
	 * @param clazz 目标类
	 */
	protected void setIntrospectionClass(Class<?> clazz) {
		if (this.cachedIntrospectionResults != null && this.cachedIntrospectionResults.getBeanClass() != clazz) {
			this.cachedIntrospectionResults = null;
		}
	}

	/**
	 * 获取缓存的 JavaBean 内省结果。如果缓存不存在，则通过静态工厂方法获取。
	 *
	 * @return CachedIntrospectionResults 实例（非空）
	 */
	private CachedIntrospectionResults getCachedIntrospectionResults() {
		if (this.cachedIntrospectionResults == null) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(this.getWrappedClass());
		}
		return this.cachedIntrospectionResults;
	}

	/**
	 * 设置安全访问控制上下文，用于在安全管理器下执行反射调用。
	 *
	 * @param acc AccessControlContext，可为 null
	 */
	public void setSecurityContext(@Nullable AccessControlContext acc) {
		this.acc = acc;
	}

	/**
	 * 获取安全访问控制上下文。
	 *
	 * @return 安全上下文，可能为 null
	 */
	@Nullable
	public AccessControlContext getSecurityContext() {
		return this.acc;
	}

	/**
	 * 将给定的值转换为指定属性所需的类型。
	 * 此方法利用缓存的类型描述符（TypeDescriptor）进行转换。
	 *
	 * @param value        待转换的值
	 * @param propertyName 属性名称
	 * @return 转换后的值
	 * @throws TypeMismatchException 如果类型转换失败
	 */
	@Nullable
	public Object convertForProperty(@Nullable Object value, String propertyName) throws TypeMismatchException {
		CachedIntrospectionResults cachedIntrospectionResults = this.getCachedIntrospectionResults();
		PropertyDescriptor pd = cachedIntrospectionResults.getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new InvalidPropertyException(this.getRootClass(), this.getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		// 获取或创建属性的 TypeDescriptor（用于类型转换）
		TypeDescriptor td = cachedIntrospectionResults.getTypeDescriptor(pd);
		if (td == null) {
			td = cachedIntrospectionResults.addTypeDescriptor(pd, new TypeDescriptor(this.property(pd)));
		}
		// 调用父类的转换方法
		return this.convertForProperty(propertyName, null, value, td);
	}

	/**
	 * 从 PropertyDescriptor 构建一个 Spring 的 Property 对象，用于类型描述。
	 *
	 * @param pd 属性描述符
	 * @return Property 对象
	 */
	private Property property(PropertyDescriptor pd) {
		GenericTypeAwarePropertyDescriptor gpd = (GenericTypeAwarePropertyDescriptor) pd;
		return new Property(gpd.getBeanClass(), gpd.getReadMethod(), gpd.getWriteMethod(), gpd.getName());
	}

	/**
	 * 获取本地属性处理器（非递归），用于直接读写当前包装对象的属性。
	 * 重写父类抽象方法。
	 *
	 * @param propertyName 属性名
	 * @return 属性处理器，如果属性不存在则返回 null
	 */
	@Nullable
	@Override
	protected BeanPropertyHandler getLocalPropertyHandler(String propertyName) {
		PropertyDescriptor pd = this.getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		return (pd != null ? new BeanPropertyHandler(pd) : null);
	}

	/**
	 * 创建一个新的嵌套属性访问器，用于处理嵌套路径。
	 *
	 * @param object     当前嵌套层级的对象
	 * @param nestedPath 嵌套路径（例如 "address.city"）
	 * @return 新的 BeanWrapperImpl 实例
	 */
	@Override
	protected BeanWrapperImpl newNestedPropertyAccessor(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}

	/**
	 * 创建属性不可写的异常信息，包含可能的属性名称建议。
	 *
	 * @param propertyName 属性名
	 * @return NotWritablePropertyException 异常
	 */
	@Override
	protected NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
		PropertyMatches matches = PropertyMatches.forProperty(propertyName, this.getRootClass());
		throw new NotWritablePropertyException(this.getRootClass(), this.getNestedPath() + propertyName,
				matches.buildErrorMessage(), matches.getPossibleMatches());
	}

	/**
	 * 返回当前包装对象的所有属性描述符。
	 *
	 * @return PropertyDescriptor 数组
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.getCachedIntrospectionResults().getPropertyDescriptors();
	}

	/**
	 * 根据属性路径获取属性描述符（支持嵌套路径）。
	 *
	 * @param propertyName 属性路径（例如 "address.street"）
	 * @return 属性描述符
	 * @throws InvalidPropertyException 如果属性不存在
	 */
	@Override
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException {
		// 获取对应嵌套路径的包装器
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.getPropertyAccessorForPropertyPath(propertyName);
		String finalPath = this.getFinalPath(nestedBw, propertyName);
		PropertyDescriptor pd = nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(finalPath);
		if (pd == null) {
			throw new InvalidPropertyException(this.getRootClass(), this.getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		return pd;
	}

	// ======================= 内部类 BeanPropertyHandler =======================

	/**
	 * 针对单个属性的处理器，封装了属性的读/写操作、类型信息等。
	 * 继承自 AbstractNestablePropertyAccessor.PropertyHandler。
	 */
	private class BeanPropertyHandler extends AbstractNestablePropertyAccessor.PropertyHandler {

		private final PropertyDescriptor pd;

		/**
		 * 构造属性处理器。
		 *
		 * @param pd 属性描述符
		 */
		public BeanPropertyHandler(PropertyDescriptor pd) {
			// 调用父类构造器，传递属性类型、是否有读方法、是否有写方法
			super(pd.getPropertyType(), pd.getReadMethod() != null, pd.getWriteMethod() != null);
			this.pd = pd;
		}

		/**
		 * 获取属性的可解析类型（ResolvableType），包含泛型信息。
		 *
		 * @return ResolvableType
		 */
		@Override
		public ResolvableType getResolvableType() {
			// 通过读方法的返回类型获取 ResolvableType
			return ResolvableType.forMethodReturnType(this.pd.getReadMethod());
		}

		/**
		 * 获取属性的类型描述符（TypeDescriptor），用于类型转换。
		 *
		 * @return TypeDescriptor
		 */
		@Override
		public TypeDescriptor toTypeDescriptor() {
			return new TypeDescriptor(BeanWrapperImpl.this.property(this.pd));
		}

		/**
		 * 获取指定嵌套层级的类型描述符（用于泛型嵌套属性，例如 List<Person> 中的 Person）。
		 *
		 * @param level 嵌套层级索引
		 * @return TypeDescriptor，可能为 null
		 */
		@Nullable
		@Override
		public TypeDescriptor nested(int level) {
			return TypeDescriptor.nested(BeanWrapperImpl.this.property(this.pd), level);
		}

		/**
		 * 获取当前属性的值。
		 *
		 * @return 属性值
		 * @throws Exception 如果读方法调用失败
		 */
		@Nullable
		@Override
		public Object getValue() throws Exception {
			Method readMethod = this.pd.getReadMethod();
			// 如果有安全管理器，使用特权执行
			if (System.getSecurityManager() != null) {
				// 确保方法可访问
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					ReflectionUtils.makeAccessible(readMethod);
					return null;
				});
				return AccessController.doPrivileged((PrivilegedAction<Object>) () ->
						{
							try {
								return readMethod.invoke(BeanWrapperImpl.this.getWrappedInstance(), (Object[]) null);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						},
						BeanWrapperImpl.this.acc);
			} else {
				// 普通环境，直接反射调用
				ReflectionUtils.makeAccessible(readMethod);
				return readMethod.invoke(BeanWrapperImpl.this.getWrappedInstance(), (Object[]) null);
			}
		}

		/**
		 * 设置当前属性的值。
		 *
		 * @param value 新值
		 * @throws Exception 如果写方法调用失败
		 */
		@Override
		public void setValue(@Nullable Object value) throws Exception {
			// 获取写方法（优先使用实际可访问的写方法，考虑桥接方法）
			Method writeMethod = (this.pd instanceof GenericTypeAwarePropertyDescriptor ?
					((GenericTypeAwarePropertyDescriptor) this.pd).getWriteMethodForActualAccess() :
					this.pd.getWriteMethod());
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					ReflectionUtils.makeAccessible(writeMethod);
					return null;
				});
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					try {
						writeMethod.invoke(BeanWrapperImpl.this.getWrappedInstance(), value);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
					return null;
				}, BeanWrapperImpl.this.acc);
			} else {
				ReflectionUtils.makeAccessible(writeMethod);
				writeMethod.invoke(BeanWrapperImpl.this.getWrappedInstance(), value);
			}
		}
	}
}