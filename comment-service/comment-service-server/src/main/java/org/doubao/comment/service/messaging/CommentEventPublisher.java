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

/**
 * 评论事件发布器
 * <p>
 * 负责发布评论相关的消息事件，如评论通知、回复通知等
 * 通过RabbitMQ异步发送消息到消息队列
 */
@Component
public class CommentEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(CommentEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Resource
	private QuoteClient quoteClient;

	/**
	 * 推送评论通知
	 * <p>
	 * 根据评论类型（主评论或回复）和相关参数，构建评论事件并异步发送到消息队列
	 * 该方法通过线程池异步执行，避免阻塞主业务流程
	 * <p>
	 * 业务逻辑：
	 * 1. 根据isComment参数确定操作类型（评论引文或回复评论）
	 * 2. 获取引文信息以确定接收通知的用户
	 * 3. 构建CommentEvent事件对象
	 * 4. 通过RabbitMQ发送事件到指定交换机和路由键
	 * 5. 添加异常处理确保消息发送失败时能记录日志
	 * 
	 * @param repliedUserId 被回复的用户ID，如果是主评论则为null
	 * @param isComment 是否为主评论，true表示对引文的评论，false表示对评论的回复
	 * @param quoteId 引文ID，用于关联评论和引文
	 * @param content 当前评论的内容
	 * @param originalComment 原始评论内容，用于回复场景
	 * @param operatorUserId 操作用户ID，即评论发布者
	 * @param operatorUserName 操作用户名，用于通知显示
	 * @param operatorUserAvatar 操作用户头像，用于通知显示
	 */
	public void pushCommentNotification(Long repliedUserId, boolean isComment, String quoteId, String content, String originalComment,
									 Long operatorUserId, String operatorUserName, String operatorUserAvatar) {

		// 使用线程池异步执行消息发送，避免阻塞主业务流程
		taskExecutor.asyncExecute(() -> {
			String action = "REPLY_COMMENT";
			String target = "comment";
			LOGGER.info("==================开始构建MQ消息");
			Long receiverId = repliedUserId;
			// 调用引文服务获取引文详情
			QuoteVo quoteVo = quoteClient.detail(Long.parseLong(quoteId)).getData();
			if (isComment) {
				action = "QUOTE_COMMENT";
				target = "quote";
				// 如果是主评论，接收通知的用户是引文的创建者
				receiverId = quoteVo.getCreatedId();
			}
			// 防止自己评论自己时发送通知
			if (Objects.equals(receiverId, operatorUserId)) {
				LOGGER.info("用户{}自己评论了，不需要推送", receiverId);
				return null;
			}
			// 构建评论事件对象，用于通知相关用户
			CommentEvent event = new CommentEvent(
					action,
					receiverId,
					target,
					quoteId,
					isComment ? 0 : 1,
					quoteVo.getContent(),
					isComment ? content : originalComment,
					isComment ? "" : content,
					operatorUserId,
					operatorUserName,
					operatorUserAvatar
			);
			// 封装事件到消息体
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", event);
			// 构建业务事件对象
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "COMMENT_EVENT_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.COMMENT_EVENT);
			businessEvent.setExtInfo(message);
			LOGGER.info("==================开始发送MQ消息");

			// 通过RabbitMQ发送消息到指定交换机和路由键
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_COMMENT_ROUTING_KEY,
					businessEvent
			);
			LOGGER.info("[register] COMMENT_EVENT事件发送成功，eventId: {}", eventId);
			return null;
		}).whenComplete((v, t) -> {
			// 异常处理：记录消息发送失败的日志
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知评论失败", t);
			}
		});

	}
}