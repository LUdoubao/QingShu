package org.doubao.notification.service.service.impl;

import org.doubao.notification.service.config.RabbitConfig;
import org.doubao.notification.service.dto.NotificationDTO;
import org.doubao.notification.service.entity.Notification;
import org.doubao.notification.service.service.PushService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PushServiceImpl implements PushService {

	@Autowired
	private RabbitTemplate rabbitTemplate;


	@Override
	public void pushToUser(Long userId, Notification notification) {
		String routingKey = RabbitConfig.USER_NOTIFICATION_ROUTING_KEY_PREFIX + userId;
		rabbitTemplate.convertAndSend(
				RabbitConfig.NOTIFICATION_EXCHANGE,
				routingKey,
				NotificationDTO.fromEntity(notification)
		);
	}
}
