package org.doubao.dialog.service.dto;


/**
 * 聊天消息DTO
 * 用途：封装单条聊天消息的信息，用于AI模型对话上下文的构建
 * 适用场景：构建AI对话历史，包含系统提示、用户消息、AI回复等不同类型的消息
 */
public class ChatMessage {

	/**
	 * 角色：user, assistant, system
	 * 业务说明：标识消息发送者的角色，用于AI模型理解对话上下文
	 * 枚举值：user（用户）、assistant（AI助手）、system（系统提示）
	 * 使用场景：AI模型根据角色区分不同类型的输入，生成相应格式的回复
	 */
	private String role;

	/**
	 * 消息内容
	 * 业务说明：消息的具体文本内容，用于AI模型理解用户意图或提供上下文信息
	 * 数据格式：纯文本，支持中英文、数字、符号等
	 * 内容类型：系统提示语、用户问题、AI回复等
	 */
	private String content;

	public ChatMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}