package org.doubao.notification.service.listener;

import org.doubao.notification.service.entity.Notification;
import org.doubao.mall.common.event.AuditEvent;
import org.doubao.mall.common.event.LikeEvent;
import org.doubao.mall.common.event.NotificationEvent;
import org.doubao.mall.common.event.SystemEvent;
import org.doubao.notification.service.service.NotificationService;
import org.doubao.notification.service.utils.NotificationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Component
public class NotificationListener {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationListener.class);

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationFormatter formatter;

	@RabbitListener(queues = "notification.quote.verify")
	public void verifyQuote(String quoteJson) throws MessagingException {
		sendEmail("3082738259@qq.com", "审核引文通知", "您有待审核的引文，引文信息：" + quoteJson);
	}

	@RabbitListener(queues = "notification.quote.add")
	public void addQuote(String quoteJson) throws MessagingException {
		sendEmail("3082738259@qq.com", "新增引文通知", "有新引文添加，引文信息：" + quoteJson);
	}

	@RabbitListener(queues = "notification.user.verification")
	public void userNotification(Map<String, String>  message) throws MessagingException {
		String to = message.get("to");
		String content = message.get("content");
		String subject = message.get("subject");
		sendEmail(to, subject, content);
	}

	private void sendEmail(String to, String subject, String content) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom("3411426617@qq.com");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(content, false);
		mailSender.send(message);
	}


	@RabbitListener(queues = "notification.queue")
	public void handleNotificationEvent(NotificationEvent event) {
		LOG.info("Received notification event: {}", event);

		Notification notification = new Notification();
		switch (event.getType()) {
			case "AUDIT":
				notification = formatter.formatAuditNotification((AuditEvent) event);
			case "SYSTEM":
				if (event instanceof SystemEvent) {
					notification = formatter.formatSystemNotification((SystemEvent) event);
				}
				break;
			case "LIKE":
				if (event instanceof LikeEvent) {
					notification = formatter.formatLikeNotification((LikeEvent) event);
				}
				break;
			default:
				LOG.warn("Unsupported notification type: {}", event.getType());
				return;
		}

		notificationService.sendNotification(notification);
	}

}