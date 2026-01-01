package org.doubao.dialog.service.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员回复请求DTO
 * 用途：封装管理员对用户咨询的回复信息，包含对话标识、管理员身份、回复内容
 * 适用场景：管理员在后台管理系统中回复用户咨询时使用
 */
public class AdminReplyRequest {

	/**
	 * 对话ID
	 * 业务说明：标识管理员回复所属的对话，用于关联回复与原对话
	 * 校验规则：不能为空，必须为正整数
	 * 数据来源：从对话列表获取的对话唯一标识
	 */
	@NotNull(message = "对话ID不能为空")
	private Long dialogId;

	/**
	 * 管理员ID
	 * 业务说明：标识执行回复操作的管理员身份，用于权限控制和日志记录
	 * 校验规则：不能为空，必须为正整数
	 * 数据来源：管理员登录后获取的用户ID
	 */
	@NotNull(message = "管理员ID不能为空")
	private Long adminId;

	/**
	 * 回复内容
	 * 业务说明：管理员对用户咨询的具体回复内容，支持文本格式
	 * 校验规则：不能为空，长度限制根据业务需求配置
	 * 输入要求：需符合平台规范，不得包含敏感内容
	 */
	@NotBlank(message = "回复内容不能为空")
	private String content;

	public @NotNull(message = "对话ID不能为空") Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(@NotNull(message = "对话ID不能为空") Long dialogId) {
		this.dialogId = dialogId;
	}

	public @NotNull(message = "管理员ID不能为空") Long getAdminId() {
		return adminId;
	}

	public void setAdminId(@NotNull(message = "管理员ID不能为空") Long adminId) {
		this.adminId = adminId;
	}

	public @NotBlank(message = "回复内容不能为空") String getContent() {
		return content;
	}

	public void setContent(@NotBlank(message = "回复内容不能为空") String content) {
		this.content = content;
	}
}