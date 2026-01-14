package org.doubao.topic.service.vo;

import java.time.LocalDateTime;

/**
 * 话题视图对象
 * 用于向前端传递话题信息，包含话题的基本信息和统计信息
 */
public class TopicVO {
    /**
     * 话题ID（主键）
     */
    private Long id;
    /**
     * 话题名称（唯一，格式建议带#，如#治愈文案#）
     */
    private String name;
    /**
     * 话题描述（支持富文本）
     */
    private String description;
    /**
     * 话题封面图KEY（关联文件存储）
     */
    private String coverKey;
    /**
     * 创建人ID（关联user表）
     */
    private Long creatorId;
    /**
     * 话题分类ID（关联topic_category表）
     */
    private Long categoryId;
    /**
     * 状态：0-审核中 1-已发布 2-屏蔽 3-草稿 4-未通过 5-下架
     */
    private Integer status;
    /**
     * 是否官方推荐：0-否 1-是
     */
    private Integer isRecommend;
    /**
     * 排序权重（值越大越靠前，用于推荐排序）
     */
    private Integer weight;
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    /**
     * 关联文案总数
     */
    private Integer quoteCount;
    /**
     * 关注用户总数
     */
    private Integer followCount;
    /**
     * 话题总浏览量
     */
    private Long viewCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Integer getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(Integer quoteCount) {
        this.quoteCount = quoteCount;
    }

    public Integer getFollowCount() {
        return followCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}