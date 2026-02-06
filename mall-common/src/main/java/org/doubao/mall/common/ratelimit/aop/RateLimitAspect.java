package org.doubao.mall.common.ratelimit.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.doubao.mall.common.exception.RateLimitExceededException;
import org.doubao.mall.common.ratelimit.annotation.RateLimit;
import org.doubao.mall.common.ratelimit.enums.RateLimitDimension;
import org.doubao.mall.common.ratelimit.service.RateLimitService;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class RateLimitAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitAspect.class);
	private static final String KEY_PREFIX = "rate_limit";
	private final RateLimitService rateLimitService;

	public RateLimitAspect(RateLimitService rateLimitService) {
		this.rateLimitService = rateLimitService;
	}

	@Around("@annotation(rateLimit)")
	public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		String baseKey = rateLimit.key();
		if (baseKey == null || baseKey.trim().isEmpty()) {
			baseKey = method.getDeclaringClass().getSimpleName() + ":" + method.getName();
		}
		List<String> keys = buildKeys(rateLimit.dimensions(), baseKey);
		
		LOGGER.info("[RateLimit] 方法: {}.{} | 维度: {} | Key: {} | 限流配置: {}/{}/{} | 超时: {}ms",
				method.getDeclaringClass().getSimpleName(),
				method.getName(),
				rateLimit.dimensions(),
				baseKey,
				rateLimit.count(),
				rateLimit.interval(),
				rateLimit.timeUnit(),
				rateLimit.timeoutMillis());
		
		if (keys.isEmpty()) {
			LOGGER.warn("[RateLimit] 未生成有效的限流键，跳过限流检查");
			return joinPoint.proceed();
		}
		
		LOGGER.debug("[RateLimit] 实际使用的Redis键: {}", keys);
		
		boolean allowed = rateLimitService.tryAcquire(
				keys,
				rateLimit.count(),
				rateLimit.interval(),
				rateLimit.timeUnit(),
				rateLimit.timeoutMillis()
		);
		
		if (allowed) {
			LOGGER.info("[RateLimit] 请求通过 - 方法: {}.{}", 
				method.getDeclaringClass().getSimpleName(), method.getName());
			return joinPoint.proceed();
		} else {
			LOGGER.warn("[RateLimit] 请求被限流 - 方法: {}.{} | Keys: {}", 
				method.getDeclaringClass().getSimpleName(), method.getName(), keys);
			
			String fallbackMethod = rateLimit.fallbackMethod();
			if (fallbackMethod == null || fallbackMethod.trim().isEmpty()) {
				LOGGER.error("[RateLimit] 限流触发且无降级方法，抛出异常");
				throw new RateLimitExceededException("请求过于频繁，请稍后再试");
			}
			
			LOGGER.info("[RateLimit] 执行降级方法: {}", fallbackMethod);
			return invokeFallback(joinPoint, fallbackMethod);
		}
	}

	private List<String> buildKeys(RateLimitDimension[] dimensions, String baseKey) {
		List<String> keys = new ArrayList<>();
		for (RateLimitDimension dimension : dimensions) {
			String dimensionKey = buildDimensionKey(dimension, baseKey);
			if (dimensionKey != null) {
				keys.add(dimensionKey);
			}
		}
		return keys;
	}

	private String buildDimensionKey(RateLimitDimension dimension, String baseKey) {
		switch (dimension) {
			case GLOBAL:
				return KEY_PREFIX + ":global:" + baseKey;
			case IP:
				return KEY_PREFIX + ":ip:" + resolveClientIp() + ":" + baseKey;
			case USER:
				return KEY_PREFIX + ":user:" + resolveUserId() + ":" + baseKey;
			default:
				return null;
		}
	}

	private String resolveClientIp() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return "unknown";
		}
		HttpServletRequest request = attributes.getRequest();
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.trim().isEmpty()) {
			return forwarded.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.trim().isEmpty()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}

	private String resolveUserId() {
		UserLoginVo user = UserContext.getUser();
		if (user == null || user.getId() == null) {
			return "anonymous";
		}
		return String.valueOf(user.getId());
	}

	private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethod) throws Throwable {
		Object target = joinPoint.getTarget();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		Class<?> targetClass = target.getClass();
		try {
			Method noArgs = targetClass.getDeclaredMethod(fallbackMethod);
			noArgs.setAccessible(true);
			return noArgs.invoke(target);
		} catch (NoSuchMethodException ignored) {
			Method sameArgs = targetClass.getDeclaredMethod(fallbackMethod, method.getParameterTypes());
			sameArgs.setAccessible(true);
			return sameArgs.invoke(target, joinPoint.getArgs());
		}
	}
}
