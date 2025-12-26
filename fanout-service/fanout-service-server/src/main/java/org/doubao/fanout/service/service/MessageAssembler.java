package org.doubao.fanout.service.service;

import org.doubao.mall.common.entity.FanoutMessage;
import org.doubao.mall.common.entity.BusinessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageAssembler {

	private static final Logger log = LoggerFactory.getLogger(MessageAssembler.class);
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${fanout.cache.redis.user-info-prefix:user:info:}")
	private String userInfoPrefix;

	/**
	 * 组装粉丝动态消息
	 */
	public FanoutMessage assemble(BusinessEvent event, Long followerId) {
		FanoutMessage message = new FanoutMessage();

		// 基本信息
		message.setMessageId(generateMessageId());
		message.setEventId(event.getEventId());
		message.setFollowerId(followerId);
		message.setActorId(event.getActorId());
		message.setEventType(event.getEventType());
		message.setTargetId(event.getTargetId());
		message.setCreateTime(System.currentTimeMillis());


		message.setContent(assembleContent(event));

		return message;
	}

	/**
	 * 生成消息ID
	 */
	private String generateMessageId() {
		return "msg_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
	}


	/**
	 * 根据事件类型组装内容
	 */
	private Map<String, Object> assembleContent(BusinessEvent event) {
		Map<String, Object> content = new HashMap<>();

		switch (event.getEventType()) {
			case DYNAMIC_PUBLISH:
				assembleDynamicPublishContent(event, content);
				break;
			case DYNAMIC_TOP_LIST:
				assembleDynamicToplistContent(event, content);
				break;
			case DYNAMIC_DELETE:
				assembleDynamicDeleteContent(event, content);
				break;
			case LIKE_CANCEL:
				assembleLikeCancelContent(event, content);
				break;
			default:
				content.putAll(event.getExtInfo());
				log.warn("[assembleContent] No specific content assembler for event type: {}", event.getEventType());
		}

		return content;
	}

	private void assembleDynamicPublishContent(BusinessEvent event, Map<String, Object> content) {
		content.put("title", event.getExtInfo().getOrDefault("dynamicTitle", ""));
		content.put("content", event.getExtInfo().getOrDefault("dynamicContent", ""));
		content.put("publishTime", event.getTimestamp());
	}

	private void assembleDynamicToplistContent(BusinessEvent event, Map<String, Object> content) {
		content.put("dynamicId", event.getTargetId());
		content.put("toplistName", event.getExtInfo().getOrDefault("toplistName", ""));
		content.put("rank", event.getExtInfo().getOrDefault("rank", 0));
	}

	private void assembleDynamicDeleteContent(BusinessEvent event, Map<String, Object> content) {
		content.put("dynamicId", event.getTargetId());
		content.put("deleteTime", event.getTimestamp());
	}

	private void assembleLikeCancelContent(BusinessEvent event, Map<String, Object> content) {
		content.put("dynamicId", event.getTargetId());
		content.put("cancelTime", event.getTimestamp());
	}
}
