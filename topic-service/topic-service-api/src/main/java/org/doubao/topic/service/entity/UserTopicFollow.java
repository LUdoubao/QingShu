package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户话题关注实体类
 * 记录用户关注话题的关系，支撑动态推送
 */
@TableName("user_topic_follow")
public class UserTopicFollow extends BaseEntity {
    @TableId(type = IdType.AUTO)
    /**
     * 关注ID（主键）
     */
    private Long id;
    @TableField(value = "user_id")
    /**
     * 用户ID（关联user表）
     */
    private Long userId;
    @TableField(value = "topic_id")
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    @TableField(value = "follow_time")
    /**
     * 关注时间
     */
    private LocalDateTime followTime;
    @TableField(value = "unfollow_time")
    /**
     * 取消关注时间（NULL=未取消）
     */
    private LocalDateTime unfollowTime;
    @TableField(value = "is_valid")
    /**
     * 是否有效：0-已取消 1-有效
     */
    private Integer isValid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFollowTime() {
        return followTime;
    }

    public void setFollowTime(LocalDateTime followTime) {
        this.followTime = followTime;
    }

    public LocalDateTime getUnfollowTime() {
        return unfollowTime;
    }

    public void setUnfollowTime(LocalDateTime unfollowTime) {
        this.unfollowTime = unfollowTime;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}