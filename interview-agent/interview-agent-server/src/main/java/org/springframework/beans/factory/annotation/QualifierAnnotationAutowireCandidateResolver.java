package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 基于注解的限定符解析器。
 * 扩展了GenericTypeAwareAutowireCandidateResolver，增加了对@Qualifier、JSR-330 @Qualifier以及自定义限定符注解的支持。
 * 同时也负责处理@Value注解，用于获取建议的注入值。
 */
public class QualifierAnnotationAutowireCandidateResolver extends GenericTypeAwareAutowireCandidateResolver {

	/**
	 * 存储被认为是“限定符”的注解类型集合。
	 * 默认包含Spring的@Qualifier和JSR-330的@Qualifier（如果类路径下存在）。
	 */
	private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet(2);

	/**
	 * 用于标识“值”注解的注解类型，默认为Spring的@Value。
	 * 用于getSuggestedValue方法中提取注入的表达式或值。
	 */
	private Class<? extends Annotation> valueAnnotationType = Value.class;

	/**
	 * 默认构造器。
	 * 初始化qualifierTypes集合，默认添加Spring的@Qualifier。
	 * 尝试通过反射加载JSR-330（javax.inject）的@Qualifier注解，如果类路径存在则加入集合。
	 */
	public QualifierAnnotationAutowireCandidateResolver() {
		this.qualifierTypes.add(Qualifier.class);

		try {
			this.qualifierTypes.add((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Qualifier", QualifierAnnotationAutowireCandidateResolver.class.getClassLoader()));
		} catch (ClassNotFoundException var2) {
			// 如果JSR-330不可用，则忽略，仅支持Spring的Qualifier
		}

	}

	/**
	 * 构造器，指定一种自定义的限定符注解类型。
	 * @param qualifierType 要作为限定符使用的注解类型
	 */
	public QualifierAnnotationAutowireCandidateResolver(Class<? extends Annotation> qualifierType) {
		Assert.notNull(qualifierType, "'qualifierType' must not be null");
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * 构造器，指定一组自定义的限定符注解类型。
	 * @param qualifierTypes 要作为限定符使用的注解类型集合
	 */
	public QualifierAnnotationAutowireCandidateResolver(Set<Class<? extends Annotation>> qualifierTypes) {
		Assert.notNull(qualifierTypes, "'qualifierTypes' must not be null");
		this.qualifierTypes.addAll(qualifierTypes);
	}

	/**
	 * 向解析器中添加一种限定符注解类型。
	 * @param qualifierType 要添加的限定符注解类型
	 */
	public void addQualifierType(Class<? extends Annotation> qualifierType) {
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * 设置用于标识“值”的注解类型。
	 * 默认为@Value，但可以自定义为其他注解（例如，用于不同环境的配置注解）。
	 * @param valueAnnotationType 新的值注解类型
	 */
	public void setValueAnnotationType(Class<? extends Annotation> valueAnnotationType) {
		this.valueAnnotationType = valueAnnotationType;
	}

	/**
	 * 核心方法：判断给定的Bean定义持有者是否是满足指定依赖描述符的自动装配候选者。
	 * 在父类检查（类型匹配、泛型匹配等）通过的基础上，进一步检查限定符注解是否匹配。
	 *
	 * @param bdHolder Bean定义及其名称的持有者
	 * @param descriptor 依赖描述符，描述了需要被注入的点（字段、方法参数等）
	 * @return 如果匹配则返回true
	 */
	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		// 1. 调用父类检查（GenericTypeAwareAutowireCandidateResolver），进行类型、泛型等基本匹配
		boolean match = super.isAutowireCandidate(bdHolder, descriptor);

		if (match) {
			// 2. 检查依赖点（字段/参数）上的注解是否与候选Bean的限定符匹配
			match = this.checkQualifiers(bdHolder, descriptor.getAnnotations());

			if (match) {
				// 3. 如果依赖点是一个方法参数，还需要额外检查该方法上的注解（主要针对基于方法的限定符，较少用）
				MethodParameter methodParam = descriptor.getMethodParameter();
				if (methodParam != null) {
					Method method = methodParam.getMethod();
					// 只对非void返回类型的方法进行检查？这里的逻辑是：如果方法不存在或是void返回类型，则检查方法上的注解。
					// 这是为了处理一些特殊情况，例如@Bean方法本身可能带有限定符注解。
					if (method == null || Void.TYPE == method.getReturnType()) {
						match = this.checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
					}
				}
			}
		}
		return match;
	}

	/**
	 * 检查给定的注解数组（来自依赖点）是否与候选Bean的限定符信息匹配。
	 *
	 * @param bdHolder 候选Bean的持有者
	 * @param annotationsToSearch 需要检查的注解数组（通常来自依赖点的注解）
	 * @return 如果所有相关的限定符注解都匹配，则返回true
	 */
	protected boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
		if (ObjectUtils.isEmpty(annotationsToSearch)) {
			return true; // 依赖点没有注解，直接通过限定符检查
		}

		SimpleTypeConverter typeConverter = new SimpleTypeConverter(); // 用于后续属性值的类型转换
		// 遍历依赖点上的每一个注解
		for (Annotation annotation : annotationsToSearch) {
			Class<? extends Annotation> type = annotation.annotationType();
			boolean checkMeta = true;      // 标记是否需要检查这个注解的元注解
			boolean fallbackToMeta = false; // 标记是否因为当前注解检查失败而需要回退检查其元注解

			// 判断当前注解是否是“限定符”（是qualifierTypes集合中的注解，或是被这些注解标注的元注解）
			if (this.isQualifier(type)) {
				// 检查当前注解本身是否匹配候选Bean
				if (!this.checkQualifier(bdHolder, annotation, typeConverter)) {
					// 当前注解检查失败，尝试检查它的元注解
					fallbackToMeta = true;
				} else {
					// 当前注解检查成功，则无需再检查其元注解
					checkMeta = false;
				}
			}

			// 如果需要检查元注解（可能是当前注解不是限定符，也可能是当前限定符注解匹配失败fallbackToMeta）
			if (checkMeta) {
				boolean foundMeta = false; // 标记是否找到了符合条件的元注解
				// 遍历当前注解的所有元注解
				for (Annotation metaAnn : type.getAnnotations()) {
					Class<? extends Annotation> metaType = metaAnn.annotationType();
					// 判断元注解是否是“限定符”
					if (this.isQualifier(metaType)) {
						foundMeta = true; // 找到了限定符类型的元注解
						// 如果是因为fallback，且元注解的value为空，则匹配失败（因为fallback期望找到一个具体的元注解来匹配）
						// 或者，检查这个元注解本身是否匹配候选Bean
						if ((fallbackToMeta && ObjectUtils.isEmpty(AnnotationUtils.getValue(metaAnn)))
								|| !this.checkQualifier(bdHolder, metaAnn, typeConverter)) {
							return false; // 任何一个符合条件的元注解不匹配，整个Bean就不匹配
						}
					}
				}
				// 如果是fallback情况，但根本没有找到任何限定符类型的元注解，也匹配失败
				if (fallbackToMeta && !foundMeta) {
					return false;
				}
			}
		}
		// 所有注解（及必要的元注解）检查都通过
		return true;
	}

	/**
	 * 判断一个注解类型是否被视作“限定符”。
	 * 规则：注解类型直接存在于qualifierTypes集合中，或者该注解本身被qualifierTypes集合中的某个注解所标注（即元注解）。
	 *
	 * @param annotationType 待判断的注解类型
	 * @return 如果是限定符则返回true
	 */
	protected boolean isQualifier(Class<? extends Annotation> annotationType) {
		for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
			if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 核心匹配逻辑：检查一个特定的限定符注解是否与候选Bean的限定符信息匹配。
	 * 匹配数据来源：BeanDefinition中存储的AutowireCandidateQualifier对象、BeanDefinition的属性、工厂方法注解、Bean类上的注解等。
	 *
	 * @param bdHolder Bean定义持有者
	 * @param annotation 依赖点上的限定符注解实例
	 * @param typeConverter 类型转换器，用于转换属性值
	 * @return 如果匹配则返回true
	 */
	protected boolean checkQualifier(BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {
		Class<? extends Annotation> type = annotation.annotationType();
		RootBeanDefinition bd = (RootBeanDefinition) bdHolder.getBeanDefinition();

		// 1. 首先从BeanDefinition中查找已注册的、类型完全相同的AutowireCandidateQualifier
		AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());
		if (qualifier == null) {
			// 尝试用短类名查找（Spring内部可能存的是短名）
			qualifier = bd.getQualifier(ClassUtils.getShortName(type));
		}

		// 2. 如果BeanDefinition中没有显式注册对应的Qualifier，则尝试从Bean的各种元素上查找该注解
		Annotation targetAnnotation = null;
		if (qualifier == null) {
			// 2.1 从BeanDefinition的qualifiedElement查找（例如被@Qualifier标注的字段/方法参数）
			targetAnnotation = this.getQualifiedElementAnnotation(bd, type);
			// 2.2 从工厂方法上查找
			if (targetAnnotation == null) {
				targetAnnotation = this.getFactoryMethodAnnotation(bd, type);
			}
			// 2.3 如果当前bd是装饰过的（如内部Bean），尝试从其原始定义中查找工厂方法注解
			if (targetAnnotation == null) {
				RootBeanDefinition dbd = this.getResolvedDecoratedDefinition(bd);
				if (dbd != null) {
					targetAnnotation = this.getFactoryMethodAnnotation(dbd, type);
				}
			}
			// 2.4 从Bean的实际类型（Class）上查找
			if (targetAnnotation == null) {
				if (this.getBeanFactory() != null) {
					try {
						Class<?> beanType = this.getBeanFactory().getType(bdHolder.getBeanName());
						if (beanType != null) {
							targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(beanType), type);
						}
					} catch (NoSuchBeanDefinitionException var13) {
						// 忽略异常，继续下面的检查
					}
				}
				// 2.5 从BeanDefinition中记录的Bean Class上查找
				if (targetAnnotation == null && bd.hasBeanClass()) {
					targetAnnotation = AnnotationUtils.getAnnotation(ClassUtils.getUserClass(bd.getBeanClass()), type);
				}
			}

			// 3. 如果找到了完全相同的注解实例（equals比较），则直接匹配成功
			//    这通常发生在依赖点的注解和Bean上的注解是同一个（例如都是默认值的@Qualifier）
			if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
				return true;
			}
		}

		// 4. 获取依赖点上注解的所有属性（键值对）
		Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
		// 如果注解没有属性，且BeanDefinition中也没有注册对应的Qualifier，则不匹配
		if (attributes.isEmpty() && qualifier == null) {
			return false;
		}

		// 5. 逐个属性进行比较
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			String attributeName = entry.getKey();
			Object expectedValue = entry.getValue(); // 依赖点注解上期望的值
			Object actualValue = null;                // 从候选Bean上找到的实际值

			// 5.1 首先从已注册的AutowireCandidateQualifier中获取属性值
			if (qualifier != null) {
				actualValue = qualifier.getAttribute(attributeName);
			}
			// 5.2 其次从BeanDefinition的通用属性中获取
			if (actualValue == null) {
				actualValue = bd.getAttribute(attributeName);
			}
			// 5.3 特殊处理：如果属性名为"value"，且期望值是字符串，且Bean的名称匹配这个值，则认为匹配。
			//     这是对@Qualifier("beanName")这种用法的支持。
			if (actualValue == null && attributeName.equals("value") && expectedValue instanceof String
					&& bdHolder.matchesName((String) expectedValue)) {
				continue; // 当前属性匹配成功，检查下一个属性
			}
			// 5.4 再次尝试：从注解的默认值中获取实际值（当Bean上未显式指定时）
			if (actualValue == null && qualifier != null) {
				actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
			}

			// 5.5 如果找到了实际值，可能需要进行类型转换（例如String -> Integer）
			if (actualValue != null) {
				actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
			}

			// 5.6 比较期望值和（转换后的）实际值。如果不相等，则整个注解不匹配。
			if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}
		// 6. 所有属性都匹配成功
		return true;
	}

	/**
	 * 从BeanDefinition的qualifiedElement（被装饰的注入点元素）上获取指定类型的注解。
	 */
	@Nullable
	protected Annotation getQualifiedElementAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
		AnnotatedElement qualifiedElement = bd.getQualifiedElement();
		return qualifiedElement != null ? AnnotationUtils.getAnnotation(qualifiedElement, type) : null;
	}

	/**
	 * 从BeanDefinition的工厂方法上获取指定类型的注解。
	 */
	@Nullable
	protected Annotation getFactoryMethodAnnotation(RootBeanDefinition bd, Class<? extends Annotation> type) {
		Method resolvedFactoryMethod = bd.getResolvedFactoryMethod();
		return resolvedFactoryMethod != null ? AnnotationUtils.getAnnotation(resolvedFactoryMethod, type) : null;
	}

	/**
	 * 判断一个依赖是否是必须的。
	 * 覆盖父类方法，除了父类的规则，还考虑了@Autowired(required = false)的情况。
	 */
	@Override
	public boolean isRequired(DependencyDescriptor descriptor) {
		// 先调用父类判断（例如，会检查@Inject是否出现在Optional中）
		if (!super.isRequired(descriptor)) {
			return false;
		}
		// 再检查@Autowired的required属性
		Autowired autowired = descriptor.getAnnotation(Autowired.class);
		return autowired == null || autowired.required(); // 没有@Autowired注解，或者required=true，都是必须的
	}

	/**
	 * 判断依赖描述符上是否有任何限定符注解。
	 * 用于快速判断是否需要进入详细的限定符匹配流程。
	 */
	@Override
	public boolean hasQualifier(DependencyDescriptor descriptor) {
		for (Annotation ann : descriptor.getAnnotations()) {
			if (this.isQualifier(ann.annotationType())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取依赖的建议值。主要用于处理@Value注解。
	 * 从依赖点（字段或方法参数）的注解中查找@Value注解，并提取其值（通常是SpEL表达式或属性占位符）。
	 *
	 * @param descriptor 依赖描述符
	 * @return @Value注解中“value”属性的值，如果没找到则返回null
	 */
	@Override
	@Nullable
	public Object getSuggestedValue(DependencyDescriptor descriptor) {
		// 1. 先从依赖点的直接注解上找
		Object value = this.findValue(descriptor.getAnnotations());
		if (value == null) {
			// 2. 如果没找到，且是方法参数，则尝试从方法级别的注解上找
			MethodParameter methodParam = descriptor.getMethodParameter();
			if (methodParam != null) {
				value = this.findValue(methodParam.getMethodAnnotations());
			}
		}
		return value;
	}

	/**
	 * 从注解数组中查找@Value（或自定义的值注解）并提取其值。
	 */
	@Nullable
	protected Object findValue(Annotation[] annotationsToSearch) {
		if (annotationsToSearch.length > 0) {
			// 使用工具类获取合并后的注解属性，支持注解继承和覆盖
			AnnotationAttributes attr = AnnotatedElementUtils.getMergedAnnotationAttributes(
					AnnotatedElementUtils.forAnnotations(annotationsToSearch), this.valueAnnotationType);
			if (attr != null) {
				return this.extractValue(attr);
			}
		}
		return null;
	}

	/**
	 * 从值注解的属性映射中提取“value”属性的值。
	 * 这是@Value注解必须有的属性。
	 */
	protected Object extractValue(AnnotationAttributes attr) {
		Object value = attr.get("value");
		if (value == null) {
			throw new IllegalStateException("Value annotation must have a value attribute");
		} else {
			return value;
		}
	}
}