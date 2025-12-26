package org.doubao.dialog.service.enums;

public enum ChatModule {
	CHAT_HELP("网站帮助", "CHAT_HELP"),
	CHAT_TITLE("生成标题", "CHAT_TITLE"),
	CHAT_POETRY("诗词助手", "CHAT_POETRY");
	private final String name;
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
