package org.doubao.like.service.messaging;

import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.mall.common.event.LikeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class LikeEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(LikeEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;

	public void pushLikeNotification(Long userId, int entityType, String entityId, boolean isLike, String content,
									 Long operatorUserId, String operatorUserName) {
		if (Objects.equals(userId, operatorUserId)) {
			LOGGER.info("用户{}对内容{}进行{}操作，无需通知自己", operatorUserId, entityId, isLike ? "点赞" : "取消点赞");
			return;
		}
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
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", event);
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "LIKE_EVENT_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.LIKE_EVENT);
			businessEvent.setExtInfo(message);
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_LIKE_ROUTING_KEY,
					businessEvent
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知文案所属用户失败", t);
			}
		});

	}
}