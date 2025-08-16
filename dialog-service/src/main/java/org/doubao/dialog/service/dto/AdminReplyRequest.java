package org.doubao.dialog.service.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员回复请求DTO
 */
public class AdminReplyRequest {

	/**
	 * 对话ID
	 */
	@NotNull(message = "对话ID不能为空")
	private Long dialogId;

	/**
	 * 管理员ID
	 */
	@NotNull(message = "管理员ID不能为空")
	private Long adminId;

	/**
	 * 回复内容
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