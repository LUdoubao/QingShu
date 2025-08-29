package org.doubao.notification.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.doubao.notification.service.dto.NotificationDTO;
import org.doubao.notification.service.dto.UnreadCountDTO;
import org.doubao.notification.service.entity.Notification;
import org.doubao.notification.service.enums.NotificationStatus;
import org.doubao.notification.service.mapper.NotificationMapper;
import org.doubao.notification.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
	@Autowired
	private NotificationMapper notificationMapper;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	@Transactional
	public void sendNotification(Notification notification) {
		notification.setStatus(NotificationStatus.UNREAD.getCode());
		notificationMapper.insert(notification);

		// 更新Redis未读计数
		String key = "notification:unread:" + notification.getUserId();
		redisTemplate.opsForValue().increment(key, 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size, String status) {
		LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
		query.eq(Notification::getUserId, userId)
				.orderByDesc(Notification::getCreatedTime);

		if (StringUtils.isNotBlank(status)) {
			query.eq(Notification::getStatus, NotificationStatus.valueOf(status));
		}

		Page<Notification> notificationPage = notificationMapper.selectPage(
				new Page<>(page, size), query);

		return (Page<NotificationDTO>) notificationPage.convert(NotificationDTO::fromEntity);
	}

	@Override
	@Transactional
	public UnreadCountDTO markAsRead(Long id) {
		Notification notification = notificationMapper.selectById(id);
		if (notification != null && notification.getStatus() == NotificationStatus.UNREAD.getCode()) {
			notification.setStatus(NotificationStatus.READ.getCode());
			notificationMapper.updateById(notification);

			// 更新Redis未读计数
			String key = "notification:unread:" + notification.getUserId();
			redisTemplate.opsForValue().decrement(key, 1);
		}
		if (notification!=null && notification.getUserId() != null) {
			return getUnreadCount(notification.getUserId());
		}
		return null;
	}

	@Override
	@Transactional
	public void batchMarkAsRead(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return;

		List<Notification> notifications = notificationMapper.selectBatchIds(ids);
		Map<Long, Long> userCountMap = notifications.stream()
				.filter(n -> n.getStatus() == NotificationStatus.UNREAD.getCode())
				.collect(Collectors.groupingBy(
						Notification::getUserId,
						Collectors.counting()
				));

		notificationMapper.batchMarkAsRead(ids);

		// 批量更新Redis未读计数
		userCountMap.forEach((userId, count) -> {
			String key = "notification:unread:" + userId;
			redisTemplate.opsForValue().decrement(key, count);
		});
	}

	@Override
	public void deleteNotification(Long id) {
		notificationMapper.deleteById(id);
	}

	@Override
	public UnreadCountDTO getUnreadCount(Long userId) {
		String key = "notification:unread:" + userId;
		String countStr = redisTemplate.opsForValue().get(key);

		if (countStr == null) {
			// 缓存未命中，从数据库加载
			int count = notificationMapper.selectUnreadCount(userId);
			redisTemplate.opsForValue().set(key, String.valueOf(count));
			return new UnreadCountDTO(count);
		}

		return new UnreadCountDTO(Integer.parseInt(countStr));
	}

	@Override
	public NotificationDTO getNotificationDetail(Long id) {
		Notification notification = notificationMapper.selectById(id);
		if (notification != null) {
			return NotificationDTO.fromEntity(notification);
		}
		return null;
	}
}
