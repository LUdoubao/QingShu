//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * 用于 {@link EnableAsync} 注解在 PROXY 模式下的配置类。
 * 当 {@code @EnableAsync(mode = AdviceMode.PROXY)} 时，Spring 会导入此配置类。
 * 该类负责注册一个 {@link AsyncAnnotationBeanPostProcessor}，该后置处理器会为带有
 * {@code @Async} 注解（或自定义注解）的 Bean 创建代理，从而实现异步方法的拦截。
 *
 * <p>该类继承自 {@link AbstractAsyncConfiguration}，后者负责解析 {@code @EnableAsync}
 * 注解的元数据，并提供对异步执行器（Executor）和异常处理器（AsyncUncaughtExceptionHandler）
 * 的访问。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAsync
 * @see AsyncAnnotationBeanPostProcessor
 */
@Configuration(proxyBeanMethods = false)  // 配置类，不代理 Bean 方法，避免不必要的 CGLIB 代理
@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 标记为基础架构 Bean，在组件扫描和日志中会特殊处理
public class ProxyAsyncConfiguration extends AbstractAsyncConfiguration {

	/**
	 * 默认构造器，由 Spring 容器通过反射调用。
	 */
	public ProxyAsyncConfiguration() {
	}

	/**
	 * 创建并配置 {@link AsyncAnnotationBeanPostProcessor} Bean。
	 * 该 Bean 的名称固定为 "org.springframework.context.annotation.internalAsyncAnnotationProcessor"，
	 * 用于替换默认的后置处理器。Spring 在解析异步注解时会查找此名称的 Bean。
	 *
	 * @return 配置好的异步注解后置处理器
	 */
	@Bean(name = {"org.springframework.context.annotation.internalAsyncAnnotationProcessor"})
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public AsyncAnnotationBeanPostProcessor asyncAdvisor() {
		// 确保 @EnableAsync 注解元数据已注入（由父类 AbstractAsyncConfiguration 负责注入）
		Assert.notNull(this.enableAsync, "@EnableAsync annotation metadata was not injected");

		// 创建后置处理器实例
		AsyncAnnotationBeanPostProcessor bpp = new AsyncAnnotationBeanPostProcessor();

		// 设置异步执行器和异常处理器（来自父类的配置）
		// 父类 AbstractAsyncConfiguration 的 configure() 方法会从 @EnableAsync 注解中解析这些组件
		bpp.configure(this.executor, this.exceptionHandler);

		// 获取用户可能指定的自定义异步注解类型
		// 如果 @EnableAsync 的 annotation 属性有自定义值（不是默认的 Async.class），则设置
		Class<? extends Annotation> customAsyncAnnotation = this.enableAsync.getClass("annotation");
		if (customAsyncAnnotation != AnnotationUtils.getDefaultValue(EnableAsync.class, "annotation")) {
			bpp.setAsyncAnnotationType(customAsyncAnnotation);
		}

		// 设置是否代理目标类（即是否使用 CGLIB 代理）
		// 对应 @EnableAsync 的 proxyTargetClass 属性，默认为 false（使用 JDK 动态代理）
		bpp.setProxyTargetClass(this.enableAsync.getBoolean("proxyTargetClass"));

		// 设置 BeanPostProcessor 的执行顺序，对应 @EnableAsync 的 order 属性
		bpp.setOrder((Integer) this.enableAsync.getNumber("order"));

		return bpp;
	}
}