package org.doubao.dialog.service.enums;

public enum ClearScopeEnum {

	ALL("ALL", "全部消息"),
	BEFORE_TIME("BEFORE_TIME", "指定时间前消息"),
	SPECIFIC_TYPE("SPECIFIC_TYPE", "指定类型消息");

	private final String code; // 存储到数据库/日志的编码
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