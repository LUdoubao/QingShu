package org.doubao.fanout.service.service;


import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.fanout.service.mapper.FanoutFailRecordMapper;
import org.doubao.fanout.service.model.FanoutFailRecord;
import org.doubao.mall.common.entity.FanoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.time.Duration;

@Service
public class FailMessageService {

	private static final Logger log = LoggerFactory.getLogger(FailMessageService.class);
	@Resource
	private FanoutFailRecordMapper fanoutFailRecordMapper;
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Resource
	private MessageAssembler messageAssembler;

	@org.springframework.beans.factory.annotation.Value("${fanout.retry.max-times:3}")
	private int maxRetryTimes;

	@org.springframework.beans.factory.annotation.Value("${fanout.retry.interval:300000}")
	private long retryInterval;

	/**
	 * 记录粉丝消息分发失败
	 */
	@Transactional
	public void recordFailMessage(BusinessEvent event, Long followerId, String reason) {
		try {
			FanoutFailRecord record = new FanoutFailRecord();
			record.setEventId(event.getEventId());
			record.setEventType(event.getEventType().name());
			record.setActorId(event.getActorId());
			record.setFollowerId(followerId);
			record.setFailReason(reason);
			record.setEventContent(JSON.toJSONString(event));

			fanoutFailRecordMapper.insert(record);
			log.info("[recordFailMessage] Recorded failed message, eventId: {}, followerId: {}",
					event.getEventId(), followerId);
		} catch (Exception e) {
			log.error("[recordFailMessage] Error recording failed message, eventId: {}", event.getEventId(), e);
		}
	}

	/**
	 * 记录下游服务分发失败
	 */
	@Transactional
	public void recordDownstreamFail(BusinessEvent event, String reason) {
		// 下游服务失败，followerId为null
		recordFailMessage(event, null, reason);
	}

	/**
	 * 定时重试失败消息
	 */
	@Scheduled(fixedRateString = "${fanout.retry.interval:300000}")
	@Async("retryExecutor")
	public void retryFailedMessages() {
		log.info("[retryFailedMessages] Starting to retry failed messages");

		try {
			// 查询需要重试的记录：重试次数小于最大次数，且最后重试时间超过间隔或从未重试
			LocalDateTime retryTimeThreshold = LocalDateTime.now().minus(Duration.ofMillis(retryInterval));
			List<FanoutFailRecord> failRecords = fanoutFailRecordMapper
					.findByRetryCountLessThanAndLastRetryTimeBeforeOrLastRetryTimeIsNull(
							maxRetryTimes, retryTimeThreshold);

			log.info("[retryFailedMessages] Found {} failed messages to retry", failRecords.size());

			for (FanoutFailRecord record : failRecords) {
				retrySingleRecord(record);
			}
		} catch (Exception e) {
			log.error("[retryFailedMessages] Error during retry process", e);
		}
	}

	/**
	 * 重试单个失败记录
	 */
	@Transactional
	public void retrySingleRecord(FanoutFailRecord record) {
		try {
			log.info("[retrySingleRecord] Retrying message, id: {}, eventId: {}, retryCount: {}",
					record.getId(), record.getEventId(), record.getRetryCount());

			// 解析事件对象
			BusinessEvent event = JSON.parseObject(record.getEventContent(), BusinessEvent.class);
			if (event == null) {
				log.error("[retrySingleRecord] Failed to parse event content, id: {}", record.getId());
				return;
			}

			// 执行重试
			boolean success = false;
			if (record.getFollowerId() != null) {
				// 重试粉丝消息
				success = retryFanMessage(event, record.getFollowerId());
			} else {
				// 重试过下游服务消息
				success = retryDownstreamMessage(event);
			}

			// 更新记录
			record.setRetryCount(record.getRetryCount() + 1);
			record.setLastRetryTime(LocalDateTime.now());

			if (success) {
				log.info("[retrySingleRecord] Retry successful, id: {}, eventId: {}",
						record.getId(), record.getEventId());
			} else {
				log.warn("[retrySingleRecord] Retry still failed, id: {}, eventId: {}",
						record.getId(), record.getEventId());
			}

			fanoutFailRecordMapper.insert(record);
		} catch (Exception e) {
			log.error("[retrySingleRecord] Error retrying message, id: {}", record.getId(), e);
		}
	}

	/**
	 * 重试粉丝消息
	 */
	private boolean retryFanMessage(BusinessEvent event, Long followerId) {
		try {
			FanoutMessage message = messageAssembler.assemble(event, followerId);
			String queueName = "user.timeline.queue." + followerId;
			rabbitTemplate.convertAndSend(queueName, message);
			return true;
		} catch (Exception e) {
			log.error("[retryFanMessage] Failed to retry fan message, eventId: {}, followerId: {}",
					event.getEventId(), followerId, e);
			return false;
		}
	}

	/**
	 * 重试过下游服务消息
	 */
	private boolean retryDownstreamMessage(BusinessEvent event) {
		try {
			// 根据事件类型发送到对应的下游服务
			EventType eventType = event.getEventType();

			switch (eventType) {
				case DYNAMIC_PUBLISH:
				case DYNAMIC_DELETE:
					// 发送到动态存储服务
					rabbitTemplate.convertAndSend("dynamic.storage.queue", event);
					break;
				case USER_REGISTER:
					// 发送到通知服务
					rabbitTemplate.convertAndSend("notification.queue", event);
					break;
				default:
					// 默认发送到通知服务
					rabbitTemplate.convertAndSend("notification.queue", event);
			}

			return true;
		} catch (Exception e) {
			log.error("[retryDownstreamMessage] Failed to retry downstream message, eventId: {}",
					event.getEventId(), e);
			return false;
		}
	}

	/**
	 * 手动触发重试
	 */
	public void manualRetry(Long recordId) {
		try {
			FanoutFailRecord record = fanoutFailRecordMapper.selectById(recordId);
			if (record == null) {
				log.error("[manualRetry] Record not found, recordId: {}", recordId);
				throw new RuntimeException("Record not found");
			}

			retrySingleRecord(record);
		} catch (Exception e) {
			log.error("[manualRetry] Error manually retrying message, recordId: {}", recordId, e);
			throw new RuntimeException("Failed to manually retry message", e);
		}
	}
}
