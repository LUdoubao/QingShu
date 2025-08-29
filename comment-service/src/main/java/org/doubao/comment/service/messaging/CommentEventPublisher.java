package org.doubao.comment.service.messaging;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.event.CommentEvent;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CommentEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(CommentEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Resource
	private QuoteClient quoteClient;

	public void pushCommentNotification(Long repliedUserId, boolean isComment, String quoteId, String content,
									 Long operatorUserId, String operatorUserName) {

		taskExecutor.asyncExecute(() -> {
			LOGGER.info("==================开始构建MQ消息");
			Long receiverId = repliedUserId;
			QuoteVo quoteVo = quoteClient.detail(Long.parseLong(quoteId)).getData();
			if (isComment) {
				// 查询引文所属用户
				receiverId = quoteVo.getCreatedId();
			}
			if (Objects.equals(receiverId, operatorUserId)) {
				LOGGER.info("用户{}自己评论了，不需要推送", receiverId);
				return null;
			}
			// 异步发送MQ消息通知文案所属用户
			CommentEvent event = new CommentEvent(
					receiverId,
					isComment,
					quoteId,
					quoteVo.getContent(),
					content,
					operatorUserId,
					operatorUserName
			);
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", event);
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "COMMENT_EVENT_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.COMMENT_EVENT);
			businessEvent.setExtInfo(message);
			LOGGER.info("==================开始发送MQ消息");

			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_COMMENT_ROUTING_KEY,
					businessEvent
			);
			LOGGER.info("[register] COMMENT_EVENT事件发送成功，eventId: {}", eventId);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知评论失败", t);
			}
		});

	}
}