package org.doubao.favorite.service.dto;

import java.util.List;

public class BatchDelDto {
	private Long folderId;
	private List<Long> quoteIds;
	private Long userId;

	public Long getFolderId() {
		return folderId;
	}

	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}

	public List<Long> getQuoteIds() {
		return quoteIds;
	}

	public void setQuoteIds(List<Long> quoteIds) {
		this.quoteIds = quoteIds;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}