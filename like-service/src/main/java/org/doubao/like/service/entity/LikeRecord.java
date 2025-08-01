package org.doubao.like.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.like.service.enums.EntityTypeEnum;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

// LikeRecord.java
@TableName("like_records")
public class LikeRecord extends BaseEntity {
	@TableId(value = "like_id", type = IdType.AUTO)
	private Long likeId;
	@TableField("user_id")
	private Long userId;
	@TableField("entity_type")
	private int entityType;
	@TableField("entity_id")
	private String entityId;
	@TableField("liked")
	private int liked;

	public LikeRecord() {
	}

	public LikeRecord(Long userId, int entityType, String entityId, int liked, LocalDateTime now) {
		this.userId = userId;
		this.entityType = entityType;
		this.entityId = entityId;
		this.liked = liked;
		this.setCreatedTime(now);
	}

	public Long getLikeId() {
		return likeId;
	}

	public void setLikeId(Long likeId) {
		this.likeId = likeId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public int getLiked() {
		return liked;
	}

	public void setLiked(int liked) {
		this.liked = liked;
	}
}