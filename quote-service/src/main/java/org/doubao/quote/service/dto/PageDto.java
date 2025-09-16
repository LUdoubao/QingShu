package org.doubao.quote.service.dto;

import java.util.List;

public class PageDto {
	private Integer page;
	private Integer size;
	private Long categoryId;
	private List<Long> tagIds;
	private Long userId;
	private Integer original;
	private String quoteKeyword;

	public String getQuoteKeyword() {
		return quoteKeyword;
	}

	public void setQuoteKeyword(String quoteKeyword) {
		this.quoteKeyword = quoteKeyword;
	}

	public Integer getOriginal() {
		return original;
	}

	public void setOriginal(Integer original) {
		this.original = original;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<Long> getTagIds() {
		return tagIds;
	}

	public void setTagIds(List<Long> tagIds) {
		this.tagIds = tagIds;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
}
