package org.doubao.user.service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.service.annotation.UserRateLimiter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


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

		String redisKey = "rate_limit:" + rateLimiter.key() + ":" + userId;


		Long count = redisTemplate.opsForValue().increment(redisKey, 1);
		if (count != null && count == 1) {

			redisTemplate.expire(redisKey, rateLimiter.period(), TimeUnit.SECONDS);
		}


		if (count != null && count > rateLimiter.count()) {
			throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
		}
	}
}
