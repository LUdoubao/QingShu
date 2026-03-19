package org.doubao.recommend.service.domain;


import java.time.LocalDateTime;

/**
 * 用户画像快照行对象
 * 用于数据库存储的用户画像快照记录
 * 将 UserProfile 中的 Map 类型字段序列化为 JSON 字符串存储
 */
public class UserProfileSnapshotRow {
    /**
     * 用户身份标识
     * 用于唯一标识一个用户的身份字符串
     */
    private String userIdentity;
    
    /**
     * 用户 ID
     * 数字类型的用户唯一标识符
     */
    private Long userId;
    
    /**
     * 标签权重（JSON 字符串）
     * 存储用户对各标签的偏好权重，格式：{"tag1":1.5,"tag2":2.0}
     */
    private String tagWeights;
    
    /**
     * 话题权重（JSON 字符串）
     * 存储用户对各话题的偏好权重，格式：{"topicId1":1.2,"topicId2":3.5}
     */
    private String topicWeights;
    
    /**
     * 作者权重（JSON 字符串）
     * 存储用户对各作者的偏好权重，格式：{"author1":2.5,"author2":1.8}
     */
    private String authorWeights;
    
    /**
     * 朝代权重（JSON 字符串）
     * 存储用户对各朝代的偏好权重，格式：{"唐朝":3.0,"宋朝":2.5}
     */
    private String dynastyWeights;
    
    /**
     * 分类权重（JSON 字符串）
     * 存储用户对各诗词分类的偏好权重，格式：{"山水诗":2.0,"边塞诗":1.5}
     */
    private String categoryWeights;
    
    /**
     * 最近浏览的内容 ID 列表（JSON 字符串）
     * 按时间倒序存储用户最近浏览的内容 ID，格式：[1001,1002,1003]
     */
    private String recentContentIds;
    
    /**
     * 最后活跃时间
     * 用户最后一次活跃的时间戳
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 更新时间
     * 用户画像最后一次更新的时间戳
     */
    private LocalDateTime updatedTime;

    /**
     * 获取用户身份标识
     * @return 用户身份标识
     */
    public String getUserIdentity() {
        return userIdentity;
    }

    /**
     * 设置用户身份标识
     * @param userIdentity 用户身份标识
     */
    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    /**
     * 获取标签权重（JSON 字符串）
     * @return 标签权重 JSON 字符串
     */
    public String getTagWeights() {
        return tagWeights;
    }

    /**
     * 设置标签权重（JSON 字符串）
     * @param tagWeights 标签权重 JSON 字符串
     */
    public void setTagWeights(String tagWeights) {
        this.tagWeights = tagWeights;
    }

    /**
     * 获取用户 ID
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取话题权重（JSON 字符串）
     * @return 话题权重 JSON 字符串
     */
    public String getTopicWeights() {
        return topicWeights;
    }

    /**
     * 设置话题权重（JSON 字符串）
     * @param topicWeights 话题权重 JSON 字符串
     */
    public void setTopicWeights(String topicWeights) {
        this.topicWeights = topicWeights;
    }

    /**
     * 获取作者权重（JSON 字符串）
     * @return 作者权重 JSON 字符串
     */
    public String getAuthorWeights() {
        return authorWeights;
    }

    /**
     * 设置作者权重（JSON 字符串）
     * @param authorWeights 作者权重 JSON 字符串
     */
    public void setAuthorWeights(String authorWeights) {
        this.authorWeights = authorWeights;
    }

    /**
     * 获取朝代权重（JSON 字符串）
     * @return 朝代权重 JSON 字符串
     */
    public String getDynastyWeights() {
        return dynastyWeights;
    }

    /**
     * 设置朝代权重（JSON 字符串）
     * @param dynastyWeights 朝代权重 JSON 字符串
     */
    public void setDynastyWeights(String dynastyWeights) {
        this.dynastyWeights = dynastyWeights;
    }

    /**
     * 获取分类权重（JSON 字符串）
     * @return 分类权重 JSON 字符串
     */
    public String getCategoryWeights() {
        return categoryWeights;
    }

    /**
     * 设置分类权重（JSON 字符串）
     * @param categoryWeights 分类权重 JSON 字符串
     */
    public void setCategoryWeights(String categoryWeights) {
        this.categoryWeights = categoryWeights;
    }

    /**
     * 获取最近浏览的内容 ID 列表（JSON 字符串）
     * @return 最近浏览的内容 ID 列表 JSON 字符串
     */
    public String getRecentContentIds() {
        return recentContentIds;
    }

    /**
     * 设置最近浏览的内容 ID 列表（JSON 字符串）
     * @param recentContentIds 最近浏览的内容 ID 列表 JSON 字符串
     */
    public void setRecentContentIds(String recentContentIds) {
        this.recentContentIds = recentContentIds;
    }

    /**
     * 获取最后活跃时间
     * @return 最后活跃时间
     */
    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 设置最后活跃时间
     * @param lastActiveTime 最后活跃时间
     */
    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 设置更新时间
     * @param updatedTime 更新时间
     */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
