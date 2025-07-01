package org.doubao.mall.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

public class BaseEntity {
	@TableField("created_id")
	private Long createdId;
	@TableField("updated_id")
	private Long updatedId;
	@TableField("created_time")
	private LocalDateTime createdTime;
	@TableField("updated_time")
	private LocalDateTime updatedTime;
	@TableLogic
	@TableField("deleted")
	private Integer deleted;

	public Long getCreatedId() {
		return createdId;
	}

	public void setCreatedId(Long createdId) {
		this.createdId = createdId;
	}

	public Long getUpdatedId() {
		return updatedId;
	}

	public void setUpdatedId(Long updatedId) {
		this.updatedId = updatedId;
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

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}
}
