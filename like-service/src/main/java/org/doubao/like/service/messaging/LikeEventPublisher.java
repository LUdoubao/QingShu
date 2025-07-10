package org.doubao.like.service.messaging;

import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.notification.service.config.RabbitConfig;
import org.doubao.notification.service.event.LikeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LikeEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(LikeEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;

	public void pushLikeNotification(Long userId, int entityType, Long entityId, boolean isLike, String content,
									 Long operatorUserId, String operatorUserName) {
		taskExecutor.asyncExecute(() -> {
			// 异步发送MQ消息通知文案所属用户
			LikeEvent event = new LikeEvent(
					userId,
					entityType,
					entityId,
					isLike,
					content,
					operatorUserId,
					operatorUserName
			);
			String routingKey = RabbitConfig.USER_NOTIFICATION_ROUTING_KEY_PREFIX + userId;
			rabbitTemplate.convertAndSend(
					RabbitConfig.NOTIFICATION_EXCHANGE,
					routingKey,
					event
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知文案所属用户失败", t);
			}
		});

	}
}