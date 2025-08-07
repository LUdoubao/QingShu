package org.doubao.comment.service.messaging;

import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.service.CommentService;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.event.CommentEvent;
import org.doubao.mall.common.event.LikeEvent;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CommentEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(CommentEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Resource
	private QuoteClient quoteClient;
	public void pushLikeNotification(Long userId, int entityType, String entityId, boolean isLike, String content,
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
			String routingKey = Constants.USER_NOTIFICATION_ROUTING_KEY_PREFIX + userId;
			rabbitTemplate.convertAndSend(
					Constants.NOTIFICATION_EXCHANGE,
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
	public void pushCommentNotification(Long userId, boolean isComment, String quoteId, String content,
									 Long operatorUserId, String operatorUserName) {
		taskExecutor.asyncExecute(() -> {
			LOGGER.info("==================开始构建MQ消息");
			Long receiverId = userId;
			QuoteVo quoteVo = quoteClient.detail(Long.parseLong(quoteId)).getData();
			if (isComment) {
				// 查询引文所属用户
				receiverId = quoteVo.getCreatedId();
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
			LOGGER.info("==================开始发送MQ消息");
			String routingKey = Constants.USER_NOTIFICATION_ROUTING_KEY_PREFIX + receiverId;
			rabbitTemplate.convertAndSend(
					Constants.NOTIFICATION_EXCHANGE,
					routingKey,
					event
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知评论失败", t);
			}
		});

	}
}