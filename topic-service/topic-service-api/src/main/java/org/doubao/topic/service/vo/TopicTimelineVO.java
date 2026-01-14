package org.doubao.topic.service.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 话题动态视图对象
 * 用于向前端传递话题动态信息，包含动态的基本信息
 */
public class TopicTimelineVO {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    /**
     * 事件类型：1-新增文案 2-热门文案 3-官方推荐 4-话题更新
     */
    private Integer eventType;
    /**
     * 目标ID（如文案ID、话题ID）
     */
    private Long targetId;
    /**
     * 事件发起者ID（用户/管理员）
     */
    private Long actorId;
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    /**
     * 动态权重（用于排序）
     */
    private BigDecimal weight;
    /**
     * 逻辑删除：0-有效 1-删除
     */
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}