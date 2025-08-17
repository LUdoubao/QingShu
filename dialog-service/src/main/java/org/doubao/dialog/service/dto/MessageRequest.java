package org.doubao.dialog.service.dto;

import javax.validation.constraints.NotBlank;

/**
 * 消息请求DTO
 */
public class MessageRequest {

	/**
	 * 对话ID（首次发送可为空）
	 */
	private Long dialogId;

	private String title;
	/**
	 * 消息内容
	 */
	@NotBlank(message = "消息内容不能为空")
	private String content;
	/**
	 * 用户ID（未登录可为空）
	 */
	private Long userId;
	private String module;

	private String aiModel;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAiModel() {
		return aiModel;
	}

	public void setAiModel(String aiModel) {
		this.aiModel = aiModel;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public @NotBlank(message = "消息内容不能为空") String getContent() {
		return content;
	}

	public void setContent(@NotBlank(message = "消息内容不能为空") String content) {
		this.content = content;
	}
}