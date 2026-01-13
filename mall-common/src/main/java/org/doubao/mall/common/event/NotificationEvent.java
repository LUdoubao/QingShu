package org.doubao.mall.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class NotificationEvent implements Serializable {
	private String action;         // 事件动作
	private String type;           // 事件类型
	private Long userId;           // 接收通知的用户ID
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime eventTime; // 事件创建时间

	protected NotificationEvent() {
		this.eventTime = LocalDateTime.now();
	}

	protected NotificationEvent(String type) {
		this();
		this.type = type;
	}
	protected NotificationEvent(String type, Long userId) {
		this(type);
		this.userId = userId;
	}

	protected NotificationEvent(String type, String action) {
		this(type);
		this.action = action;
	}
	protected NotificationEvent(String type, Long userId, String action) {
		this(type, action);
		this.userId = userId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	// Getters and Setters
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDateTime getEventTime() {
		return eventTime;
	}

	public void setEventTime(LocalDateTime eventTime) {
		this.eventTime = eventTime;
	}

	@Override
	public String toString() {
		return "NotificationEvent{" +
				"type='" + type + '\'' +
				", userId=" + userId +
				", eventTime=" + eventTime +
				'}';
	}
}