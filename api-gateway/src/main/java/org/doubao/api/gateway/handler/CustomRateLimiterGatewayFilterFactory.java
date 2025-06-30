package org.doubao.api.gateway.handler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义网关限流过滤器工厂
 * <p>
 * 功能：基于Redis实现请求限流，当超过阈值时返回429错误
 * 继承自Spring Cloud Gateway的抽象过滤器工厂
 */
@Component
public class CustomRateLimiterGatewayFilterFactory
		extends AbstractGatewayFilterFactory<CustomRateLimiterGatewayFilterFactory.Config> {

	// 核心依赖：Redis限流器和Key解析器
	private final RedisRateLimiter defaultRateLimiter;
	private final KeyResolver defaultKeyResolver;

	/**
	 * 构造函数（依赖注入）
	 * @param rateLimiterProvider Redis限流器对象提供者
	 * @param keyResolverProvider Key解析器对象提供者
	 */
	public CustomRateLimiterGatewayFilterFactory(ObjectProvider<RedisRateLimiter> rateLimiterProvider,
												 ObjectProvider<KeyResolver> keyResolverProvider) {
		super(Config.class);  // 声明配置类
		this.defaultRateLimiter = rateLimiterProvider.getIfAvailable();  // 获取限流器Bean
		this.defaultKeyResolver = keyResolverProvider.getIfAvailable();   // 获取Key解析器Bean
	}

	/**
	 * 创建限流过滤器
	 * @param config 自定义配置（包含路由ID）
	 * @return GatewayFilter实例
	 */
	@Override
	public GatewayFilter apply(Config config) {
		// 创建实际的网关过滤器
		return (exchange, chain) -> {
			// 1. 使用Key解析器获取限流键（如用户ID、IP等）
			return defaultKeyResolver.resolve(exchange).flatMap(key -> {
				// 2. 检查请求是否被允许（使用Redis限流器）
				//   参数：路由ID + 限流键
				return defaultRateLimiter.isAllowed(config.getRouteId(), key).flatMap(response -> {
					// 3. 允许请求：继续过滤器链
					if (response.isAllowed()) {
						return chain.filter(exchange);
					}
					// 4. 拒绝请求：返回429错误
					else {
						// 设置HTTP状态码为429（Too Many Requests）
						ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatus.TOO_MANY_REQUESTS);
						// 设置响应内容类型为JSON
						exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

						// 构建错误响应体
						Map<String, Object> body = new HashMap<>();
						body.put("code", 429);
						body.put("message", "请求过于频繁，请稍后再试");
						body.put("timestamp", System.currentTimeMillis());

						byte[] bytes;
						try {
							// 将Map序列化为JSON字节
							bytes = new com.fasterxml.jackson.databind.ObjectMapper()
									.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
						} catch (Exception e) {
							// 序列化失败时返回默认错误信息
							bytes = "{\"code\":429,\"message\":\"Too Many Requests\"}"
									.getBytes(StandardCharsets.UTF_8);
						}

						// 创建数据缓冲区并写入响应
						DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
						return exchange.getResponse().writeWith(Mono.just(buffer));
					}
				});
			});
		};
	}

	/**
	 * 配置类（存储路由ID）
	 * 说明：每个路由可以有自己的限流配置
	 */
	public static class Config implements HasRouteId {
		private String routeId;  // 关联的路由标识

		@Override
		public String getRouteId() {
			return routeId;
		}

		public void setRouteId(String routeId) {
			this.routeId = routeId;
		}
	}
}