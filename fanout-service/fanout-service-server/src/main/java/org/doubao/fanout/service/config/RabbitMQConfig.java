package org.doubao.fanout.service.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
	// 交换机名称
	public static final String FANOUT_EVENT_EXCHANGE = "fanout.event.exchange";
	public static final String FANOUT_FAIL_EXCHANGE = "fanout.fail.exchange";
	public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
	public static final String DYNAMIC_EXCHANGE = "dynamic.exchange";


	// 队列名称
	public static final String FANOUT_EVENT_QUEUE = "fanout.event.queue";
	public static final String FANOUT_FAIL_QUEUE = "fanout.fail.queue";
	public static final String DYNAMIC_QUEUE = "dynamic.queue";
	public static final String NOTIFICATION_QUEUE = "notification.queue";

	// 死信队列
	public static final String FANOUT_DLQ_QUEUE = "fanout.dlq.queue";
	public static final String FANOUT_DLQ_EXCHANGE = "fanout.dlq.exchange";

	/**
	 * 声明事件交换机
	 */
	@Bean
	public TopicExchange  fanoutEventExchange() {
		return ExchangeBuilder.topicExchange(FANOUT_EVENT_EXCHANGE)
				.durable(true)
				.build();
	}

	/**
	 * 声明失败交换机
	 */
	@Bean
	public DirectExchange fanoutFailExchange() {
		return ExchangeBuilder.directExchange(FANOUT_FAIL_EXCHANGE)
				.durable(true)
				.build();
	}

	/**
	 * 声明死信交换机
	 */
	@Bean
	public TopicExchange  fanoutDlqExchange() {
		return ExchangeBuilder.topicExchange(FANOUT_DLQ_EXCHANGE)
				.durable(true)
				.build();
	}

	/**
	 * 声明事件处理队列
	 */
	@Bean
	public Queue fanoutEventQueue() {
		return QueueBuilder.durable(FANOUT_EVENT_QUEUE)
				.withArgument("x-dead-letter-exchange", FANOUT_DLQ_EXCHANGE)
				.withArgument("x-dead-letter-routing-key", "dlq.key.event")
				.withArgument("x-message-ttl", 60000) // 1分钟过期
				.build();
	}

	/**
	 * 声明失败队列
	 */
	@Bean
	public Queue fanoutFailQueue() {
		return QueueBuilder.durable(FANOUT_FAIL_QUEUE)
				.withArgument("x-dead-letter-exchange", FANOUT_DLQ_EXCHANGE)
				.withArgument("x-dead-letter-routing-key", "dlq.key.fail")
				.build();
	}

	/**
	 * 声明死信队列
	 */
	@Bean
	public Queue fanoutDlqQueue() {
		return QueueBuilder.durable(FANOUT_DLQ_QUEUE)
				.build();
	}

	/**
	 * 动态存储服务队列
	 */
	@Bean
	public Queue dynamicQueue() {
		return QueueBuilder.durable(DYNAMIC_QUEUE)
				.build();
	}
	@Bean
	public TopicExchange notificationExchange() {
		return new TopicExchange(NOTIFICATION_EXCHANGE);
	}
	@Bean
	public TopicExchange dynamicExchange() {
		return new TopicExchange(DYNAMIC_EXCHANGE);
	}
	/**
	 * 通知服务队列
	 */
	@Bean
	public Queue notificationQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE)
				.build();
	}

	@Bean
	public Binding notificationBinding() {
		return BindingBuilder.bind(notificationQueue())
				.to(notificationExchange())
				.with("notification.#");
	}

	@Bean
	public Binding dynamicBinding() {
		return BindingBuilder.bind(dynamicQueue())
				.to(dynamicExchange())
				.with("dynamic.#");
	}

	/**
	 * 绑定事件队列到事件交换机
	 */
	@Bean
	public Binding fanoutEventBinding() {
		return BindingBuilder.bind(fanoutEventQueue())
				.to(fanoutEventExchange())
				.with("event.key.#"); // 匹配所有事件类型
	}

	/**
	 * 绑定失败队列到失败交换机
	 */
	@Bean
	public Binding fanoutFailBinding() {
		return BindingBuilder.bind(fanoutFailQueue())
				.to(fanoutFailExchange())
				.with("fail.key");
	}

	/**
	 * 绑定死信队列到死信交换机
	 */
	@Bean
	public Binding fanoutDlqBinding() {
		return BindingBuilder.bind(fanoutDlqQueue())
				.to(fanoutDlqExchange())
				.with("dlq.key.#"); // 匹配所有死信路由键
	}

	/**
	 * 消息转换器，使用fastJson序列化
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 配置RabbitTemplate
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter());

		// 消息发送确认
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack) {
				// 处理消息发送失败的情况
				logger.error("消息发送失败: {}", cause);
			}
		});

		// 消息返回处理
		rabbitTemplate.setReturnsCallback(returnedMessage -> {
			// 处理消息无法路由的情况
			logger.error("消息返回: {}", returnedMessage);
		});

		return rabbitTemplate;
	}
}
