package org.doubao.user.server.relation.service;


import java.util.concurrent.TimeUnit;

public interface RelationCacheService {
	void deleteCache(String cacheKey);

	void deleteCacheBatch(String cacheKeyPrefix);

	Integer getInteger(String cacheKey);

	void set(String cacheKey, int followerCount, int cacheTtlSeconds, TimeUnit timeUnit);
}
