package org.doubao.user.server.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_follow_operate_log")
public class UserFollowOperateLog {
	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;
	@TableField("user_id")
	private Long userId;
	@TableField("operate_type")
	private Integer operateType; // 操作类型（1=关注，2=取消关注，3=批量关注，4=批量取消）
	@TableField("target_user_ids")
	private String targetUserIds; // 目标用户ID列表（JSON格式）
	@TableField("operate_time")
	private LocalDateTime operateTime;
	@TableField("status")
	private Integer status; // 状态（1=有效，2=已撤销）

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getOperateType() {
		return operateType;
	}

	public void setOperateType(Integer operateType) {
		this.operateType = operateType;
	}

	public String getTargetUserIds() {
		return targetUserIds;
	}

	public void setTargetUserIds(String targetUserIds) {
		this.targetUserIds = targetUserIds;
	}

	public LocalDateTime getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(LocalDateTime operateTime) {
		this.operateTime = operateTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}