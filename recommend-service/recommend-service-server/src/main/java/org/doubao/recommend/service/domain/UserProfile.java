package org.doubao.recommend.service.domain;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像对象
 * 用于存储和分析用户的兴趣偏好，支持推荐系统的个性化推荐
 * 包含用户对标签、话题、作者、朝代、分类等多个维度的权重信息
 */
public class UserProfile implements Serializable {
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
     * 标签权重映射表
     * 存储用户对各标签的偏好权重，key 为标签名，value 为权重值
     */
    private Map<String, Double> tagWeights = new HashMap<>();
    
    /**
     * 话题权重映射表
     * 存储用户对各话题的偏好权重，key 为话题 ID，value 为权重值
     */
    private Map<String, Double> topicWeights = new HashMap<>();
    
    /**
     * 作者权重映射表
     * 存储用户对各作者的偏好权重，key 为作者名，value 为权重值
     */
    private Map<String, Double> authorWeights = new HashMap<>();
    
    /**
     * 朝代权重映射表
     * 存储用户对各朝代的偏好权重，key 为朝代名，value 为权重值
     */
    private Map<String, Double> dynastyWeights = new HashMap<>();
    
    /**
     * 分类权重映射表
     * 存储用户对各诗词分类的偏好权重，key 为分类名，value 为权重值
     */
    private Map<String, Double> categoryWeights = new HashMap<>();
    
    /**
     * 最近浏览的内容 ID 列表
     * 按时间倒序存储用户最近浏览的内容 ID，用于实时兴趣捕捉
     */
    private List<Long> recentContentIds = new ArrayList<>();
    
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
     * 添加标签权重
     * 将指定标签的权重累加到用户画像中
     *
     * @param tag 标签名称
     * @param weight 权重值
     */
    public void addTag(String tag, double weight) {
        tagWeights.merge(tag, weight, Double::sum);
    }

    /**
     * 添加话题权重
     * 将指定话题的权重累加到用户画像中
     *
     * @param topicId 话题 ID
     * @param weight 权重值
     */
    public void addTopic(String topicId, double weight) {
        topicWeights.merge(topicId, weight, Double::sum);
    }

    /**
     * 添加作者权重
     * 将指定作者的权重累加到用户画像中
     *
     * @param author 作者名称
     * @param weight 权重值
     */
    public void addAuthor(String author, double weight) {
        authorWeights.merge(author, weight, Double::sum);
    }

    /**
     * 添加朝代权重
     * 将指定朝代的权重累加到用户画像中
     *
     * @param dynasty 朝代名称
     * @param weight 权重值
     */
    public void addDynasty(String dynasty, double weight) {
        dynastyWeights.merge(dynasty, weight, Double::sum);
    }

    /**
     * 添加分类权重
     * 将指定分类的权重累加到用户画像中
     *
     * @param category 分类名称
     * @param weight 权重值
     */
    public void addCategory(String category, double weight) {
        categoryWeights.merge(category, weight, Double::sum);
    }

    /**
     * 推送最近浏览的内容到列表头部
     * 维护用户最近浏览的内容 ID 列表，保持最新的 N 条记录
     * 如果内容已存在则先移除再添加到头部
     *
     * @param contentId 内容 ID
     * @param maxSize 列表最大长度
     */
    public void pushRecentContent(Long contentId, int maxSize) {
        if (contentId == null) {
            return;
        }
        // 移除已存在的记录（如果有）
        recentContentIds.remove(contentId);
        // 添加到列表头部
        recentContentIds.add(0, contentId);
        // 如果超过最大长度，截取前 maxSize 个元素
        if (recentContentIds.size() > maxSize) {
            recentContentIds = new ArrayList<>(recentContentIds.subList(0, maxSize));
        }
    }

    /**
     * 获取 Map 中权重最高的 N 个条目
     * 按权重值降序排序，返回 Top N 个条目
     *
     * @param map 权重映射表
     * @param n 返回的条目数量
     * @param <K> 键的类型
     * @return 权重最高的 N 个条目列表
     */
    public static <K> List<Map.Entry<K, Double>> topEntries(Map<K, Double> map, int n) {
        return map.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }

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
     * 获取标签权重映射表
     * @return 标签权重映射表
     */
    public Map<String, Double> getTagWeights() {
        return tagWeights;
    }

    /**
     * 设置标签权重映射表
     * @param tagWeights 标签权重映射表
     */
    public void setTagWeights(Map<String, Double> tagWeights) {
        this.tagWeights = tagWeights;
    }

    /**
     * 获取话题权重映射表
     * @return 话题权重映射表
     */
    public Map<String, Double> getTopicWeights() {
        return topicWeights;
    }

    /**
     * 设置话题权重映射表
     * @param topicWeights 话题权重映射表
     */
    public void setTopicWeights(Map<String, Double> topicWeights) {
        this.topicWeights = topicWeights;
    }

    /**
     * 获取作者权重映射表
     * @return 作者权重映射表
     */
    public Map<String, Double> getAuthorWeights() {
        return authorWeights;
    }

    /**
     * 设置作者权重映射表
     * @param authorWeights 作者权重映射表
     */
    public void setAuthorWeights(Map<String, Double> authorWeights) {
        this.authorWeights = authorWeights;
    }

    /**
     * 获取朝代权重映射表
     * @return 朝代权重映射表
     */
    public Map<String, Double> getDynastyWeights() {
        return dynastyWeights;
    }

    /**
     * 设置朝代权重映射表
     * @param dynastyWeights 朝代权重映射表
     */
    public void setDynastyWeights(Map<String, Double> dynastyWeights) {
        this.dynastyWeights = dynastyWeights;
    }

    /**
     * 获取分类权重映射表
     * @return 分类权重映射表
     */
    public Map<String, Double> getCategoryWeights() {
        return categoryWeights;
    }

    /**
     * 设置分类权重映射表
     * @param categoryWeights 分类权重映射表
     */
    public void setCategoryWeights(Map<String, Double> categoryWeights) {
        this.categoryWeights = categoryWeights;
    }

    /**
     * 获取最近浏览的内容 ID 列表
     * @return 最近浏览的内容 ID 列表
     */
    public List<Long> getRecentContentIds() {
        return recentContentIds;
    }

    /**
     * 设置最近浏览的内容 ID 列表
     * @param recentContentIds 最近浏览的内容 ID 列表
     */
    public void setRecentContentIds(List<Long> recentContentIds) {
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
