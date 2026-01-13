package org.doubao.like.service.dto;

import java.util.List;

public class LikeQueryDto {
	private String userId;
	private List<String> commentId;
	public LikeQueryDto(String userId, List<String> commentId) {
		this.userId = userId;
		this.commentId = commentId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getCommentId() {
		return commentId;
	}

	public void setCommentId(List<String> commentId) {
		this.commentId = commentId;
	}
}