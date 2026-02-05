package org.doubao.mall.common.config;

import org.doubao.mall.common.ratelimit.aop.RateLimitAspect;
import org.doubao.mall.common.ratelimit.service.RateLimitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RateLimitAutoConfiguration {
	@Bean
	public RateLimitService rateLimitService(StringRedisTemplate redisTemplate) {
		return new RateLimitService(redisTemplate);
	}

	@Bean
	public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService) {
		return new RateLimitAspect(rateLimitService);
	}
}
