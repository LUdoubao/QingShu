package org.doubao.dialog.service.dto;

import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
public class MessageDTO {

	/**
	 * 对话ID
	 */
	private Long dialogId;

	/**
	 * 消息内容
	 */
	private String content;

	/**
	 * 是否管理员回复
	 */
	private Boolean isAdmin;

	/**
	 * 发送时间
	 */
	private LocalDateTime sendTime;

	/**
	 * 发送者类型：0-用户 1-AI 2-管理员
	 */
	private Integer senderType;

	/**
	 * 标题
	 */
	private String title;



	public MessageDTO(Long dialogId, String content, Boolean isAdmin, LocalDateTime sendTime, Integer senderType) {
		this.dialogId = dialogId;
		this.content = content;
		this.isAdmin = isAdmin;
		this.sendTime = sendTime;
		this.senderType = senderType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public MessageDTO() {
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getAdmin() {
		return isAdmin;
	}

	public void setAdmin(Boolean admin) {
		isAdmin = admin;
	}

	public LocalDateTime getSendTime() {
		return sendTime;
	}

	public void setSendTime(LocalDateTime sendTime) {
		this.sendTime = sendTime;
	}

	public Integer getSenderType() {
		return senderType;
	}

	public void setSenderType(Integer senderType) {
		this.senderType = senderType;
	}
}