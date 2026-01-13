package org.doubao.dialog.service.enums;

/**
 * 消息推送类型枚举
 * 业务说明：定义WebSocket消息推送的不同类型，用于区分不同类型的消息推送
 * 适用场景：实时消息推送、状态变更通知、在线状态更新
 */
public enum MessagePushType {
	/**
	 * 已读消息确认
	 * 业务说明：通知发送方消息已被接收方阅读
	 * 推送内容：消息ID列表、已读时间等
	 * 使用场景：消息已读状态同步、消息状态确认
	 */
	MSG_READ("MSG_READ", "已读消息确认"),

	/**
	 * 发送消息确认
	 * 业务说明：通知发送方消息已发送成功
	 * 推送内容：消息ID列表、发送时间等
	 * 使用场景：消息发送状态确认、消息状态同步
	 */
	SENT("SENT", "发送消息确认"),
	/**
	 * 私信消息
	 * 业务说明：推送私信消息给接收方
	 * 推送内容：消息内容、发送者信息、时间等
	 * 使用场景：实时私信消息推送、聊天消息传输
	 */
	PRIVATE_MSG("PRIVATE_MSG", "私信消息"),
	/**
	 * 用户上线
	 * 业务说明：通知相关用户目标用户已上线
	 * 推送内容：用户ID、上线状态等
	 * 使用场景：在线状态同步、好友状态更新
	 */
	USER_ONLINE("USER_ONLINE", "用户上线"),
	/**
	 * 用户下线
	 * 业务说明：通知相关用户目标用户已下线
	 * 推送内容：用户ID、下线状态等
	 * 使用场景：在线状态同步、好友状态更新
	 */
	USER_OFFLINE("USER_OFFLINE", "用户下线");
	/**
	 * 推送类型名称
	 * 业务说明：用于WebSocket消息类型标识
	 */
	private  final String name;
	/**
	 * 推送类型描述
	 * 业务说明：用于日志记录和调试信息显示
	 */
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