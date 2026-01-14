package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 话题审核日志实体类
 * 记录话题审核过程中的相关信息，支撑内容合规和审核追溯
 */
@TableName("topic_audit_log")
public class TopicAuditLog extends BaseEntity {
    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 话题ID
     */
    @TableField(value = "topic_id")
    private Long topicId;
    /**
     * 审核结果：0-待审核 1-通过 2-拒绝
     */
    @TableField(value = "audit_status")
    private Integer auditStatus;
    /**
     * 审核理由
     */
    @TableField(value = "audit_reason")
    private String auditReason;
    /**
     * 审核员ID（关联user表，管理员角色）
     */
    @TableField(value = "auditor_id")
    private Long auditorId;
    /**
     * 审核时间
     */
    @TableField(value = "audit_time")
    private LocalDateTime auditTime;

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

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }
}