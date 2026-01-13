package org.doubao.dialog.service.dto;

import org.doubao.dialog.service.enums.ChatModule;

import javax.validation.constraints.NotBlank;

/**
 * 消息请求DTO
 * 用途：封装用户发送消息的完整信息，包括对话上下文、用户信息、模块类型等
 * 适用场景：用户在前端界面发送消息时，后端接收并处理的消息参数对象
 */
public class MessageRequest {

	/**
	 * 对话ID（首次发送可为空）
	 * 业务说明：标识消息所属的对话，新对话首次发送时为空，后续消息需传递对话ID
	 * 数据来源：首次发送时由系统创建，后续从对话列表获取
	 * 关联关系：关联assistant_dialog表的主键ID
	 */
	private Long dialogId;


	/**
	 * 消息内容
	 * 业务说明：用户发送的具体消息内容，用于AI理解用户意图并生成回复
	 * 校验规则：不能为空，长度限制根据业务需求配置
	 * 内容类型：支持文本、表情、问题描述等
	 */
	@NotBlank(message = "消息内容不能为空")
	private String content;
	/**
	 * 用户ID（未登录可为空）
	 * 业务说明：标识发送消息的用户身份，用于个性化服务和权限控制
	 * 数据来源：用户登录后从Token解析获取
	 * 使用场景：区分不同用户的消息，支持个性化回复和历史记录
	 */
	private Long userId;

	/**
	 * 模块名称
	 * 业务说明：标识用户当前使用的功能模块，用于AI调整回复策略和内容
	 * 枚举值：参考ChatModule枚举（HELP-帮助助手、POETRY-诗词助手等）
	 * 影响范围：AI系统提示语、回复内容、功能特性等
	 * @see ChatModule
	 */
	private String module;

	/**
	 * AI类型
	 * 业务说明：指定使用的AI助手类型，用于路由到不同的AI处理逻辑
	 * 枚举值：HELP（帮助助手）、POETRY（诗词助手）、TITLE（标题生成）等
	 * 业务逻辑：不同类型AI有不同的系统提示语和回复策略
	 */
	private String aiType;

	/**
	 * AI模型名称
	 * 业务说明：指定使用的具体AI模型，影响回复质量和性能表现
	 * 枚举值：gpt-3.5-turbo、claude-2、自定义模型等
	 * 配置来源：可从配置文件获取默认值，支持动态指定
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