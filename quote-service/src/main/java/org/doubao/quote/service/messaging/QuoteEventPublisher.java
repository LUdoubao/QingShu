package org.doubao.quote.service.messaging;

import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.event.AuditQuoteEvent;
import org.doubao.mall.common.event.VerifyQuoteEvent;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.feign.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class QuoteEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Resource
	private UserClient userClient;

	// 推送引文更新通知(系统通知)
	public void pushQuoteUpdateNotification(Long userId,
											Long targetId, String quoteContent) {
		taskExecutor.asyncExecute(() -> {
			VerifyQuoteEvent event = new VerifyQuoteEvent(
					userId,
					targetId,
					quoteContent
			);
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", event);
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "QUOTE_EVENT_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.QUOTE_EVENT);
			businessEvent.setExtInfo(message);
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_QUOTE_ROUTING_KEY,
					businessEvent
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知文案所属用户失败", t);
			}
		});

	}

	// 推送引文审核通知
	public void pushQuoteVerifyNotification(Long quoteId, String content,
											String status, String reason, String submitterName,
											Long receiverId) {
		taskExecutor.asyncExecute(() -> {
			// 异步发送MQ消息通知
			AuditQuoteEvent event = new AuditQuoteEvent(
					receiverId,
					quoteId,
					status,
					content,
					reason,
					submitterName
			);
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", event);
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "QUOTE_EVENT_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.QUOTE_EVENT);
			businessEvent.setExtInfo(message);
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_QUOTE_ROUTING_KEY,
					businessEvent
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知文案所属用户失败", t);
			}
		});
	}

	public void pushFanoutFeedPublish(Quote quote) {
		taskExecutor.asyncExecute(() -> {
			List<UserInfo> userInfos = userClient.getUsersByIds(Collections.singleton(quote.getCreatedId())).getData();

			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "DYNAMIC_PUBLISH_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.DYNAMIC_PUBLISH);
			businessEvent.setActorId(quote.getCreatedId());
			businessEvent.setTargetId(quote.getId());
			Map<String, Object> extInfo = new HashMap<>();
			if (userInfos != null && !userInfos.isEmpty()) {
				extInfo.put("userInfo", userInfos.get(0));
			}
			extInfo.put("dynamicContent", quote.getContent());
			extInfo.put("dynamicTitle", "新文案发布");
			extInfo.put("dynamicType", "quote");
			businessEvent.setExtInfo(extInfo);
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.QUOTE_PUBLISH_ROUTING_KEY,
					businessEvent
			);
			LOGGER.info("[publish] DYNAMIC_PUBLISH事件发送成功，eventId: {}", eventId);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知文案所属用户失败", t);
			}
		});
	}
}