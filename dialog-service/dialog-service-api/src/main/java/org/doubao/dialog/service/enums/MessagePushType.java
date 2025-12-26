package org.doubao.dialog.service.enums;

public enum MessagePushType {
	MSG_READ("MSG_READ", "已读消息确认"),
	PRIVATE_MSG("PRIVATE_MSG", "私信消息"),
	USER_ONLINE("USER_ONLINE", "用户上线"),
	USER_OFFLINE("USER_OFFLINE", "用户下线");
	private  final String name;
	private  final String desc;
	MessagePushType(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}
}