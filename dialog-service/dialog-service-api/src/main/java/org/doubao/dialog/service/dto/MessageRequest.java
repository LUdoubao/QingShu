package org.doubao.dialog.service.dto;

import org.doubao.dialog.service.enums.ChatModule;

import javax.validation.constraints.NotBlank;

/**
 * 消息请求DTO
 */
public class MessageRequest {

	/**
	 * 对话ID（首次发送可为空）
	 */
	private Long dialogId;


	/**
	 * 消息内容
	 */
	@NotBlank(message = "消息内容不能为空")
	private String content;
	/**
	 * 用户ID（未登录可为空）
	 */
	private Long userId;

	/**
	 * 模块名称
	 * @see ChatModule
	 */
	private String module;

	/**
	 * AI类型
	 */
	private String aiType;

	/**
	 * AI模型名称
	 */
	private String aiModel;

	public String getAiType() {
		return aiType;
	}

	public void setAiType(String aiType) {
		this.aiType = aiType;
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