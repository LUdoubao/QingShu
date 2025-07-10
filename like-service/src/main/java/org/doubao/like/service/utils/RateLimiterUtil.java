package org.doubao.like.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterUtil {
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	public boolean tryAcquire(String key, int maxCount, int seconds) {
		Long current = redisTemplate.opsForValue().increment(key, 1);
		if (current == null) {
			redisTemplate.opsForValue().set(key, 1);
			return true;
		}
		if (current == 1) {
			redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
			return true;
		}
		return current <= maxCount;
	}
}
