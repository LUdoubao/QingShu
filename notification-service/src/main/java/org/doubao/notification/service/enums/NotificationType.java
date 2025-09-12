package org.doubao.notification.service.enums;

public enum NotificationType {
	SYSTEM("SYSTEM", "系统"),
	COMMENT("COMMENT", "评论"),
	CHAT("CHAT", "聊天"),
	LIKE("LIKE", "点赞");
	private  final String value;
	private  final String label;
	NotificationType(String value, String label) {
		this.value = value;
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public String getLabel() {
		return label;
	}
	public static NotificationType getByValue(String value) {
		for (NotificationType type : NotificationType.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		return null;
	}
}
