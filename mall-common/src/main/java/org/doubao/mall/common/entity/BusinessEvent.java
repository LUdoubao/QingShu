package org.doubao.mall.common.entity;



import org.doubao.mall.common.enums.EventType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BusinessEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	// 事件唯一ID
	private String eventId;

	// 事件类型
	private EventType eventType;

	// 操作人ID
	private Long actorId;

	// 目标ID
	private Long targetId;

	// 扩展信息
	private Map<String, Object> extInfo = new HashMap<>();

	// 事件发生时间戳
	private Long timestamp;


	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public Map<String, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<String, Object> extInfo) {
		this.extInfo = extInfo;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
