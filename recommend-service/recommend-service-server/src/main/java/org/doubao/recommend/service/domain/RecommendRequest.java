package org.doubao.recommend.service.domain;


/**
 * 推荐请求参数对象
 * 用于封装推荐系统中的各种请求参数，支持多种推荐场景
 */
public class RecommendRequest {
    /**
     * 用户 ID，用于标识当前请求的用户
     * 用于获取用户的个性化推荐数据
     */
    private Long userId;
    
    /**
     * 用户身份标识
     * 用于区分不同类型的用户身份（如普通用户、VIP 用户等）
     */
    private String userIdentity;
    
    /**
     * 推荐场景
     * 可选值：home(首页)/detail(详情页)/topic(话题页)/author(作者页)/guess(猜你喜欢)
     * 不同场景会使用不同的推荐算法和策略
     */
    private String scene;
    
    /**
     * 内容 ID
     * 在详情页等场景下，用于标识当前浏览的内容 ID
     * 用于计算相关内容推荐
     */
    private Long contentId;
    
    /**
     * 话题 ID
     * 在话题页场景下，用于标识当前浏览的话题 ID
     */
    private Long topicId;
    
    /**
     * 作者标识
     * 在作者页场景下，用于标识当前浏览的作者
     */
    private String author;
    
    /**
     * 页码，默认值为 1
     * 用于分页查询，从第 1 页开始
     */
    private Integer page = 1;
    
    /**
     * 每页大小，默认值为 20
     * 控制每页返回的推荐结果数量
     */
    private Integer pageSize = 20;
    
    /**
     * 返回结果上限，默认值为 100
     * 用于限制单次请求最大返回数量，防止过多数据加载
     */
    private Integer limit = 100;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
