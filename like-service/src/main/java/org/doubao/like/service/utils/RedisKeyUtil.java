package org.doubao.like.service.utils;

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
		return "hot:contents:current";
	}

	public static String getLastHotContentsKey() {
		return "hot:contents:last";
	}
}