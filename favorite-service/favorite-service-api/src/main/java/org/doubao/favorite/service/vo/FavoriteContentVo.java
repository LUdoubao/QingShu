package org.doubao.favorite.service.vo;

import org.doubao.favorite.service.entity.FavoriteContent;

import java.util.Map;

public class FavoriteContentVo extends FavoriteContent {
	private Map<String, Object> quote;
	private Long favoriteCount;

	public Long getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(Long favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public Map<String, Object> getQuote() {
		return quote;
	}

	public void setQuote(Map<String, Object> quote) {
		this.quote = quote;
	}
}