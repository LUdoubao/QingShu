package org.doubao.dialog.service.enums;


/**
 * 发送者类型枚举
 * 业务说明：定义消息发送者的类型，用于区分消息来源和处理不同类型的发送者逻辑
 * 适用场景：消息分类处理、权限控制、消息样式区分
 */
public enum SenderTypeEnum {

	/**
	 * 用户
	 * 业务说明：普通用户发送的消息
	 * 编码值：0
	 * 使用场景：用户间聊天、用户与AI对话中的用户消息
	 */
	USER(0, "用户"),
	/**
	 * AI助手
	 * 业务说明：AI助手自动回复的消息
	 * 编码值：1
	 * 使用场景：AI助手回复、AI助手主动推送消息
	 */
	AI(1, "AI"),
	/**
	 * 管理员
	 * 业务说明：系统管理员发送的消息
	 * 编码值：2
	 * 使用场景：客服回复、系统通知、管理员手动回复
	 */
	ADMIN(2, "管理员");

	/**
	 * 发送者类型编码
	 * 业务说明：存储到数据库的编码值，用于数据持久化和查询
	 */
	private final Integer code;
	/**
	 * 发送者类型描述
	 * 业务说明：用于前端展示和日志打印的友好描述
	 */
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