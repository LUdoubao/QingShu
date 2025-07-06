package org.doubao.notification.service.listener;

import org.doubao.notification.service.entity.Notification;
import org.doubao.notification.service.event.AuditEvent;
import org.doubao.notification.service.event.NotificationEvent;
import org.doubao.notification.service.event.SystemEvent;
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

@Component
public class NotificationListener {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationListener.class);

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationFormatter formatter;

	@RabbitListener(queues = "notification.order.created")
	public void handleOrderCreated(String orderJson) throws MessagingException {
		sendEmail("3082738259@qq.com", "订单创建成功", "您的订单已成功创建，订单信息：" + orderJson);
	}

	@RabbitListener(queues = "notification.payment.success")
	public void paymentSuccess(Long orderId) throws MessagingException {
		sendEmail("3082738259@qq.com", "支付成功", "您的订单已支付成功，订单号：" + orderId);
	}

	@RabbitListener(queues = "notification.quote.verify")
	public void verifyQuote(String quoteJson) throws MessagingException {
		sendEmail("3082738259@qq.com", "审核引文通知", "您有待审核的引文，引文信息：" + quoteJson);
	}

	@RabbitListener(queues = "notification.quote.add")
	public void addQuote(String quoteJson) throws MessagingException {
		sendEmail("3082738259@qq.com", "新增引文通知", "有新引文添加，引文信息：" + quoteJson);
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
			// 其他类型处理...
			default:
				LOG.warn("Unsupported notification type: {}", event.getType());
				return;
		}

		notificationService.sendNotification(notification);
	}

}