package org.doubao.notification.service.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.rabbitmq.client.Channel;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.event.*;
import org.doubao.notification.service.config.UniversalDateTimeDeserializer;
import org.doubao.notification.service.entity.Notification;
import org.doubao.notification.service.service.NotificationService;
import org.doubao.notification.service.utils.NotificationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class NotificationListener {
	private static final ParserConfig FIXED_CONFIG;

	static {
		// 初始化配置（只执行一次）
		FIXED_CONFIG = new ParserConfig();
		FIXED_CONFIG.putDeserializer(LocalDateTime.class, new UniversalDateTimeDeserializer());
	}
	private static final Logger LOG = LoggerFactory.getLogger(NotificationListener.class);

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationFormatter formatter;

	@RabbitListener(
			queues = "notification.queue",
			ackMode = "MANUAL",
			concurrency = "3-5"
	)
	public void userNotification(@Payload BusinessEvent event,
								 Channel channel,
								 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
		try {
			EventType eventType = event.getEventType();
			Map<String, Object> extInfo = event.getExtInfo();
			Object notificationEvent = extInfo.get("NotificationEvent");
			switch (eventType) {
				case USER_REGISTER:
					String to = String.valueOf(extInfo.get("to"));
					LOG.info("[用户相关事件] Received email to {}", to);
					String content = String.valueOf(extInfo.get("content"));
					LOG.info("[用户相关事件] Received email content: {}", content);
					String subject = String.valueOf(extInfo.get("subject"));
					LOG.info("[用户相关事件] Received email subject: {}", subject);
					sendEmail(to, subject, content);
					break;
				case COMMENT_EVENT:
				case LIKE_EVENT:
				case QUOTE_EVENT:
					LOG.info("[引文事件] Received notification event: {}", JSON.toJSONString(extInfo));
					handleNotificationEvent(notificationEvent);
					break;
				case NEW_MESSAGE:
					LOG.info("[新消息事件] Received notification event: {}", JSON.toJSONString(extInfo));
					handleNotificationEvent(notificationEvent);
					break;
				default:
					throw new IllegalArgumentException("Unsupported event type: " + eventType.getDescription());
			}

			// 成功时确认
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			LOG.error("[userNotification] Error processing received event", e);
			channel.basicNack(deliveryTag, false, false); // 不重新入队
		}

	}

	private void sendEmail (String to, String subject, String content) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom("3411426617@qq.com","青书");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(content, false);
		mailSender.send(message);
	}


	public void handleNotificationEvent(Object notificationEvent) {
		LOG.info("Received notification event: {}", notificationEvent);
		JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(notificationEvent));
		String type = jsonObject.getString("type");
		LOG.info("Notification type: {}", type);
		Notification notification;
		switch (type) {
			case "SYSTEM":
				String action = jsonObject.getString("action");
				switch ( action) {
					case "AUDIT_QUOTE":
						AuditQuoteEvent auditQuoteEvent =  JSON.parseObject(JSON.toJSONString(notificationEvent), AuditQuoteEvent.class, FIXED_CONFIG );
						notification = formatter.formatSystemNotification(auditQuoteEvent);

						break;
					case "VERIFY_QUOTE":
						VerifyQuoteEvent verifyQuoteEvent =  JSON.parseObject(JSON.toJSONString(notificationEvent), VerifyQuoteEvent.class, FIXED_CONFIG );
						notification = formatter.formatSystemNotification(verifyQuoteEvent);
						break;
					default:
						LOG.error("Unsupported notification action: {}",action);
						throw new IllegalArgumentException("Unsupported event action: " +action);
				}
				break;
			case "LIKE":
				LikeEvent likeEvent =  JSON.parseObject(JSON.toJSONString(notificationEvent), LikeEvent.class, FIXED_CONFIG );
				notification = formatter.formatLikeNotification(likeEvent);
				break;
			case "COMMENT":
				CommentEvent commentEvent =  JSON.parseObject(JSON.toJSONString(notificationEvent), CommentEvent.class, FIXED_CONFIG );
				notification = formatter.formatCommentNotification(commentEvent);
				break;
			case "CHAT":
				DialogEvent newMessageEvent =  JSON.parseObject(JSON.toJSONString(notificationEvent), DialogEvent.class, FIXED_CONFIG );
				notification = formatter.formatNewMessageNotification(newMessageEvent);
				break;
			default:
				LOG.error("Unsupported notification type: {}", type);
				return;
		}

		notificationService.sendNotification(notification);
	}

}