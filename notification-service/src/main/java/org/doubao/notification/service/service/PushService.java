package org.doubao.notification.service.service;

import org.doubao.notification.service.entity.Notification;

public interface PushService {
	void pushToUser(Long userId, Notification notification);
}
