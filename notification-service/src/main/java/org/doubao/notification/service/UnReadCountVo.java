package org.doubao.notification.service;

public class UnReadCountVo {
	private Long likeCount;
	private Long commentCount;
	private Long systemCount;

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}

	public Long getSystemCount() {
		return systemCount;
	}

	public void setSystemCount(Long systemCount) {
		this.systemCount = systemCount;
	}
}
