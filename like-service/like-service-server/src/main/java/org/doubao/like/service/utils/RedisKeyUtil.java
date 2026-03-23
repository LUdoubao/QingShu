package org.doubao.like.service.utils;

import org.doubao.like.service.enums.HotListType;

// RedisKeyUtil.java
public class RedisKeyUtil {
	// 用户点赞状态：like:{userId}:{entityType}:{entityId}
	public static String getUserLikeKey(Long userId, String entityType, String entityId) {
		return "like:" + userId + ":" + entityType + ":" + entityId;
	}

	// 实体点赞计数：like:{entityType}:{entityId}:count
	public static String getEntityLikeCountKey(String entityType, String entityId) {
		return "like:" + entityType + ":" + entityId + ":count";
	}

	// 限流键：rate:like:{userId}
	public static String getRateLimitKey(Long userId) {
		return "rate:like:" + userId;
	}

	public static String getHotContentsKey() {
		return getHotContentsKey(HotListType.ALL);
	}

	public static String getLastHotContentsKey() {
		return getLastHotContentsKey(HotListType.ALL);
	}

	public static String getHotContentsKey(HotListType type) {
		return "hot:contents:" + type.getCode() + ":current";
	}

	public static String getLastHotContentsKey(HotListType type) {
		return "hot:contents:" + type.getCode() + ":last";
	}
}
