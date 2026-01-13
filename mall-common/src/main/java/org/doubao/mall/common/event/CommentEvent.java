package org.doubao.mall.common.event;

public class CommentEvent extends NotificationEvent{
	/**
	 * 引文或评论 quote/comment
	 */
	private String target;
	/**
	 * 目标id quoteId/commentId
	 */
	private String targetId;
	/**
	 * 0：评论 1：回复
	 */
	private int commentType;
	/**
	 * 评论内容
	 */
	private String commentContent;
	/**
	 * 原引文内容
	 */
	private String content;
	/**
	 * 回复内容
	 */
	private String replyContent;
	private Long operatorUserId;
	private String operatorUserName;
	private String operatorUserAvatar;

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public int getCommentType() {
		return commentType;
	}

	public void setCommentType(int commentType) {
		this.commentType = commentType;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}

	public String getOperatorUserAvatar() {
		return operatorUserAvatar;
	}

	public void setOperatorUserAvatar(String operatorUserAvatar) {
		this.operatorUserAvatar = operatorUserAvatar;
	}

	public CommentEvent(String action ,Long userId, String target, String targetId, int commentType,
						String content, String commentContent, String replyContent,
						Long operatorUserId, String operatorUserName, String operatorUserAvatar) {
		super("COMMENT", userId,  action);
		this.target = target;
		this.targetId = targetId;
		this.commentType = commentType;
		this.replyContent = replyContent;
		this.operatorUserAvatar = operatorUserAvatar;
		this.content = content;
		this.commentContent = commentContent;
		this.operatorUserId = operatorUserId;
		this.operatorUserName = operatorUserName;
	}
}
