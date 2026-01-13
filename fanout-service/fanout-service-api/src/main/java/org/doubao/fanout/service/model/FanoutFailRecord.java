package org.doubao.fanout.service.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("fanout_fail_record")
public class FanoutFailRecord {

	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	// 事件ID
	@TableField(value = "event_id")
	private String eventId;

	// 事件类型
	@TableField(value = "event_type")
	private String eventType;

	// 操作人ID
	@TableField(value = "actor_id")
	private Long actorId;

	// 粉丝ID，可为null
	@TableField(value = "follower_id")
	private Long followerId;

	// 失败原因
	@TableField(value = "fail_reason")
	private String failReason;

	// 记录创建时间
	@TableField(value = "created_time")
	private LocalDateTime createdTime;

	// 已重试次数
	@TableField(value = "retry_count")
	private Integer retryCount = 0;

	// 最后重试时间
	@TableField(value = "last_retry_time")
	private LocalDateTime lastRetryTime;

	// 事件内容JSON
	@TableField(value = "event_content")
	private String eventContent;

	public void prePersist() {
		if (createdTime == null) {
			createdTime = LocalDateTime.now();
		}
		if (retryCount == null) {
			retryCount = 0;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public Long getFollowerId() {
		return followerId;
	}

	public void setFollowerId(Long followerId) {
		this.followerId = followerId;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public LocalDateTime getLastRetryTime() {
		return lastRetryTime;
	}

	public void setLastRetryTime(LocalDateTime lastRetryTime) {
		this.lastRetryTime = lastRetryTime;
	}

	public String getEventContent() {
		return eventContent;
	}

	public void setEventContent(String eventContent) {
		this.eventContent = eventContent;
	}
}
