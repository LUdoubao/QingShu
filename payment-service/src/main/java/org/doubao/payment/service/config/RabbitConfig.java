package org.doubao.payment.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String PAYMENT_EXCHANGE = "payment.exchange";
	public static final String PAYMENT_QUEUE = "payment.queue";
	public static final String PAYMENT_ROUTING_KEY = "payment.success";

	@Bean
	public Exchange paymentExchange() {
		return ExchangeBuilder.topicExchange(PAYMENT_EXCHANGE).durable(true).build();
	}

	@Bean
	public Queue paymentQueue() {
		return QueueBuilder.durable(PAYMENT_QUEUE).build();
	}

	@Bean
	public Binding paymentBinding() {
		return BindingBuilder.bind(paymentQueue()).to(paymentExchange()).with(PAYMENT_ROUTING_KEY).noargs();
	}
}