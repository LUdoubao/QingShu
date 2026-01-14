package org.doubao.topic.service.dto;

/**
 * 话题查询数据传输对象
 * 用于接收查询话题列表时的筛选条件和分页参数
 */
public class TopicQueryDTO {
    /**
     * 关键词（话题名称或描述）
     */
    private String keyword;
    /**
     * 分类ID
     */
    private Long categoryId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 是否推荐
     */
    private Integer isRecommend;
    /**
     * 页码
     */
    private Integer page = 1;
    /**
     * 每页大小
     */
    private Integer size = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}