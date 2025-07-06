package org.doubao.notification.service.enums;

public enum NotificationStatus {
	UNREAD(0, "未读"),
	READ(1, "已读");
	private final String value;
	private final int code;
	NotificationStatus(int code, String value) {
		this.code = code;
		this.value = value;
	}
	public int getCode() {
		return code;
	}
	public String getValue() {
		return value;
	}
	public static NotificationStatus getByValue(String value) {
		for (NotificationStatus status : NotificationStatus.values()) {
			if (status.getValue().equals(value)) {
				return status;
			}
		}
		return null;
	}
}
