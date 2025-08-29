package org.doubao.user.server.core.messaging;

import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
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

	public void sendVerificationEmail(String email, String code, String subject) {
		taskExecutor.asyncExecute(() -> {
			Map<String, Object> message = new HashMap<>();
			message.put("to", email);
			message.put("subject", subject);
			message.put("content", "验证码：" + code + "，5分钟内有效");
			Long timestamp = System.currentTimeMillis();
			BusinessEvent event = new BusinessEvent();

			String eventId = "USER_REGISTER_" + timestamp;
			event.setEventId(eventId);
			event.setTimestamp(timestamp);
			event.setEventType(EventType.USER_REGISTER);
			event.setExtInfo(message);
			rabbitTemplate.convertAndSend(Constants.FANOUT_EVENT_EXCHANGE,
					Constants.USER_REGISTER_ROUTING_KEY, event);
			LOGGER.info("[register] USER_REGISTER事件发送成功，eventId: {}", eventId);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息推送用户注册验证通知失败", t);
			}
		});

	}
}