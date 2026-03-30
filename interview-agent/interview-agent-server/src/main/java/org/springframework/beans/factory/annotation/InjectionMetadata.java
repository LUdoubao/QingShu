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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Spring 依赖注入元数据核心类
 * <p>作用：封装【目标Bean类】中所有需要依赖注入的元素（@Autowired标注的字段/方法）
 * <p>核心职责：
 * 1. 存储目标类的所有注入元素
 * 2. 执行依赖注入（@Autowired注入的最终落地类）
 * 3. 管理注入元素的生命周期（检查、刷新、清理）
 * <p>@Autowired 注解的注入逻辑，最终都是通过该类的 inject() 方法完成的
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class InjectionMetadata {

	/**
	 * 空注入元数据常量（无任何注入元素，默认单例）
	 * 用于没有@Autowired等注入注解的类，避免空指针
	 */
	public static final InjectionMetadata EMPTY = new InjectionMetadata(Object.class, Collections.emptyList()) {
		@Override
		protected boolean needsRefresh(Class<?> clazz) {
			return false;
		}
		@Override
		public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		}
		@Override
		public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) {
		}
		@Override
		public void clear(@Nullable PropertyValues pvs) {
		}
	};

	/** 目标Bean的Class对象（被@Autowired标注的业务类/组件类） */
	private final Class<?> targetClass;

	/**
	 * 注入元素集合
	 * 存储目标类中所有【@Autowired标注的字段/方法】对应的 InjectedElement 实例
	 */
	private final Collection<InjectedElement> injectedElements;

	/**
	 * 已检查的注入元素集合（线程安全volatile修饰）
	 * 避免重复注册、重复处理注入元素
	 */
	@Nullable
	private volatile Set<InjectedElement> checkedElements;

	/**
	 * 构造方法：创建注入元数据
	 * @param targetClass 目标Bean类
	 * @param elements 该类的所有注入元素（@Autowired字段/方法）
	 */
	public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
		this.targetClass = targetClass;
		this.injectedElements = elements;
	}

	/**
	 * 判断是否需要刷新注入元数据
	 * 场景：类被重新加载/增强时，需要重新解析@Autowired注解
	 * @param clazz 当前目标类
	 * @return true=需要刷新，false=无需刷新
	 */
	protected boolean needsRefresh(Class<?> clazz) {
		return this.targetClass != clazz;
	}

	/**
	 * 检查并注册配置成员
	 * 作用：将@Autowired标注的字段/方法注册为Spring外部管理的配置成员，防止重复处理
	 * @param beanDefinition Bean的定义信息
	 */
	public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		Set<InjectedElement> checkedElements = new LinkedHashSet<>(this.injectedElements.size());
		// 遍历所有注入元素（@Autowired字段/方法）
		for (InjectedElement element : this.injectedElements) {
			Member member = element.getMember();
			// 未被Spring管理则注册
			if (!beanDefinition.isExternallyManagedConfigMember(member)) {
				beanDefinition.registerExternallyManagedConfigMember(member);
				checkedElements.add(element);
			}
		}
		this.checkedElements = checkedElements;
	}

	/**
	 * 【核心方法】执行依赖注入
	 * <p>@Autowired 注解的最终注入入口！Spring创建Bean时，会调用此方法完成自动装配
	 * @param target 目标Bean实例（需要注入依赖的对象）
	 * @param beanName 目标Bean的名称
	 * @param pvs Bean的属性值
	 * @throws Throwable 注入过程中的反射异常
	 */
	public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
		// 优先使用已检查的注入元素，无则使用原始集合
		Collection<InjectedElement> elementsToIterate =
				(this.checkedElements != null ? this.checkedElements : this.injectedElements);

		if (!elementsToIterate.isEmpty()) {
			// 遍历所有@Autowired注入元素，逐个执行注入
			for (InjectedElement element : elementsToIterate) {
				// 调用注入元素的 inject 方法，完成字段/方法的依赖注入
				element.inject(target, beanName, pvs);
			}
		}
	}

	/**
	 * 清理注入元素的状态
	 * @param pvs Bean的属性值
	 */
	public void clear(@Nullable PropertyValues pvs) {
		Collection<InjectedElement> elementsToIterate =
				(this.checkedElements != null ? this.checkedElements : this.injectedElements);
		if (!elementsToIterate.isEmpty()) {
			for (InjectedElement element : elementsToIterate) {
				element.clearPropertySkipping(pvs);
			}
		}
	}

	/**
	 * 静态工厂方法：根据注入元素创建 InjectionMetadata 实例
	 * @param elements 注入元素集合
	 * @param clazz 目标类
	 * @return 注入元数据对象
	 */
	public static InjectionMetadata forElements(Collection<InjectedElement> elements, Class<?> clazz) {
		return elements.isEmpty() ? new InjectionMetadata(clazz, Collections.emptyList()) :
				new InjectionMetadata(clazz, elements);
	}

	/**
	 * 静态工具方法：判断元数据是否需要刷新
	 * @param metadata 注入元数据
	 * @param clazz 目标类
	 * @return true=需要刷新
	 */
	public static boolean needsRefresh(@Nullable InjectionMetadata metadata, Class<?> clazz) {
		return metadata == null || metadata.needsRefresh(clazz);
	}

	/**
	 * 【注入元素抽象类】
	 * <p>封装单个依赖注入元素：对应【@Autowired标注的字段】或【@Autowired标注的方法】
	 * <p>每个@Autowired注解的成员，都会被封装为一个 InjectedElement 实例
	 */
	public abstract static class InjectedElement {

		/** 注入成员：Field（字段）/ Method（方法） */
		protected final Member member;

		/** 是否为字段注入：true=字段(@Autowired字段)，false=方法(@Autowired方法/Setter) */
		protected final boolean isField;

		/** 属性描述符：用于Setter方法注入的属性信息 */
		@Nullable
		protected final PropertyDescriptor pd;

		/** 是否跳过注入：volatile保证线程安全，用于属性覆盖判断 */
		@Nullable
		protected volatile Boolean skip;

		/**
		 * 构造方法：创建注入元素
		 * @param member 注入成员（Field/Method）
		 * @param pd 属性描述符
		 */
		protected InjectedElement(Member member, @Nullable PropertyDescriptor pd) {
			this.member = member;
			this.isField = member instanceof Field;
			this.pd = pd;
		}

		/** 获取注入成员（字段/方法） */
		public final Member getMember() {
			return this.member;
		}

		/**
		 * 获取注入资源的类型
		 * 字段：返回字段类型；方法：返回方法参数类型
		 * @return 依赖的Bean类型
		 */
		protected final Class<?> getResourceType() {
			if (this.isField) {
				return ((Field) this.member).getType();
			}
			else {
				return (this.pd != null ? this.pd.getPropertyType() :
						((Method) this.member).getParameterTypes()[0]);
			}
		}

		/**
		 * 校验注入资源类型是否匹配
		 * 防止@Autowired注入类型不兼容的Bean
		 * @param resourceType 待注入的资源类型
		 */
		protected final void checkResourceType(Class<?> resourceType) {
			Class<?> injectType;
			if (this.isField) {
				injectType = ((Field) this.member).getType();
			}
			else {
				injectType = (this.pd != null ? this.pd.getPropertyType() :
						((Method) this.member).getParameterTypes()[0]);
			}
			// 类型不兼容则抛出异常
			if (!resourceType.isAssignableFrom(injectType) && !injectType.isAssignableFrom(resourceType)) {
				throw new IllegalStateException(
						"Specified type [" + injectType + "] is incompatible with resource type [" + resourceType.getName() + "]");
			}
		}

		/**
		 * 【注入元素核心方法】执行单个元素的依赖注入
		 * <p>分两种场景：@Autowired字段注入、@Autowired方法注入
		 * @param target 目标Bean实例
		 * @param requestingBeanName Bean名称
		 * @param pvs 属性值
		 * @throws Throwable 反射注入异常
		 */
		protected void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs)
				throws Throwable {

			// ===================== 场景1：@Autowired 字段注入 =====================
			if (this.isField) {
				Field field = (Field) this.member;
				// 反射暴力破解：允许访问private字段
				ReflectionUtils.makeAccessible(field);
				// 为字段赋值：从Spring容器获取Bean，注入到目标字段
				field.set(target, getResourceToInject(target, requestingBeanName));
			}

			// ===================== 场景2：@Autowired 方法/Setter注入 =====================
			else {
				// 检查是否需要跳过注入（属性已手动配置则跳过）
				if (checkPropertySkipping(pvs)) {
					return;
				}
				try {
					Method method = (Method) this.member;
					// 反射暴力破解：允许访问private方法
					ReflectionUtils.makeAccessible(method);
					// 调用方法：将从容器获取的Bean作为参数传入
					method.invoke(target, getResourceToInject(target, requestingBeanName));
				}
				catch (InvocationTargetException ex) {
					// 抛出方法执行的原始异常
					throw ex.getTargetException();
				}
			}
		}

		/**
		 * 检查是否需要跳过注入
		 * 场景：如果属性已手动配置（XML/JavaConfig），则跳过@Autowired自动注入
		 */
		protected boolean checkPropertySkipping(@Nullable PropertyValues pvs) {
			Boolean skip = this.skip;
			if (skip != null) {
				return skip;
			}
			if (pvs == null) {
				this.skip = false;
				return false;
			}
			synchronized (pvs) {
				skip = this.skip;
				if (skip != null) {
					return skip;
				}
				// 属性已存在则跳过自动注入
				if (this.pd != null && pvs.contains(this.pd.getName())) {
					this.skip = true;
					return true;
				}
				// 注册已处理属性
				if (pvs instanceof MutablePropertyValues) {
					((MutablePropertyValues) pvs).registerProcessedProperty(this.pd.getName());
				}
				this.skip = false;
				return false;
			}
		}

		/**
		 * 清理属性跳过标记
		 */
		protected void clearPropertySkipping(@Nullable PropertyValues pvs) {
			if (pvs != null) {
				synchronized (pvs) {
					if (Boolean.FALSE.equals(this.skip) && this.pd != null && pvs instanceof MutablePropertyValues) {
						((MutablePropertyValues) pvs).clearProcessedProperty(this.pd.getName());
					}
				}
			}
		}

		/**
		 * 【抽象方法】获取需要注入的资源（Bean）
		 * <p>子类实现：AutowiredAnnotationBeanPostProcessor 中实现此方法
		 * 作用：从Spring容器中查找@Autowired需要的Bean（byType/byName）
		 */
		@Nullable
		protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
			return null;
		}

		//  equals/hashCode：基于注入成员（字段/方法）判断唯一性
		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof InjectedElement)) {
				return false;
			}
			InjectedElement otherElement = (InjectedElement) other;
			return this.member.equals(otherElement.member);
		}
		@Override
		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " for " + this.member;
		}
	}
}