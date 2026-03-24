package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("search_doc_index")
public class SearchDocIndexDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("biz_type")
    private String bizType;
    @TableField("biz_id")
    private Long bizId;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("author_name")
    private String authorName;
    @TableField("source")
    private String source;
    @TableField("category_id")
    private Long categoryId;
    @TableField("category_name")
    private String categoryName;
    @TableField("tag_names_text")
    private String tagNamesText;
    @TableField("search_text")
    private String searchText;
    @TableField("search_text_normalized")
    private String searchTextNormalized;
    @TableField("status")
    private Integer status;
    @TableField("is_original")
    private Integer isOriginal;
    @TableField("publish_time")
    private LocalDateTime publishTime;
    @TableField("view_count")
    private Long viewCount;
    @TableField("like_count")
    private Long likeCount;
    @TableField("comment_count")
    private Long commentCount;
    @TableField("favorite_count")
    private Long favoriteCount;
    @TableField("hot_score")
    private Double hotScore;
    @TableField("quality_score")
    private Double qualityScore;
    @TableField("is_deleted")
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTagNamesText() { return tagNamesText; }
    public void setTagNamesText(String tagNamesText) { this.tagNamesText = tagNamesText; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
    public String getSearchTextNormalized() { return searchTextNormalized; }
    public void setSearchTextNormalized(String searchTextNormalized) { this.searchTextNormalized = searchTextNormalized; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsOriginal() { return isOriginal; }
    public void setIsOriginal(Integer isOriginal) { this.isOriginal = isOriginal; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }
    public Double getHotScore() { return hotScore; }
    public void setHotScore(Double hotScore) { this.hotScore = hotScore; }
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
