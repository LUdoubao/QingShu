package org.doubao.notification.service.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
	public static final String BUSINESS_EXCHANGE = "business.exchange";

	@Bean
	public Exchange businessExchange() {
		return ExchangeBuilder.topicExchange(BUSINESS_EXCHANGE).durable(true).build();
	}

	@Bean
	public Queue orderCreatedQueue() {
		return new Queue("notification.order.created", true);
	}

	@Bean
	public Queue orderPaymentSuccessQueue() {
		return new Queue("notification.payment.success", true);
	}

	@Bean
	public Binding orderCreatedBinding() {
		return BindingBuilder.bind(orderCreatedQueue())
				.to(businessExchange())
				.with("order.created").noargs();
	}

	@Bean
	public Binding paymentSuccessBinding() {
		return BindingBuilder.bind(orderPaymentSuccessQueue())
				.to(businessExchange())
				.with("payment.success").noargs();
	}
}
