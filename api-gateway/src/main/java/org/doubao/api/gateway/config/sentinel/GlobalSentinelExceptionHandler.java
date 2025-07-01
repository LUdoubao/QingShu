package org.doubao.api.gateway.config.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局Sentinel异常处理器
 * <p>
 * 职责：专门处理Sentinel框架抛出的各种流控异常
 * <p>
 * 特点：
 * 1. 高优先级（@Order(-2)）：确保在默认异常处理器前捕获Sentinel异常
 * 2. 针对性处理：仅处理ParamFlowException（参数限流异常）
 * 3. 友好响应：返回结构化的JSON错误信息
 * 4. WebFlux支持：完全响应式编程实现
 */
@Component
@Order(-2)
public class GlobalSentinelExceptionHandler implements WebExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSentinelExceptionHandler.class);

	/**
	 * 异常处理方法（扩展熔断异常处理）
	 */
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		// 参数限流异常
		if (ex instanceof ParamFlowException) {
			return buildResponse(exchange, "sentinel-参数请求过于频繁，请稍后再试",
					HttpStatus.TOO_MANY_REQUESTS);
		}
		// 熔断降级异常
		else if (ex instanceof DegradeException) {
			DegradeException de = (DegradeException) ex;

			// 熔断日志（包含资源名和熔断规则）
			LOGGER.info("熔断触发: resource={}, grade={}, trigger={}",
					de.getRule().getResource(),
					de.getRule().getGrade(),
					de.getRule().getCount());

			String message = "服务暂时不可用，请稍后再试";

			// 根据熔断类型定制提示
			switch(de.getRule().getGrade()) {
				case RuleConstant.DEGRADE_GRADE_RT:
					message = "系统响应过慢，正在恢复中";
					break;
				case RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO:
					message = "服务稳定性下降，正在恢复中";
					break;
				case RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT:
					message = "服务异常增多，正在恢复中";
					break;
			}

			return buildResponse(exchange, message, HttpStatus.SERVICE_UNAVAILABLE);
		}
		// 其他类型的Sentinel异常（可选）
		else if (ex instanceof BlockException) {
			return buildResponse(exchange, "系统保护中，请稍后再试",
					HttpStatus.TOO_MANY_REQUESTS);
		}

		return Mono.error(ex);
	}

	/**
	 * 构建标准化的错误响应
	 */
	private Mono<Void> buildResponse(ServerWebExchange exchange,
									 String message,
									 HttpStatus status) {
		// 构建响应内容
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("code", status.value());
		result.put("message", message);
		result.put("timestamp", System.currentTimeMillis());
		result.put("path", exchange.getRequest().getPath().value());

		byte[] bytes;
		try {
			bytes = new ObjectMapper().writeValueAsBytes(result);
		} catch (Exception e) {
			return Mono.error(e);
		}

		// 配置响应头
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		// 使用异步方式写入响应
		return response.writeWith(
				Mono.just(response.bufferFactory().wrap(bytes))
		);
	}
}