package org.doubao.search.service.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索结果视图对象
 * 用于封装搜索接口返回给前端的所有数据，包括分页信息、结果列表和相关推荐
 */
@Schema(description = "搜索结果封装对象")
public class SearchResultVO {

	@Schema(description = "搜索结果总条数")
	private Long total;

	@Schema(description = "当前页码，从1开始")
	private Integer page;

	@Schema(description = "每页显示的记录数")
	private Integer pageSize;

	@Schema(description = "搜索结果列表，包含符合条件的文案信息")
	private List<CopywritingVO> items;

	@Schema(description = "相关搜索建议，基于当前关键词的推荐搜索词")
	private List<String> relatedSearches;

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public List<CopywritingVO> getItems() {
		return items;
	}

	public void setItems(List<CopywritingVO> items) {
		this.items = items;
	}

	public List<String> getRelatedSearches() {
		return relatedSearches;
	}

	public void setRelatedSearches(List<String> relatedSearches) {
		this.relatedSearches = relatedSearches;
	}

	/**
	 * 文案信息视图对象
	 * 封装单条文案的详细信息，用于前端展示
	 */
	@Schema(description = "文案信息视图对象")
	public static class CopywritingVO {

		@Schema(description = "文案唯一标识ID")
		private Long id;

		@Schema(description = "文案标题")
		private String title;

		@Schema(description = "文案摘要信息")
		private String summary;

		@Schema(description = "搜索关键词在文案中的高亮位置信息")
		private HighlightVO highlight;

		@Schema(description = "文案作者信息")
		private AuthorVO author;

		@Schema(description = "文案标签列表")
		private List<String> tags;

		@Schema(description = "文案统计数据，包括点赞、收藏等")
		private StatisticsVO stats;

		@Schema(description = "文案发布时间")
		private LocalDateTime publishTime;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		public HighlightVO getHighlight() {
			return highlight;
		}

		public void setHighlight(HighlightVO highlight) {
			this.highlight = highlight;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public AuthorVO getAuthor() {
			return author;
		}

		public void setAuthor(AuthorVO author) {
			this.author = author;
		}

		public StatisticsVO getStats() {
			return stats;
		}

		public void setStats(StatisticsVO stats) {
			this.stats = stats;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public LocalDateTime getPublishTime() {
			return publishTime;
		}

		public void setPublishTime(LocalDateTime publishTime) {
			this.publishTime = publishTime;
		}
	}

	/**
	 * 高亮信息视图对象
	 * 存储搜索关键词在文案中被匹配到的位置，用于前端高亮显示
	 */
	@Schema(description = "搜索关键词高亮信息")
	public static class HighlightVO {

		@Schema(description = "标题中包含关键词的片段，已添加高亮标记")
		private List<String> title;

		@Schema(description = "摘要中包含关键词的片段，已添加高亮标记")
		private List<String> summary;

		public List<String> getTitle() {
			return title;
		}

		public void setTitle(List<String> title) {
			this.title = title;
		}

		public List<String> getSummary() {
			return summary;
		}

		public void setSummary(List<String> summary) {
			this.summary = summary;
		}
	}

	/**
	 * 作者信息视图对象
	 * 封装文案作者的基本信息，用于前端展示
	 */
	@Schema(description = "文案作者信息")
	public static class AuthorVO {

		@Schema(description = "作者唯一标识ID")
		private Long id;

		@Schema(description = "作者昵称")
		private String nickname;

		@Schema(description = "作者头像URL")
		private String avatar;

		@Schema(description = "作者是否为认证用户")
		private Boolean isVerified;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getAvatar() {
			return avatar;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}

		public Boolean getVerified() {
			return isVerified;
		}

		public void setVerified(Boolean verified) {
			isVerified = verified;
		}
	}

	/**
	 * 统计数据视图对象
	 * 封装文案的各种互动统计数据，用于前端展示热度信息
	 */
	@Schema(description = "文案统计数据")
	public static class StatisticsVO {

		@Schema(description = "点赞数量")
		private Integer likeCount;

		@Schema(description = "收藏数量")
		private Integer collectCount;

		@Schema(description = "评论数量")
		private Integer commentCount;

		@Schema(description = "浏览量")
		private Integer viewCount;

		public Integer getLikeCount() {
			return likeCount;
		}

		public void setLikeCount(Integer likeCount) {
			this.likeCount = likeCount;
		}

		public Integer getCollectCount() {
			return collectCount;
		}

		public void setCollectCount(Integer collectCount) {
			this.collectCount = collectCount;
		}

		public Integer getCommentCount() {
			return commentCount;
		}

		public void setCommentCount(Integer commentCount) {
			this.commentCount = commentCount;
		}

		public Integer getViewCount() {
			return viewCount;
		}

		public void setViewCount(Integer viewCount) {
			this.viewCount = viewCount;
		}
	}
}
