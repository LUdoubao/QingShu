package org.doubao.notification.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.notification.service.dto.NotificationDTO;
import org.doubao.notification.service.dto.NotificationQueryDto;
import org.doubao.notification.service.dto.UnreadCountDTO;
import org.doubao.notification.service.entity.Notification;

import java.util.List;

public interface NotificationService {
	void sendNotification(Notification notification);

	/**
	 * 获取用户通知信息（不包含私信通知）
	 */
	Page<NotificationDTO> getUserNotifications(NotificationQueryDto queryDto);
	UnreadCountDTO markAsRead(Long id);
	void batchMarkAsRead(List<Long> ids);
	void deleteNotification(Long id);
	UnreadCountDTO getUnreadCount(Long userId);

	NotificationDTO getNotificationDetail(Long id);

	UnreadCountDTO getUnreadCountType(NotificationQueryDto notificationQueryDto);
}