package org.doubao.search.service.model.entity.es;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文案Elasticsearch实体类
 * 用于在Elasticsearch中存储和检索文案数据，支持全文检索功能
 */
@Document(indexName = "#{@elasticsearchIndexProperties.copywriting}") // 索引名从配置获取，支持动态配置
public class CopywritingEsEntity {

	/**
	 * 文案唯一标识ID
	 * 在ES中作为文档ID，使用keyword类型确保精确匹配
	 */
	@Id
	private Long id;

	/**
	 * 文案标题
	 * 使用ik_max_word_pinyin分词器（支持中文分词和拼音）
	 * 搜索时使用ik_smart_pinyin分词器（更智能的分词）
	 * boost=3.0f表示标题权重高于其他字段，在搜索结果排序中影响更大
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin", searchAnalyzer = "ik_smart_pinyin")
	private String title;

	/**
	 * 文案摘要
	 * 权重略低于标题，高于正文内容
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin", searchAnalyzer = "ik_smart_pinyin")
	private String summary;

	/**
	 * 文案正文内容
	 * 完整内容，用于全文检索
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin", searchAnalyzer = "ik_smart_pinyin")
	private String content;

	/**
	 * 作者ID
	 * keyword类型用于精确匹配，支持按作者筛选
	 */
	@Field(type = FieldType.Keyword)
	private Long authorId;

	/**
	 * 作者名称
	 * 支持按作者名搜索，同时保留keyword字段用于精确匹配
	 */
	@Field(type = FieldType.Text, analyzer = "ik_max_word_pinyin", searchAnalyzer = "ik_smart_pinyin")
	private String authorName;

	/**
	 * 作者头像URL
	 * 直接存储URL，用于前端展示
	 */
	@Field(type = FieldType.Keyword)
	private String authorAvatar;

	/**
	 * 作者是否为认证用户
	 * boolean类型支持按认证状态筛选
	 */
	@Field(type = FieldType.Boolean)
	private Boolean isVerifiedAuthor;

	/**
	 * 文案标签列表
	 * keyword类型支持精确匹配和聚合查询，用于标签筛选
	 */
	@Field(type = FieldType.Keyword)
	private List<String> tags;

	/**
	 * 内容类型
	 * 如"article"、"story"、"poem"等，支持按类型筛选
	 */
	@Field(type = FieldType.Keyword)
	private String contentType;

	/**
	 * 审核状态
	 * 如"PENDING"、"APPROVED"、"REJECTED"，用于筛选已审核内容
	 */
	@Field(type = FieldType.Keyword)
	private String auditStatus;

	/**
	 * 点赞数量
	 * 用于排序和筛选热门内容
	 */
	@Field(type = FieldType.Integer)
	private Integer likeCount;

	/**
	 * 收藏数量
	 * 用于排序和筛选热门内容
	 */
	@Field(type = FieldType.Integer)
	private Integer collectCount;

	/**
	 * 评论数量
	 * 用于排序和筛选热门内容
	 */
	@Field(type = FieldType.Integer)
	private Integer commentCount;

	/**
	 * 浏览量
	 * 用于排序和筛选热门内容
	 */
	@Field(type = FieldType.Integer)
	private Integer viewCount;

	/**
	 * 发布时间
	 * date类型支持按时间范围筛选和排序
	 */
	@Field(type = FieldType.Date)
	private LocalDateTime publishTime;

	/**
	 * 更新时间
	 * 用于跟踪内容更新，支持按更新时间筛选
	 */
	@Field(type = FieldType.Date)
	private LocalDateTime updateTime;

	/**
	 * 是否删除
	 * 逻辑删除标记，用于过滤已删除内容
	 */
	@Field(type = FieldType.Boolean)
	private Boolean isDeleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public String getAuthorAvatar() {
		return authorAvatar;
	}

	public void setAuthorAvatar(String authorAvatar) {
		this.authorAvatar = authorAvatar;
	}

	public Boolean getVerifiedAuthor() {
		return isVerifiedAuthor;
	}

	public void setVerifiedAuthor(Boolean verifiedAuthor) {
		isVerifiedAuthor = verifiedAuthor;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public String getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
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

	public LocalDateTime getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(LocalDateTime publishTime) {
		this.publishTime = publishTime;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	public Boolean getDeleted() {
		return isDeleted;
	}

	public void setDeleted(Boolean deleted) {
		isDeleted = deleted;
	}
}
