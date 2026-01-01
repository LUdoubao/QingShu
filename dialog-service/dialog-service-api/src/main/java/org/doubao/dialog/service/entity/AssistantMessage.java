package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话消息表实体类
 * 业务说明：存储对话消息的详细信息，包括消息内容、发送者、发送时间等
 * 数据表：assistant_message
 * 适用场景：AI助手对话历史记录、消息检索、对话状态跟踪
 */
@TableName("assistant_message")
public class AssistantMessage {

	/**
	 * 消息ID
	 * 业务说明：消息记录的唯一标识符，自增主键
	 * 数据类型：Long类型，自动生成
	 * 关联关系：作为外键被其他表引用的基础
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 所属对话ID
	 * 业务说明：关联消息所属的对话，用于消息归类和对话历史检索
	 * 数据类型：Long类型，外键关联assistant_dialog表
	 * 使用场景：对话消息查询、对话上下文构建
	 */
	@TableField("dialog_id")
	private Long dialogId;

	/**
	 * 发送者类型：0-用户 1-AI 2-管理员
	 * 业务说明：标识消息发送者的类型，用于区分不同来源的消息
	 * 枚举值：0=USER（用户发送）、1=AI（AI助手回复）、2=ADMIN（管理员回复）
	 * 使用场景：消息显示样式、权限控制、消息处理逻辑
	 */
	@TableField("sender_type")
	private Integer senderType;

	/**
	 * 发送者ID（用户/管理员ID）
	 * 业务说明：发送消息的用户或管理员的唯一标识符
	 * 数据类型：Long类型，关联用户或管理员表
	 * 使用场景：消息归属判断、权限验证、用户信息查询
	 */
	@TableField("sender_id")
	private Long senderId;

	/**
	 * 消息内容
	 * 业务说明：消息的具体文本内容，可能包含用户问题、AI回复、管理员回复等
	 * 数据格式：纯文本，支持中英文、数字、符号等
	 * 内容限制：根据业务需求配置长度限制
	 */
	@TableField("content")
	private String content;

	/**
	 * 发送时间
	 * 业务说明：消息的发送时间戳，用于消息排序和时间线展示
	 * 数据类型：LocalDateTime，精确到秒
	 * 时区处理：系统默认时区（东八区）
	 * 使用场景：消息时间显示、对话历史按时间排序
	 */
	@TableField("send_time")
	private LocalDateTime sendTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSenderType() {
		return senderType;
	}

	public void setSenderType(Integer senderType) {
		this.senderType = senderType;
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSendTime() {
		return sendTime;
	}

	public void setSendTime(LocalDateTime sendTime) {
		this.sendTime = sendTime;
	}
}