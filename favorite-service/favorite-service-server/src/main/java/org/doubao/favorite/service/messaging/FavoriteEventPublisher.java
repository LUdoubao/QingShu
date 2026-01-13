package org.doubao.favorite.service.messaging;

import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FavoriteEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(FavoriteEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String FAVORITE_EXCHANGE = "favorite-exchange";

	public void publishFavoriteEvent(Long userId, Long quoteId) {
	}
}