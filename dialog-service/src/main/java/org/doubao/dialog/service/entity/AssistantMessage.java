package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话消息表实体类
 */
@TableName("assistant_message")
public class AssistantMessage {

	/**
	 * 消息ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 所属对话ID
	 */
	@TableField("dialog_id")
	private Long dialogId;

	/**
	 * 发送者类型：0-用户 1-AI 2-管理员
	 */
	@TableField("sender_type")
	private Integer senderType;

	/**
	 * 发送者ID（用户/管理员ID）
	 */
	@TableField("sender_id")
	private Long senderId;

	/**
	 * 消息内容
	 */
	@TableField("content")
	private String content;

	/**
	 * 发送时间
	 */
	@TableField("send_time")
	private LocalDateTime sendTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSenderType() {
		return senderType;
	}

	public void setSenderType(Integer senderType) {
		this.senderType = senderType;
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSendTime() {
		return sendTime;
	}

	public void setSendTime(LocalDateTime sendTime) {
		this.sendTime = sendTime;
	}
}