package org.doubao.share.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShareRabbitMQConfig {

	// 交换机名称
	public static final String SHARE_EXCHANGE = "share.exchange";
	public static final String VIEW_COUNT_EXCHANGE = "view.count.exchange";

	// 队列名称
	public static final String ACCESS_RECORD_QUEUE = "share.access.record.queue";
	public static final String VIEW_COUNT_QUEUE = "view.count.increase.queue";

	// 路由键
	public static final String ACCESS_RECORD_ROUTING_KEY = "share.access.record";
	public static final String VIEW_COUNT_ROUTING_KEY = "view.count.share";

	// 声明交换机
	@Bean
	public DirectExchange shareExchange() {
		return new DirectExchange(SHARE_EXCHANGE, true, false);
	}

	@Bean
	public DirectExchange viewCountExchange() {
		return new DirectExchange(VIEW_COUNT_EXCHANGE, true, false);
	}

	// 声明队列
	@Bean
	public Queue accessRecordQueue() {
		return new Queue(ACCESS_RECORD_QUEUE, true, false, false);
	}

	@Bean
	public Queue viewCountQueue() {
		return new Queue(VIEW_COUNT_QUEUE, true, false, false);
	}

	// 绑定队列和交换机
	@Bean
	public Binding accessRecordBinding() {
		return BindingBuilder.bind(accessRecordQueue()).to(shareExchange()).with(ACCESS_RECORD_ROUTING_KEY);
	}

	@Bean
	public Binding viewCountBinding() {
		return BindingBuilder.bind(viewCountQueue()).to(viewCountExchange()).with(VIEW_COUNT_ROUTING_KEY);
	}
}
