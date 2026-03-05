package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 话题标签关联实体类
 * 复用现有标签体系，增强话题检索能力
 */
@TableName("topic_tag")
public class TopicTag extends BaseEntity {
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    /**
     * 标签ID（关联现有tag表）
     */
    @TableField(value = "tag_id")
    private Long tagId;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}