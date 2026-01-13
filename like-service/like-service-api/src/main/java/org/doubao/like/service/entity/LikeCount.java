package org.doubao.like.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

// LikeCount.java
@TableName("like_counts")
public class LikeCount {
	@TableField(value = "entity_type")
	private int entityType;
	@TableField(value = "entity_id")
	private String entityId;
	@TableField(value = "count")
	private Integer count;
	@TableField(value = "updated_at")
	private LocalDateTime updatedAt;

	public LikeCount(int entityType, String entityId) {
		this.entityType = entityType;
		this.entityId = entityId;
	}

	public LikeCount(int entityType, String entityId, int count, LocalDateTime updatedAt) {
		this.entityType = entityType;
		this.entityId = entityId;
		this.count = count;
		this.updatedAt = updatedAt;
	}

	public LikeCount() {

	}

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}