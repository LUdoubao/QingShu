package org.doubao.fanout.service.component;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.fanout.service.service.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@Component
public class EventReceiver {

	private final Logger log = LoggerFactory.getLogger(EventReceiver.class);
	@Resource
	private EventHandler eventHandler;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Value("${fanout.cache.redis.processed-key-prefix:fanout:processed:}")
	private String processedKeyPrefix;

	/**
	 * 监听并接收上游事件
	 */
	@RabbitListener(queues = "fanout.event.queue", ackMode = "MANUAL")
	public void receiveEvent(@Valid @Payload BusinessEvent event,
							 @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
							 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
							 Channel channel) throws IOException {
		// 生成traceId用于链路追踪
		String traceId = UUID.randomUUID().toString();
		MDC.put("traceId", traceId);

		try {
			log.info("[receiveEvent] Received event, eventId: {}, event: {}", event.getEventId(), JSON.toJSONString(event));

			// 事件合法性校验（非空、类型合法）
			if (!validateEvent(event)) {
				log.error("[receiveEvent] 事件非法，traceId: {}, event: {}", traceId, event);
				// 非法事件直接确认（避免重新入队）
				channel.basicAck(deliveryTag, false);
				return;
			}

			// 检查事件是否已处理（去重）
			String processedKey = processedKeyPrefix + event.getEventId();
			Boolean isProcessed = redisTemplate.hasKey(processedKey);
			if (Boolean.TRUE.equals(isProcessed)) {
				log.info("[receiveEvent] Event already processed, eventId: {}", event.getEventId());
				channel.basicAck(deliveryTag, false);
				return;
			}

			// 处理事件
			eventHandler.handle(event);
			// 手动确认
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			log.error("[receiveEvent] Error processing received event", e);
			// 处理失败，拒绝消息并重新入队
			channel.basicNack(deliveryTag, false, true);
		} finally {
			MDC.remove("traceId");
		}
	}

	/**
	 * 事件合法性校验
	 */
	private boolean validateEvent(BusinessEvent event) {
		if (event == null || event.getEventId() == null || event.getEventType() == null) {
			return false;
		}
		// 校验事件类型是否存在（避免非法类型）
		try {
			EventType.valueOf(event.getEventType().name());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
}
