package org.doubao.search.service;

/**
 * 搜索类型枚举
 */
public enum SearchType {
	QUOTE_CONTENT("quote_content", "文案内容"),
	QUOTE_SOURCE("quote_source", "文案来源"),
	QUOTE_AUTHOR("quote_author", "文案作者"),
	USER("user", "用户"),
	CATEGORY("category", "分类"),
	TAG("tag", "标签");
	private String value;
	private String desc;
	SearchType(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}
	public String getValue() {
		return value;
	}
	public String getDesc() {
		return desc;
	}
	public static SearchType getByValue(String value) {
		for (SearchType type : SearchType.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		return null;
	}
}