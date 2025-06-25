package org.doubao.payment.service.stream;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public static final String BUSINESS_EXCHANGE = "business.exchange";

	public void publishPaymentSuccess(Long orderId) {
		rabbitTemplate.convertAndSend(BUSINESS_EXCHANGE, "payment.success", orderId);
	}
}
