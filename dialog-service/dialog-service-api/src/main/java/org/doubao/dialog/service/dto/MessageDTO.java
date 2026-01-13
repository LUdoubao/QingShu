package org.doubao.dialog.service.dto;

import java.time.LocalDateTime;

/**
 * 消息响应DTO
 * 用途：封装消息处理的响应结果，包括对话信息、消息内容、发送者类型等
 * 适用场景：用户发送消息后，系统返回AI回复或管理员回复的完整信息
 */
public class MessageDTO {

	/**
	 * 对话ID
	 * 业务说明：标识消息所属的对话，用于关联对话历史和上下文
	 * 数据来源：新建对话时生成的唯一标识，或从现有对话中获取
	 * 关联关系：关联assistant_dialog表的主键ID
	 */
	private Long dialogId;

	/**
	 * 消息内容
	 * 业务说明：消息的具体文本内容，可能是AI回复或管理员回复
	 * 数据格式：纯文本内容，支持中英文、数字、符号等
	 * 内容类型：AI生成的回复、管理员手动回复、系统提示等
	 */
	private String content;

	/**
	 * 是否管理员回复
	 * 业务说明：标识消息是否为管理员回复，用于前端区分显示样式
	 * 布尔值：true=管理员回复，false=AI回复
	 * 显示逻辑：前端根据此字段显示不同的消息气泡样式
	 */
	private Boolean isAdmin;

	/**
	 * 发送时间
	 * 业务说明：消息的发送时间戳，用于消息排序和时间显示
	 * 时间格式：LocalDateTime格式，精确到秒
	 * 时区处理：系统默认时区（通常为东八区）
	 */
	private LocalDateTime sendTime;

	/**
	 * 发送者类型：0-用户 1-AI 2-管理员
	 * 业务说明：标识消息的发送者类型，用于区分不同来源的消息
	 * 枚举值：0=用户发送、1=AI助手回复、2=管理员回复
	 * 使用场景：前端根据发送者类型显示不同的头像、昵称等信息
	 */
	private Integer senderType;

	/**
	 * 标题
	 * 业务说明：对话标题，用于标识对话主题和内容概要
	 * 生成方式：AI根据对话内容自动生成，或管理员手动设置
	 * 显示位置：对话列表、对话详情页头部等
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