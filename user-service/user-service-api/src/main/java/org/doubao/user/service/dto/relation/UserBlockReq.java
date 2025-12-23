package org.doubao.user.service.dto.relation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 拉黑操作请求参数
 */
@ApiModel("拉黑操作请求参数")
public class UserBlockReq {
	@ApiModelProperty(value = "目标用户ID", required = true)
	@NotNull(message = "目标用户ID不能为空")
	private Long targetUserId;

	public @NotNull(message = "目标用户ID不能为空") Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(@NotNull(message = "目标用户ID不能为空") Long targetUserId) {
		this.targetUserId = targetUserId;
	}
}
