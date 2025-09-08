package org.doubao.mall.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EventType {
	// 动态流相关事件
	DYNAMIC_PUBLISH("动态发布", 1),
	DYNAMIC_TOP_LIST("动态上榜", 2),
	DYNAMIC_DELETE("动态删除", 3),
	LIKE_CANCEL("取消点赞", 4),

	// 用户相关事件
	USER_REGISTER("用户注册", 5),
	COMMENT_EVENT("评论事件", 6),
	LIKE_EVENT("点赞事件",7),

	// 聊天相关事件
	NEW_MESSAGE("新消息", 9),

	// 引文事件
	QUOTE_EVENT("引文事件", 8);

	private final int value;

	private final String description;

	EventType( String description, int value) {
		this.value = value;
		this.description = description;
	}

	public int getValue() {
		return value;
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
