package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

/**
 * 文案话题关联实体类
 * 存储文案与话题之间的多对多关联关系
 */
@TableName("quote_topic")
public class QuoteTopic extends BaseDel {
    @TableId(type = IdType.AUTO)
    /**
     * 关联ID（主键）
     */
    private Long id;
    @TableField(value = "quote_id")
    /**
     * 文案ID（关联quote表）
     */
    private Long quoteId;
    @TableField(value = "topic_id")
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    @TableField(value = "bind_time")
    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;
    @TableField(value = "binder_id")
    /**
     * 绑定人ID（用户/管理员）
     */
    private Long binderId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getBindTime() {
        return bindTime;
    }

    public void setBindTime(LocalDateTime bindTime) {
        this.bindTime = bindTime;
    }

    public Long getBinderId() {
        return binderId;
    }

    public void setBinderId(Long binderId) {
        this.binderId = binderId;
    }
}