//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * 处理器执行链，封装了处理器（Handler）和一系列拦截器（HandlerInterceptor）。
 * 在 Spring Web MVC 中，当请求被分发到对应的处理器时，会通过 HandlerExecutionChain
 * 来依次执行拦截器的前置处理、后置处理以及完成处理后的清理工作。
 *
 * <p>该类负责管理拦截器链的执行顺序：
 * <ul>
 *   <li>{@code preHandle} 方法按照拦截器添加的顺序依次执行，如果某个拦截器的 preHandle
 *       返回 false，则执行链中断并触发已执行拦截器的 afterCompletion。</li>
 *   <li>{@code postHandle} 方法按照拦截器添加的逆序执行（在处理器执行之后、视图渲染之前）。</li>
 *   <li>{@code afterCompletion} 方法在请求处理完成后（无论成功或异常）按照拦截器添加的逆序执行。</li>
 *   <li>对于异步处理，还支持 {@code afterConcurrentHandlingStarted} 方法。</li>
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 2.0
 * @see HandlerInterceptor
 * @see AsyncHandlerInterceptor
 */
public class HandlerExecutionChain {

	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);

	/** 实际的处理器对象（例如 Controller 实例或 HandlerMethod） */
	private final Object handler;

	/** 拦截器列表，按添加顺序存储 */
	private final List<HandlerInterceptor> interceptorList;

	/** 当前已执行到哪个拦截器的索引，用于在异常时触发 afterCompletion */
	private int interceptorIndex = -1;

	/**
	 * 构造一个只有处理器、没有拦截器的执行链。
	 *
	 * @param handler 处理器对象
	 */
	public HandlerExecutionChain(Object handler) {
		this(handler, (HandlerInterceptor[]) null);
	}

	/**
	 * 构造一个包含处理器和给定拦截器的执行链。
	 *
	 * @param handler     处理器对象
	 * @param interceptors 拦截器数组（可为 null）
	 */
	public HandlerExecutionChain(Object handler, @Nullable HandlerInterceptor... interceptors) {
		this(handler, (interceptors != null ? Arrays.asList(interceptors) : Collections.emptyList()));
	}

	/**
	 * 构造一个包含处理器和给定拦截器列表的执行链。
	 *
	 * @param handler         处理器对象
	 * @param interceptorList 拦截器列表
	 */
	public HandlerExecutionChain(Object handler, List<HandlerInterceptor> interceptorList) {
		this.interceptorList = new ArrayList<>();
		this.interceptorIndex = -1;

		// 如果传入的处理器本身就是一个 HandlerExecutionChain，则提取其内部的处理器和拦截器
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
			this.handler = originalChain.getHandler();
			this.interceptorList.addAll(originalChain.interceptorList);
		} else {
			this.handler = handler;
		}
		// 添加给定的拦截器列表
		this.interceptorList.addAll(interceptorList);
	}

	/**
	 * 返回处理器对象。
	 *
	 * @return 处理器（通常是一个 Controller 实例或 HandlerMethod）
	 */
	public Object getHandler() {
		return this.handler;
	}

	/**
	 * 向拦截器列表末尾添加一个拦截器。
	 *
	 * @param interceptor 要添加的拦截器
	 */
	public void addInterceptor(HandlerInterceptor interceptor) {
		this.interceptorList.add(interceptor);
	}

	/**
	 * 在指定索引位置插入一个拦截器。
	 *
	 * @param index       插入位置（从 0 开始）
	 * @param interceptor 要插入的拦截器
	 */
	public void addInterceptor(int index, HandlerInterceptor interceptor) {
		this.interceptorList.add(index, interceptor);
	}

	/**
	 * 添加多个拦截器到列表末尾。
	 *
	 * @param interceptors 要添加的拦截器数组
	 */
	public void addInterceptors(HandlerInterceptor... interceptors) {
		CollectionUtils.mergeArrayIntoCollection(interceptors, this.interceptorList);
	}

	/**
	 * 返回拦截器数组（可能为 null）。
	 *
	 * @return 拦截器数组，如果没有拦截器则返回 null
	 */
	@Nullable
	public HandlerInterceptor[] getInterceptors() {
		return !this.interceptorList.isEmpty() ? this.interceptorList.toArray(new HandlerInterceptor[0]) : null;
	}

	/**
	 * 返回不可修改的拦截器列表视图。
	 *
	 * @return 拦截器列表（不可修改），如果没有拦截器则返回空列表
	 */
	public List<HandlerInterceptor> getInterceptorList() {
		return !this.interceptorList.isEmpty() ? Collections.unmodifiableList(this.interceptorList) : Collections.emptyList();
	}

	/**
	 * 执行所有拦截器的 preHandle 方法。
	 * 如果所有拦截器都返回 true，则返回 true；否则返回 false，并触发已执行拦截器的 afterCompletion。
	 *
	 * @param request  当前 HTTP 请求
	 * @param response 当前 HTTP 响应
	 * @return 如果所有 preHandle 都返回 true 则返回 true，否则返回 false
	 * @throws Exception 如果拦截器抛出异常
	 */
	boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 按顺序执行每个拦截器的 preHandle
		for (int i = 0; i < this.interceptorList.size(); i++) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			if (!interceptor.preHandle(request, response, this.handler)) {
				// 如果某个拦截器返回 false，则触发 afterCompletion 并返回 false
				triggerAfterCompletion(request, response, null);
				return false;
			}
			// 记录当前已成功执行的拦截器索引，用于 afterCompletion
			this.interceptorIndex = i;
		}
		return true;
	}

	/**
	 * 执行所有拦截器的 postHandle 方法（按逆序执行）。
	 *
	 * @param request  当前 HTTP 请求
	 * @param response 当前 HTTP 响应
	 * @param mv       处理器返回的 ModelAndView（可能为 null）
	 * @throws Exception 如果拦截器抛出异常
	 */
	void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv) throws Exception {
		// 从最后一个拦截器开始向前执行 postHandle
		for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			interceptor.postHandle(request, response, this.handler, mv);
		}
	}

	/**
	 * 触发所有已经执行了 preHandle 的拦截器的 afterCompletion 方法（按逆序执行）。
	 * 通常在请求处理完成（包括异常情况）后调用，用于资源清理。
	 *
	 * @param request  当前 HTTP 请求
	 * @param response 当前 HTTP 响应
	 * @param ex       处理过程中发生的异常（可能为 null）
	 */
	void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
		// 从最后一个成功执行 preHandle 的拦截器开始向前调用 afterCompletion
		for (int i = this.interceptorIndex; i >= 0; i--) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			try {
				interceptor.afterCompletion(request, response, this.handler, ex);
			} catch (Throwable ex2) {
				// 如果 afterCompletion 本身抛出异常，仅记录日志，不影响其他拦截器的执行
				logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
			}
		}
	}

	/**
	 * 触发所有实现了 AsyncHandlerInterceptor 的拦截器的 afterConcurrentHandlingStarted 方法（按逆序执行）。
	 * 该方法在异步请求处理开始时被调用（即处理器返回 Callable 或 DeferredResult 后，且尚未开始异步处理时）。
	 *
	 * @param request  当前 HTTP 请求
	 * @param response 当前 HTTP 响应
	 */
	void applyAfterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response) {
		for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			if (interceptor instanceof AsyncHandlerInterceptor) {
				try {
					AsyncHandlerInterceptor asyncInterceptor = (AsyncHandlerInterceptor) interceptor;
					asyncInterceptor.afterConcurrentHandlingStarted(request, response, this.handler);
				} catch (Throwable ex) {
					if (logger.isErrorEnabled()) {
						logger.error("Interceptor [" + interceptor + "] failed in afterConcurrentHandlingStarted", ex);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "HandlerExecutionChain with [" + this.getHandler() + "] and " + this.interceptorList.size() + " interceptors";
	}
}