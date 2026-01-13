package org.doubao.notification.service.dto;

public class UnreadCountDTO {
	private Integer unreadCount;
	private Integer commentCount;
	private Integer likeCount;
	private Integer SystemCount;
	private Integer chatCount;

	public UnreadCountDTO() {
	}

	public UnreadCountDTO(Integer count) {
		this.unreadCount = count;
	}

	public Integer getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(Integer unreadCount) {
		this.unreadCount = unreadCount;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public Integer getSystemCount() {
		return SystemCount;
	}

	public void setSystemCount(Integer systemCount) {
		SystemCount = systemCount;
	}

	public Integer getChatCount() {
		return chatCount;
	}

	public void setChatCount(Integer chatCount) {
		this.chatCount = chatCount;
	}
}