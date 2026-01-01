package org.doubao.dialog.service.enums;

/**
 * 消息类型枚举
 * 对应MessageResendReq中的msgType字段，与消息存储表的msg_type字段一致
 * 业务说明：定义系统支持的消息类型，用于消息分类存储、处理和显示
 * 适用场景：消息发送、消息重发、消息类型判断
 */
public enum MsgTypeEnum {

	/**
	 * 文本消息
	 * 业务说明：纯文本内容的消息，支持中英文、数字、符号等
	 * 编码值：TEXT
	 * 使用场景：普通文字聊天、问题描述、文本回复
	 */
	TEXT("TEXT", "文本消息"),
	/**
	 * 图片消息
	 * 业务说明：包含图片资源的消息，存储图片URL或文件ID
	 * 编码值：IMAGE
	 * 使用场景：图片分享、截图发送、视觉内容展示
	 */
	IMAGE("IMAGE", "图片消息"),
	/**
	 * 语音消息
	 * 业务说明：包含语音资源的消息，存储语音文件URL或ID
	 * 编码值：VOICE
	 * 使用场景：语音聊天、语音回复、语音留言
	 */
	VOICE("VOICE", "语音消息"),
	/**
	 * 文件消息
	 * 业务说明：包含文件资源的消息，存储文件URL或ID
	 * 编码值：FILE
	 * 使用场景：文档分享、资料传输、文件发送
	 */
	FILE("FILE", "文件消息"),
	/**
	 * 系统通知消息
	 * 业务说明：系统自动生成的通知消息，如"对方已读"、"消息撤回"等
	 * 编码值：SYSTEM
	 * 使用场景：系统状态通知、消息状态变更、系统提示
	 */
	SYSTEM("SYSTEM", "系统通知消息"); // 扩展：系统消息（如"对方已读"）

	/**
	 * 消息类型编码
	 * 业务说明：存储到数据库的编码值，用于数据持久化和查询
	 */
	private final String code; // 存储到数据库的编码
	/**
	 * 消息类型描述
	 * 业务说明：用于前端展示和日志打印的友好描述
	 */
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