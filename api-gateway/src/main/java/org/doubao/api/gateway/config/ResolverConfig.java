package org.doubao.api.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class ResolverConfig {
	/**
	 * 组合 IP + 路径 限流
	 */
	@Bean
	public KeyResolver ipAndPathKeyResolver() {
		return exchange -> {
			String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
					.getAddress().getHostAddress();
			String path = exchange.getRequest().getPath().toString();
			return Mono.just(ip + ":" + path);
		};
	}
}