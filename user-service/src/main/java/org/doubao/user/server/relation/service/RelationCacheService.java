package org.doubao.user.server.relation.service;

import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.vo.PageResult;

import java.util.concurrent.TimeUnit;

public interface RelationCacheService {
	PageResult<UserInfo> getFollowerPage(String cacheKey);

	void setFollowerPage(String cacheKey, PageResult<UserInfo> result, int cacheTtlSeconds);

	PageResult<UserInfo> getFollowingPage(String cacheKey);

	void setFollowingPage(String cacheKey, PageResult<UserInfo> result, int cacheTtlSeconds);

	void deleteCache(String cacheKey);

	void deleteCacheBatch(String cacheKeyPrefix);

	Integer getInteger(String cacheKey);

	void set(String cacheKey, int followerCount, int cacheTtlSeconds, TimeUnit timeUnit);
}
