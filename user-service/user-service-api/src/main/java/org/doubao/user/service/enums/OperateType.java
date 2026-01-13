package org.doubao.user.service.enums;

public enum OperateType {
	FOLLOW(1, "关注"),
	UNFOLLOW(2, "取消关注"),
	BATCH_FOLLOW(3, "批量关注"),
	BATCH_UNFOLLOW(4, "批量取消关注");

	private final int value;
	private final String desc;
	public int getValue() {
		return value;
	}
	public String getDesc() {
		return desc;
	}
	OperateType(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	public static OperateType getByValue(int value) {
		for (OperateType type : OperateType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
}