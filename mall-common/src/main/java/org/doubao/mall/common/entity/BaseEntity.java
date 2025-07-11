package org.doubao.mall.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@TableField(value = "created_id", fill = FieldFill.INSERT)
	private Long createdId;
	@TableField(value = "updated_id", fill = FieldFill.INSERT_UPDATE)
	private Long updatedId;
	@TableField(value = "created_time", fill = FieldFill.INSERT)
	private LocalDateTime createdTime;
	@TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedTime;


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

}
