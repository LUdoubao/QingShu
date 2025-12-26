package org.doubao.dialog.service.dto;

import java.util.List;

/**
 * AI服务请求DTO
 */
public class AIRequest {

	/**
	 * 对话上下文消息列表
	 */
	private List<ChatMessage> messages;

	/**
	 * 模型名称
	 */
	private String model;

	/**
	 * 最大token数
	 */
	private Integer maxTokens;

	/**
	 * 温度参数
	 */
	private Double temperature;

	/**
	 * AI类型
	 *
	 */
	private String aiType;

	/**
	 * 用户ID
	 */
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAiType() {
		return aiType;
	}

	public void setAiType(String aiType) {
		this.aiType = aiType;
	}

	public List<ChatMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessage> messages) {
		this.messages = messages;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
}