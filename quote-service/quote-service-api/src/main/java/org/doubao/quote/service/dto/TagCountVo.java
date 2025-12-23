package org.doubao.quote.service.dto;

public class TagCountVo {
	private Long id;
	private String name;
	private Long quoteCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getQuoteCount() {
		return quoteCount;
	}

	public void setQuoteCount(Long quoteCount) {
		this.quoteCount = quoteCount;
	}
}