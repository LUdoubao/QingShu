package org.doubao.dialog.service.req;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.doubao.dialog.service.vo.MessageVO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息通知请求参数对象
 * 适用场景：
 * 1. 用户离线线时，通过MQ推送私信/系统通知（后续用户上线后消费）
 * 2. 服务间调用传递通知信息（如用户被拉黑、会话被删除的系统通知）
 * 核心作用：统一通知数据格式，支持私信通知和系统通知两种类型
 * 业务说明：定义消息通知的请求参数，包含通知类型、目标用户、通知内容等必要信息
 */
@ApiModel(description = "消息通知请求参数，支持私信通知和系统通知")
public class NotificationReq implements Serializable {
	private static final long serialVersionUID = 1L; // 序列化版本号，确保MQ传输兼容性

	// ========================= 基础通用字段 =========================
	private String title;
	/**
	 * 通知类型
	 * 枚举约束：NOTIFY_PRIVATE_MSG（私信通知）、NOTIFY_SYSTEM（系统通知）
	 * 业务规则：
	 * - 私信通知：需传递messageVO字段（完整消息信息）
	 * - 系统通知：需传递systemNotify相关字段（通知类型、内容）
	 * 业务说明：标识通知的类型，用于区分私信通知和系统通知的处理逻辑
	 * 数据校验：不能为空，仅支持NOTIFY_PRIVATE_MSG和NOTIFY_SYSTEM
	 * 使用场景：确定通知的处理方式和内容结构
	 */
	@NotBlank(message = "通知类型不能为空，请选择NOTIFY_PRIVATE_MSG或NOTIFY_SYSTEM")
	@ApiModelProperty(
			value = "通知类型：NOTIFY_PRIVATE_MSG（私信通知）、NOTIFY_SYSTEM（系统通知）",
			required = true,
			example = "NOTIFY_PRIVATE_MSG",
			allowableValues = "NOTIFY_PRIVATE_MSG,NOTIFY_SYSTEM"
	)
	private String notifyType;

	/**
	 * 目标用户ID
	 * 业务规则：通知的接收者ID，必须为已存在的用户（后端需校验用户有效性）
	 * 业务说明：标识通知的接收用户，用于消息路由和权限验证
	 * 数据校验：不能为空，必须为正整数
	 * 使用场景：确定通知消息的接收目标
	 */
	@NotNull(message = "目标用户ID不能为空")
	@ApiModelProperty(
			value = "通知接收者用户ID",
			required = true,
			example = "123456",
			notes = "需确保用户ID在user-service中已注册"
	)
	private Long targetUserId;

	/**
	 * 通知生成时间戳（毫秒）
	 * 业务规则：默认取系统当前时间，用于排序和超时判断（超过24小时的通知可丢弃）
	 * 业务说明：记录通知生成的时间，用于消息排序和过期处理
	 * 数据格式：毫秒级时间戳
	 * 使用场景：通知消息排序、过期判断、时间线管理
	 */
	@ApiModelProperty(
			value = "通知生成时间戳（毫秒），默认取当前时间",
			required = false,
			example = "1725788800000",
			notes = "未传递时后端自动填充System.currentTimeMillis()"
	)
	private Long createTime = System.currentTimeMillis();

	// ========================= 私信通知专用字段 =========================
	/**
	 * 私信消息VO
	 * 业务规则：
	 * - 当notifyType=NOTIFY_PRIVATE_MSG时，此字段为必填
	 * - 包含完整的消息信息（消息ID、发送者、内容、时间等），用于用户上线后补推
	 */
	@ApiModelProperty(
			value = "私信消息VO（仅NOTIFY_PRIVATE_MSG类型需传递）",
			required = false,
			notes = "需包含msgId、senderId、content、sendTime等核心字段"
	)
	private MessageVO messageVO;

	// ========================= 系统通知专用字段 =========================
	/**
	 * 系统通知子类型
	 * 枚举约束：SESSION_DELETED（会话被删除）、USER_BLOCKED（用户被拉黑）、FRIEND_APPLY（好友申请）等
	 * 业务规则：当notifyType=NOTIFY_SYSTEM时，此字段为必填
	 */
	@ApiModelProperty(
			value = "系统通知子类型（仅NOTIFY_SYSTEM类型需传递）",
			required = false,
			example = "SESSION_DELETED",
			notes = "需与前端约定枚举值，确保通知类型一致性"
	)
	private String systemNotifyType;

	/**
	 * 系统通知内容
	 * 业务规则：
	 * - 当notifyType=NOTIFY_SYSTEM时，此字段为必填
	 * - 支持纯文本或JSON格式（如包含会话ID、操作人等扩展信息）
	 */
	@ApiModelProperty(
			value = "系统通知内容（仅NOTIFY_SYSTEM类型需传递）",
			required = false,
			example = "您的会话【工作群聊】已被管理员删除",
			notes = "复杂通知可传递JSON字符串，如{\"sessionId\":123,\"operatorId\":456}"
	)
	private String systemNotifyContent;

	/**
	 * 系统通知关联ID
	 * 业务规则：
	 * - 可选字段，关联业务实体ID（如会话ID、好友申请ID）
	 * - 用于前端跳转（如点击通知进入对应会话）
	 */
	@ApiModelProperty(
			value = "系统通知关联ID（可选，如会话ID、好友申请ID）",
			required = false,
			example = "789",
			notes = "前端可通过此ID实现通知跳转功能"
	)
	private Long systemNotifyRelateId;


	// ========================= 校验方法（确保参数合法性） =========================
	/**
	 * 校验通知参数完整性（按通知类型校验必填字段）
	 * 调用场景：MQ发送前、WebSocket补推前，避免非法参数传递
	 * @return 校验结果：true=参数完整，false=参数缺失
	 */
	public boolean validate() {
		// 1. 通用字段校验（已通过JSR380注解校验，此处补充非空判断）
		if (StrUtil.isBlank(notifyType) || ObjectUtil.isNull(targetUserId)) {
			return false;
		}

		// 2. 按通知类型校验专用字段
		switch (notifyType) {
			case "NOTIFY_PRIVATE_MSG":
				// 私信通知：必须包含messageVO，且messageVO需非空
				return ObjectUtil.isNotNull(messageVO) && StrUtil.isNotBlank(messageVO.getId());
			case "NOTIFY_SYSTEM":
				// 系统通知：必须包含子类型和内容
				return StrUtil.isNotBlank(systemNotifyType) && StrUtil.isNotBlank(systemNotifyContent);
			default:
				// 未知通知类型
				return false;
		}
	}

	/**
	 * 转换为JSON字符串（用于MQ传输和日志打印）
	 * @return 标准化JSON字符串
	 */
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public @NotNull(message = "目标用户ID不能为空") Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(@NotNull(message = "目标用户ID不能为空") Long targetUserId) {
		this.targetUserId = targetUserId;
	}

	public @NotBlank(message = "通知类型不能为空，请选择NOTIFY_PRIVATE_MSG或NOTIFY_SYSTEM") String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(@NotBlank(message = "通知类型不能为空，请选择NOTIFY_PRIVATE_MSG或NOTIFY_SYSTEM") String notifyType) {
		this.notifyType = notifyType;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public MessageVO getMessageVO() {
		return messageVO;
	}

	public void setMessageVO(MessageVO messageVO) {
		this.messageVO = messageVO;
	}

	public String getSystemNotifyType() {
		return systemNotifyType;
	}

	public void setSystemNotifyType(String systemNotifyType) {
		this.systemNotifyType = systemNotifyType;
	}

	public Long getSystemNotifyRelateId() {
		return systemNotifyRelateId;
	}

	public void setSystemNotifyRelateId(Long systemNotifyRelateId) {
		this.systemNotifyRelateId = systemNotifyRelateId;
	}

	public String getSystemNotifyContent() {
		return systemNotifyContent;
	}

	public void setSystemNotifyContent(String systemNotifyContent) {
		this.systemNotifyContent = systemNotifyContent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	private String content;

	private String extra;

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
