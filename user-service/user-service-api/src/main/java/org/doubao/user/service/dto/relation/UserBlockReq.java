package org.doubao.user.service.dto.relation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * 拉黑操作请求参数
 */
@ApiModel("拉黑操作请求参数")
public class UserBlockReq {
	@ApiModelProperty(value = "目标用户ID", required = true)
	private Long targetUserId;

	public  Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}
}
