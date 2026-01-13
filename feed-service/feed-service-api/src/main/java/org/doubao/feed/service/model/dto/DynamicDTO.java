package org.doubao.feed.service.model.dto;



import java.time.LocalDateTime;

public class DynamicDTO {
	/**
	 * 动态ID
	 */
	private Long id;

	/**
	 * 事件ID
	 */
	private String eventId;

	/**
	 * 事件发起者ID
	 */
	private Long actorId;

	/**
	 * 发起者名称
	 */
	private String actorName;

	/**
	 * 发起者头像
	 */
	private String actorAvatar;

	/**
	 * 事件类型
	 */
	private Integer eventType;

	/**
	 * 事件类型名称
	 */
	private String eventTypeName;

	/**
	 * 目标ID
	 */
	private Long targetId;

	/**
	 * 目标类型
	 */
	private String targetType;

	/**
	 * 内容摘要
	 */
	private String contentSummary;

	/**
	 * 内容链接
	 */
	private String contentUrl;

	/**
	 * 事件发生时间
	 */
	private LocalDateTime eventTime;

	/**
	 * 权重
	 */
	private Double weight;

	/**
	 * 点赞数
	 */
	private Integer likeCount;

	/**
	 * 是否已点赞
	 */
	private Boolean liked;

	/**
	 * 评论数
	 */
	private Integer commentCount;

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

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public String getActorAvatar() {
		return actorAvatar;
	}

	public void setActorAvatar(String actorAvatar) {
		this.actorAvatar = actorAvatar;
	}

	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	public String getEventTypeName() {
		return eventTypeName;
	}

	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
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

	public String getContentSummary() {
		return contentSummary;
	}

	public void setContentSummary(String contentSummary) {
		this.contentSummary = contentSummary;
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

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
}