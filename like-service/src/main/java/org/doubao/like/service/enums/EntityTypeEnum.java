package org.doubao.like.service.enums;

// EntityTypeEnum.java
public enum EntityTypeEnum {
	CONTENT(0, "CONTENT"),  // 文案
	COMMENT(1, "COMMENT");  // 评论
	private Integer type;
	private String name;
	EntityTypeEnum(Integer type, String name) {
		this.type = type;
		this.name = name;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public static String getNameByType(Integer type) {
		for (EntityTypeEnum value : EntityTypeEnum.values()) {
			if (value.getType().equals(type)) {
				return value.getName();
			}
		}
		return null;
	}

	public static EntityTypeEnum getByName(String name) {
		for (EntityTypeEnum value : EntityTypeEnum.values()) {
			if (value.getName().equals(name)) {
				return value;
			}
		}
		return null;
	}
}