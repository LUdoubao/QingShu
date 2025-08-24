package org.doubao.user.server.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_relation")
public class UserRelation {
	@TableId(type = IdType.AUTO)
	private Long id;
	@TableField(value = "user_id")
	private Long userId;
	@TableField(value = "target_user_id")
	private Long targetUserId;
	@TableField(value = "relation_type")
	private Integer relationType;
	@TableField(value = "is_mutual")
	private Integer isMutual;
	@TableField(value = "created_time")
	private LocalDateTime createdTime;
	@TableField(value = "updated_time")
	private LocalDateTime updatedTime;

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

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}

	public Integer getRelationType() {
		return relationType;
	}

	public void setRelationType(Integer relationType) {
		this.relationType = relationType;
	}

	public Integer getIsMutual() {
		return isMutual;
	}

	public void setIsMutual(Integer isMutual) {
		this.isMutual = isMutual;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public LocalDateTime getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}
}
