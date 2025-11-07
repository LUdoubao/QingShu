package org.doubao.quote.service.dto;

public class QueryDataPageDto {
	private Integer page;
	private Integer size;
	private Integer original;
	private String quoteKeyword = null;
	private int status;

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

	public Integer getOriginal() {
		return original;
	}

	public void setOriginal(Integer original) {
		this.original = original;
	}

	public String getQuoteKeyword() {
		return quoteKeyword;
	}

	public void setQuoteKeyword(String quoteKeyword) {
		this.quoteKeyword = quoteKeyword;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
