package org.doubao.topic.service.dto;

/**
 * 用户话题关注数据传输对象
 * 用于处理用户关注话题操作的数据传输
 */
public class UserTopicFollowDTO {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 话题ID
     */
    private Long topicId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }
}