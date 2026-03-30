//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

/**
 * 启用 Spring 异步方法执行能力的注解。
 * 通过在 Spring 配置类上添加此注解，可以启用 {@code @Async} 注解（或自定义注解）的处理，
 * 使得被标注的方法在单独的线程中异步执行。
 *
 * <p>使用示例：
 * <pre class="code">
 * &#64;Configuration
 * &#64;EnableAsync
 * public class AppConfig {
 *     // 可以自定义执行器（Executor）的 Bean
 *     &#64;Bean(name = "taskExecutor")
 *     public Executor taskExecutor() {
 *         return new ThreadPoolTaskExecutor();
 *     }
 * }
 *
 * &#64;Service
 * public class MyService {
 *     &#64;Async
 *     public void asyncMethod() {
 *         // 此方法将在单独的线程中异步执行
 *     }
 *
 *     &#64;Async("specificExecutor")
 *     public void asyncMethodWithExecutor() {
 *         // 将使用名为 "specificExecutor" 的 Executor Bean 执行
 *     }
 * }
 * </pre>
 *
 * <p>该注解通过 {@link AsyncConfigurationSelector} 导入具体的配置类：
 * <ul>
 *   <li>当 {@link #mode()} 为 {@link AdviceMode#PROXY} 时，导入 {@link ProxyAsyncConfiguration}，
 *       使用 Spring AOP 代理（JDK 动态代理或 CGLIB）实现异步。</li>
 *   <li>当 {@link #mode()} 为 {@link AdviceMode#ASPECTJ} 时，导入
 *       {@code org.springframework.scheduling.aspectj.AspectJAsyncConfiguration}，
 *       使用 AspectJ 编译时织入（或加载时织入）实现异步。</li>
 * </ul>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Async
 * @see AsyncConfigurationSelector
 * @see ProxyAsyncConfiguration
 */
@Target(ElementType.TYPE)               // 只能用于类或接口（通常是配置类）
@Retention(RetentionPolicy.RUNTIME)     // 运行时保留，便于通过反射读取
@Documented                             // 生成 Javadoc 时包含此注解信息
@Import(AsyncConfigurationSelector.class) // 导入选择器，根据模式导入相应配置类
public @interface EnableAsync {

	/**
	 * 指定要识别的异步注解类型。默认值为 {@link Annotation}，表示识别标准的 {@link Async} 注解。
	 * 如果需要使用自定义注解（例如 {@code @MyAsync}），可以设置此属性为自定义注解的 Class 对象。
	 *
	 * <p>注意：自定义注解本身必须带有 {@code @Async} 元注解，或者其处理逻辑与 {@code @Async} 一致。
	 * 实际上，Spring 通过检查注解上的 {@code @Async} 元注解或直接匹配来识别异步方法。
	 *
	 * <p>示例：
	 * <pre class="code">
	 * &#64;Target(ElementType.METHOD)
	 * &#64;Retention(RetentionPolicy.RUNTIME)
	 * &#64;Async  // 自定义注解带有 @Async 元注解
	 * public &#64;interface MyAsync {
	 * }
	 *
	 * &#64;Configuration
	 * &#64;EnableAsync(annotation = MyAsync.class)
	 * public class AppConfig {
	 * }
	 * </pre>
	 *
	 * @return 自定义异步注解类型，默认为 {@link Annotation}（表示使用标准 {@link Async}）
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

	/**
	 * 是否代理目标类（即是否使用 CGLIB 代理）。
	 * 默认值为 {@code false}，表示优先使用 JDK 动态代理（基于接口）。
	 * 如果设置为 {@code true}，则强制使用 CGLIB 代理（即使目标类实现了接口）。
	 *
	 * <p>该属性仅在 {@link #mode()} 为 {@link AdviceMode#PROXY} 时生效。
	 * 当使用 AspectJ 模式时，此属性被忽略。
	 *
	 * @return 是否强制代理目标类，默认 false
	 * @see org.springframework.aop.framework.ProxyConfig#setProxyTargetClass
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 指定通知模式（AdviceMode），决定异步切面的实现方式。
	 * 可选值：
	 * <ul>
	 *   <li>{@link AdviceMode#PROXY}：使用 Spring AOP 代理（默认），基于 JDK 动态代理或 CGLIB。</li>
	 *   <li>{@link AdviceMode#ASPECTJ}：使用 AspectJ 编译时织入，需要添加 spring-aspects 模块，
	 *       并在构建时启用 AspectJ 编译器或加载时织入。</li>
	 * </ul>
	 *
	 * <p>PROXY 模式通常足够，且不需要额外的构建步骤。ASPECTJ 模式可以代理私有方法或自调用方法，
	 * 但需要更多配置。
	 *
	 * @return 通知模式，默认 PROXY
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 指定异步通知器的顺序（order）。
	 * 该值影响多个通知器（如事务、缓存等）的执行顺序。
	 * 值越小优先级越高，默认值为 {@link Integer#MAX_VALUE}（最低优先级），
	 * 以确保其他通知器（如事务）在异步方法调用前有机会执行。
	 *
	 * <p>例如，如果需要在事务提交后再执行异步方法，可以设置比事务通知器更小的 order 值，
	 * 但实际上异步通知通常放在最后执行，以避免事务上下文丢失。
	 *
	 * @return 顺序值，默认 Integer.MAX_VALUE
	 */
	int order() default Integer.MAX_VALUE;
}