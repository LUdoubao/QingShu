package org.doubao.mall.common.ratelimit.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RateLimitService {
	private static final String LUA_SCRIPT_PATH = "ratelimit/multi_dimension_rate_limit.lua";
	private final StringRedisTemplate redisTemplate;
	private final DefaultRedisScript<Long> script;

	public RateLimitService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.script = new DefaultRedisScript<>();
		this.script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH));
		this.script.setResultType(Long.class);
	}

	public boolean tryAcquire(List<String> keys, int maxTokens, int interval, TimeUnit timeUnit, long timeoutMillis) {
		long intervalMillis = timeUnit.toMillis(interval);
		if (intervalMillis <= 0) {
			intervalMillis = 1000L;
		}
		long deadline = System.currentTimeMillis() + Math.max(timeoutMillis, 0);
		boolean allow;
		do {
			allow = doAcquire(keys, 1, intervalMillis, maxTokens);
			if (allow || timeoutMillis <= 0) {
				return allow;
			}
			try {
				Thread.sleep(Math.min(50L, Math.max(timeoutMillis, 1L)));
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return false;
			}
		} while (System.currentTimeMillis() < deadline);
		return false;
	}

	private boolean doAcquire(List<String> keys, int permits, long intervalMillis, int maxTokens) {
		Long result = redisTemplate.execute(
				script,
				keys,
				String.valueOf(System.currentTimeMillis()),
				String.valueOf(permits),
				String.valueOf(intervalMillis),
				String.valueOf(maxTokens),
				String.valueOf(System.nanoTime())
		);
		return result != null && result == 1L;
	}
}
