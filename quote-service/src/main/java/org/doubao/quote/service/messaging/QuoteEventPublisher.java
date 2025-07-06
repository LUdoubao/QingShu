package org.doubao.quote.service.messaging;

import org.apache.commons.lang.StringUtils;
import org.doubao.notification.service.config.RabbitConfig;
import org.doubao.notification.service.event.AuditEvent;
import org.doubao.notification.service.event.SystemEvent;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.QuoteTag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class QuoteEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String QUOTE_EXCHANGE = "quote.exchange";

	public void publishQuoteVerify(QuoteUpdateDto quoteUpdateDto) {
		// 拼接quoteUpdateDto json字符串
		String quoteUpdateDtoJson = "{\"quoteId\":\"" + quoteUpdateDto.getQuoteId() + "\",\"AQuoteVo\":\""
				+ quoteUpdateDto.getAfterQuoteVo().toString() + "\",\"BQuoteVo\":\"" + quoteUpdateDto.getBeforeQuoteVo().toString() + "\"}";
		rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.verify", quoteUpdateDtoJson);
	}

	public void publishQuoteAdd(Quote quote, List<QuoteTag> quoteTags) {
		// 拼接quoteUpdateDto json字符串
		String quoteJson = "{\"id\":\"" + quote.getId() + "\",\"content\":\"" + quote.getContent() + "\",\"author\":\""
				+ quote.getAuthor() + "\",\"source\":\"" + quote.getSource() + "\",\"categoryId\":\""
				+ quote.getCategoryId() + "\",\"tagIds\":\"" + quoteTags + "\"}";
		rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.add", quoteJson);
	}

	// 推送引文更新通知(系统通知)
	public void pushQuoteUpdateNotification(Long userId, String action, String target,
											Long targetId, String result, String details) {
		SystemEvent event = new SystemEvent(
				userId,
				action,
				target,
				targetId,
				result,
				details
		);
		String queueName = RabbitConfig.USER_NOTIFICATION_QUEUE_PREFIX + userId;
		rabbitTemplate.convertAndSend(
				RabbitConfig.NOTIFICATION_EXCHANGE,
				queueName,
				event
		);
	}

	// 推送引文审核通知
	public void pushQuoteVerifyNotification(Long quoteId, String content,
											String status, String reason, String submitterName,
											Long receiverId) {

		AuditEvent event = new AuditEvent(
				receiverId,
				quoteId,
				content,
				status,
				reason,
				submitterName
		);
		String queueName = RabbitConfig.USER_NOTIFICATION_QUEUE_PREFIX + receiverId;

		rabbitTemplate.convertAndSend(
				RabbitConfig.NOTIFICATION_EXCHANGE,
				queueName,
				event
		);
	}
}