package org.doubao.mall.common.dto;

/**
 * 话题绑定数据传输对象
 * 用于将文案与话题进行绑定操作的数据传输
 */
public class TopicBindDTO {
    /**
     * 文案ID
     */
    private Long quoteId;
    /**
     * 话题ID
     */
    private Long topicId;
    /**
     * 绑定者ID
     */
    private Long binderId;

    /**
     * 绑定状态
     */
    private Integer status;

    public TopicBindDTO() {
    }

    public TopicBindDTO(Long quoteId, Integer status) {
        this.quoteId = quoteId;
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getBinderId() {
        return binderId;
    }

    public void setBinderId(Long binderId) {
        this.binderId = binderId;
    }
}