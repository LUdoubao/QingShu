package org.doubao.quote.service.dto;

import lombok.Data;

@Data
public class QuoteDTO {
	private String content;
	private String author;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
