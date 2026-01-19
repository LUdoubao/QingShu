package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;
import org.doubao.mall.common.entity.BaseSingleDel;

import java.time.LocalDateTime;

/**
 * 文案话题关联实体类
 * 存储文案与话题之间的多对多关联关系
 */
@TableName("quote_topic")
public class QuoteTopic extends BaseSingleDel {
    /**
     * 关联ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 文案ID（关联quote表）
     */
    @TableField(value = "quote_id")
    private Long quoteId;
    /**
     * 话题ID（关联topic表）
     */
    @TableField(value = "topic_id")
    private Long topicId;
    /**
     * 绑定时间
     */
    @TableField(value = "bind_time")
    private LocalDateTime bindTime;
    /**
     * 绑定者ID（关联user表）
     */
    @TableField(value = "binder_id")
    private Long binderId;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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