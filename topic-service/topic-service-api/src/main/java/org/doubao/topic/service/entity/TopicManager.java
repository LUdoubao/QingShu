package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 话题管理员实体类
 * 支持多管理员协作管理话题
 */
@TableName("topic_manager")
public class TopicManager extends BaseEntity {
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
    @TableField(value = "manager_id")
    /**
     * 管理员ID（关联user表，支持普通用户成为管理员）
     */
    private Long managerId;
    @TableField(value = "role")
    /**
     * 管理角色：ADMIN-超级管理员 EDITOR-内容编辑
     */
    private String role;
    @TableField(value = "add_time")
    /**
     * 添加时间
     */
    private LocalDateTime addTime;
    @TableField(value = "remove_time")
    /**
     * 移除时间（NULL=有效）
     */
    private LocalDateTime removeTime;
    @TableField(value = "is_valid")
    /**
     * 是否有效：0-已移除 1-有效
     */
    private Integer isValid;

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

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }

    public LocalDateTime getRemoveTime() {
        return removeTime;
    }

    public void setRemoveTime(LocalDateTime removeTime) {
        this.removeTime = removeTime;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}