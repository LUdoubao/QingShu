package org.doubao.quote.service.vo;

// 数据概览VO
public class ContentOverviewVo {
	/** 总文章数 */
	private long totalArticles;
	/** 总浏览量 */
	private long totalViews;
	/** 总点赞数 */
	private long totalLikes;
	/** 总评论数 */
	private long totalComments;
	/** 总收藏数 */
	private long totalFavorites;

	public long getTotalArticles() {
		return totalArticles;
	}

	public void setTotalArticles(long totalArticles) {
		this.totalArticles = totalArticles;
	}

	public long getTotalViews() {
		return totalViews;
	}

	public void setTotalViews(long totalViews) {
		this.totalViews = totalViews;
	}

	public long getTotalLikes() {
		return totalLikes;
	}

	public void setTotalLikes(long totalLikes) {
		this.totalLikes = totalLikes;
	}

	public long getTotalComments() {
		return totalComments;
	}

	public void setTotalComments(long totalComments) {
		this.totalComments = totalComments;
	}

	public long getTotalFavorites() {
		return totalFavorites;
	}

	public void setTotalFavorites(long totalFavorites) {
		this.totalFavorites = totalFavorites;
	}
}
