package org.doubao.dialog.service.enums;


/**
 * 对话状态枚举
 */
public enum DialogStatusEnum {

	ACTIVE(0, "活跃"),
	RESOLVED(1, "已解决"),
	PENDING(2, "待跟进");

	private final Integer code;
	private final String desc;

	DialogStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DialogStatusEnum getByCode(Integer code) {
		for (DialogStatusEnum status : values()) {
			if (status.getCode().equals(code)) {
				return status;
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}