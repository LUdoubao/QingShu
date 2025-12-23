package org.doubao.quote.service.vo;

import java.time.LocalDate;

// 趋势数据VO
public class ContentTrendVo {
	/** 日期 */
	private LocalDate date;
	/** 浏览量 */
	private long views;
	/** 点赞数 */
	private long likes;
	/** 评论数 */
	private long comments;
	/** 收藏数 */
	private long favorites;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public long getViews() {
		return views;
	}

	public void setViews(long views) {
		this.views = views;
	}

	public long getLikes() {
		return likes;
	}

	public void setLikes(long likes) {
		this.likes = likes;
	}

	public long getComments() {
		return comments;
	}

	public void setComments(long comments) {
		this.comments = comments;
	}

	public long getFavorites() {
		return favorites;
	}

	public void setFavorites(long favorites) {
		this.favorites = favorites;
	}
}