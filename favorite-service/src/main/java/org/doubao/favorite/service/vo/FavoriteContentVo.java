package org.doubao.favorite.service.vo;

import org.doubao.favorite.service.entity.FavoriteContent;

import java.util.Map;

public class FavoriteContentVo extends FavoriteContent {
	private Map<String, Object> quote;

	public Map<String, Object> getQuote() {
		return quote;
	}

	public void setQuote(Map<String, Object> quote) {
		this.quote = quote;
	}
}
