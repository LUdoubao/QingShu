//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.scheduling.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.lang.Nullable;

/**
 * {@link AsyncConfigurationSelector} 是 Spring 中用于处理 {@link EnableAsync} 注解的导入选择器。
 * 它根据 {@link EnableAsync} 注解中指定的 {@link AdviceMode}（通知模式）来决定引入哪个配置类，
 * 从而实现异步方法的两种不同实现方式：
 * <ul>
 *     <li><b>PROXY</b>：使用 JDK 动态代理或 CGLIB 代理（默认），通过 Spring AOP 拦截异步方法调用。</li>
 *     <li><b>ASPECTJ</b>：使用 AspectJ 编译时织入（或加载时织入），通过字节码增强实现异步方法。</li>
 * </ul>
 * 此类继承自 {@link AdviceModeImportSelector}，后者是一个泛型类，用于根据 {@code AdviceMode} 选择导入的配置类。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAsync
 * @see ProxyAsyncConfiguration
 */
public class AsyncConfigurationSelector extends AdviceModeImportSelector<EnableAsync> {

	/**
	 * AspectJ 异步配置类的全限定名，定义为常量以便复用。
	 * 该类位于 spring-aspects 模块中，负责注册 AspectJ 切面并处理 @Async 注解的方法。
	 */
	private static final String ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.scheduling.aspectj.AspectJAsyncConfiguration";

	/**
	 * 默认无参构造器，由 Spring 容器通过反射调用。
	 */
	public AsyncConfigurationSelector() {
	}

	/**
	 * 根据给定的通知模式（AdviceMode）返回需要导入的配置类名称数组。
	 * 此方法由 Spring 的注解驱动基础设施调用，当解析到 @EnableAsync 注解时会执行此方法。
	 *
	 * @param adviceMode 从 @EnableAsync 注解中解析出的通知模式（PROXY 或 ASPECTJ）
	 * @return 需要导入的配置类名称数组，若未匹配到任何模式则返回 null
	 */
	@Nullable
	public String[] selectImports(AdviceMode adviceMode) {
		switch (adviceMode) {
			case PROXY:
				// 对于 PROXY 模式，导入标准的代理异步配置类（ProxyAsyncConfiguration）
				// 该类会注册一个 AsyncAnnotationBeanPostProcessor，用于创建代理对象来拦截 @Async 方法
				return new String[]{ProxyAsyncConfiguration.class.getName()};

			case ASPECTJ:
				// 对于 ASPECTJ 模式，导入 AspectJ 异步配置类
				// 该类负责注册一个 AsyncAnnotationAdvisor 以及 AspectJ 织入所需的组件
				return new String[]{ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME};

			default:
				// 理论上不会走到这里，因为 AdviceMode 枚举只有 PROXY 和 ASPECTJ 两种值
				return null;
		}
	}
}