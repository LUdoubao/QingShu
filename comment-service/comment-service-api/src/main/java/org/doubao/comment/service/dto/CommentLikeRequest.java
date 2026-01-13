package org.doubao.comment.service.dto;

/**
 * 评论服务自定义的点赞请求数据传输对象
 * <p>
 * 用于封装评论点赞/取消点赞操作的请求参数
 * 该类是对点赞服务ToggleLikeRequest的适配，避免直接依赖
 */
public class CommentLikeRequest {
	/**
	 * 执行点赞操作的用户ID
	 * <p>
	 * 表示发起点赞或取消点赞操作的用户唯一标识符
	 */
	private Long operatorUserId;
	/**
	 * 实体类型
	 * <p>
	 * 表示被点赞实体的类型，1表示评论类型
	 */
	private int entityType;
	/**
	 * 实体ID
	 * <p>
	 * 表示被点赞评论的唯一标识符
	 */
	private String entityId;
	/**
	 * 被点赞的评论作者ID
	 * <p>
	 * 评论所属作者的用户ID，用于通知和统计
	 */
	private Long userId;
	/**
	 * 评论内容
	 * <p>
	 * 被点赞评论的文本内容，主要用于发送通知时使用
	 */
	private String content;

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