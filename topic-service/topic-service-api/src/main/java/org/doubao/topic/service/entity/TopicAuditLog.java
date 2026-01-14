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
    @TableId(type = IdType.AUTO)
    /**
     * 日志ID（主键）
     */
    private Long id;
    @TableField(value = "topic_id")
    /**
     * 话题ID（关联topic表）
     */
    private Long topicId;
    @TableField(value = "audit_status")
    /**
     * 审核结果：0-待审核 1-通过 2-拒绝
     */
    private Integer auditStatus;
    @TableField(value = "audit_reason")
    /**
     * 审核理由
     */
    private String auditReason;
    @TableField(value = "auditor_id")
    /**
     * 审核员ID（关联user表，管理员角色）
     */
    private Long auditorId;
    @TableField(value = "audit_time")
    /**
     * 审核时间
     */
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