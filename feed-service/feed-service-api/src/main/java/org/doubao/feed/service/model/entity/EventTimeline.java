package org.doubao.feed.service.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

@TableName("event_timeline")
public class EventTimeline extends BaseDel {

	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 事件发起者ID（大V用户）
	 */
	@TableField("actor_id")
	private Long actorId;

	/**
	 * 事件ID
	 */
	@TableField("event_id")
	private String eventId;

	/**
	 * 事件类型
	 */
	@TableField("event_type")
	private Integer eventType;

	/**
	 * 目标ID
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

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
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