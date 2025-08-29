package org.doubao.fanout.service.enums;

public enum TargetType {
	QUEUE("队列"),
	TOPIC("主题"),
	SERVICE("服务接口");

	private final String description;

	TargetType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
