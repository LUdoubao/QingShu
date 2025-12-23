package org.doubao.comment.service.dto;

// 评论服务自定义的点赞请求DTO（避免直接依赖点赞服务的ToggleLikeRequest）
public class CommentLikeRequest {
	private Long operatorUserId;  // 执行点赞的用户ID
	private int entityType;      // 实体类型：1表示评论
	private String entityId;       // 评论ID
	private Long userId;         // 被点赞的评论作者ID
	private String content;      // 可选：评论内容（用于通知）

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}