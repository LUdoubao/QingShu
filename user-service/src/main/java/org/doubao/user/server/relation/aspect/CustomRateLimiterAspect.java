package org.doubao.user.server.relation.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.server.relation.annotation.UserRateLimiter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面（基于Redis实现）
 */
@Aspect
@Component
public class CustomRateLimiterAspect {

	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	// 定义切点表达式并绑定注解参数
	@Pointcut("@annotation(rateLimiter)")
	public void rateLimitPointcut(UserRateLimiter rateLimiter) {
	}
	@Before("rateLimitPointcut(rateLimiter)")
	public void before(JoinPoint joinPoint, UserRateLimiter rateLimiter) {
		Long userId = UserContext.getUserId();
		// 1. 解析限流键
		String redisKey = "rate_limit:" + rateLimiter.key() + ":" + userId;

		// 2. 执行限流逻辑
		Long count = redisTemplate.opsForValue().increment(redisKey, 1);
		if (count != null && count == 1) {
			// 首次访问，设置过期时间
			redisTemplate.expire(redisKey, rateLimiter.period(), TimeUnit.SECONDS);
		}

		// 3. 超过限制则抛出异常
		if (count != null && count > rateLimiter.count()) {
			throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
		}
	}
}