package org.doubao.dialog.service.vo;

import lombok.Data;
import org.doubao.dialog.service.entity.AssistantMessage;

import java.time.LocalDateTime;

/**
 * 对话列表视图对象
 */
public class DialogVO {

	/**
	 * 对话ID
	 */
	private Long dialogId;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 最后一条消息
	 */
	private AssistantMessage lastMessage;

	private Integer messageCount;

	public Integer getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}

	/**
	 * 状态：0-活跃 1-已解决 2-待跟进
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private LocalDateTime createdTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public AssistantMessage getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(AssistantMessage lastMessage) {
		this.lastMessage = lastMessage;
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
}