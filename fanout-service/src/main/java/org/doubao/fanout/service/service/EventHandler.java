package org.doubao.fanout.service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.doubao.mall.common.entity.BusinessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EventHandler {

	private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
	@Resource
	private FanoutDispatcher fanoutDispatcher;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	// 事件计数器
	private final Counter eventReceivedCounter;

	@Value("${fanout.cache.redis.processed-key-prefix:fanout:processed:}")
	private String processedKeyPrefix;

	@Value("${fanout.event.processed-ttl:86400}") // 24小时
	private long processedTtl;

	public EventHandler(MeterRegistry meterRegistry) {
		this.eventReceivedCounter = meterRegistry.counter("fanout.events.received");
	}

	/**
	 * 处理接收到的事件
	 */
	@Async("eventExecutor")
	public void handle(BusinessEvent event) {
		String eventId = event.getEventId();
		log.info("[handle] Processing event, eventId: {}, eventType: {}", eventId, event.getEventType());

		try {
			// 记录事件已处理（用于去重）
			recordProcessedEvent(eventId);

			// 增加事件接收计数
			eventReceivedCounter.increment();

			// 执行分发
			fanoutDispatcher.dispatch(event);

			log.info("[handle] Completed processing event, eventId: {}", eventId);
		} catch (Exception e) {
			log.error("[handle] Error processing event, eventId: {}", eventId, e);
		}
	}

	/**
	 * 记录已处理的事件，用于去重
	 */
	private void recordProcessedEvent(String eventId) {
		try {
			String processedKey = processedKeyPrefix + eventId;
			redisTemplate.opsForValue().set(processedKey, "1", processedTtl, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("[recordProcessedEvent] Error recording processed event, eventId: {}", eventId, e);
			// 记录失败不影响主流程，仅日志告警
		}
	}
}
