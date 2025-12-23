package org.doubao.comment.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

// CommentDTO.java
public class CommentDTO {
	private String content;
	private String postId;
	private String parentId;
	private Long repliedUserId;

	public Long getRepliedUserId() {
		return repliedUserId;
	}

	public void setRepliedUserId(Long repliedUserId) {
		this.repliedUserId = repliedUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	@JsonIgnore
	public boolean isReply() {
		return StringUtils.hasText(parentId);
	}
}
