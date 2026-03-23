package org.doubao.like.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HotContentResponse {
	private Integer page;
	private Integer limit;
	private Long total;
	private Integer windowHours;
	private String type;
	private String scoreSource;
	private LocalDateTime generatedAt;
	private RankMeta rankMeta;
	private List<HotContentItem> hotContents;

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getWindowHours() {
		return windowHours;
	}

	public void setWindowHours(Integer windowHours) {
		this.windowHours = windowHours;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScoreSource() {
		return scoreSource;
	}

	public void setScoreSource(String scoreSource) {
		this.scoreSource = scoreSource;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public RankMeta getRankMeta() {
		return rankMeta;
	}

	public void setRankMeta(RankMeta rankMeta) {
		this.rankMeta = rankMeta;
	}

	public List<HotContentItem> getHotContents() {
		return hotContents;
	}

	public void setHotContents(List<HotContentItem> hotContents) {
		this.hotContents = hotContents;
	}

	public static class RankMeta {
		private String type;
		private Integer windowHours;
		private String scoreSource;
		private Integer refreshIntervalMinutes;
		private Long risingMinCurrentLikes;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Integer getWindowHours() {
			return windowHours;
		}

		public void setWindowHours(Integer windowHours) {
			this.windowHours = windowHours;
		}

		public String getScoreSource() {
			return scoreSource;
		}

		public void setScoreSource(String scoreSource) {
			this.scoreSource = scoreSource;
		}

		public Integer getRefreshIntervalMinutes() {
			return refreshIntervalMinutes;
		}

		public void setRefreshIntervalMinutes(Integer refreshIntervalMinutes) {
			this.refreshIntervalMinutes = refreshIntervalMinutes;
		}

		public Long getRisingMinCurrentLikes() {
			return risingMinCurrentLikes;
		}

		public void setRisingMinCurrentLikes(Long risingMinCurrentLikes) {
			this.risingMinCurrentLikes = risingMinCurrentLikes;
		}
	}

	public static class HotContentItem {
		private Long contentId;
		private Integer rank;
		private Long likeCount;
		private Long recentLikeCount;
		private Long heatValue;
		private Integer rankChange;
		private String trend;
		private Map<String, Object> quote;

		public Long getContentId() {
			return contentId;
		}

		public void setContentId(Long contentId) {
			this.contentId = contentId;
		}

		public Integer getRank() {
			return rank;
		}

		public void setRank(Integer rank) {
			this.rank = rank;
		}

		public Long getLikeCount() {
			return likeCount;
		}

		public void setLikeCount(Long likeCount) {
			this.likeCount = likeCount;
		}

		public Long getRecentLikeCount() {
			return recentLikeCount;
		}

		public void setRecentLikeCount(Long recentLikeCount) {
			this.recentLikeCount = recentLikeCount;
		}

		public Long getHeatValue() {
			return heatValue;
		}

		public void setHeatValue(Long heatValue) {
			this.heatValue = heatValue;
		}

		public Integer getRankChange() {
			return rankChange;
		}

		public void setRankChange(Integer rankChange) {
			this.rankChange = rankChange;
		}

		public String getTrend() {
			return trend;
		}

		public void setTrend(String trend) {
			this.trend = trend;
		}

		public Map<String, Object> getQuote() {
			return quote;
		}

		public void setQuote(Map<String, Object> quote) {
			this.quote = quote;
		}
	}
}
