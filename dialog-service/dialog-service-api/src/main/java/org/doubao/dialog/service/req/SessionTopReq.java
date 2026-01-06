package org.doubao.dialog.service.req;





import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 会话置顶/取消置顶请求参数对象
 * 前端调用/dialog/session/top接口时，需传递此参数
 * 用于指定目标会话ID及置顶状态（1=置顶，0=取消置顶）
 * 业务说明：定义会话置顶操作的请求参数，包含目标会话ID和置顶状态
 */
//@ApiModel(description = "会话置顶/取消置顶请求参数，包含会话ID和置顶状态")
public class SessionTopReq {

	/**
	 * 会话ID
	 * 业务规则：
	 * 1. 必须传递已存在的会话ID（需通过dialog_sessions表校验有效性）
	 * 2. 会话必须归属当前登录用户所有（需校验userId与会话的归属关系）
	 * 业务说明：标识需要置顶或取消置顶的目标会话
	 * 数据校验：不能为空，必须为正整数
	 * 使用场景：用户对特定会话执行置顶或取消置顶操作
	 */
	@NotNull(message = "会话ID不能为空，请传递合法的会话ID")
	private Long sessionId;

	/**
	 * 置顶状态
	 * 枚举约束：仅支持0（取消置顶）和1（置顶）
	 * 业务规则：
	 * 1. 置顶状态为1时，会话会在列表中优先排序（按置顶状态→最后消息时间倒序）
	 * 2. 置顶状态为0时，会话恢复正常排序（仅按最后消息时间倒序）
	 * 业务说明：标识会话的置顶状态，用于控制会话在列表中的显示顺序
	 * 数据校验：不能为空，仅支持0和1
	 * 使用场景：用户置顶重要会话或取消置顶普通会话
	 */
	@NotNull(message = "置顶状态不能为空，请选择0（取消置顶）或1（置顶）")
	@Pattern(regexp = "^[01]$", message = "置顶状态非法，仅支持0（取消置顶）或1（置顶）")
	private Integer isTop;

	public @NotNull(message = "会话ID不能为空，请传递合法的会话ID") Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(@NotNull(message = "会话ID不能为空，请传递合法的会话ID") Long sessionId) {
		this.sessionId = sessionId;
	}

	public @NotNull(message = "置顶状态不能为空，请选择0（取消置顶）或1（置顶）") @Pattern(regexp = "^[01]$", message = "置顶状态非法，仅支持0（取消置顶）或1（置顶）") Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(@NotNull(message = "置顶状态不能为空，请选择0（取消置顶）或1（置顶）") @Pattern(regexp = "^[01]$", message = "置顶状态非法，仅支持0（取消置顶）或1（置顶）") Integer isTop) {
		this.isTop = isTop;
	}
}
