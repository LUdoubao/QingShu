package org.doubao.feed.service.service;

import org.doubao.feed.service.feign.UserClient;
import org.doubao.feed.service.mapper.EventTimelineMapper;
import org.doubao.feed.service.mapper.UserTimelineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UnreadService {

	private static final Logger log = LoggerFactory.getLogger(UnreadService.class);
	@Resource
	private UserClient userClient;
	@Resource
	private UserTimelineMapper userTimelineMapper;
	@Resource
	private EventTimelineMapper eventTimelineMapper;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	// Redis缓存键
	private static final String LAST_READ_TIME_KEY = "feed:lastReadTime:";
	private static final String UNREAD_COUNT_KEY = "feed:unreadCount:";

	// 时间格式化器
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	/**
	 * 获取未读动态数量
	 */
	public long getUnreadCount(Long userId) {
		String countKey = UNREAD_COUNT_KEY + userId;

		// 1. 从缓存获取
		Object countObj = redisTemplate.opsForValue().get(countKey);
		if (countObj != null) {
			return Long.parseLong(countObj.toString());
		}

		// 2. 计算未读数量
		LocalDateTime lastReadTime = getLastReadTime(userId);
		List<Long> followees = userClient.getFollowees(userId).getData();

		if (followees.isEmpty()) {
			return 0;
		}

		// 3. 区分普通用户和大V（简化实现）
		long normalUnread = userTimelineMapper.countUnreadTimeline(userId, followees, lastReadTime);
		// 大V未读数量计算逻辑...

		long totalUnread = normalUnread;

		// 4. 更新缓存
		redisTemplate.opsForValue().set(countKey, totalUnread, 1, TimeUnit.HOURS);

		return totalUnread;
	}

	/**
	 * 更新最后阅读时间
	 */
	public void updateLastReadTime(Long userId, LocalDateTime time) {
		String timeKey = LAST_READ_TIME_KEY + userId;
		redisTemplate.opsForValue().set(timeKey, time.format(DATE_FORMATTER));

		// 清除未读数量缓存
		String countKey = UNREAD_COUNT_KEY + userId;
		redisTemplate.delete(countKey);
	}

	/**
	 * 标记所有动态为已读
	 */
	public void markAllAsRead(Long userId) {
		updateLastReadTime(userId, LocalDateTime.now());
	}

	/**
	 * 获取最后阅读时间
	 */
	private LocalDateTime getLastReadTime(Long userId) {
		String timeKey = LAST_READ_TIME_KEY + userId;
		Object timeObj = redisTemplate.opsForValue().get(timeKey);

		if (timeObj != null && !timeObj.toString().isEmpty()) {
			try {
				return LocalDateTime.parse(timeObj.toString(), DATE_FORMATTER);
			} catch (Exception e) {
				log.error("[getLastReadTime] 解析时间失败", e);
			}
		}

		// 默认返回7天前的时间
		return LocalDateTime.now().minusDays(7);
	}

	/**
	 * 增加未读数量（用于监听新动态事件）
	 */
	public void incrementUnreadCount(Long userId) {
		String countKey = UNREAD_COUNT_KEY + userId;
		redisTemplate.opsForValue().increment(countKey);
	}
}
