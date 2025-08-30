package org.doubao.feed.service.model.entity;


import com.baomidou.mybatisplus.annotation.*;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

@TableName("user_timeline")
public class UserTimeline extends BaseDel {

	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户ID（动态接收者）
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 事件ID
	 */
	@TableField("event_id")
	private String eventId;

	/**
	 * 事件发起者ID
	 */
	@TableField("actor_id")
	private Long actorId;

	/**
	 * 事件类型：1-发布动态，2-上榜单，3-删除动态，4-点赞，5-取消点赞
	 */
	@TableField("event_type")
	private Integer eventType;

	/**
	 * 目标ID（如动态ID）
	 */
	@TableField("target_id")
	private Long targetId;

	/**
	 * 目标类型
	 */
	@TableField("target_type")
	private String targetType;

	/**
	 * 事件发生时间
	 */
	@TableField("event_time")
	private LocalDateTime eventTime;

	/**
	 * 动态权重（用于排序）
	 */
	@TableField("weight")
	private Double weight;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public LocalDateTime getEventTime() {
		return eventTime;
	}

	public void setEventTime(LocalDateTime eventTime) {
		this.eventTime = eventTime;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
}
