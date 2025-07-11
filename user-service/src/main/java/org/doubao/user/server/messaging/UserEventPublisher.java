package org.doubao.user.server.messaging;

import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String USER_VERIFICATION_EXCHANGE = "user.verification";

	public void sendVerificationEmail(String email, String code) {
		taskExecutor.asyncExecute(() -> {
			Map<String, String> message = new HashMap<>();
			message.put("to", email);
			message.put("subject", "您的注册验证码");
			message.put("content", "验证码：" + code + "，5分钟内有效");

			rabbitTemplate.convertAndSend(USER_VERIFICATION_EXCHANGE, "user.verification", message);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息推送用户注册验证通知失败", t);
			}
		});

	}
}