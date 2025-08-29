package org.doubao.mall.common.event;

public class LikeEvent extends NotificationEvent{
	private int entityType;
	private String entityId;
	private boolean like;
	private String content;
	private Long operatorUserId;
	private String operatorUserName;


	public LikeEvent(Long userId, int entityType, String entityId, boolean like, String content, Long operatorUserId, String operatorUserName) {
		super("LIKE", userId);
		this.entityType = entityType;
		this.entityId = entityId;
		this.like = like;
		this.content = content;
		this.operatorUserId = operatorUserId;
		this.operatorUserName = operatorUserName;
	}

	public String getOperatorUserName() {
		return operatorUserName;
	}

	public void setOperatorUserName(String operatorUserName) {
		this.operatorUserName = operatorUserName;
	}

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public boolean isLike() {
		return like;
	}

	public void setLike(boolean like) {
		this.like = like;
	}
}
