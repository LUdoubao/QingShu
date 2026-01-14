package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 话题动态实体类
 * 推模式存储话题动态，支撑信息流
 */
@TableName("topic_timeline")
public class TopicTimeline extends BaseDel {
    @TableId(type = IdType.AUTO)
    /**
     * 主键ID
     */
    private Long id;
    @TableField(value = "topic_id")
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    @TableField(value = "event_type")
    /**
     * 事件类型：1-新增文案 2-热门文案 3-官方推荐 4-话题更新
     */
    private Integer eventType;
    @TableField(value = "target_id")
    /**
     * 目标ID（如文案ID、话题ID）
     */
    private Long targetId;
    @TableField(value = "actor_id")
    /**
     * 事件发起者ID（用户/管理员）
     */
    private Long actorId;
    @TableField(value = "event_time")
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    @TableField(value = "weight")
    /**
     * 动态权重（用于排序）
     */
    private BigDecimal weight;

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
}