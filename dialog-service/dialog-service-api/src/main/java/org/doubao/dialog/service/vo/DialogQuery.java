package org.doubao.dialog.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 对话查询视图对象
 * 适用场景：
 * 1. 管理员查询对话列表（/admin/dialog/list接口）
 * 2. 支持按状态、用户、时间范围、关键词等多维度筛选对话记录
 * 3. 为后台管理界面提供灵活的对话查询能力
 * 业务说明：定义对话查询的筛选条件，用于管理员对用户对话进行检索和管理
 */
@ApiModel(description = "对话查询条件对象，支持按状态、用户、时间范围等筛选")
public class DialogQuery {

	/**
	 * 查询状态条件
	 * 枚举约束：0（活跃对话-用户正在交互中）、1（已解决对话-问题已处理）、2（待跟进对话-需人工介入）
	 * 业务规则：
	 * 1. 活跃对话：最近24小时内有消息交互，或状态为"处理中"
	 * 2. 已解决对话：用户标记为"问题已解决"或管理员确认处理完成
	 * 3. 待跟进对话：超过24小时无新消息，或用户标记"需要帮助"
	 * 业务说明：用于按对话处理状态进行筛选，便于管理员分层管理对话
	 * 数据校验：可选参数，传null时查询所有状态的对话
	 * 使用场景：管理员按状态筛选对话，优先处理待跟进对话
	 */
	@ApiModelProperty(
			value = "对话状态筛选条件（0-活跃，1-已解决，2-待跟进）",
			required = false,
			example = "0",
			allowableValues = "0,1,2",
			notes = "传null时查询所有状态的对话记录"
		)
	private Integer status;

	/**
	 * 查询用户ID条件
	 * 业务规则：
	 * 1. 仅查询指定用户ID的对话记录（用于管理员定位特定用户对话）
	 * 2. 需校验用户ID有效性（通过user-service接口验证用户存在性）
	 * 业务说明：用于按用户维度筛选对话，便于管理员追踪特定用户交互
	 * 数据校验：可选参数，传null时查询所有用户的对话
	 * 使用场景：管理员查询特定用户的历史对话记录
	 */
	@ApiModelProperty(
			value = "用户ID筛选条件",
			required = false,
			example = "123456",
			notes = "传null时查询所有用户对话记录"
		)
	private Long userId;

	/**
	 * 查询开始时间
	 * 业务规则：
	 * 1. 与endTime配合使用，查询[startTime, endTime]时间范围内的对话
	 * 2. 时间格式：YYYY-MM-DD，精确到日期（实际查询时转换为当天00:00:00）
	 * 3. 仅当startTime和endTime同时传递时，才进行时间范围筛选
	 * 业务说明：用于按时间维度筛选对话，便于管理员按时间段检索
	 * 数据校验：可选参数，传null时不进行时间范围筛选
	 * 使用场景：管理员查询指定时间范围内的对话记录
	 */
	@ApiModelProperty(
			value = "查询开始时间（YYYY-MM-DD）",
			required = false,
			example = "2024-01-01",
			notes = "需与endTime配合使用，仅当两者都非空时生效"
		)
	private LocalDate startTime;

	/**
	 * 查询结束时间
	 * 业务规则：
	 * 1. 与startTime配合使用，查询[startTime, endTime]时间范围内的对话
	 * 2. 时间格式：YYYY-MM-DD，精确到日期（实际查询时转换为当天23:59:59）
	 * 3. 仅当startTime和endTime同时传递时，才进行时间范围筛选
	 * 业务说明：用于按时间维度筛选对话，便于管理员按时间段检索
	 * 数据校验：可选参数，传null时不进行时间范围筛选
	 * 使用场景：管理员查询指定时间范围内的对话记录
	 */
	@ApiModelProperty(
			value = "查询结束时间（YYYY-MM-DD）",
			required = false,
			example = "2024-01-31",
			notes = "需与startTime配合使用，仅当两者都非空时生效"
		)
	private LocalDate endTime;

	/**
	 * 关键词搜索条件
	 * 业务规则：
	 * 1. 支持在对话内容中进行模糊匹配（通过MySQL LIKE %keyword%查询）
	 * 2. 长度限制：最大支持50个字符（避免慢查询影响系统性能）
	 * 3. 敏感词处理：关键词不进行敏感词过滤，但对话内容需正常过滤
	 * 业务说明：用于在对话内容中进行关键词检索，便于管理员快速定位
	 * 数据校验：可选参数，传null或空字符串时不进行关键词筛选
	 * 使用场景：管理员通过关键词搜索特定内容的对话记录
	 */
	@ApiModelProperty(
			value = "关键词搜索条件（在对话内容中模糊匹配）",
			required = false,
			example = "问题解决",
			notes = "传null或空字符串时不进行关键词筛选，最大支持50字符"
		)
	private String keyword;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDate getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDate startTime) {
		this.startTime = startTime;
	}

	public LocalDate getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDate endTime) {
		this.endTime = endTime;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}