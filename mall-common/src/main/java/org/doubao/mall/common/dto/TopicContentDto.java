package org.doubao.mall.common.dto;

import java.util.List;

public class TopicContentDto {
	private List<Long> contentIds;
	private Long currentUserId;

	public List<Long> getContentIds() {
		return contentIds;
	}

	public void setContentIds(List<Long> contentIds) {
		this.contentIds = contentIds;
	}

	public Long getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(Long currentUserId) {
		this.currentUserId = currentUserId;
	}
}
