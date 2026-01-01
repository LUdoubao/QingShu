package org.doubao.dialog.service.enums;

/**
 * 聊天模块枚举
 * 业务说明：定义不同类型的AI聊天模块，用于区分不同的AI助手功能
 * 适用场景：根据用户选择的模块，调整AI助手的回复策略和内容
 */
public enum ChatModule {
	/**
	 * 网站帮助模块
	 * 业务说明：专门回答网站功能相关的帮助问题，提供网站使用指导
	 * 显示名称：网站帮助
	 * 枚举值：CHAT_HELP
	 */
	CHAT_HELP("网站帮助", "CHAT_HELP"),
	/**
	 * 生成标题模块
	 * 业务说明：根据用户输入内容生成合适的标题，用于内容创作辅助
	 * 显示名称：生成标题
	 * 枚举值：CHAT_TITLE
	 */
	CHAT_TITLE("生成标题", "CHAT_TITLE"),
	/**
	 * 诗词助手模块
	 * 业务说明：专门回答与诗词相关的问题，提供诗词创作、解析等服务
	 * 显示名称：诗词助手
	 * 枚举值：CHAT_POETRY
	 */
	CHAT_POETRY("诗词助手", "CHAT_POETRY");
	/**
	 * 枚举显示名称
	 * 业务说明：用于前端显示的友好名称
	 */
	private final String name;
	/**
	 * 枚举值
	 * 业务说明：用于后端处理和数据库存储的唯一标识
	 */
	private final String value;
	ChatModule(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	public static ChatModule getByValue(String value) {
		for (ChatModule module : ChatModule.values()) {
			if (module.getValue().equals(value)) {
				return module;
			}
		}
		return null;
	}
	public static ChatModule getByName(String name) {
		for (ChatModule module : ChatModule.values()) {
			if (module.getName().equals(name)) {
				return module;
			}
		}
		return null;
	}
}
