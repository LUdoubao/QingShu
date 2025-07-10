package org.doubao.like.service.enums;

public enum LikeAction {
	LIKE(1, "like"),
	CANCEL(2, "cancel");

	private Integer type;
	private String name;

	private LikeAction(Integer type, String name) {
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
}
