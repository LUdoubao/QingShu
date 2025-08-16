package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 对话主表实体类
 */
@TableName("assistant_dialog")
public class AssistantDialog {

	/**
	 * 对话ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户ID（未登录为NULL）
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 状态：0-活跃 1-已解决 2-待跟进
	 */
	@TableField("status")
	private Integer status;

	/**
	 * 标题
	 */
	@TableField("title")
	private String title;
	/**
	 * 创建时间
	 */
	@TableField("created_time")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 */
	@TableField("updated_time")
	private LocalDateTime updatedTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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