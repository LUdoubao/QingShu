package org.doubao.like.service.dto.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 热门文案响应DTO
 * <p>
 * 返回按点赞数排序的热门文案列表
 */
@ApiModel(description = "热门文案响应数据")
public class HotContentResponse {

	/**
	 * 热门文案列表
	 * <p>
	 * 按点赞数从高到低排序，包含：
	 * - 文案ID
	 * - 当前点赞总数
	 */
	@ApiModelProperty(
			value = "热门文案列表",
			required = true,
			notes = "按点赞数降序排列的文案数据"
	)
	private List<Map<String, Object>> hotContents;

	public List<Map<String, Object>> getHotContents() {
		return hotContents;
	}

	public void setHotContents(List<Map<String, Object>> hotContents) {
		this.hotContents = hotContents;
	}
}