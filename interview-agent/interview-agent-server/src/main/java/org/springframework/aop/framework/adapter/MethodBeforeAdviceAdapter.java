//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.aop.framework.adapter;

import java.io.Serializable;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * {@link AdvisorAdapter} 接口的实现类，用于将 {@link MethodBeforeAdvice} 适配为 AOP Alliance
 * 的 {@link MethodInterceptor}，使得前置通知能够参与到 Spring AOP 的拦截器链中。
 *
 * <p>Spring AOP 框架内部使用适配器模式统一处理不同类型的通知（Advice）。
 * 每个具体的通知类型都有对应的适配器，该适配器负责将通知包装成一个 {@link MethodInterceptor}，
 * 以便在方法调用链中按顺序执行。
 *
 * <p>对于前置通知（MethodBeforeAdvice），该适配器会返回一个 {@link MethodBeforeAdviceInterceptor}，
 * 该拦截器会在目标方法执行之前调用通知中的 {@link MethodBeforeAdvice#before} 方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see MethodBeforeAdvice
 * @see MethodBeforeAdviceInterceptor
 * @see AfterReturningAdviceAdapter
 * @see ThrowsAdviceAdapter
 */
final class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

	/**
	 * 默认无参构造器。
	 */
	MethodBeforeAdviceAdapter() {
	}

	/**
	 * 判断当前适配器是否支持给定的 Advice 类型。
	 * 只有实现了 {@link MethodBeforeAdvice} 接口的通知才被支持。
	 *
	 * @param advice 需要判断的通知实例
	 * @return 如果 advice 是 MethodBeforeAdvice 类型则返回 true，否则返回 false
	 */
	@Override
	public boolean supportsAdvice(Advice advice) {
		return advice instanceof MethodBeforeAdvice;
	}

	/**
	 * 将给定的 Advisor（其内部包装了 MethodBeforeAdvice）适配为 MethodInterceptor。
	 * 实际返回的是 {@link MethodBeforeAdviceInterceptor}，该拦截器会在调用目标方法前执行前置通知。
	 *
	 * @param advisor 包含 MethodBeforeAdvice 通知的 Advisor 对象
	 * @return 一个 MethodInterceptor，用于在拦截器链中执行前置通知逻辑
	 */
	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		// 从 Advisor 中获取实际的 MethodBeforeAdvice 通知
		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		// 包装成 MethodBeforeAdviceInterceptor 并返回
		return new MethodBeforeAdviceInterceptor(advice);
	}
}