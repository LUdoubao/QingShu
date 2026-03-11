package org.doubao.api.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
	@Autowired
	private WebClient.Builder webClientBuilder;
	private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final List<String> EXCLUDE_URLS = Arrays.asList(
		"/auth/",
		"/user/login",
		"/user/register",
		"/user/verify",
		"/user/forgot-password",
		"/public/",
		"/dialog/ws",
		"/actuator/health"
	);


	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获取当前请求的完整路径
		String path = exchange.getRequest().getURI().getPath();
		LOG.info("请求路径：{}", path);
		// 检查请求路径是否在排除列表(不需要JWT验证的路径)
		// 使用流操作检测路径前缀匹配，若匹配则跳过验证直接放行
		if (EXCLUDE_URLS.stream().anyMatch(path::startsWith)) {
			return chain.filter(exchange);
		}
		// 从Authorization请求头中获取令牌
		String token = exchange.getRequest().getHeaders().getFirst("Authorization");
		// 验证令牌格式：必须存在且以"Bearer "开头
		if (token == null || !token.startsWith("Bearer ")) {
			// 设置401状态
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			// 立即结束响应
			return exchange.getResponse().setComplete();
		}

		// 提取实际的JWT令牌(去掉"Bearer "前缀)
		String jwt = token.substring(7);
		// 使用WebClient异步调用认证服务进行令牌验证
		return webClientBuilder.build()  // 构建WebClient实例
				.get()  // 设置HTTP方法为GET
				.uri(uriBuilder -> uriBuilder  // 函数式构建URL
						.scheme("http")        // 设置协议
						.host("auth-service")  // 设置目标服务(服务发现名称)
						.path("/auth/verify")  // 设置验证API路径
						.queryParam("token", jwt)  // 添加令牌查询参数
						.build()               // 完成URI构建
				)
				.retrieve()  // 发送请求并获取响应
				.onStatus(HttpStatus::isError, clientResponse ->  // 处理HTTP错误状态
						// 转换错误响应为异常流
						Mono.error(new JwtException("Token validation failed: " +
								clientResponse.statusCode()))
				)
				.bodyToMono(DefaultClaims.class)  // 将响应体转换为String类型
				.flatMap(claims -> {     // 处理验证成功的情况
					// 修改原始请求：添加用户名头
					ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
							.header("X-User-Id", claims.get("userId").toString())
							.header("X-User-Name", claims.get("username").toString())
							.build();

					LOG.info("token校验成功：接口路径：{}，用户名：{}", path, claims.get("username").toString());
					// 使用修改后的请求继续过滤器链处理
					return chain.filter(exchange.mutate().request(mutatedRequest).build());
				})
				.onErrorResume(e -> {       // 处理所有验证异常
					// 记录详细错误日志
					LOG.error("JWT 解析失败", e);

					// 检查根原因是否为服务不可用异常
					Throwable rootCause = e;
					while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
						rootCause = rootCause.getCause();
					}

					LOG.error("根异常类型：{}", rootCause.getClass().getName());

					// 判断异常类型并设置响应状态
					if (rootCause instanceof ServiceUnavailableException) {
						// 服务不可用
						exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
					} else if (isAuthenticationException(e)) {
						// 认证错误（JWT相关异常）
						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					} else {
						// 其他非认证错误统一视为服务不可用
						exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
					}

					// 立即结束响应
					return exchange.getResponse().setComplete();
				});
	}
	// 辅助方法：判断是否为认证相关异常
	private boolean isAuthenticationException(Throwable e) {
		return e instanceof JwtException;     // JWT错误
	}
	@Override
	public int getOrder() {
		return -1;
	}
}
