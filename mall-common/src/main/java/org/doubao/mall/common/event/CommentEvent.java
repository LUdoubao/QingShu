package org.doubao.mall.common.event;

public class CommentEvent extends NotificationEvent{
	private boolean isComment;
	private String quoteId;
	private String commentContent;
	private String content;
	private Long operatorUserId;
	private String operatorUserName;

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public boolean isComment() {
		return isComment;
	}

	public void setComment(boolean comment) {
		isComment = comment;
	}

	public String getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
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

	public CommentEvent(Long userId, boolean isComment, String quoteId, String content,
						String commentContent,
						Long operatorUserId, String operatorUserName) {
		super("COMMENT", userId);
		this.isComment = isComment;
		this.quoteId = quoteId;
		this.content = content;
		this.commentContent = commentContent;
		this.operatorUserId = operatorUserId;
		this.operatorUserName = operatorUserName;
	}
}
