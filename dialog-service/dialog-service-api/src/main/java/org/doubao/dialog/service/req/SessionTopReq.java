package org.doubao.dialog.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 会话置顶/取消置顶请求参数对象
 * 前端调用/dialog/session/top接口时，需传递此参数
 * 用于指定目标会话ID及置顶状态（1=置顶，0=取消置顶）
 */
@ApiModel(description = "会话置顶/取消置顶请求参数，包含会话ID和置顶状态")
public class SessionTopReq {

	/**
	 * 会话ID
	 * 业务规则：
	 * 1. 必须传递已存在的会话ID（需通过dialog_sessions表校验有效性）
	 * 2. 会话必须归属当前登录用户所有（需校验userId与会话的归属关系）
	 */
	@NotNull(message = "会话ID不能为空，请传递合法的会话ID")
	@ApiModelProperty(
			value = "目标会话ID（需为当前用户名下已存在的会话）",
			required = true,
			example = "456",
			notes = "会话ID可通过会话列表接口（/dialog/session/list）获取"
	)
	private Long sessionId;

	/**
	 * 置顶状态
	 * 枚举约束：仅支持0（取消置顶）和1（置顶）
	 * 业务规则：
	 * 1. 置顶状态为1时，会话会在列表中优先排序（按置顶状态→最后消息时间倒序）
	 * 2. 置顶状态为0时，会话恢复正常排序（仅按最后消息时间倒序）
	 */
	@NotNull(message = "置顶状态不能为空，请选择0（取消置顶）或1（置顶）")
	@Pattern(regexp = "^[01]$", message = "置顶状态非法，仅支持0（取消置顶）或1（置顶）")
	@ApiModelProperty(
			value = "置顶状态：0=取消置顶，1=置顶",
			required = true,
			example = "1",
			allowableValues = "0,1",
			notes = "置顶会话会在列表顶部显示，最多支持同时置顶20个会话（后端默认限制）"
	)
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
