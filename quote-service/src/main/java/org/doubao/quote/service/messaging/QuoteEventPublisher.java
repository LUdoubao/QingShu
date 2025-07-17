package org.doubao.quote.service.messaging;

import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.mall.common.event.AuditEvent;
import org.doubao.mall.common.event.SystemEvent;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.QuoteTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuoteEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String QUOTE_EXCHANGE = "quote.exchange";

	public void publishQuoteVerify(QuoteUpdateDto quoteUpdateDto) {
		taskExecutor.asyncExecute(() -> {
			// 异步发送MQ消息 推送引文审核通知
			// 拼接quoteUpdateDto json字符串
			String quoteUpdateDtoJson = "{\"quoteId\":\"" + quoteUpdateDto.getQuoteId() + "\",\"AQuoteVo\":\""
					+ quoteUpdateDto.getAfterQuoteVo().toString() + "\",\"BQuoteVo\":\"" + quoteUpdateDto.getBeforeQuoteVo().toString() + "\"}";
			rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.verify", quoteUpdateDtoJson);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息推送引文审核通知失败", t);
			}
		});

	}

	public void publishQuoteAdd(Quote quote, List<QuoteTag> quoteTags) {
		taskExecutor.asyncExecute(() -> {
			// 拼接quoteUpdateDto json字符串
			String quoteJson = "{\"id\":\"" + quote.getId() + "\",\"content\":\"" + quote.getContent() + "\",\"author\":\""
					+ quote.getAuthor() + "\",\"source\":\"" + quote.getSource() + "\",\"categoryId\":\""
					+ quote.getCategoryId() + "\",\"tagIds\":\"" + quoteTags + "\"}";
			rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.add", quoteJson);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息推送引文新增通知失败", t);
			}
		});
	}

	// 推送引文更新通知(系统通知)
	public void pushQuoteUpdateNotification(Long userId, String action, String target,
											Long targetId, String result, String details) {
		taskExecutor.asyncExecute(() -> {
			// 异步发送MQ消息 通知文案所属用户
			SystemEvent event = new SystemEvent(
					userId,
					action,
					target,
					targetId,
					result,
					details
			);
			String queueName = Constants.USER_NOTIFICATION_ROUTING_KEY_PREFIX + userId;
			rabbitTemplate.convertAndSend(
					Constants.NOTIFICATION_EXCHANGE,
					queueName,
					event
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
			AuditEvent event = new AuditEvent(
					receiverId,
					quoteId,
					content,
					status,
					reason,
					submitterName
			);
			String queueName = Constants.USER_NOTIFICATION_ROUTING_KEY_PREFIX + receiverId;

			rabbitTemplate.convertAndSend(
					Constants.NOTIFICATION_EXCHANGE,
					queueName,
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