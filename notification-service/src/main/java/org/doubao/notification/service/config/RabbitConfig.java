package org.doubao.notification.service.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
	public static final String USER_VERIFICATION_EXCHANGE = "user.verification";
	public static final String BUSINESS_EXCHANGE = "business.exchange";
	public static final String QUOTE_EXCHANGE = "quote.exchange";
	public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_LIKE_QUEUE = "notification.queue.like";
	public static final String USER_NOTIFICATION_ROUTING_KEY_PREFIX = "notification.user.";


	@Bean
	public Exchange quoteExchange() {
		return ExchangeBuilder.topicExchange(QUOTE_EXCHANGE).durable(true).build();
	}

	@Bean
	public Exchange userExchange() {
		return ExchangeBuilder.topicExchange(USER_VERIFICATION_EXCHANGE).durable(true).build();
	}

	@Bean
	public Queue quoteVerifyQueue() {
		return new Queue("notification.quote.verify", true);
	}

	@Bean
	public Queue quoteAddQueue() {
		return new Queue("notification.quote.add", true);
	}


	@Bean
	public Queue userVerificationQueue() {
		return new Queue("notification.user.verification", true);
	}

	@Bean
	public Binding quoteVerifyBinding() {
		return BindingBuilder.bind(quoteVerifyQueue())
				.to(quoteExchange())
				.with("quote.verify").noargs();
	}

	@Bean
	public Binding quoteAddBinding() {
		return BindingBuilder.bind(quoteAddQueue())
				.to(quoteExchange())
				.with("quote.add").noargs();
	}

	@Bean
	public Binding userVerificationBinding() {
		return BindingBuilder.bind(userVerificationQueue())
				.to(userExchange())
				.with("user.verification").noargs();
	}

	@Bean
	public TopicExchange notificationExchange() {
		return new TopicExchange(NOTIFICATION_EXCHANGE,true, false);
	}

	@Bean
	public Queue notificationQueue() {
		return new Queue(NOTIFICATION_QUEUE, true);
	}

	@Bean
	public Queue notificationLikeQueue() {
		return new Queue(NOTIFICATION_LIKE_QUEUE, true);
	}

	@Bean
	public Binding notificationBinding() {
		return BindingBuilder.bind(notificationQueue())
				.to(notificationExchange())
				.with("notification.#");
	}

	@Bean
	public Binding notificationLikeBinding() {
		return BindingBuilder.bind(notificationLikeQueue())
				.to(notificationExchange())
				.with("notification.#");
	}
}
