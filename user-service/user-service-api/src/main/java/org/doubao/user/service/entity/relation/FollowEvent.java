package org.doubao.user.service.entity.relation;

/**
 * 关注事件实体（用于RabbitMQ消息）
 */
public class FollowEvent {
	private Long userId; // 操作方用户ID
	private Long targetUserId; // 目标用户ID
	private long timestamp; // 事件时间戳

	public FollowEvent() {
	}

	public FollowEvent(Long userId, Long targetUserId, long timestamp) {
		this.userId = userId;
		this.targetUserId = targetUserId;
		this.timestamp = timestamp;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}