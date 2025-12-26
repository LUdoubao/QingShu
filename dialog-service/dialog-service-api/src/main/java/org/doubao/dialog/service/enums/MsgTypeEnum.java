package org.doubao.dialog.service.enums;

/**
 * 消息类型枚举
 * 对应MessageResendReq中的msgType字段，与消息存储表的msg_type字段一致
 */
public enum MsgTypeEnum {

	TEXT("TEXT", "文本消息"),
	IMAGE("IMAGE", "图片消息"),
	VOICE("VOICE", "语音消息"),
	FILE("FILE", "文件消息"),
	SYSTEM("SYSTEM", "系统通知消息"); // 扩展：系统消息（如"对方已读"）

	private final String code; // 存储到数据库的编码
	private final String desc; // 类型描述（用于前端展示/日志打印）

	MsgTypeEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	/**
	 * 根据编码获取枚举（用于数据库查询结果转换）
	 * @param code 消息类型编码（如"TEXT"）
	 * @return 对应的枚举值，无匹配时返回null
	 */
	public static MsgTypeEnum getByCode(String code) {
		for (MsgTypeEnum type : values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}
}