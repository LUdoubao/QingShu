package org.doubao.like.service.dto.response;

import java.util.Map;

public class LikeQuoteVo {
	private Map<String, Object> quote;
	private Long likeCount;

	public Map<String, Object> getQuote() {
		return quote;
	}

	public void setQuote(Map<String, Object> quote) {
		this.quote = quote;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}
}
