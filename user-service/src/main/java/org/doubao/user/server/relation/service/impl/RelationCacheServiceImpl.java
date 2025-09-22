package org.doubao.user.server.relation.service.impl;

import org.doubao.user.server.relation.service.RelationCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RelationCacheServiceImpl implements RelationCacheService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RelationCacheServiceImpl.class);
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public void deleteCache(String cacheKey) {
		redisTemplate.delete(cacheKey);
	}

	@Override
	public void deleteCacheBatch(String cacheKeyPrefix) {
		LOGGER.info("删除缓存前缀：{}", cacheKeyPrefix);
		redisTemplate.execute((RedisConnection connection) -> {
			// 添加通配符 * 来匹配所有以该前缀开头的键
			ScanOptions scanOptions = ScanOptions.scanOptions()
					.match(cacheKeyPrefix + "*")  // 关键修改：添加 *
					.count(100)
					.build();

			// 执行 SCAN 命令
			Cursor<byte[]> cursor = connection.scan(scanOptions);

			// 遍历匹配的键并删除
			List<String> keysToDelete = new ArrayList<>();
			while (cursor.hasNext()) {
				byte[] keyBytes = cursor.next();
				String key = new String(keyBytes, StandardCharsets.UTF_8);
				LOGGER.info("删除缓存：{}", key);
				keysToDelete.add(key);
			}
			cursor.close();

			// 批量删除提高性能
			if (!keysToDelete.isEmpty()) {
				redisTemplate.delete(keysToDelete);
				LOGGER.info("成功删除 {} 个缓存键", keysToDelete.size());
			}

			return null;
		});
	}

	@Override
	public Integer getInteger(String cacheKey) {
		Object value = redisTemplate.opsForValue().get(cacheKey);
		return value instanceof Integer ? (Integer) value : null;
	}

	@Override
	public void set(String cacheKey, int followerCount, int cacheTtlSeconds, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(cacheKey, followerCount, cacheTtlSeconds, timeUnit);
	}
}
