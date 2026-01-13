package org.doubao.dialog.service.enums;

/**
 * 消息清空范围枚举
 * 业务说明：定义消息清空操作的不同范围，用于区分清空全部消息、指定时间前消息或指定类型消息
 * 适用场景：用户手动清空聊天记录、系统自动清理过期消息
 */
public enum ClearScopeEnum {

	/**
	 * 全部消息
	 * 业务说明：清空会话中的所有消息记录
	 * 编码值：ALL
	 * 使用场景：彻底清理会话历史，释放存储空间
	 */
	ALL("ALL", "全部消息"),
	/**
	 * 指定时间前消息
	 * 业务说明：清空指定时间点之前的所有消息，保留指定时间后的消息
	 * 编码值：BEFORE_TIME
	 * 使用场景：清理过期历史消息，保留近期对话记录
	 */
	BEFORE_TIME("BEFORE_TIME", "指定时间前消息"),
	/**
	 * 指定类型消息
	 * 业务说明：仅清空指定类型的消息（如仅清空文字消息或图片消息）
	 * 编码值：SPECIFIC_TYPE
	 * 使用场景：按消息类型清理，保留其他类型消息
	 */
	SPECIFIC_TYPE("SPECIFIC_TYPE", "指定类型消息");

	/**
	 * 枚举编码
	 * 业务说明：存储到数据库和日志中的编码值，用于数据持久化和查询
	 */
	private final String code; // 存储到数据库/日志的编码
	/**
	 * 枚举描述
	 * 业务说明：用于前端展示和日志说明的友好描述
	 */
	private final String desc; // 描述（用于前端展示/日志说明）

	ClearScopeEnum(String code, String desc) {
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
	 * 根据编码获取枚举（用于参数解析/数据库查询转换）
	 * @param code 清空范围编码（如"ALL"）
	 * @return 对应的枚举值，无匹配时返回null
	 */
	public static ClearScopeEnum getByCode(String code) {
		for (ClearScopeEnum scope : values()) {
			if (scope.getCode().equals(code)) {
				return scope;
			}
		}
		return null;
	}
}