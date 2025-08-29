package org.doubao.mall.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EventType {
	// 动态流相关事件
	DYNAMIC_PUBLISH("动态发布"),
	DYNAMIC_TOP_LIST("动态上榜"),
	DYNAMIC_DELETE("动态删除"),
	LIKE_CANCEL("取消点赞"),

	// 用户相关事件
	USER_REGISTER("用户注册"),
	COMMENT_EVENT("评论事件"),
	LIKE_EVENT("点赞事件"),

	// 引文事件
	QUOTE_EVENT("引文事件");


	private final String description;

	EventType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * 获取所有事件类型的路由键
	 */
	public static List<String> getAllKeys() {
		return Arrays.stream(values())
				.map(type -> "event.key." + type.name())
				.collect(Collectors.toList());
	}
}
