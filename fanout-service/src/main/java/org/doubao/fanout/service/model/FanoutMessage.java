package org.doubao.fanout.service.model;


import org.doubao.mall.common.enums.EventType;

import java.io.Serializable;
import java.util.Map;

public class FanoutMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	// 消息唯一ID
	private String messageId;

	// 关联的事件ID
	private String eventId;

	// 目标粉丝ID
	private Long followerId;

	// 操作人ID
	private Long actorId;

	// 操作人名称
	private String actorName;

	// 事件类型
	private EventType eventType;

	// 目标ID
	private Long targetId;

	// 消息内容
	private Map<String, Object> content;

	// 消息创建时间
	private Long createdTime;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public Long getFollowerId() {
		return followerId;
	}

	public void setFollowerId(Long followerId) {
		this.followerId = followerId;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public Long getCreatedTime() {
		return createdTime;
	}

	public void setCreateTime(Long createdTime) {
		this.createdTime = createdTime;
	}
}
