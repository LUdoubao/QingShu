package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 话题统计实体类
 * 冗余统计数据，提升查询性能
 */
@TableName("topic_statistics")
public class TopicStatistics {
    /**
     * 话题ID（主键，关联topic表）
     */
    @TableId(value = "topic_id")
    private Long topicId;
    /**
     * 关联文案总数
     */
    @TableField(value = "quote_count")
    private Integer quoteCount;
    /**
     * 参与用户数（发布文案的独立用户数）
     */
    @TableField(value = "active_user_count")
    private Integer activeUserCount;
    /**
     * 关注用户总数
     */
    @TableField(value = "follow_count")
    private Integer followCount;
    /**
     * 话题总浏览量
     */
    @TableField(value = "view_count")
    private Long viewCount;
    /**
     * 今日新增文案数
     */
    @TableField(value = "today_quote_count")
    private Integer todayQuoteCount;
    /**
     * 热门文案ID（点赞数最高，便于快速展示）
     */
    @TableField(value = "hot_quote_id")
    private Long hotQuoteId;
    /**
     * 统计更新时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Integer getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(Integer quoteCount) {
        this.quoteCount = quoteCount;
    }

    public Integer getActiveUserCount() {
        return activeUserCount;
    }

    public void setActiveUserCount(Integer activeUserCount) {
        this.activeUserCount = activeUserCount;
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

    public Integer getTodayQuoteCount() {
        return todayQuoteCount;
    }

    public void setTodayQuoteCount(Integer todayQuoteCount) {
        this.todayQuoteCount = todayQuoteCount;
    }

    public Long getHotQuoteId() {
        return hotQuoteId;
    }

    public void setHotQuoteId(Long hotQuoteId) {
        this.hotQuoteId = hotQuoteId;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}