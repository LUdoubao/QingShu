package org.doubao.order.service.messaging;

import org.doubao.order.service.entity.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String BUSINESS_EXCHANGE = "business.exchange";

	public void publishOrderCreated(Order order) {
		// 拼接order json字符串
		String orderJson = "{\"id\":\"" + order.getId() + "\",\"productId\":\"" + order.getProductId() + "\",\"count\":\""
				+ order.getCount() + "\",\"totalAmount\":\"" + order.getTotalAmount() + "\"}";
		rabbitTemplate.convertAndSend(BUSINESS_EXCHANGE, "order.created", orderJson);
	}
	public void publishOrderPaid(Long orderId) {
		rabbitTemplate.convertAndSend(BUSINESS_EXCHANGE, "order.paid", orderId);
	}
}