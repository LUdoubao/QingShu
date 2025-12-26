package org.doubao.dialog.service.enums;


/**
 * 发送者类型枚举
 */
public enum SenderTypeEnum {

	USER(0, "用户"),
	AI(1, "AI"),
	ADMIN(2, "管理员");

	private final Integer code;
	private final String desc;

	SenderTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SenderTypeEnum getByCode(Integer code) {
		for (SenderTypeEnum type : values()) {
			if (type.getCode().equals(code)) {
				return type;
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