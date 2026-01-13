package org.doubao.dialog.service.vo;




import org.doubao.dialog.service.entity.AssistantMessage;

import java.time.LocalDateTime;

/**
 * 对话列表视图对象
 * 适用场景：
 * 1. 管理员对话列表界面展示（/admin/dialog/list接口返回）
 * 2. 提供对话的核心信息（ID、用户、标题、状态、消息数等）
 * 3. 便于管理员快速了解对话概况，进行分层管理
 * 业务说明：定义对话列表页展示的视图对象，封装对话的基本信息和状态
 */
//@ApiModel(description = "对话列表视图对象，包含对话ID、用户ID、标题、状态等信息")
public class DialogVO {

	/**
	 * 对话唯一标识ID
	 * 业务规则：
	 * 1. 与assistant_dialog表的主键ID保持一致
	 * 2. 用于对话详情页跳转、对话操作等场景
	 * 业务说明：唯一标识一个对话记录，用于对话的关联和查询
	 * 数据格式：64位长整型
	 * 使用场景：对话详情页跳转、对话操作、关联查询
	 */
	private Long dialogId;

	/**
	 * 用户ID
	 * 业务规则：
	 * 1. 与assistant_dialog表的user_id字段保持一致
	 * 2. 用于标识对话的发起用户，便于管理员追踪用户行为
	 * 业务说明：标识对话所属的用户，用于用户维度的对话管理
	 * 数据格式：64位长整型
	 * 使用场景：用户对话统计、用户行为分析、用户对话详情跳转
	 */
	private Long userId;

	/**
	 * 对话标题
	 * 业务规则：
	 * 1. 通常为对话第一条消息的摘要（截取前30个字符）
	 * 2. 若第一条消息过短，则使用完整内容作为标题
	 * 3. 若为AI对话，可能根据AI回复生成智能标题
	 * 业务说明：提供对话的简要描述，便于管理员快速识别对话内容
	 * 数据格式：最大50个字符
	 * 使用场景：对话列表展示、对话快速定位
	 */
	private String title;

	/**
	 * 最后一条消息
	 * 业务规则：
	 * 1. 包含最后一条消息的完整信息（内容、时间、发送者等）
	 * 2. 用于展示对话最新进展，便于管理员快速了解对话状态
	 * 业务说明：封装对话的最新消息信息，用于展示对话的最新动态
	 * 数据格式：AssistantMessage对象
	 * 使用场景：对话列表页显示最新消息预览
	 */
	private AssistantMessage lastMessage;

	/**
	 * 消息总数
	 * 业务规则：
	 * 1. 统计该对话下所有有效消息的数量（不包含已删除消息）
	 * 2. 用于衡量对话长度和用户参与度
	 * 业务说明：记录对话中的消息总数，用于对话分析和管理
	 * 数据格式：整型数字
	 * 使用场景：对话复杂度评估、用户参与度分析
	 */
	private Integer messageCount;

	public Integer getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}

	/**
	 * 对话状态
	 * 枚举约束：0（活跃对话-用户正在交互中）、1（已解决对话-问题已处理）、2（待跟进对话-需人工介入）
	 * 业务规则：
	 * 1. 活跃对话：最近24小时内有消息交互，或状态为"处理中"
	 * 2. 已解决对话：用户标记为"问题已解决"或管理员确认处理完成
	 * 3. 待跟进对话：超过24小时无新消息，或用户标记"需要帮助"
	 * 业务说明：标识对话的处理状态，用于管理员分层管理对话
	 * 数据校验：必须为0、1或2
	 * 使用场景：对话状态管理、优先级排序
	 */
	private Integer status;

	/**
	 * 对话创建时间
	 * 业务规则：
	 * 1. 与assistant_dialog表的created_time字段保持一致
	 * 2. 用于对话时间线管理，按时间排序等
	 * 业务说明：记录对话的创建时间，用于时间维度的对话管理
	 * 数据格式：本地时间格式
	 * 使用场景：对话时间线展示、对话排序、历史对话检索
	 */
	private LocalDateTime createdTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getDialogId() {
		return dialogId;
	}

	public void setDialogId(Long dialogId) {
		this.dialogId = dialogId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public AssistantMessage getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(AssistantMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}
}