package org.doubao.user.service.enums;

public enum RelationType {
	FOLLOW(1, "关注"),
	BLOCK(2, "拉黑");

	private final int value;
	private final String desc;

	RelationType(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public int getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public static RelationType getByValue(int value) {
		for (RelationType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		return null;
	}
}