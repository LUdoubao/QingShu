package org.doubao.dialog.service.dto;

import java.util.List;

/**
 * AI服务请求DTO
 * 用途：封装调用AI服务所需的完整请求参数，包括对话上下文、模型配置、用户信息等
 * 适用场景：用户发送消息时，系统构建AI请求参数并调用AI服务生成回复
 */
public class AIRequest {

	/**
	 * 对话上下文消息列表
	 * 业务说明：包含完整的对话历史（系统提示、用户消息、AI回复），用于AI理解上下文语境
	 * 数据格式：[{"role":"system","content":"你是引文网站的智能助手..."}, {"role":"user","content":"你好"}, {"role":"assistant","content":"你好，有什么可以帮助你的吗？"}]
	 */
	private List<ChatMessage> messages;

	/**
	 * 模型名称
	 * 业务说明：指定使用的AI模型（如gpt-3.5-turbo、claude-2等），影响回复质量和性能
	 * 配置来源：通过application.yml配置文件指定默认模型，支持动态切换
	 */
	private String model;

	/**
	 * 最大token数
	 * 业务说明：限制AI回复的最大长度，防止过长回复影响用户体验和系统性能
	 * 默认值：200（可通过配置文件调整）
	 */
	private Integer maxTokens;

	/**
	 * 温度参数
	 * 业务说明：控制AI回复的随机性和创造性，值越高回复越随机多变，值越低越确定性
	 * 默认值：0.7（可通过配置文件调整）
	 */
	private Double temperature;

	/**
	 * AI类型
	 * 业务说明：区分不同类型的AI助手（如帮助助手、诗词助手等），用于路由到不同处理逻辑
	 * 枚举值：HELP（帮助助手）、POETRY（诗词助手）等
	 */
	private String aiType;

	/**
	 * 用户ID
	 * 业务说明：标识请求AI服务的用户，用于个性化回复、使用统计、权限控制
	 * 数据来源：从用户Token解析获取，确保请求的合法性
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