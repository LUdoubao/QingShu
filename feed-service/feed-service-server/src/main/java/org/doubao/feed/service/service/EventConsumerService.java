package org.doubao.feed.service.service;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.doubao.feed.service.feign.UserClient;
import org.doubao.feed.service.model.entity.EventTimeline;
import org.doubao.feed.service.model.entity.UserTimeline;
import org.doubao.mall.common.entity.BusinessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventConsumerService {

	private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);
	@Resource
	private UserClient userClient;
	@Resource
	private UserTimelineService userTimelineService;
	@Resource
	private EventTimelineService eventTimelineService;
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Resource
	private UnreadService unreadService;

	// 大V粉丝数阈值
	private static final long VIP_FOLLOWER_THRESHOLD = 10000;

	/**
	 * 消费动态事件
	 */
	@RabbitListener(queues = "dynamic.queue", ackMode = "MANUAL")
	@Transactional(rollbackFor = Exception.class)
	public void consumeDynamicEvent(@Payload String message, Message amqpMessage, Channel channel) throws IOException {
		try {
			log.info("[consumeDynamicEvent] 接收事件: {}", message);

			// 解析事件
			BusinessEvent event = JSON.parseObject(message, BusinessEvent.class);

			// 处理事件
			handleEvent(event);

			// 手动确认消息
			channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
		} catch (Exception e) {
			log.error("[consumeDynamicEvent] 处理事件失败", e);
			// 处理失败，拒绝消息并使其进入死信队列
			channel.basicReject(amqpMessage.getMessageProperties().getDeliveryTag(), false);
		}
	}

	/**
	 * 处理业务事件
	 */
	private void handleEvent(BusinessEvent event) {
		Long actorId = event.getActorId();
		if (actorId == null) {
			log.error("[handleEvent] 事件发起者ID为空");
			return;
		}

		// 获取发起者的粉丝数
		Integer followerCount = userClient.getFollowerCount(actorId).getData();
		if (followerCount == null) {
			log.error("[handleEvent] 获取粉丝数失败，actorId: {}", actorId);
			return;
		}

		// 根据粉丝数判断是普通用户还是大V
		if (followerCount <= VIP_FOLLOWER_THRESHOLD) {
			// 普通用户：推模式，写入粉丝的timeline
			handleNormalUserEvent(event, actorId);
		} else {
			// 大V用户：拉模式，写入自己的事件池
			handleVipUserEvent(event, actorId);
		}
	}

	/**
	 * 处理普通用户事件
	 */
	private void handleNormalUserEvent(BusinessEvent event, Long actorId) {
		// 获取该用户的所有粉丝
		List<Long> followers = userClient.getFollowers(actorId).getData();
		if (followers.isEmpty()) {
			log.info("[handleNormalUserEvent] 创作者无粉丝，无需处理，actorId: {}", actorId);
			return;
		}

		// 构建用户时间线列表
		List<UserTimeline> timelines = new ArrayList<>();
		for (Long followerId : followers) {
			UserTimeline timeline = buildUserTimeline(event, followerId, actorId);
			timelines.add(timeline);
		}

		// 批量插入
		userTimelineService.batchInsert(timelines);

		// 发送动态聚合事件
		// sendAggregateEvent(followers);

		// 更新粉丝的未读数量
		followers.forEach(unreadService::incrementUnreadCount);
	}

	/**
	 * 处理大V用户事件
	 */
	private void handleVipUserEvent(BusinessEvent event, Long actorId) {
		// 构建大V事件
		EventTimeline timeline = buildEventTimeline(event, actorId);

		// 插入数据库
		eventTimelineService.save(timeline);
	}

	/**
	 * 构建UserTimeline对象
	 */
	private UserTimeline buildUserTimeline(BusinessEvent event, Long userId, Long actorId) {
		UserTimeline timeline = new UserTimeline();
		timeline.setUserId(userId);
		timeline.setEventId(event.getEventId() == null ? UUID.randomUUID().toString() : event.getEventId());
		timeline.setActorId(actorId);
		timeline.setEventType(event.getEventType().getValue());
		timeline.setTargetId(event.getTargetId());
		timeline.setTargetType(event.getExtInfo().get("dynamicType").toString());
		timeline.setEventTime(LocalDateTime.now());
		timeline.setDeleted(0);
		return timeline;
	}

	/**
	 * 构建EventTimeline对象
	 */
	private EventTimeline buildEventTimeline(BusinessEvent event, Long actorId) {
		EventTimeline timeline = new EventTimeline();
		timeline.setActorId(actorId);
		timeline.setEventId(event.getEventId() == null ? UUID.randomUUID().toString() : event.getEventId());
		timeline.setEventType(event.getEventType().getValue());
		timeline.setTargetId(event.getTargetId());
		timeline.setTargetType(event.getExtInfo().get("targetType").toString());
		timeline.setEventTime(LocalDateTime.now());
		timeline.setDeleted(0);
		return timeline;
	}

	/**
	 * 发送动态聚合事件
	 */
	private void sendAggregateEvent(List<Long> userIds) {
		for (Long userId : userIds) {
			rabbitTemplate.convertAndSend(
					"FEED_PROCESS_EXCHANGE",
					"feed.aggregate.user",
					userId.toString()
			);
		}
	}

	// 内部类：缓存更新事件
	private static class CacheUpdateEvent {
		private String key;
		private String eventId;
		private LocalDateTime eventTime;

		public CacheUpdateEvent(String key, String eventId, LocalDateTime eventTime) {
			this.key = key;
			this.eventId = eventId;
			this.eventTime = eventTime;
		}

		// getter and setter
		public String getKey() { return key; }
		public void setKey(String key) { this.key = key; }
		public String getEventId() { return eventId; }
		public void setEventId(String eventId) { this.eventId = eventId; }
		public LocalDateTime getEventTime() { return eventTime; }
		public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
	}
}