package org.doubao.dialog.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.doubao.dialog.service.enums.ClearScopeEnum;
import org.doubao.dialog.service.enums.MsgTypeEnum;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息清空请求DTO
 * 用途：接收客户端/服务端发起的消息清空请求，支持多维度清空范围（全部消息/指定时间前消息/指定类型消息）
 * 适用场景：用户手动清空聊天记录、会话过期自动清理、敏感消息批量删除
 * 业务说明：定义消息清空操作的请求参数，包含清空范围、目标会话、操作人等必要信息
 */
@ApiModel(value = "MessageClearReq", description = "消息清空请求参数")
public class MessageClearReq implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 会话ID
	 * 说明：关联需清空消息所属的会话，确保清空范围精准（必选，关联dialog_sessions表id）
	 * 业务说明：标识需要清空消息的目标会话，确保操作范围的准确性
	 * 数据校验：不能为空，必须为正整数
	 * 使用场景：用户选择特定会话进行消息清空操作
	 */
	@NotNull(message = "会话ID不能为空")
	@Positive(message = "会话ID必须为正整数")
	@ApiModelProperty(value = "会话ID（关联dialog_sessions表主键）", required = true, example = "123456")
	private Long conversationId;

	/**
	 * 操作人ID
	 * 说明：发起清空操作的用户ID，需为会话合法参与者（避免越权清空他人会话）
	 * 业务说明：标识执行清空操作的用户，用于权限验证和操作记录
	 * 数据校验：不能为空，必须为正整数
	 * 权限控制：仅会话参与者可以清空该会话的消息
	 */
	@NotNull(message = "操作人ID不能为空")
	@Positive(message = "操作人ID必须为正整数")
	@ApiModelProperty(value = "清空操作人ID（需为会话参与者）", required = true, example = "10001")
	private Long operatorId;

	/**
	 * 清空范围（核心参数）
	 * 枚举：ALL=全部消息，BEFORE_TIME=指定时间前消息，SPECIFIC_TYPE=指定类型消息
	 * 业务说明：定义消息清空的具体范围，支持多种清空策略
	 * 枚举类型：ClearScopeEnum（全部消息、指定时间前消息、指定类型消息）
	 * 数据校验：不能为空
	 * 使用场景：根据用户需求选择不同的清空策略
	 */
	@NotNull(message = "清空范围不能为空")
	@ApiModelProperty(value = "清空范围（ALL=全部消息，BEFORE_TIME=指定时间前消息，SPECIFIC_TYPE=指定类型消息）",
			required = true, example = "ALL")
	private ClearScopeEnum clearScope;

	/**
	 * 指定时间（仅clearScope=BEFORE_TIME时必选）
	 * 说明：清空此时间点之前的所有消息（格式：yyyy-MM-dd HH:mm:ss）
	 */
	@ApiModelProperty(value = "指定时间（仅清空范围为BEFORE_TIME时必填，格式：yyyy-MM-dd HH:mm:ss）",
			example = "2025-09-01 00:00:00")
	private LocalDateTime clearTime;

	/**
	 * 指定消息类型列表（仅clearScope=SPECIFIC_TYPE时必选）
	 * 说明：仅清空列表中的消息类型（如[TEXT, IMAGE]表示只清空文本和图片消息）
	 */
	@ApiModelProperty(value = "指定消息类型列表（仅清空范围为SPECIFIC_TYPE时必填，可选值：TEXT/IMAGE/VOICE/FILE/SYSTEM）",
			example = "[\"TEXT\", \"IMAGE\"]")
	private List<MsgTypeEnum> msgTypes;

	/**
	 * 清空原因（可选）
	 * 说明：记录清空触发原因，用于日志审计和问题排查（如用户手动清空、系统自动清理）
	 */
	@Size(max = 200, message = "清空原因长度不能超过200字符")
	@ApiModelProperty(value = "清空原因（用于日志审计）", example = "用户手动清空半年前的聊天记录")
	private String clearReason;

	/**
	 * 自定义校验：清空范围对应的参数完整性
	 * 逻辑：
	 * 1. 范围为BEFORE_TIME时，clearTime必须非空
	 * 2. 范围为SPECIFIC_TYPE时，msgTypes必须非空且非空列表
	 * @return 校验结果（true=通过，false=失败）
	 */
	public boolean validateScopeParams() {
		// 1. 校验"指定时间前"的参数
		if (ClearScopeEnum.BEFORE_TIME.equals(this.clearScope) && this.clearTime == null) {
			return false;
		}
		// 2. 校验"指定类型"的参数
		if (ClearScopeEnum.SPECIFIC_TYPE.equals(this.clearScope)
				&& (this.msgTypes == null || this.msgTypes.isEmpty())) {
			return false;
		}
		// 3. 其他范围无需额外参数，直接通过
		return true;
	}

	/**
	 * 自定义校验：指定时间的合理性（不能晚于当前时间）
	 * 说明：避免清空"未来时间"的消息（无实际意义，且可能导致逻辑异常）
	 * @return 校验结果（true=通过，false=失败）
	 */
	public boolean validateClearTimeReasonable() {
		// 仅当范围为BEFORE_TIME时需校验时间合理性
		if (ClearScopeEnum.BEFORE_TIME.equals(this.clearScope) && this.clearTime != null) {
			return this.clearTime.isBefore(LocalDateTime.now());
		}
		return true;
	}

	public @NotNull(message = "会话ID不能为空") @Positive(message = "会话ID必须为正整数") Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(@NotNull(message = "会话ID不能为空") @Positive(message = "会话ID必须为正整数") Long conversationId) {
		this.conversationId = conversationId;
	}

	public @NotNull(message = "操作人ID不能为空") @Positive(message = "操作人ID必须为正整数") Long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(@NotNull(message = "操作人ID不能为空") @Positive(message = "操作人ID必须为正整数") Long operatorId) {
		this.operatorId = operatorId;
	}

	public @NotNull(message = "清空范围不能为空") ClearScopeEnum getClearScope() {
		return clearScope;
	}

	public void setClearScope(@NotNull(message = "清空范围不能为空") ClearScopeEnum clearScope) {
		this.clearScope = clearScope;
	}

	public LocalDateTime getClearTime() {
		return clearTime;
	}

	public void setClearTime(LocalDateTime clearTime) {
		this.clearTime = clearTime;
	}

	public List<MsgTypeEnum> getMsgTypes() {
		return msgTypes;
	}

	public void setMsgTypes(List<MsgTypeEnum> msgTypes) {
		this.msgTypes = msgTypes;
	}

	public @Size(max = 200, message = "清空原因长度不能超过200字符") String getClearReason() {
		return clearReason;
	}

	public void setClearReason(@Size(max = 200, message = "清空原因长度不能超过200字符") String clearReason) {
		this.clearReason = clearReason;
	}
}