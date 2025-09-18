package org.doubao.user.server.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户黑名单实体类
 */
@TableName("user_block")
public class UserBlock {
	/**
	 * 主键ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 当前用户ID（拉黑者）
	 */
	@TableField(value = "user_id")
	private Long userId;

	/**
	 * 被拉黑用户ID
	 */
	@TableField(value = "blocked_user_id")
	private Long blockedUserId;

	/**
	 * 拉黑时间
	 */
	@TableField(value = "created_time")
	private LocalDateTime createdTime;

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

	public Long getBlockedUserId() {
		return blockedUserId;
	}

	public void setBlockedUserId(Long blockedUserId) {
		this.blockedUserId = blockedUserId;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}
}
