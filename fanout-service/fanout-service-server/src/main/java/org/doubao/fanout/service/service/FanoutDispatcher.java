package org.doubao.fanout.service.service;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.fanout.service.enums.TargetType;
import org.doubao.fanout.service.model.FanoutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FanoutDispatcher {

	private static final Logger log = LoggerFactory.getLogger(FanoutDispatcher.class);
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Resource
	private MessageAssembler messageAssembler;
	@Resource
	private FailMessageService failMessageService;
	@Resource
	private RuleService ruleService;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	// 指标计数器
	private final Counter successCounter;
	private final Counter failCounter;

	@org.springframework.beans.factory.annotation.Value("${fanout.cache.redis.mute-key-prefix:user:mute:}")
	private String muteKeyPrefix;

	public FanoutDispatcher(MeterRegistry meterRegistry) {
		// 初始化指标
		this.successCounter = meterRegistry.counter("fanout.success");
		this.failCounter = meterRegistry.counter("fanout.fail");
	}

	/**
	 * 异步执行事件分发
	 */
	@Async("fanoutExecutor")
	public void dispatch(BusinessEvent event) {
		String eventId = event.getEventId();
		EventType eventType = event.getEventType();
		log.info("[dispatch] Starting to dispatch event, eventId: {}, eventType: {}", eventId, eventType);

		try {
			// 获取分发规则
			FanoutRule rule = ruleService.getRuleByEventType(eventType);
			if (rule == null || CollectionUtils.isEmpty(rule.getTargets())) {
				log.info("[dispatch] No fanout rule found for event, eventId: {}, eventType: {}", eventId, eventType);
				return;
			}

			// 处理粉丝相关事件
			if (isFanRelatedEvent(eventType)) {
				dispatchToFans(event, rule.getTargets());
			} else {
				// 处理非粉丝相关事件
				dispatchToDownstream(event, rule.getTargets());
			}
			log.info("[dispatch] Completed dispatching event, eventId: {}", eventId);
		} catch (Exception e) {
			log.error("[dispatch] Error dispatching event, eventId: {}", eventId, e);
			failCounter.increment();
			failMessageService.recordDownstreamFail(event, "Failed to dispatch event: " + e.getMessage());
		}
	}

	/**
	 * 判断是否为粉丝相关事件
	 */
	private boolean isFanRelatedEvent(EventType eventType) {
		switch (eventType) {
			case DYNAMIC_PUBLISH:
			case DYNAMIC_TOP_LIST:
			case DYNAMIC_DELETE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * 分发给粉丝
	 */
	private void dispatchToFans(BusinessEvent event, List<FanoutRule.Target> targets) {
		String eventId = event.getEventId();
		// 分发给粉丝
		try {
			// 组装消息
			for (FanoutRule.Target target : targets) {
				if (TargetType.QUEUE.equals(target.getType())) {
					// 发送到指定队列
					String routingKey = target.getRoutingKey() != null ? target.getRoutingKey() : "";
					rabbitTemplate.convertAndSend(target.getName(), routingKey, event);
					log.info("[dispatchToFans] Sent to exchange : {}, eventId: {}", target.getName(), eventId);
					successCounter.increment();
				} else {
					log.info("[dispatchToFans] Unsupported target type : {}, eventId: {}",
							target.getType(), eventId);
				}
			}
			successCounter.increment();
			log.trace("[dispatchToFans] Sent to follower, eventId: {}", eventId);
		} catch (Exception e) {
			log.error("[dispatchToFans] Failed to send to follower, eventId: {}", eventId, e);
			failCounter.increment();
			failMessageService.recordFailMessage(event, null, e.getMessage());
		}
	}

	/**
	 * 检查用户是否屏蔽了创作者
	 */
	private boolean isUserMuted(Long userId, Long targetId) {
		try {
			String muteKey = muteKeyPrefix + userId;
			return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(muteKey, targetId));
		} catch (Exception e) {
			log.error("[isUserMuted] Error checking mute status, userId: {}, targetId: {}", userId, targetId, e);
			// 异常情况下默认不屏蔽，避免误判
			return false;
		}
	}

	/**
	 * 分发给下游服务
	 */
	private void dispatchToDownstream(BusinessEvent event, List<FanoutRule.Target> targets) {
		String eventId = event.getEventId();

		try {
			for (FanoutRule.Target target : targets) {
				try {
					if (TargetType.QUEUE.equals(target.getType())) {
						// 发送到指定队列
						String routingKey = target.getRoutingKey() != null ? target.getRoutingKey() : "";
						rabbitTemplate.convertAndSend(target.getName(), routingKey, event);
						log.info("[dispatchToDownstream] Sent to exchange: {}, eventId: {}", target.getName(), eventId);
						successCounter.increment();
					} else {
						log.info("[dispatchToDownstream] Unsupported target type: {}, eventId: {}",
								target.getType(), eventId);
					}
				} catch (Exception e) {
					log.error("[dispatchToDownstream] Failed to send to target: {}, eventId: {}",
							target.getName(), eventId, e);
					failCounter.increment();
					failMessageService.recordDownstreamFail(event,
							"Failed to send to target " + target.getName() + ": " + e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("[dispatchToDownstream] Error dispatching to downstream services, eventId: {}", eventId, e);
			failCounter.increment();
		}
	}
}
